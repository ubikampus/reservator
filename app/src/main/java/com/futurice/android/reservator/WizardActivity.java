
package com.futurice.android.reservator;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import com.futurice.android.reservator.common.LocaleManager;
import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.view.wizard
        .WizardAccountSelectionFragment;
import com.futurice.android.reservator.view.wizard
        .WizardDefaultRoomSelectionFragment;
import com.futurice.android.reservator.view.wizard.WizardDurationSelectionFragment;
import com.futurice.android.reservator.view.wizard.WizardLanguageSelectionFragment;
import com.github.paolorotolo.appintro.AppIntro;
import java.util.List;
import java.util.Set;

/**
 * Created by shoj on 10/11/2016.
 */

public final class WizardActivity extends AppIntro {

    public static final String ACCOUNT = "account";
    public static final String DEFAULT_ROOM = "default_room";
    public static final String LANGUAGE = "lang";
    public static final String DEFAULT_DURATION = "default_duration";
    public static final String MAX_DURATION = "max_duration";
    public static final String LOG_TAG = "WizardActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleManager.onAttach(getApplicationContext());
        Intent intent = getIntent();
        if (intent != null) {
            Uri data = intent.getData();
            if (data != null) {
                System.err.println("Intent received");
                handleCommandLineSettings(data);
            }
        }
        final Fragment calendarAccountSelection =
                new WizardAccountSelectionFragment();
        final Fragment roomDefaultSelection =
                new WizardDefaultRoomSelectionFragment();
        final Fragment languageSelection =
            new WizardLanguageSelectionFragment();
        final Fragment durationSelection =
            new WizardDurationSelectionFragment();


        super.addSlide(calendarAccountSelection);
        super.addSlide(roomDefaultSelection);
        super.addSlide(languageSelection);
        super.addSlide(durationSelection);

        showSkipButton(false);
        setProgressButtonEnabled(true);
        setFadeAnimation();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.onAttach(base));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSlideChanged(
            @Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.

        // load new
        if (newFragment instanceof WizardDefaultRoomSelectionFragment) {
            ((WizardDefaultRoomSelectionFragment) newFragment).reloadRooms();
        }

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        PreferenceManager.getInstance(this).setApplicationConfigured(true);

        final Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    private void handleCommandLineSettings(Uri data) {
        Set<String> parameters = data.getQueryParameterNames();
        if (parameters == null || parameters.isEmpty()) {
            return;
        }
        String account = data.getQueryParameter(ACCOUNT);
        String defaultRoom = data.getQueryParameter(DEFAULT_ROOM);
        String defaultDurationString = data.getQueryParameter(DEFAULT_DURATION);
        String maxDurationString = data.getQueryParameter(MAX_DURATION);
        String language = data.getQueryParameter(LANGUAGE);
        if (account != null ||  (defaultRoom == null && defaultDurationString == null &&
            maxDurationString == null && language == null)) {
            return;
        }
        if (defaultRoom != null) {
            PreferenceManager preferences = PreferenceManager.getInstance(this);
            preferences.setSelectedRoom(defaultRoom);
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
        final Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    private void setDurations(String defaultString, String maxString) {
        try {
            int defaultDuration = Integer.parseInt(defaultString);
            int maxDuration = Integer.parseInt(maxString);
            if (defaultDuration <= maxDuration) {
                PreferenceManager.getInstance(this).setDefaultDurationMinutes(defaultDuration);
                PreferenceManager.getInstance(this).setMaxDurationMinutes(maxDuration);
            } else {
                Log.d(LOG_TAG,"Default duration is longer than max duration, not setting: default "
                    + "duration " + defaultDuration + ", max duration " + maxDuration);
            }
        } catch (NumberFormatException e) {
            Log.d(LOG_TAG, "Could not set default duration, " + defaultString + " and max "
                + "duration " + maxString + ", one of them is not a number");
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
            Log.d(LOG_TAG, "Could not set default duration, " + durationString + " is not a "
                + "number");
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
            Log.d(LOG_TAG, "Could not set max duration, " + durationString + " is not a "
                + "number");
        }
    }

    private void setLanguage(String language) {
        List<String> languages = ((ReservatorApplication) getApplication())
            .getAvailableLanguageCodes();
        boolean contains = false;
        for (String l: languages) {
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

}
