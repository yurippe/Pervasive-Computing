package dk.atom_it.littlebigbrother.threading;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;

import java.util.List;

/**
 * Created by the wonderful Kristian on 9/28/2016.
 */

public abstract class ASyncSucks {

    private BluetoothAdapter BTAdapter;
    private WifiManager WiFiManager;

    private BroadcastReceiver BTReceiver;
    private BroadcastReceiver WIFIReceiver;

    protected Activity activity;

    private boolean isPaused = false;


    public ASyncSucks(final Activity activity){
        this.activity = activity;
        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        //Bluetooth shit
        if(BTAdapter != null){

            BTReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                        onBluetoothDiscoveryStarted();

                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                        onBluetoothDiscoveryCompleted();

                    } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        //bluetooth device found
                        final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        onBluetoothDeviceDiscovery(device);

                    }
                }
            };


            IntentFilter filter = new IntentFilter();

            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            activity.registerReceiver(BTReceiver, filter);

        } else {
            onBluetoothSetupError();
        }

        //WiFi shit
        WiFiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if(WiFiManager != null) {

            WIFIReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if(WiFiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
                        onWiFiScanResults(WiFiManager.getScanResults());
                        onWiFiScanCompleted();
                    }
                }
            };

            IntentFilter filter = new IntentFilter();
            filter.addAction(WiFiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            activity.registerReceiver(WIFIReceiver, filter);


        } else {
            onWiFiSetupError();
        }


    }

    public void startBluetoothDiscovery(){
        if(this.isPaused){return;}
        if(this.BTAdapter == null){
            onBluetoothStartError();
            return;
        }
        this.BTAdapter.startDiscovery();
    }

    public void startBluetoothDiscovery(int sleep){
        Delay(sleep, new Runnable() {
            @Override
            public void run() {
                startBluetoothDiscovery();
            }
        });
    }

    public abstract void onBluetoothSetupError();
    public abstract void onBluetoothStartError();
    public abstract void onBluetoothDeviceDiscovery(BluetoothDevice device);
    public abstract void onBluetoothDiscoveryStarted();
    public abstract void onBluetoothDiscoveryCompleted();

    public void startWiFiScan(){
        if(this.isPaused){return;}
        if(this.WiFiManager == null){
            onWiFiStartError();
            return;
        }
        onWiFiScanStarted();
        this.WiFiManager.startScan();
    }

    public void startWiFiScan(int sleep){
        Delay(sleep, new Runnable() {
            @Override
            public void run() {
                startWiFiScan();
            }
        });
    }

    public abstract void onWiFiSetupError();
    public abstract void onWiFiStartError();
    public abstract void onWiFiScanResults(List<ScanResult> APs);
    public abstract void onWiFiScanStarted();
    public abstract void onWiFiScanCompleted();


    public void Delay(int milliseconds, Runnable runnable){
        Handler handler = new Handler();
        handler.postDelayed(runnable, milliseconds);
    }


    public void unregisterAllReceivers(){

        if(this.BTReceiver != null) {
            this.activity.unregisterReceiver(this.BTReceiver);
        }

        if(this.WIFIReceiver != null) {
            this.activity.unregisterReceiver(this.WIFIReceiver);
        }

    }

    public void pause(){
        this.isPaused = true;
    }

    public void resume(){
        this.isPaused = false;
    }



}
