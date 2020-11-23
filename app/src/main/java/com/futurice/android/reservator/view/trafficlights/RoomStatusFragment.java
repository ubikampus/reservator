package com.futurice.android.reservator.view.trafficlights;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.futurice.android.reservator.R;

public class RoomStatusFragment extends Fragment {

    public interface RoomStatusPresenter {
    void setRoomStatusFragment(RoomStatusFragment fragment);
    }

    private RoomStatusPresenter presenter;

    private TextView roomTitleText = null;
    private TextView statusText = null;
    private TextView statusUntilText = null;
    private TextView meetingNameText = null;
    private TextView co2Text = null;
    private TextView warningText = null;


    public void setPresenter(RoomStatusPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setRoomStatusFragment(this);
        }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*
        try {
            presenter = (RoomStatusPresenter) (((PresenterView)context).getPresenter());
            presenter.setRoomStatusFragment(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RoomStatusPresenter");
        } */
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.room_status_fragment, container, false);
        this.roomTitleText = (TextView) view.findViewById(R.id.roomTitleText);
        this.statusText = (TextView) view.findViewById(R.id.statusText);
        this.statusUntilText = (TextView) view.findViewById(R.id.statusUntilText);
        this.meetingNameText = (TextView) view.findViewById(R.id.meetingNameText);
        this.co2Text = (TextView) view.findViewById(R.id.roomCo2Text);
        this.warningText = (TextView) view.findViewById(R.id.warningText);
        return view;
    }

    public void setRoomTitleText(String text) {
        this.roomTitleText.setText(text);
    }
    public void setStatusText(String text) {
        this.statusText.setText(text);
    }
    public void setStatusUntilText(String text) {
        this.statusUntilText.setText(text);
    }
    public void setMeetingNameText(String text) {
        this.meetingNameText.setText(text);
    }
    /* public void showBookNowText() {
        this.bookNowText.setVisibility(View.VISIBLE);
    }
    public void hideBookNowText() {
        this.bookNowText.setVisibility(View.GONE);
    }
     */

    public void showCo2text(int co2ppm) {
        String text = "CO2: "+ co2ppm + " ppm";
        this.co2Text.setText(text);
        this.co2Text.setVisibility(View.VISIBLE);
    }

    public void hideCo2Text() {
        this.co2Text.setVisibility(View.INVISIBLE);
    }

    public void showAirQualityWarning(int co2treshold) {
        String text = "CO2 > "+ co2treshold + " ppm\n"  + getString(R.string.air_quality_warning_prompt);
        this.warningText.setText(text);
        this.warningText.setVisibility(View.VISIBLE);
    }

    public void hideAirQualityWarning() {
        this.warningText.setVisibility(View.INVISIBLE);
    }
}

