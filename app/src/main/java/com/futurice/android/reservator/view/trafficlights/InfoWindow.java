package com.futurice.android.reservator.view.trafficlights;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.TextView;
import com.futurice.android.reservator.BuildConfig;
import com.futurice.android.reservator.R;


public class InfoWindow extends DialogFragment {

    Button okButton;
    TextView versionName;
    Activity activity;

    private void onOkClicked() {
        this.dismissAllowingStateLoss();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.info_window, container);

        this.okButton = (Button) view.findViewById(R.id.infoOkButton);
        this.versionName = view.findViewById(R.id.infoAppVersion);
        versionName.setText(getString(R.string.version, BuildConfig.VERSION_NAME));
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkClicked();
            }
        });
		return view;
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}
	@Override
    public void show(FragmentManager magager, String tag) {
        this.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show(magager, tag);
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        this.activity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        this.activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    }
}