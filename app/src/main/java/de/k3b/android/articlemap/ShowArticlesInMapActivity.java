/*
 * Copyright (c) 2021 by k3b.
 *
 *  This file is part of AndroidGeo2ArticlesMap https://github.com/k3b/AndroidGeo2ArticlesMap .
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */

package de.k3b.android.articlemap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.k3b.geo.api.GeoPointDto;
import de.k3b.geo.api.IGeoPointInfo;
import de.k3b.geo.io.GeoUri;
import de.k3b.util.TempFileUtil;

/**
 * Translates from ACTION_SEND(TO)/VIEW with geo-uri to ACTION_SEND(TO)/VIEW with kml/kmz/gpx... uri
 */
public class ShowArticlesInMapActivity extends PermissionBaseActivity {
    public static final String TAG = "k3b.ShowArticlesInMap";

    private GeoConfig geoConfig = null;
    private Gui gui = null;

    private Handler progressMessageHandler = null;
    private ArticlesDownloadService.ProgressMessage progressMessage = null;

    private class Gui {
        private final HistoryEditText mHistory;
        private final EditText editService;
        private final CheckBox chkHide;
        private final CheckBox chkWithSymbols;
        private final TextView lblMessage;

        private Gui() {
            editService = findViewById(R.id.edit_service);

            Button cmdService = findViewById(R.id.cmd_service);
            cmdService.setOnClickListener(v -> showLanguagesPicker());

            chkHide = findViewById(R.id.chk_hide);
            chkWithSymbols = findViewById(R.id.chk_with_symbols);

            mHistory = new HistoryEditText(ShowArticlesInMapActivity.this, new int[] {
                    R.id.cmd_service_history} ,
                    editService );

            Button cmdView = findViewById(R.id.cmd_view);
            cmdView.setOnClickListener(v -> onStartQuery(false));
            /*
            Button cmdShare = findViewById(R.id.cmd_share);
            cmdShare.setOnClickListener(v -> onStartQuery(true));
             */
            Button cmdCancel = findViewById(R.id.cmd_cancel);
            cmdCancel.setOnClickListener(v -> cancel());
            lblMessage = findViewById(R.id.lbl_message);
        }

        private Gui save(GeoConfig geoConfig) {
            geoConfig.serviceName = editService.getText().toString();
            geoConfig.showSettings = !chkHide.isChecked();
            geoConfig.withSymbols = chkWithSymbols.isChecked();
            mHistory.saveHistory();
            return this;
        }

        private Gui load(GeoConfig geoConfig) {
            initServiceHistory();

            if (geoConfig.serviceName != null) {
                editService.setText(geoConfig.serviceName);
            }
            chkWithSymbols.setChecked(geoConfig.withSymbols);
            chkHide.setChecked(!geoConfig.showSettings);
            return this;
        }

