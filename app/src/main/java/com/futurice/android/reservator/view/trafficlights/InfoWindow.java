package com.futurice.android.reservator.view.trafficlights;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.futurice.android.reservator.R;


public class InfoWindow extends DialogFragment {

    Button okButton;

    private void onOkClicked() {
        this.dismissAllowingStateLoss();
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.info_window, container);

        this.okButton = (Button) view.findViewById(R.id.infoOkButton);
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
}