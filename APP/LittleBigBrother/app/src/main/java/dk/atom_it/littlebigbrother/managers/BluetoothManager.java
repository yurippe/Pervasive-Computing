package dk.atom_it.littlebigbrother.managers;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.atom_it.littlebigbrother.NotamicusApp;

/**
 * Created by Kristian on 10/3/2016.
 */

public class BluetoothManager {

    //Singleton
    private static BluetoothManager bluetoothManager;
    //Androids WifiManager
    private BluetoothAdapter BTAdapter;
    private BroadcastReceiver BTReceiver;

    private boolean scanning = false;
    private Set<BluetoothListener> listeners;
    private List<BluetoothDevice> results = new ArrayList<>();

    public static BluetoothManager getInstance(){
        if(bluetoothManager == null){
            bluetoothManager = new BluetoothManager();
        }
        if(!bluetoothManager.checkIntegrity()){
            bluetoothManager.reset();
        }
        return bluetoothManager;
    }

    protected BluetoothManager(){
        reset();
    }

    private boolean checkIntegrity(){
        return BTAdapter != null;
    }

    private void reset(){
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        if(BTAdapter != null) {

            BTReceiver = new BroadcastReceiver(){
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
            NotamicusApp.getInstance().registerReceiver(BTReceiver, filter);


        } else {
            onBluetoothError();
        }
    }

    public void stop(Context context){
        context.unregisterReceiver(BTReceiver);
        BTAdapter = null;
    }

    private void onBluetoothError(){

    }

    private void onBluetoothDiscoveryStarted(){
        scanning = true;
        results.clear();
    }

    private void onBluetoothDeviceDiscovery(BluetoothDevice device){
        results.add(device);
    }

    private void onBluetoothScanResults(){
        for(BluetoothListener listener : listeners){
            listener.onBluetoothScanResults(results);
        }
    }

    private void onBluetoothDiscoveryCompleted(){
        onBluetoothScanResults();
        for(BluetoothListener listener : listeners){
            listener.onBluetoothScanCompleted();
        }
        scanning = false;
    }

    public void scanBluetooth(BluetoothListener listener){
        if(!checkIntegrity()){
            listener.onBluetoothError();
            return;
        }
        if(listeners == null){
            listeners = new HashSet<>();
        }
        if(scanning){
            listeners.add(listener);
            listener.onBluetoothScanStarted();
        } else {
            onBluetoothDiscoveryStarted();
            BTAdapter.startDiscovery();
            listeners = new HashSet<>();
            listeners.add(listener);
            listener.onBluetoothScanStarted();
        }
    }

    public void scanBluetooth(final BluetoothListener listener, int delay){
        Delay(delay, new Runnable() {
            @Override
            public void run() {
                scanBluetooth(listener);
            }
        });
    }

    private void Delay(int milliseconds, Runnable runnable){
        Handler handler = new Handler();
        handler.postDelayed(runnable, milliseconds);
    }



}