        private void initServiceHistory() {
            List<String> serviceItems = getServiceHistoryItems();

            if (serviceItems.isEmpty()) {
                // first start: add local, english and simple(english)

                String language = Locale.getDefault().getLanguage();
                try {
                    Map<String, LanguageDefinition> languages = LanguageDefinition.getLanguages(ShowArticlesInMapActivity.this);
                    // added in reverse order
                    includeService(
                            languages.get("simple_v"), // last in list
                            languages.get("simple_p"),
                            languages.get("en_m"),
                            languages.get("en_d"),
                            languages.get("en_v"),
                            languages.get("en_p"),
                            languages.get(language + "_v"),
                            languages.get(language + "_p")  // first in list
                            );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serviceItems = getServiceHistoryItems();
            }

            if (!serviceItems.isEmpty()) {
                editService.setText(serviceItems.get(0));
            }
        }

        private List<String> getServiceHistoryItems() {
            List<String> historyItems = new ArrayList<>(mHistory.getHistoryItems(0));

            // remove empty entries
            boolean modified = false;
            for (int i = historyItems.size() - 1; i >= 0; i--) {
                String item = historyItems.get(i);
                if (item == null || item.trim().length() == 0) {
                    historyItems.remove(i);
                    modified = true;
                }
            }
            if (modified) {
                mHistory.getEditor(0).saveHistory(historyItems);
            }
            return historyItems;
        }

        public void includeService(LanguageDefinition... services) {
            String lastAdded = null;
            for (LanguageDefinition service : services) {
                if (service != null) {
                    mHistory.addHistory(0, service.getUrl());
                    lastAdded = service.getUrl();
                }
            }
            editService.setText(lastAdded);
        }

        public void excludeLastService() {
            List<String> historyItems = getServiceHistoryItems();
            historyItems.remove(0);
            mHistory.getEditor(0).saveHistory(historyItems);

            initServiceHistory();
        }

        public void setGuiMessage(int stringId, Object... parameters) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                lblMessage.setText(Html.fromHtml(getString(stringId, parameters),Html.FROM_HTML_MODE_LEGACY));
                Linkify.addLinks(gui.lblMessage, Linkify.WEB_URLS);
                // gui.lblMessage.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                lblMessage.setText(Html.fromHtml(getString(stringId, parameters)));
            }
        }
    }

    class GeoLoadTask extends AsyncTask<GeoPointDto, Void, ArticlesDownloadService.Result> {
        private final boolean useActionSend;
        public GeoLoadTask(boolean useActionSend) {
            this.useActionSend = useActionSend;
        }

        @Override
        protected ArticlesDownloadService.Result doInBackground(GeoPointDto... arg0) {
           return translateGeoToFile(arg0[0]);
        }


        private ArticlesDownloadService.Result translateGeoToFile(GeoPointDto geoPointFromIntent)  {
            String serviceName = geoConfig.serviceName;
            ArticlesDownloadService service = new ArticlesDownloadService(
                    serviceName, geoConfig.USER_AGENT, progressMessage)
                    .setMaxcount(geoConfig.maxcount);

            String name = createFileName(geoConfig.serviceName, geoPointFromIntent.getLatitude(), geoPointFromIntent.getLongitude());

            File outFile = new File(
                    createSharedOutDir(name),
                    name + geoConfig.outFileExtension);

            ArticlesDownloadService.Result result = service.saveAsEx(
                    geoPointFromIntent.getLatitude(), geoPointFromIntent.getLongitude(), geoConfig.withSymbols, outFile);
            return result;
        }

        @Override
        protected void onPostExecute(ArticlesDownloadService.Result result) {
            showResult(result, useActionSend);
        }
    }

    @Override
    protected void onCreateEx(Bundle savedInstanceState) {
        super.onCreateEx(savedInstanceState);
        geoConfig = new GeoConfig(this);

        GeoPointDto geoPointFromIntent = getGeoPointDtoFromIntent(getIntent());

        if (!geoConfig.showSettings && geoPointFromIntent != null && !GeoPointDto.isEmpty(geoPointFromIntent)) {
            queryDataFromArticleServer(geoPointFromIntent, false, false);
        } else {
            setContentView(R.layout.activity_choose_url);
            gui = new Gui().load(geoConfig);
            progressMessageHandler = new Handler();
            progressMessage = message -> progressMessageHandler.post(() -> gui.lblMessage.setText(message));

            if (!GeoPointDto.isEmpty(geoPointFromIntent)) {
                this.geoConfig.demoUri = geoPointFromIntent;
            }
            progressMessage.message("Using " + toString(this.geoConfig.demoUri));
        }
    }

    private void queryDataFromArticleServer(GeoPointDto geoPointFromIntent, boolean inDemoMode, boolean useActionSend) {
        geoConfig.inDemoMode = inDemoMode;
        String name = createFileName(geoConfig.serviceName, geoPointFromIntent.getLatitude(), geoPointFromIntent.getLongitude());
        File outFile = new File(
                createSharedOutDir(name),
                name + geoConfig.outFileExtension);
        createSharedUri(outFile);

        new GeoLoadTask(useActionSend).execute(geoPointFromIntent);
    }

    /** called in gui thread, after receiving answer from wikipedia */
    @SuppressLint("StringFormatMatches")
    private void showResult(ArticlesDownloadService.Result result, boolean useActionSend) {

        if (result.errorMessageId != 0) {
            showErrorMessage(getString(result.errorMessageId, geoConfig.serviceName, result.outFile, toString(geoConfig.demoUri),""));
        } else if (result.points == null || result.points.isEmpty()) {
            showMessage(getString(R.string.warn_no_article_found, geoConfig.serviceName, result.outFile, toString(geoConfig.demoUri), ""));
        }
        if (result.errorMessageId == R.string.error_service_url_invalid) {
            gui.excludeLastService();
        }

        if (result.errorMessageId == 0 && result.points != null && !result.points.isEmpty()) {
            // success: forward same Action/Mime to final kmz receiver

            Uri outUri = createSharedUri(result.outFile);

            Intent newIntent;
            if (useActionSend) {
                // https://developer.android.com/reference/android/content/Intent#ACTION_SEND
                newIntent = new Intent(Intent.ACTION_SEND)
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setType("*/*")
                        .putExtra(Intent.EXTRA_TITLE, geoConfig.serviceName)
                        .putExtra(Intent.EXTRA_STREAM, outUri);
                newIntent.setClipData(ClipData.newRawUri("", outUri));

                newIntent = Intent.createChooser(newIntent, geoConfig.serviceName);
            } else {
                // https://developer.android.com/reference/android/content/Intent#ACTION_VIEW
                newIntent = new Intent(Intent.ACTION_VIEW)
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .putExtra(Intent.EXTRA_TITLE, geoConfig.serviceName)
                        .setDataAndTypeAndNormalize(outUri, geoConfig.outMimeType);
            }

            try {
                // start the image capture Intent
                startActivity(newIntent);

                if (!geoConfig.inDemoMode) {
                    finish();
                }
                gui.setGuiMessage(R.string.info_success,
                        geoConfig.serviceName, result.outFile, toString(geoConfig.demoUri), ""+ result.points.size());
            } catch (ActivityNotFoundException ex) {
                Log.e(TAG, newIntent.toUri(Intent.URI_INTENT_SCHEME));

                gui.setGuiMessage(R.string.error_location_map_viewer_not_found,
                        geoConfig.serviceName, result.outFile, toString(geoConfig.demoUri), "");
            }

        }
    }

    private void showErrorMessage(CharSequence message) {
        Toast.makeText(this, message,
                Toast.LENGTH_LONG).show();
        Log.e(TAG, message.toString());
        showMessage(message);
    }

    private void showMessage(CharSequence message) {
        if (progressMessage != null) {
            progressMessage.message(message);
        }
    }

    private GeoPointDto getGeoPointDtoFromIntent(Intent intent) {
        final Uri uri = (intent != null) ? intent.getData() : null;
        String uriAsString = (uri != null) ? uri.toString() : null;
        GeoPointDto pointFromIntent = null;
        if (uriAsString != null) {
            Log.i(TAG,getString(R.string.app_name) + ": received  " + uriAsString);
            GeoUri parser = new GeoUri(GeoUri.OPT_PARSE_INFER_MISSING);
            pointFromIntent = parser.fromUri(uriAsString, new GeoPointDto());
        }
        return pointFromIntent;
    }

    protected File getSharedDir() {
        File sharedDir = new File(this.getCacheDir(), "shared");
        // unused temporary files from send/get_content after some time.
        TempFileUtil.removeOldTempFiles(sharedDir, System.currentTimeMillis());
        sharedDir.mkdirs();

        return sharedDir;
    }

    protected String createFileName(String serviceName, double latitude, double longitude) {
        return serviceName+ "." + latitude + "_" +longitude;
    }

    protected Uri createSharedUri(File outFile) {
        return FileProvider.getUriForFile(this, "de.k3b.geo2articles", outFile);
    }

    private File createSharedOutDir(String name) {
        return new File(getSharedDir(), name);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        save();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_choose_url, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        save();
        int itemId = menuItem.getItemId();
        boolean isCmdShare = itemId == R.id.cmd_share;
        if (itemId == R.id.cmd_cancel_pick) {
            finish();
            return true;
        } else if (itemId == R.id.cmd_ok || isCmdShare) {
            GeoPointDto geoPointFromIntent = getGeoPointDtoFromIntent(getIntent());

            if (geoPointFromIntent == null) {
                geoPointFromIntent = new GeoConfig(this).demoUri;
            }
            if (geoPointFromIntent != null) {
                queryDataFromArticleServer(geoPointFromIntent, false, isCmdShare);
            } else {
                finish();
            }

            return true;
        }
        return super.onOptionsItemSelected(menuItem);

    }

    private void onStartQuery(boolean useActionSend) {
        save();
        queryDataFromArticleServer(geoConfig.demoUri, true, useActionSend);
    }

    private void save() {
        gui.save(geoConfig);
        geoConfig.save(this);
    }

    private void cancel() {
        save();
        finish();
    }

    private void showLanguagesPicker() {
        final ArrayAdapter<LanguageDefinition> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        try {
            adapter.addAll(LanguageDefinition.getLanguagesArray(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.lbl_service)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setAdapter(adapter,
                        (dialog, which) -> {
                            dialog.dismiss();
                            onAddLanguagePick(adapter.getItem(which));
                        })
                .show();
    }

    private void onAddLanguagePick(LanguageDefinition item) {
        gui.includeService(item);
    }

    public static String toString(IGeoPointInfo point) {
        if (point != null) {
            StringBuilder result = new StringBuilder();
            if (point.getName() != null && !point.getName().isEmpty()) {
                result.append(point.getName());
            }
            if (!GeoPointDto.isEmpty(point)) {
                result
                        .append("(")
                        .append(point.getLatitude())
                        .append(",")
                        .append(point.getLongitude())
                        .append(")");
            }
            if (result.length() > 0) {
                return result.toString();
            }
        }

        return "";
    }

}
