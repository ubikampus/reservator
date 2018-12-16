package com.futurice.android.reservator;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;

import android.util.DisplayMetrics;
import com.futurice.android.reservator.common.LocaleManager;
import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Model;
import com.futurice.android.reservator.model.platformcalendar.PlatformCalendarDataProxy;
import com.futurice.android.reservator.model.platformcontacts.PlatformContactsAddressBook;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ReservatorApplication extends Application {
    private final long ADDRESS_CACHE_CLEAR_INTERVAL = 6 * 60 * 60 * 1000; // Once every six hours

    private Model model;

    @Override
    public void onCreate() {
        super.onCreate();

        this.model= new Model(getContentResolver(),AccountManager.get(this), this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.onAttach(this);
    }

    // The following 3 getters are deprecated, use getModel() instead

    public DataProxy getDataProxy() {
        return this.model.getDataProxy();
    }

    public void resetDataProxy() {
        this.model.resetDataProxy();
    }

    public AddressBook getAddressBook() {
        return this.model.getAddressBook();
    }

    public Model getModel() {
        return this.model;
    }

    public String getSettingValue(int settingNameId, String defaultValue) {
        SharedPreferences settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), 0);
        return settings.getString(getString(settingNameId), defaultValue);
    }

    public String getFavouriteRoomName() {
        return this.getSettingValue(R.string.PREFERENCES_ROOM_NAME, getString(R.string.lobbyRoomName));
    }

    public ArrayList<String> getAvailableLanguageCodes() {
        return new ArrayList<String>(Arrays.asList("mul", "en", "fi", "sv", "ja"));
    }

}
