/*
 * Copyright (c) 2021 by k3b.
 *
 * This file is part of k3b-geoHelper library.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.k3b.android.articlemap;

import android.util.Log;

import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import de.k3b.geo.GeoConfig;
import de.k3b.geo.api.IGeoPointInfo;
import de.k3b.geo.io.DownloadGpxKmlZipWithSymbolsService;
import de.k3b.geo.io.gpx.GpxReader;

/**
 * Translates geo / gps location to local kml/kmz file with nearby wikipedia articles.
 */
public class ArticlesDownloadService extends DownloadGpxKmlZipWithSymbolsService {
    public static final String TAG = "k3b.ArticlesDownload";

    private final String serviceName;
    private final ProgressMessage progressMessage;

    int radius = 10000;
    int maxcount = 5;

    public interface ProgressMessage {
        void message(final CharSequence message);
    }
    /**
     * @param serviceName where the data comes from. i.e.  "en.wikipedia.org" or "de.wikivoyage.org"
     * @param userAgent a string identifying the calling app.
     *                  i.e. "MyHelloWikipediaApp/1.0 (https://github.com/MyName/MyHelloWikipediaApp)"
     * @param progressMessage if not null: progress messages go here.
     */
    public ArticlesDownloadService(String serviceName, String userAgent, ProgressMessage progressMessage) {
        super(userAgent, null);
        this.serviceName = serviceName;
        this.progressMessage = progressMessage;
    }

    public ArticlesDownloadService setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public ArticlesDownloadService setMaxcount(int maxcount) {
        this.maxcount = maxcount;
        return this;
    }

    // from https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
    private boolean isOnline() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();

            // ping
            SocketAddress sockaddr = new InetSocketAddress("en.wikipedia.org", 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private InputStream getInputStream(String urlString) throws IOException {
        return getInputStream(new URL(urlString));
    }

    private InputStream getInputStream(URL url) throws IOException {
        URLConnection hc = url.openConnection();

        // see https://meta.wikimedia.org/wiki/Special:MyLanguage/User-Agent_policy
        hc.setRequestProperty("User-Agent",userAgent);

        return hc.getInputStream();
    }

    private Result getGeoPointInfos(Object lat, Object lon, boolean withSymbols) {
        String urlString = this.getQueryGeoUrlString(lat, lon, withSymbols, serviceName.contains(".wikidata."));
        String message = "downloading articles from " + serviceName;
        Log.i(TAG, message + " using " + urlString);
        if (progressMessage != null) {
            progressMessage.message(message);
        }

        try (InputStream inputStream = this.getInputStream(urlString) ){
            GpxReader<IGeoPointInfo> parser = new GpxReader<>();
            message = "analysing articles ...";
            Log.d(TAG,message);
            if (progressMessage != null) {
                progressMessage.message(message);
            }

            List<IGeoPointInfo> points = parser.getTracks(new InputSource(inputStream));
            return new Result(null, points, 0, null);
        } catch (Exception ex) {
            message = "cannot read from '" + this.serviceName + "' using '" + urlString + "'" ;
            Log.e(TAG, message,ex);
            if (progressMessage != null) {
                progressMessage.message(message);
            }
            if (isOnline()) {
                return new Result(null, null, R.string.error_service_url_invalid, null);
            }
            return new Result(null, null, R.string.error_internet_not_available, null);
        }
    }

    public Result saveAsEx(Object lat, Object lon, boolean withSymbols, File out) {
        Result result = getGeoPointInfos(lat, lon, withSymbols);
        int size = result.points != null ? result.points.size()  : 0;
        if (size > 0) {
            try {
                String message = "writing " + size + " articles to " + out.getAbsolutePath();
                Log.d(TAG, message);
                if (progressMessage != null) {
                    progressMessage.message(message);
                }

                saveAs(result.points, out);
                return new Result(out, result.points, result.errorMessageId, null);
            } catch (IOException ex) {
                return new Result(out, result.points, R.string.error_cannot_write_local_file, ex);
            }
        }
        return result;
    }

    /** api creates url that encodes what we want to get from wikipedia
     *
     * examples
     * * https://de.wikivoyage.org/w/api.php?action=query&format=xml&prop=coordinates|info|pageimages|extracts&inprop=url&piprop=thumbnail&generator=geosearch&ggscoord=28.12722|-15.43139&ggsradius=10000&ggslimit=5&pilimit=5&exsentences=2&explaintext&exintro
     * * https://www.wikidata.org/w/api.php?action=query&format=xml&prop=entityterms|coordinates|info&inprop=url&generator=geosearch&ggscoord=36.45284|28.22016&ggsradius=10000&ggslimit=25
     * */
    private String getQueryGeoUrlString(Object lat, Object lon, boolean withSymbols, boolean fromWikiData) {
        // see https://www.mediawiki.org/wiki/Special:MyLanguage/API:Main_page
        StringBuilder urlString = new StringBuilder().append("https://")
                .append(serviceName)
                .append("/w/api.php" +
                "?action=query" +
                "&format=xml" +
                "&prop=coordinates|info");

        if (fromWikiData) {
            urlString.append("|entityterms");
        } else {
            if (withSymbols) {
                urlString.append("|pageimages");
            }

            urlString.append("|extracts");
        }

        urlString.append(
                "&inprop=url" +
                "&generator=geosearch" +
                "&ggscoord=")
                .append(lat).append("|").append(lon)
                .append("&ggsradius=").append(radius)
                .append("&ggslimit=").append(maxcount);

        if (!fromWikiData) {
            if (withSymbols) {
                urlString.append("&piprop=thumbnail")
                        .append("&pithumbsize=").append(GeoConfig.THUMBSIZE)
                        .append("&pilimit=").append(maxcount);
            }

            urlString
                    // prop extracts: 2Sentenses in non-html before TOC
                    .append("&exsentences=2&explaintext&exintro");
        }
        return urlString.toString();
    }

    static class Result {
        final File outFile;
        final int errorMessageId;
        private final Exception exception;
        final List<IGeoPointInfo> points;

        Result(File outFile, List<IGeoPointInfo> points, int errorMessageId, Exception exception) {
            this.outFile = outFile;
            this.points = points != null ? points : new ArrayList<>();
            this.errorMessageId = errorMessageId;
            this.exception = exception;
        }
    }

}
