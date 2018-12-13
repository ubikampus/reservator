package com.futurice.android.reservator.view.wizard;

import android.os.Bundle;
import android.util.Log;
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
import com.futurice.android.reservator.common.LocaleManager;
import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.DataProxy;
import com.github.paolorotolo.appintro.ISlidePolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

/**
 * Created by shoj on 10/11/2016.
 */

public final class WizardLanguageSelectionFragment extends android.support.v4.app.Fragment implements
    ISlidePolicy {

    @BindView(R.id.wizard_accounts_radiogroup)
    RadioGroup languageRadioGroup;
    @BindView(R.id.wizard_accounts_title)
    TextView title;

    ArrayList<String> languages;

    Unbinder unbinder;

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
        languages = ((ReservatorApplication) getActivity().getApplication())
            .getAvailableLanguageCodes();
        languageRadioGroup.removeAllViews();
        Locale currentLocale = Locale.getDefault();
        int index = 0;
        for (String language: languages) {
            RadioButton languageRadioButton = new RadioButton(getActivity());
            Locale locale = new Locale(language);
            languageRadioButton.setTag(language);
            languageRadioButton.setText(locale.getDisplayLanguage());
            languageRadioGroup.addView(languageRadioButton);
            if (language.equalsIgnoreCase(currentLocale.getLanguage())) {
                languageRadioGroup.check(languageRadioButton.getId());
            }
            index++;
        }
        languageRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String language = ((RadioButton) group.findViewById(checkedId)).getTag()
                    .toString();
                LocaleManager.setNewLocale(getActivity().getBaseContext(), language);
            }
        });
    }

    @Override
    public boolean isPolicyRespected() {
        return languageRadioGroup.getCheckedRadioButtonId() != -1;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {

    }
}
