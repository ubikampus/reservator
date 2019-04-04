package com.futurice.android.reservator.view.trafficlights;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.Helpers;

public class OngoingReservationFragment extends Fragment {

    private final int CANCEL_COUNTDOWN_SECONDS = 5;

    public interface OngoingReservationPresenter {
    void setOngoingReservationFragment(OngoingReservationFragment fragment);
    void onReservationMinutesChanged(int newMinutes);
    void onReservationMinutesUpdated(int minutes);

    void onReservationChangeStarted();
    void onReservationChangeEnded();
    }

    private OngoingReservationPresenter presenter;

    private TextView barDurationText;
    private SeekBar seekBar;

    private ProgressBar changeProgressBar;
    private Button cancelChangeButton;
    private Button cancelReservationButton;
    private TextView modifyPrompt;
    private TextView notModifiableText;

    private CountDownTimer changeTimer;

    private int tickCounter = 0;

    private int remainingMinutes = 0;
    private int maxMinutes = 0;
    private int savedProgress = 0;
    private int progress = 0;

    private boolean modifiable = true;

    private boolean isCountingDown = false;


    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        //int minutesIncrement = 5;
        //int currentTime = Integer.valueOf(getCurrentTime());

        @Override
        public void onProgressChanged(android.widget.SeekBar seekBar, int barProgress, boolean b) {

            //barProgress = ((int) Math.round(barProgress / minutesIncrement)) * minutesIncrement;

            //This should now move in 5 (minute) increments
            //For some reason, progress shows up as a string saying progress instead of immediately changing to the int

            //textViewBarBegin.setText(getCurrentTime() + "");

            //progress += Integer.valueOf(getCurrentTime());
            //seekBar.setProgress(barProgress);
            barDurationText.setText(Helpers.convertToHoursAndMinutes(barProgress));
            //textViewBarEnd.setText("" + progress); //Add amount to current time
            presenter.onReservationMinutesUpdated(barProgress);
            //presenter.onReservationChangeStarted();
        }

