package com.futurice.android.reservator;
import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.futurice.android.reservator.common.LedHelper;
import com.futurice.android.reservator.common.LocaleManager;
import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.Model;
import com.futurice.android.reservator.view.trafficlights.TrafficLightsPageFragment;
import com.futurice.android.reservator.view.trafficlights.TrafficLightsPresenter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends FragmentActivity {

    public final int PERMISSIONS_REQUEST = 234;
    public final int SETTINGS_REQUEST = 235;

    private boolean permissionsGot = false;
    private boolean initCompleted = false;
    private boolean uiStarted = false;

    private final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    private FragmentManager fragmentManager;

    private TrafficLightsPageFragment trafficLightsPageFragment;
    private TrafficLightsPresenter presenter;

    private Model model;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.onAttach(base));
    }

    public void turnKioskOn() {
        Log.d("MainActivity", "Turn kiosk on.");
        // get policy manager
        DevicePolicyManager myDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        // get this app package name
        ComponentName mDPM = new ComponentName(this, MyAdmin.class);

        /*
        List<ComponentName> admins = myDevicePolicyManager.getActiveAdmins();
        String result = "DEVICE ADMINS:\n";

        for (int i=0; i<admins.size(); i++) {
            result += admins.get(i).toString()+"\n";
        }

        result+="THIS.PACKAGENAME:\n"+this.getPackageName();

        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        */

        if (myDevicePolicyManager.isDeviceOwnerApp(this.getPackageName())) {
            Log.d("MainActivity", "App is the device owner");
            // get this app package name
            String[] packages = {this.getPackageName()};
            // mDPM is the admin package, and allow the specified packages to lock task
            myDevicePolicyManager.setLockTaskPackages(mDPM, packages);
            startLockTask();
        } else {
            Toast.makeText(getApplicationContext(),"Not owner", Toast.LENGTH_LONG).show();
        }
    }

    public void turnKioskOff() {
        // get policy manager
        DevicePolicyManager myDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        // get this app package name
        ComponentName mDPM = new ComponentName(this, MyAdmin.class);

        if (myDevicePolicyManager.isDeviceOwnerApp(this.getPackageName())) {
            // get this app package name
            String[] packages = {this.getPackageName()};
            // mDPM is the admin package, and allow the specified packages to lock task
            myDevicePolicyManager.setLockTaskPackages(mDPM, packages);
            stopLockTask();
        } else {
            Toast.makeText(getApplicationContext(),"Not owner", Toast.LENGTH_LONG).show();
        }
    }

    BroadcastReceiver calendarChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onCalendarUpdated();
        }
    };

    BroadcastReceiver kioskOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Reservator","KIOSK_ON intent received");
            turnKioskOn();
        }
    };

    BroadcastReceiver kioskOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Reservator","KIOSK_OFF intent received");
            turnKioskOff();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if(keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            //Toast.makeText(this, "Volume button is disabled", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            //Toast.makeText(this, "Volume button is disabled", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){

        if(keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            //Toast.makeText(this, "Volume button is disabled", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            //Toast.makeText(this, "Volume button is disabled", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.getWindow().getDecorView().setSystemUiVisibility(this.flags);
        if (!hasFocus) {

            windowCloseHandler.post(windowCloserRunnable);
        }
    }
    private void toggleRecents() {
        Intent closeRecents = new Intent("com.android.systemui.recent.action.TOGGLE_RECENTS");
        closeRecents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ComponentName recents = new ComponentName("com.android.systemui", "com.android.systemui.recent.RecentsActivity");
        closeRecents.setComponent(recents);
        this.startActivity(closeRecents);
    }
    private Handler windowCloseHandler = new Handler();
    private Runnable windowCloserRunnable = new Runnable() {@Override public void run() {
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (cn != null && cn.getClassName().equals("com.android.systemui.recent.RecentsActivity")) {
            toggleRecents();
        }
    }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        LocaleManager.onAttach(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Mute the audio

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(
                AudioManager.STREAM_SYSTEM,
                0,
                0);

        this.registerReceiver(calendarChangeReceiver, new IntentFilter(CalendarStateReceiver.CALENDAR_CHANGED));
        this.registerReceiver(kioskOnReceiver, new IntentFilter(KioskStateReceiver.KIOSK_ON));
        this.registerReceiver(kioskOffReceiver, new IntentFilter(KioskStateReceiver.KIOSK_OFF));
        this.turnKioskOn();
        //Log.d("Futurice","componentName="+DeviceAdmin.getComponentName(this));
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getWindow().getDecorView().setSystemUiVisibility(this.flags);

        if (!permissionsGot) {
            permissionsGot = checkPermissions();
            if (permissionsGot) {
                onPermissionsOk();
            }
        } else {
            onPermissionsOk();
        }
    }

    private void startUi() {
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
        this.uiStarted = true;

        this.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                        //Log.d("reservator", "onSystemUiVisibilityChange()");
                        getWindow().getDecorView().setSystemUiVisibility(flags);
                    }
                });
    }

    public void onCalendarUpdated() {
        if (this.model != null && this.initCompleted && this.uiStarted)
            this.model.getDataProxy().refreshRoomReservations(this.model.getFavoriteRoom());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LedHelper.getInstance().setGreenBrightness(0);
        LedHelper.getInstance().setRedBrightness(0);
        unregisterReceiver(calendarChangeReceiver);
        unregisterReceiver(kioskOnReceiver);
        unregisterReceiver(kioskOffReceiver);
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //Log.d("Reservator","touch event");
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
            this.hideSoftKeyboard();
        return super.dispatchTouchEvent(event);
    }

    private void onInitCompleted() {
        initCompleted = true;
        //this.model.getDataProxy().refreshRoomReservations(this.model.getFavoriteRoom());
        if (!uiStarted)
            this.startUi();
    }

    private boolean isConfigurationOk() {
        if ((!PreferenceManager.getInstance(this).getApplicationConfigured())) {
            return false;
        }

        // If the configuration did not work, open the configuration wizard again
        if (((ReservatorApplication)getApplication()).getDataProxy().hasFatalError() ) {
            return false;
        }

        return true;
    }
    // Functionality moved from LoginActivity

    private void showWizardActivity() {
        final Intent i = new Intent(this, WizardActivity.class);
        startActivityForResult(i, SETTINGS_REQUEST);
    }

    // This will be called when WizardActivity returns
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        //Re-check if configuration is now correct
        // Configuration should now be ok, try using it
        ((ReservatorApplication) getApplication()).resetDataProxy();
        if (isConfigurationOk() ) {
            Intent intent = getIntent();
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        }
        else {
            showWizardActivity();
        }
        }

    // Functionality moved from CheckPermissionsActivity


    public void onPermissionsOk() {
        if (isConfigurationOk())
            onInitCompleted();
        else
            showWizardActivity();
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
        if (!permissionsGot) {
            permissionsGot = checkPermissions();
            if (permissionsGot) {
                onPermissionsOk();
            }
        } else {
            onPermissionsOk();
        }
        */
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length >= 4 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionsOk();
                }
                return;
            }
        }
    }

    public boolean checkPermissions()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        )
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                new AlertDialog.Builder(this).setTitle(R.string.permission_request_title)
                        .setMessage(R.string.permission_request_reason)
                        .setPositiveButton("Ok", new DialogInterface
                                .OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {
                                ActivityCompat.requestPermissions(
                                        MainActivity.this,
                                        new String[]{
                                                Manifest.permission.READ_CONTACTS,
                                                Manifest.permission.READ_CALENDAR,
                                                Manifest.permission.WRITE_CALENDAR,
                                                Manifest.permission.READ_EXTERNAL_STORAGE
                                        },
                                        PERMISSIONS_REQUEST);
                            }
                        })
                        .setNegativeButton("Dismiss",
                                new DialogInterface
                                        .OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .show();
            }

            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission
                                        .READ_CONTACTS,
                                Manifest.permission
                                        .READ_CALENDAR,
                                Manifest.permission
                                        .WRITE_CALENDAR,
                                Manifest.permission
                                        .READ_EXTERNAL_STORAGE
                        },
                        PERMISSIONS_REQUEST);
            }
            return false;
        } else {
            return true;
        }
    }
}
