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

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LanguageDefinition implements Comparable<LanguageDefinition> {
    private static final String LANGUAGES_PROPERIES = "languages.properties";
    private final String url;
    private final String key;
    private final String name;

    // ie new LanguageDefinition("en_d","Scientific Data 🔬")
    public LanguageDefinition(String key, String value) {
        this.url = getUrl(key);
        this.key = key;
        this.name = getLanguage(key) + " " + value + " " + this.url;
    }

    @NonNull
    private String getUrl(String key) {
        if (key.endsWith("_d")) return "www.wikidata.org";
        if (key.endsWith("_m")) return "commons.wikimedia.org";
        return getLanguage(key) + (key.endsWith("_v") ? ".wikivoyage.org" : ".wikipedia.org");
    }

    private String getLanguage(String key) {
        return key.split("_")[0];
    }

    // LanguageDefinition.getLanguages(this).values() sorted by name
    public static LanguageDefinition[] getLanguagesArray(Context context) throws IOException {
        Map<String, LanguageDefinition> map = getLanguages(context);
        List<LanguageDefinition> values = new ArrayList<>(map.values());
        Collections.sort(values);
        return values.toArray(new LanguageDefinition[map.size()]);
    }

    public static Map<String, LanguageDefinition> getLanguages(Context context) throws IOException {
        try(InputStream in =  context.getAssets().open(LANGUAGES_PROPERIES)) {
            return LanguageDefinition.getLanguages(in);
        } catch (IOException ex) {
            throw new IOException("Cannot load " + LANGUAGES_PROPERIES + " from assets", ex);
        }
    }

    public static Map<String, LanguageDefinition> getLanguages(InputStream in) throws IOException {
        Properties properties = new Properties();
        // note: Properties standard use iso-8859-1. However this app uses utf8
        properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        return getLanguages(properties);
    }

    public static Map<String, LanguageDefinition> getLanguages(Properties properties) {
        Map<String, LanguageDefinition> result = new HashMap<>();

        for (Map.Entry<Object, Object> kv : properties.entrySet()) {
            LanguageDefinition ld = new LanguageDefinition((String)kv.getKey(), (String)kv.getValue());
            result.put(ld.getKey(), ld);
            // result.put((String)kv.getKey(), )

        }
        return result;
    }

    public String getUrl() {
        return url;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(LanguageDefinition o) {
        return name.compareTo(o.name);
    }
}
