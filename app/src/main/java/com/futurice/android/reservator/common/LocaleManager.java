package com.futurice.android.reservator.common;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import java.util.Locale;

public class LocaleManager {

    private static final String LOG_TAG = "LocaleManager";

    public static Context onAttach(Context context) {
        String lang = getLanguage(context);
        return updateResources(context, lang);
    }

    public static Context setLocale(Context context) {
        return setNewLocale(context, getLanguage(context));
    }

    public static Context setNewLocale(Context context, String language) {
        setLanguage(context, language);
        return updateResources(context, language);
    }

    public static String getLanguage(Context context) {
        PreferenceManager preferences = PreferenceManager.getInstance(context);
        Log.d(LOG_TAG, "getting language: " + preferences.getSelectedLanguage());
        return preferences.getSelectedLanguage();
    }

    private static void setLanguage(Context context, String language) {
        PreferenceManager preferences = PreferenceManager.getInstance(context);
        Log.d(LOG_TAG, "setting language: " + language);
        preferences.setSelectedLanguage(language);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        Log.d(LOG_TAG, "has set locale " + locale.getLanguage());
        return context;
    }

}
