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
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import de.k3b.geo.GeoConfig;
import de.k3b.geo.api.IGeoPointInfo;
import de.k3b.geo.io.DownloadGpxKmlZipWithSymbolsService;
import de.k3b.geo.io.DownloadSymbolsBaseService.ITranslateSymbolUri;
import de.k3b.geo.io.gpx.GpxReader;

/**
 * Translates geo / gps location to local kml/kmz file with nearby wikipedia articles.
 */
public class ArticlesDownloadService extends DownloadGpxKmlZipWithSymbolsService {
    private static final String TAG = "k3b.ArticlesDownload";
    private final String serviceName;

    int radius = 10000;
    int maxcount = 5;

    /**
     * @param serviceName where the data comes from. i.e.  "en.wikipedia.org" or "de.wikivoyage.org"
     * @param userAgent a string identifying the calling app.
     *                  i.e. "MyHelloWikipediaApp/1.0 (https://github.com/MyName/MyHelloWikipediaApp)"
     *                  see https://meta.wikimedia.org/wiki/Special:MyLanguage/User-Agent_policy
     * @param translateSymbolUri Under Android you can use this to translate File-Uris to Android-Content-uris
     */
    public ArticlesDownloadService(String serviceName, String userAgent, ITranslateSymbolUri translateSymbolUri) {
        super(userAgent, translateSymbolUri);
        this.serviceName = serviceName;
    }

    public ArticlesDownloadService setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public ArticlesDownloadService setMaxcount(int maxcount) {
        this.maxcount = maxcount;
        return this;
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

    private List<IGeoPointInfo> getGeoPointInfos(Object lat, Object lon) throws IOException {
        String urlString = this.getQueryGeoUrlString(lat, lon);
        Log.i(TAG,"downloading from " + urlString);
        InputStream inputStream = this.getInputStream(urlString);
        GpxReader<IGeoPointInfo> parser = new GpxReader<>();

        List<IGeoPointInfo> points = parser.getTracks(new InputSource(inputStream));
        return points;
    }

    public List<IGeoPointInfo> saveAs(Object lat, Object lon, File out) throws IOException {
        List<IGeoPointInfo> points = getGeoPointInfos(lat, lon);
        if (points.size() > 0) {
            saveAs(points, out);
        }
        return points;
    }

    /** api creates url that encodes what we want to get from wikipedia  */
    private String getQueryGeoUrlString(Object lat, Object lon) {
        // see https://www.mediawiki.org/wiki/Special:MyLanguage/API:Main_page
        String urlString = "https://" +
                serviceName +
                "/w/api.php" +
                "?action=query" +
                "&format=xml" +
                "&prop=coordinates|info|pageimages|extracts" +
                "&inprop=url" +
                "&piprop=thumbnail" +
                "&generator=geosearch" +
                "&ggscoord=" +
                lat +
                "|" +
                lon +
                "&ggsradius=" +
                radius +
                "&ggslimit=" +
                maxcount +

                "&pithumbsize=" +
                GeoConfig.THUMBSIZE +
                "&pilimit=" +
                maxcount+

                // prop extracts: 2Sentenses in non-html before TOC
                "&exsentences=2&explaintext&exintro";
        return urlString;
    }
}
