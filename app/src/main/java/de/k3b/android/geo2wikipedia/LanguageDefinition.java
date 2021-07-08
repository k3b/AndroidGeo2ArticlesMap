package de.k3b.android.geo2wikipedia;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class LanguageDefinition {
    private static final String LANGUAGES_PROPERIES = "languages.properties";
    private final String url;
    private final String key;
    private final String name;

    public LanguageDefinition(String key, String value) {
        String languageID = getLanguage(key);

        this.url = languageID + (isWikipedia(key) ? ".wikpedia.org" : ".wikivoyage.org");
        this.key = key;
        this.name = languageID + " " + value + " " + this.getUrl();
    }

    private boolean isWikipedia(String key) {
        return !key.endsWith("_v");
    }

    private String getLanguage(String key) {
        return key.split("_")[0];
    }

    public static Map<String, LanguageDefinition> getLanguages(Context context) throws IOException {
        try(InputStream in =  context.getAssets().open(LANGUAGES_PROPERIES)) {
            return LanguageDefinition.getLanguages(in);
        } catch (IOException ex) {
            throw new IOException("Cannot load " + LANGUAGES_PROPERIES + " from assets", ex);
        }
    }

    public static Map<String, LanguageDefinition> getLanguages(InputStream in) throws IOException {
        String language = Locale.getDefault().getLanguage();
        Properties properties = new Properties();
        // note: Properties standard use iso-8859-1. However this app uses utf8
        properties.load(new InputStreamReader(in, "UTF8"));
        return getLanguages(properties);
    }

    public static Map<String, LanguageDefinition> getLanguages(Properties properties) {
        LinkedHashMap<String, LanguageDefinition> result = new LinkedHashMap<String, LanguageDefinition>(200, 0.75f, true);

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
}
