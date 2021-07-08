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
package de.k3b.android.geo2wikipedia;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GeoConfig {
    public final String serviceName = "en.wikipedia.org";
    public final String USER_AGENT = "AndroidGeo2ArticlesMap/1.0 (https://github.com/k3b/AndroidGeo2ArticlesMap)";
    public final String outFileName = "test2geo200.kmz";
    public final String outMimeType = "application/vnd.google-earth.kmz";
    public boolean showSettings = true;

    public final int maxcount = 10;

    public GeoConfig(Context context) {
        SharedPreferences prefsInstance = PreferenceManager
                .getDefaultSharedPreferences(context);
        showSettings = prefsInstance.getBoolean("showSettings", showSettings);
    }

    public void save(Context context) {
        SharedPreferences.Editor edit = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        edit.putBoolean("showSettings", showSettings);

        edit.apply();
    }

    /*                <data android:mimeType="application/vnd.google-earth.kml+xml" />
                <data android:mimeType="application/vnd.google-earth.kmz" />
                <data android:mimeType="application/xml+kml" />
                <data android:mimeType="application/xml+gpx" />
                <data android:mimeType="application/xml+poi" />
                <data android:mimeType="application/zip+xml+kml" />
                <data android:mimeType="application/zip+xml+gpx" />
                <data android:mimeType="application/zip+xml+poi" />



*/
}
