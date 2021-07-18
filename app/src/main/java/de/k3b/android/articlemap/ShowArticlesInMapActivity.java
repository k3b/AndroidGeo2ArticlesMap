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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.k3b.geo.api.GeoPointDto;
import de.k3b.geo.api.IGeoPointInfo;
import de.k3b.geo.io.Geo2WikipediaDownloadWithSymbolsService;
import de.k3b.geo.io.GeoUri;
import de.k3b.util.TempFileUtil;

/**
 * Translates from ACTION_SEND(TO)/VIEW with geo-uri to ACTION_SEND(TO)/VIEW with kml/kmz/gpx... uri
 */
public class ShowArticlesInMapActivity extends PermissionBaseActivity {
    private static final String TAG = "k3b.ShowArticlesInMap";

    private GeoConfig geoConfig = null;
    private Gui gui = null;

    private class Gui {
        private final HistoryEditText mHistory;
        private final EditText editService;
        private final Button cmdService;
        private final CheckBox chkHide;

        private Gui() {
            editService = findViewById(R.id.edit_service);

            cmdService = findViewById(R.id.cmd_service);
            cmdService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLanguagesPicker();
                }
            });

            chkHide = findViewById(R.id.chk_hide);
            mHistory = new HistoryEditText(ShowArticlesInMapActivity.this, new int[] {
                    R.id.cmd_service_history} ,
                    editService );
        }

        private Gui save(GeoConfig geoConfig) {
            geoConfig.serviceName = editService.getText().toString();
            geoConfig.showSettings = !chkHide.isChecked();

            return this;
        }

        private Gui load(GeoConfig geoConfig) {
            initServiceHistory();

            if (geoConfig.serviceName != null) {
                editService.setText(geoConfig.serviceName);
            }
            chkHide.setChecked(!geoConfig.showSettings);
            return this;
        }

        private void initServiceHistory() {
            List<String> serviceItems = getServiceHistoryItems();

            if (serviceItems.isEmpty() || serviceItems.get(0).trim().isEmpty()) {
                // first start: add local, english and simple(english)
                while (!serviceItems.isEmpty() && serviceItems.get(0).trim().isEmpty()) {
                    // remove empty items
                    serviceItems.remove(0);
                }

                String language = Locale.getDefault().getLanguage();
                try {
                    Map<String, LanguageDefinition> languages = LanguageDefinition.getLanguages(ShowArticlesInMapActivity.this);
                    // added in reverse order
                    includeService(
                            languages.get("simple_v"), // last in list
                            languages.get("simple_p"),
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
            return mHistory.getHistoryItems(0);
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
            getServiceHistoryItems().remove(0);
            initServiceHistory();
        }
    }

    class GeoLoadTask extends AsyncTask<GeoPointDto, Void, File> {

        @Override
        protected File doInBackground(GeoPointDto... arg0) {
            //Your implementation
            try {
                return translateGeoToFile(arg0[0]);
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }


        private File translateGeoToFile(GeoPointDto geoPointFromIntent) throws java.io.IOException {
            String serviceName = geoConfig.serviceName;
            ArticlesDownloadService service = new ArticlesDownloadService(
                    serviceName, geoConfig.USER_AGENT, null)
                    .setMaxcount(geoConfig.maxcount);

            String name = createFileName(geoConfig.serviceName, geoPointFromIntent.getLatitude(), geoPointFromIntent.getLongitude());

            File outFile = new File(
                    createSharedOutDir(name),
                    name + geoConfig.outFileExtension);

            List<IGeoPointInfo> result = service.saveAs(
                    geoPointFromIntent.getLatitude(), geoPointFromIntent.getLongitude(),outFile);
            return outFile;
        }

        @Override
        protected void onPostExecute(File result) {
            showResult(result);
        }
    }

    @Override
    protected void onCreateEx(Bundle savedInstanceState) {
        super.onCreateEx(savedInstanceState);
        geoConfig = new GeoConfig(this);

        GeoPointDto geoPointFromIntent = getGeoPointDtoFromIntent(getIntent());

        if (geoPointFromIntent != null && !geoConfig.showSettings) {
            queryDataFromArticleServer(geoPointFromIntent);
        } else {
            setContentView(R.layout.activity_choose_url);
            gui = new Gui().load(geoConfig);
        }
    }

    private void queryDataFromArticleServer(GeoPointDto geoPointFromIntent) {
        String name = createFileName(geoConfig.serviceName, geoPointFromIntent.getLatitude(), geoPointFromIntent.getLongitude());
        File outFile = new File(
                createSharedOutDir(name),
                name + geoConfig.outFileExtension);
        createSharedUri(outFile);

        new GeoLoadTask().execute(geoPointFromIntent);
    }

    /** called in gui thread, after receiving answer from wikipedia */
    private void showResult(File outFile) {
        if (outFile != null) {
            // success: forward same Action/Mime to final kmz receiver
            String action = getIntent().getAction();

            Uri outUri = createSharedUri(outFile);
            Intent newIntent = new Intent(action)
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (Intent.ACTION_SEND.compareTo(action) == 0) {
                newIntent.putExtra(Intent.EXTRA_STREAM, outUri);
            } else {
                // ACTION_SENDTO or ACTION_VIEW
                newIntent.setDataAndTypeAndNormalize(outUri, geoConfig.outMimeType);
            }

            // start the image capture Intent
            startActivity(newIntent);

        } else {
            Toast.makeText(this, getString(R.string.error_service, geoConfig.serviceName),
                    Toast.LENGTH_LONG).show();
            gui.excludeLastService();
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
        if (itemId == R.id.cmd_cancel_pick) {
            finish();
            return true;
        } else if (itemId == R.id.cmd_ok) {
            GeoPointDto geoPointFromIntent = getGeoPointDtoFromIntent(getIntent());

            if (geoPointFromIntent != null) {
                queryDataFromArticleServer(geoPointFromIntent);
            } else {
                finish();
            }

            return true;
        }
        return super.onOptionsItemSelected(menuItem);

    }

    private void save() {
        gui.save(geoConfig);
        geoConfig.save(this);
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
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                onAddLanguagePick(adapter.getItem(which));
                            }
                        })
                .show();
    }

    private void onAddLanguagePick(LanguageDefinition item) {
        gui.includeService(item);
    }
}
