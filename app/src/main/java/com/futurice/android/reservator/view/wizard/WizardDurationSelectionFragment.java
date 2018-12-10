package com.futurice.android.reservator.view.wizard;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.NumberPicker;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.PreferenceManager;

/**
 *
 * A fragment to select default and maximum duration
 */
public class WizardDurationSelectionFragment extends android.support.v4.app.Fragment {

    @BindView(R.id.default_duration_picker)
    NumberPicker defaultDurationPicker;

    @BindView(R.id.maximum_duration_picker)
    NumberPicker maximumDurationPicker;

    Unbinder unbinder;

    public static final int ABSOLUTE_MIN = 5;
    public static final int ABSOLUTE_MAX = 1440;

    private int currentDefault;
    private int currentMax;

    public WizardDurationSelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.wizard_duration_selection, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        defaultDurationPicker.setWrapSelectorWheel(false);
        maximumDurationPicker.setWrapSelectorWheel(false);
        currentDefault = PreferenceManager.getInstance(getContext()).getDefaultDurationMinutes();
        currentMax = PreferenceManager.getInstance(getContext()).getMaxDurationMinutes();
        defaultDurationPicker.setMinValue(ABSOLUTE_MIN);
        defaultDurationPicker.setMaxValue(currentMax);
        maximumDurationPicker.setMinValue(currentDefault);
        maximumDurationPicker.setMaxValue(ABSOLUTE_MAX);
        defaultDurationPicker.setValue(currentDefault);
        maximumDurationPicker.setValue(currentMax);
        defaultDurationPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                PreferenceManager.getInstance(getContext()).setDefaultDurationMinutes(newVal);
                if (maximumDurationPicker.getValue() < newVal) {
                    maximumDurationPicker.setValue(newVal);
                    PreferenceManager.getInstance(getContext()).setMaxDurationMinutes(newVal);
                }
                maximumDurationPicker.setMinValue(newVal);
            }
        });
        maximumDurationPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                PreferenceManager.getInstance(getContext()).setMaxDurationMinutes(newVal);
                if (defaultDurationPicker.getValue() > newVal) {
                    defaultDurationPicker.setValue(newVal);
                    PreferenceManager.getInstance(getContext()).setDefaultDurationMinutes(newVal);
                }
                defaultDurationPicker.setMaxValue(newVal);
            }
        });
    }

}
