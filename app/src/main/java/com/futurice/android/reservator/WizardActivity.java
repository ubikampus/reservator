package com.futurice.android.reservator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import com.futurice.android.reservator.common.LocaleManager;
import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.view.wizard.WizardAccountSelectionFragment;
import com.futurice.android.reservator.view.wizard.WizardDefaultRoomSelectionFragment;
import com.futurice.android.reservator.view.wizard.WizardDurationSelectionFragment;
import com.futurice.android.reservator.view.wizard.WizardLanguageSelectionFragment;
import com.github.paolorotolo.appintro.AppIntro;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by shoj on 10/11/2016.
 */

public final class WizardActivity extends AppIntro {

    public static final String LOG_TAG = "WizardActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleManager.onAttach(getApplicationContext());

        final Fragment calendarAccountSelection = new WizardAccountSelectionFragment();
        final Fragment roomDefaultSelection = new WizardDefaultRoomSelectionFragment();
        final Fragment languageSelection = new WizardLanguageSelectionFragment();
        final Fragment durationSelection = new WizardDurationSelectionFragment();

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
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
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

        finish();
        //final Intent i = new Intent(this, LoginActivity.class);
        //startActivity(i);
    }
}
