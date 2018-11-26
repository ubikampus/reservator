package com.futurice.android.reservator;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.futurice.android.reservator.common.LedHelper;
import com.futurice.android.reservator.model.Model;
import com.futurice.android.reservator.view.trafficlights.TrafficLightsPageFragment;
import com.futurice.android.reservator.view.trafficlights.TrafficLightsPresenter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity {
    private FragmentManager fragmentManager;

    private TrafficLightsPageFragment trafficLightsPageFragment;
    private TrafficLightsPresenter presenter;

    private Model model;

    private Button popupButton;
    private PopupWindow popupWindow;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onCalendarUpdated();
        }
    };

    class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

        final NetworkRequest networkRequest;

        public ConnectionStateMonitor() {
            networkRequest = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();

            /*
            networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                    .build();
            */
        }

        public void enable(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(networkRequest , this);
        }

        @Override
        public void onAvailable(Network network) {
            updateNetworkStatus();
            /*
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getNetworkInfo(network);
            boolean isConnected = (info != null && info.isConnectedOrConnecting());

            if (isConnected) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                if (nc != null) {
                    boolean isInternetValid = nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                    if (isInternetValid) {
                        setConnected();
                    }
                }
            }
            */
        }

        @Override
        public void onLost(Network network) {
            setDisconnected();
            //updateNetworkStatus();
        }

    }

    private ConnectionStateMonitor connectionStateMonitor = new ConnectionStateMonitor();

    private void setConnected() {
        if (this.presenter == null)
            return;
        this.presenter.setConnected();
    }

    private void setDisconnected() {
        if (this.presenter == null)
            return;
        this.presenter.setDisconnected();
    }

    public void updateNetworkStatus() {
        if (this.presenter == null)
            return;

        if (this.isConnectedToGoogle())
            this.setConnected();
        else
            this.setDisconnected();
    }

    private boolean isConnectedToGoogle() {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection)
                    (new URL("http://clients3.google.com/generate_204")
                            .openConnection());
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("User-Agent", "Android");
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setConnectTimeout(1500);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 204 &&
                    urlConnection.getContentLength() == 0) {
                Log.d("Reservator", "isConnectedToGoogle() returning true");
                return true;
            }
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }
    /*
    private boolean hasInternetConnection() {
        final ConnectivityManager connectivityManager = (ConnectivityManager)this.
                getSystemService(Context.CONNECTIVITY_SERVICE);

        final Network network = connectivityManager.getActiveNetwork();
        final NetworkCapabilities capabilities = connectivityManager
                .getNetworkCapabilities(network);

        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
*/
    private void openFragment(Fragment fragment) {
        if (fragmentManager != null) {
            fragmentManager.executePendingTransactions();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if (fragment.isAdded())
                ft.show(fragment);
            else
                ft.add(R.id.main_container,fragment);

            ft.commit();
        }
    }

    public String[] getAvailableAccounts() {
        List<String> accountsList = new ArrayList<>();
        for (Account account : AccountManager
                .get(this)
                .getAccountsByType(null)) {
            accountsList.add(account.name);
        }
        return accountsList.toArray(new String[accountsList.size()]);
    }

    private void showSetupWizard() {
        final Intent i = new Intent(this, WizardActivity.class);
        startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        registerReceiver(broadcastReceiver,
            new IntentFilter(CalendarStateReceiver.CALENDAR_CHANGED));

        this.fragmentManager = getSupportFragmentManager();
        this.trafficLightsPageFragment = new TrafficLightsPageFragment();

        this.model = ((ReservatorApplication)getApplication()).getModel();
        this.presenter = new TrafficLightsPresenter(this, this.model);
        this.trafficLightsPageFragment.setPresenter(this.presenter);

        this.connectionStateMonitor.enable(this);

        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        this.openFragment(this.trafficLightsPageFragment);
        this.updateNetworkStatus();

        popupButton = (Button) findViewById(R.id.infoButton);
        relativeLayout = (RelativeLayout) findViewById(R.id.trafficLightsPageFragment);

        if(popupButton != null){
            popupButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup_menu, null);

                    popupWindow = new PopupWindow(container, 400, 400, true);
                    popupWindow.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, 500,500); //put relative layout

                    //Closes window
                    container.setOnTouchListener(new View.OnTouchListener(){
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent){
                            popupWindow.dismiss();
                            return true;
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getAvailableAccounts().length <= 0) {
            showSetupWizard();
        }
    }

    // Start of implemtation of the calendar change listener with intent filters
    // TODO: make this actually work

    public void onCalendarUpdated() {
        if (this.model != null)
            this.model.getDataProxy().refreshRoomReservations(this.model.getFavoriteRoom());
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        LedHelper.getInstance().setGreenBrightness(0);
        LedHelper.getInstance().setRedBrightness(0);
        unregisterReceiver(broadcastReceiver);
    }

    public void onButtonShowPopupWindowClick(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_menu, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

}
