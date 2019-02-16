package com.futurice.android.reservator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.futurice.android.reservator.common.LocaleManager;
import com.futurice.android.reservator.common.PreferenceManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RemoteConfigActivity extends AppCompatActivity {

    public static final String ACCOUNT = "account";
    public static final String DEFAULT_ROOM = "default_room";
    public static final String LANGUAGE = "lang";
    public static final String DEFAULT_DURATION = "default_duration";
    public static final String MAX_DURATION = "max_duration";
    public static final String ROOM_DISPLAY_NAME = "room_display_name";
    public static final String LOG_TAG = "RemoteConfigActivity";

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_config);
        intent = getIntent();
        if (intent != null) {
            Uri data = intent.getData();
            if (data != null) {
                System.err.println("Intent received");
                handleCommandLineSettings(data);
            }
        } else {
            finish();
        }
    }

    private void handleCommandLineSettings(Uri data) {
        Set<String> parameters = data.getQueryParameterNames();
        if (parameters == null || parameters.isEmpty()) {
            Log.d(LOG_TAG, "Parameters are null, opening the wizard");
            final Intent i = new Intent(this, WizardActivity.class);
            startActivity(i);
            return;
        }
        String account = data.getQueryParameter(ACCOUNT);
        String defaultRoom = data.getQueryParameter(DEFAULT_ROOM);
        String defaultDurationString = data.getQueryParameter(DEFAULT_DURATION);
        String maxDurationString = data.getQueryParameter(MAX_DURATION);
        String language = data.getQueryParameter(LANGUAGE);
        String roomDisplayName = data.getQueryParameter(ROOM_DISPLAY_NAME);

        boolean accountSet = false;
        boolean roomSet = false;
        if (account == null
            && defaultRoom == null
            && defaultDurationString == null
            && maxDurationString == null
            && language == null) {
            Log.d(LOG_TAG, "Parameters not found, opening the wizard");
            final Intent i = new Intent(this, WizardActivity.class);
            startActivity(i);
        }
        if (account != null) {
            if (accountExists(account)) {
                PreferenceManager preferences = PreferenceManager.getInstance(this);
                preferences.setDefaultCalendarAccount(account);
                accountSet = true;
                Log.d(LOG_TAG, "Account configured: " + preferences.getDefaultCalendarAccount());
            } else {
                Log.d(LOG_TAG, "account " + account + " does not exist, not adding ");
            }
        }
        if (defaultRoom != null) {
            PreferenceManager preferences = PreferenceManager.getInstance(this);
            preferences.setSelectedRoom(defaultRoom);
            roomSet = true;
            Log.d(LOG_TAG, "Room configured: " + preferences.getSelectedRoom());
        }
        if (defaultDurationString != null && maxDurationString != null) {
            setDurations(defaultDurationString, maxDurationString);
        } else {
            if (defaultDurationString != null) {
                setDefaultDuration(defaultDurationString);
            }
            if (maxDurationString != null) {
                setMaxDuration(maxDurationString);
            }
        }
        if (language != null) {
            setLanguage(language);
        }

        if (roomDisplayName != null) {
            setRoomDisplayName(roomDisplayName);
        }
        if (accountSet && roomSet) {
            PreferenceManager.getInstance(this).setApplicationConfigured(true);
            Log.d(LOG_TAG, "Application configured: " + PreferenceManager.getInstance(this)
                .getApplicationConfigured());
        }
        finish();
    }

    private void setDurations(String defaultString, String maxString) {
        try {
            int defaultDuration = Integer.parseInt(defaultString);
            int maxDuration = Integer.parseInt(maxString);
            if (defaultDuration <= maxDuration) {
                PreferenceManager.getInstance(this).setDefaultDurationMinutes(defaultDuration);
                PreferenceManager.getInstance(this).setMaxDurationMinutes(maxDuration);
            } else {
                Log.d(LOG_TAG, "Default duration is longer than max duration, not setting: default "
                    + "duration "
                    + defaultDuration
                    + ", max duration "
                    + maxDuration);
            }
        } catch (NumberFormatException e) {
            Log.d(LOG_TAG, "Could not set default duration, "
                + defaultString
                + " and max "
                + "duration "
                + maxString
                + ", one of them is not a number");
        }
    }

    private void setDefaultDuration(String durationString) {
        try {
            int duration = Integer.parseInt(durationString);
            int maxDuration = PreferenceManager.getInstance(this).getMaxDurationMinutes();
            if (duration <= maxDuration) {
                PreferenceManager.getInstance(this).setDefaultDurationMinutes(duration);
            } else {
                PreferenceManager.getInstance(this).setDefaultDurationMinutes(maxDuration);
            }
        } catch (NumberFormatException e) {
            Log.d(LOG_TAG,
                "Could not set default duration, " + durationString + " is not a " + "number");
        }
    }

    private void setMaxDuration(String durationString) {
        try {
            int duration = Integer.parseInt(durationString);
            int defaultDuration = PreferenceManager.getInstance(this).getDefaultDurationMinutes();
            if (duration >= defaultDuration) {
                PreferenceManager.getInstance(this).setMaxDurationMinutes(duration);
            } else {
                PreferenceManager.getInstance(this).setMaxDurationMinutes(defaultDuration);
            }
        } catch (NumberFormatException e) {
            Log.d(LOG_TAG,
                "Could not set max duration, " + durationString + " is not a " + "number");
        }
    }

    private void setLanguage(String language) {
        List<String> languages =
            ((ReservatorApplication) getApplication()).getAvailableLanguageCodes();
        boolean contains = false;
        for (String l : languages) {
            if (language.equalsIgnoreCase(l)) {
                contains = true;
            }
        }
        if (contains) {
            LocaleManager.setNewLocale(getBaseContext(), language);
        } else {
            Log.d(LOG_TAG, "Could not set language " + language);
        }
    }

    private void setRoomDisplayName(String name) {
        PreferenceManager.getInstance(this).setRoomDisplayName(name);
    }

    private boolean accountExists(String account) {
        if (account == null) {
            return false;
        }
        List<String> accounts = getAvailableAccounts();
        for (String a: accounts) {
            if (account.equalsIgnoreCase(a)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getAvailableAccounts() {
        List<String> accountsList = new ArrayList<String>();
        for (Account account : AccountManager.get(this).getAccountsByType(null)) {
            accountsList.add(account.name);
        }
        return accountsList;
    }

}
