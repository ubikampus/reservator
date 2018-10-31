package com.futurice.android.reservator.view.trafficlights;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.futurice.android.reservator.R;

import java.util.Date;

import butterknife.BindView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class RoomStatusFragment extends Fragment {
    private static Date lastTimeConnected = new Date(0);
    private final long DISCONNECTED_WARNING_ICON_THRESHOLD = 5 * 60 * 1000;

    public interface RoomStatusPresenter {
        void setRoomStatusFragment(RoomStatusFragment fragment);
    }

    private RoomStatusPresenter presenter;

    private TextView roomTitleText = null;
    private TextView statusText = null;
    private TextView statusUntilText = null;
    private TextView meetingNameText = null;
    private TextView bookNowText = null;

    @BindView(R.id.disconnected)
    View disconnected;

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
        this.bookNowText = (TextView) view.findViewById(R.id.bookNowText);
        return view;
    }

    private void updateConnected() {
        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (ClassCastException cce) {
            return;
        }
        if (cm == null) return;

        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni != null && ni.isConnectedOrConnecting()) {
            // Connected
            lastTimeConnected = new Date();
            if (disconnected.getVisibility() != GONE) {
                disconnected.setVisibility(GONE);
            }
            if(this.statusText.getVisibility() != VISIBLE){
                statusText.setVisibility(VISIBLE);
            }
            if(this.statusUntilText.getVisibility() != VISIBLE){
                statusUntilText.setVisibility(VISIBLE);
            }
            if(this.meetingNameText.getVisibility() != VISIBLE){
                meetingNameText.setVisibility(VISIBLE);
            }

        } else {
            // Disconnected
            if (lastTimeConnected.before(new Date(new Date().getTime() - DISCONNECTED_WARNING_ICON_THRESHOLD))) {
                if (disconnected.getVisibility() != VISIBLE) {
                    disconnected.setVisibility(VISIBLE);
                }
                if(this.statusText.getVisibility() != GONE){
                    statusText.setVisibility(GONE);
                }
                if(this.statusUntilText.getVisibility() != GONE){
                    statusUntilText.setVisibility(GONE);
                }
                if(this.meetingNameText.getVisibility() != GONE){
                    meetingNameText.setVisibility(GONE);
                }
            }
        }
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
    public void showBookNowText() {
        this.bookNowText.setVisibility(VISIBLE);
    }
    public void hideBookNowText() {
        this.bookNowText.setVisibility(GONE);
    }
}