        @Override
        public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
            //savedProgress = ((int) Math.round(seekBar.getProgress() / minutesIncrement)) * minutesIncrement;
            presenter.onReservationChangeStarted();
            savedProgress = seekBar.getProgress();
        }

        @Override
        public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
            seekBar.setEnabled(false);
            //progress = ((int) Math.round(seekBar.getProgress() / minutesIncrement)) * minutesIncrement;
            progress = seekBar.getProgress();
            startChangeCountDown();
        }
    };

    private View.OnClickListener onCancelClicked = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            cancelChanges();
        }
    };
    private View.OnClickListener onCancelReservationClicked = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            animateCancelReservation();
        }
    };

    private void cancelChanges() {
        seekBar.setProgress(savedProgress);
        changeTimer.cancel();
        hideCancelWidgets();
        isCountingDown = false;
        presenter.onReservationMinutesUpdated(savedProgress);
        presenter.onReservationChangeEnded();
        seekBar.setEnabled(true);
    }

    private void animateCancelReservation() {
        ObjectAnimator animation = ObjectAnimator.ofInt(seekBar, "progress", 0);
        animation.setDuration(400);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
        presenter.onReservationChangeStarted();
        savedProgress = seekBar.getProgress();

        seekBar.setProgress(0);
        seekBar.setEnabled(false);
        progress = 0;
        startChangeCountDown();
    }

    private void saveChanges() {
        this.hideCancelWidgets();
        this.isCountingDown = false;
        this.presenter.onReservationMinutesChanged(progress);
        this.presenter.onReservationChangeEnded();
        seekBar.setEnabled(true);
    }

    public void showChancelWidgets() {
        this.changeProgressBar.setVisibility(View.VISIBLE);
        this.cancelChangeButton.setVisibility(View.VISIBLE);
        this.cancelReservationButton.setVisibility(View.GONE);
        this.modifyPrompt.setVisibility(View.GONE);
    }

    public void hideCancelWidgets() {
        this.changeProgressBar.setVisibility(View.GONE);
        this.cancelChangeButton.setVisibility(View.GONE);
        this.cancelReservationButton.setVisibility(View.VISIBLE);
        this.modifyPrompt.setVisibility(View.VISIBLE);
    }


    private void showNotModifiable() {
        if (this.changeProgressBar == null ||  this.modifyPrompt== null
                || this.cancelReservationButton == null || this.notModifiableText == null
                || this.seekBar == null)
            return;

        this.changeProgressBar.setVisibility(View.INVISIBLE);
        this.modifyPrompt.setVisibility(View.INVISIBLE);
        this.cancelReservationButton.setVisibility(View.INVISIBLE);
        this.notModifiableText.setVisibility(View.VISIBLE);
        this.seekBar.setEnabled(false);
    }

    private void showModifiable() {
        if (this.modifyPrompt== null
                || this.cancelReservationButton == null || this.notModifiableText == null
                || this.seekBar == null)
            return;

        this.modifyPrompt.setVisibility(View.VISIBLE);
        this.cancelReservationButton.setVisibility(View.VISIBLE);
        this.seekBar.setEnabled(true);
        this.notModifiableText.setVisibility(View.INVISIBLE);
    }


    public void setNotModifiable() {
        this.modifiable = false;
        this.showNotModifiable();
    }

    public void setModifiable() {
       this.modifiable = true;
       this.showModifiable();
    }

    private void updateModifiableStatusToUi() {
        if (this.modifiable)
            this.showModifiable();
        else
            this.showNotModifiable();
    }

    public void setPresenter(OngoingReservationPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setOngoingReservationFragment(this);
        }

    private void updateRemainingMinutesToUi() {
        if (this.seekBar != null && this.barDurationText != null) {
            int max = this.seekBar.getMax();

            if (max < this.remainingMinutes)
                this.seekBar.setMax(this.remainingMinutes);

            this.seekBar.setProgress(this.remainingMinutes);
            this.barDurationText.setText(Helpers.convertToHoursAndMinutes(this.remainingMinutes));
        }
    }

    private void updateMaxMinutesToUi() {
        if (this.seekBar != null)
            this.seekBar.setMax(this.maxMinutes);
    }
    public void setMaxMinutes(int minutes) {
        this.maxMinutes = minutes;
        updateMaxMinutesToUi();
    }

    public void setRemainingMinutes(int minutes) {
        this.remainingMinutes = minutes;

        if (!isCountingDown)
            this.updateRemainingMinutesToUi();
    }

    public void startChangeCountDown() {
        this.isCountingDown = true;
        this.tickCounter = CANCEL_COUNTDOWN_SECONDS;
        changeProgressBar.setProgress(CANCEL_COUNTDOWN_SECONDS);
        showChancelWidgets();
        this.changeTimer.start();
    }
    public void onChangeCountDownTick() {
        this.tickCounter--;
        if (this.tickCounter>=0)
            this.changeProgressBar.setProgress(this.tickCounter);
    }
    public void onChangeCountDownFinished() {
       this.saveChanges();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ongoing_reservation_fragment, container, false);

        this.barDurationText = (TextView) view.findViewById(R.id.barDurationText);
        this.seekBar = (SeekBar) view.findViewById(R.id.ongoingSeekBar);
        this.modifyPrompt = (TextView) view.findViewById(R.id.modifyPrompt);
        this.notModifiableText = (TextView) view.findViewById(R.id.notModifiableText);
        this.changeProgressBar = (ProgressBar) view.findViewById(R.id.cangeProgressBar);

        this.cancelChangeButton = (Button) view.findViewById(R.id.cancelChangeButton);
        this.cancelChangeButton.setOnClickListener(this.onCancelClicked);

        this.cancelReservationButton = (Button) view.findViewById(R.id.cancelReservationButton);
        this.cancelReservationButton.setOnClickListener(this.onCancelReservationClicked);

        this.seekBar.setOnSeekBarChangeListener(seekBarChangeListener);


        this.changeProgressBar.setMax(CANCEL_COUNTDOWN_SECONDS);
        this.changeProgressBar.setProgress(CANCEL_COUNTDOWN_SECONDS);

        this.updateRemainingMinutesToUi();
        this.updateMaxMinutesToUi();

        this.changeTimer=new CountDownTimer((CANCEL_COUNTDOWN_SECONDS+1)*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                onChangeCountDownTick();
            }

            @Override
            public void onFinish() {
                onChangeCountDownFinished();
            }
        };
        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        this.updateRemainingMinutesToUi();
        this.updateMaxMinutesToUi();
        this.updateModifiableStatusToUi();
    }
}
