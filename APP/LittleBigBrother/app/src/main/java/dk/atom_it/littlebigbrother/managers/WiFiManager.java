package dk.atom_it.littlebigbrother.managers;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Kristian on 10/3/2016.
 */

public class WiFiManager {

    //Singleton
    private static WiFiManager wiFiManager;
    //Androids WifiManager
    private WifiManager WiFiManager;
    private BroadcastReceiver WIFIReceiver;

    private boolean scanning = false;
    private Set<WiFiListener> listeners;

    public static WiFiManager getInstance(Context context){
        if(wiFiManager == null){
            wiFiManager = new WiFiManager(context);
        }
        if(!wiFiManager.checkIntegrity()){
            wiFiManager.reset(context);
        }
        return wiFiManager;
    }

    protected WiFiManager(Context context){
        reset(context);
    }

    private boolean checkIntegrity(){
        return WiFiManager != null;
    }

    private void reset(Context context){
        WiFiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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
            context.registerReceiver(WIFIReceiver, filter);


        } else {
            onWiFiError();
        }
    }

    public void stop(Context context){
        context.unregisterReceiver(WIFIReceiver);
        WiFiManager = null;
    }

    private void onWiFiError(){

    }

    private void onWiFiScanStarted(){
        scanning = true;
    }

    private void onWiFiScanResults(List<ScanResult> results){
        for(WiFiListener listener : listeners){
            listener.onScanResults(results);
        }
    }

    private void onWiFiScanCompleted(){
        for(WiFiListener listener : listeners){
            listener.onScanCompleted();
        }
        scanning = false;
    }

    public void scanWifi(WiFiListener listener){
        if(!checkIntegrity()){
            listener.onError();
            return;
        }
        if(listeners == null){
            listeners = new HashSet<>();
        }
        if(scanning){
            listeners.add(listener);
            listener.onScanStarted();
        } else {
            onWiFiScanStarted();
            WiFiManager.startScan();
            listeners = new HashSet<>();
            listeners.add(listener);
            listener.onScanStarted();
        }
    }


}