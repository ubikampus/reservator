package com.futurice.android.reservator.view.wizard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.futurice.android.reservator.R;
import com.futurice.android.reservator.ReservatorApplication;
import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.DataProxy;
import com.github.paolorotolo.appintro.ISlidePolicy;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by shoj on 10/11/2016.
 */

public final class WizardDefaultRoomSelectionFragment extends android.support.v4.app.Fragment implements
    ISlidePolicy {

    @BindView(R.id.wizard_accounts_radiogroup)
    RadioGroup roomRadioGroup;
    @BindView(R.id.wizard_accounts_title)
    TextView title;

    Unbinder unbinder;

    AlertDialog alertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wizard_account_selection, container, false);
        unbinder = ButterKnife.bind(this, view);

        title.setText(R.string.defaultRoomSelectionTitle);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        roomRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String roomName = ((RadioButton) group.findViewById(checkedId)).getText().toString();
                PreferenceManager preferences = PreferenceManager.getInstance(getActivity());
                preferences.setSelectedRoom(roomName);
            }
        });
    }

    public void reloadRooms() {
        PreferenceManager preferences = PreferenceManager.getInstance(getActivity());
        ReservatorApplication application = ((ReservatorApplication) getActivity().getApplication());

        roomRadioGroup.removeAllViews();
        DataProxy proxy = application.getDataProxy();

        ArrayList<String> roomNames = proxy.getRoomNames();

        if (roomNames == null || roomNames.isEmpty()) {
            showNoRoomsErrorMessage();
            return;
        }

        HashSet<String> unselectedRooms = preferences.getUnselectedRooms();

        for (String roomName : roomNames) {
            if (unselectedRooms.contains(roomName)) {
                continue;
            }

            RadioButton roomRadioButton = new RadioButton(getActivity());
            roomRadioButton.setText(roomName);
            roomRadioGroup.addView(roomRadioButton);
        }


    }

    private void showNoRoomsErrorMessage() {
        String errorMessage = getString(R.string.noCalendarRoomsError);
        final AlertDialog.Builder builder =
            new AlertDialog.Builder(getActivity());
        builder.setMessage(errorMessage)
            .setTitle(R.string.calendarRoomError)
            .setPositiveButton(R.string.button_info_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(
                        DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alertDialog = builder.create();
      //  alertDialog.setCancelable(false);
      //  alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

    @Override
    public boolean isPolicyRespected() {
        return roomRadioGroup.getCheckedRadioButtonId() != -1;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {

    }
}
