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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.k3b.geo.api.GeoPointDto;

public class GeoConfig {
    public String serviceName = null; // i.e. "en.wikipedia.org";
    public final String USER_AGENT = "AndroidGeo2ArticlesMap/1.1 (https://github.com/k3b/AndroidGeo2ArticlesMap)";
    public final String outFileExtension = ".kmz";
    public final String outMimeType = "application/vnd.google-earth.kmz";

    public boolean showSettings = true; // true always show settings before query
    public boolean withSymbols = false; // true: also load Images/Symbols (slow, extra internet bandwitdh)

    public final int maxcount = 25; // miximaum number of articles to search for

    public GeoPointDto demoUri = new GeoPointDto()
            .setLatLon(52.51,13.35)
            .setName("Berlin, Germany");

    public boolean inDemoMode = false;

    public GeoConfig(Context context) {
        SharedPreferences prefsInstance = PreferenceManager
                .getDefaultSharedPreferences(context);

        withSymbols = prefsInstance.getBoolean("withSymbols", withSymbols);
        showSettings = prefsInstance.getBoolean("showSettings", showSettings);
        serviceName = prefsInstance.getString("serviceName", serviceName);
        inDemoMode = prefsInstance.getBoolean("inDemoMode", inDemoMode);

    }

    public void save(Context context) {
        SharedPreferences.Editor edit = PreferenceManager
                .getDefaultSharedPreferences(context).edit();

        edit.putBoolean("withSymbols", withSymbols);
        edit.putBoolean("showSettings", showSettings);
        edit.putString("serviceName", serviceName);
        edit.putBoolean("inDemoMode", inDemoMode);

        edit.apply();
    }
}
