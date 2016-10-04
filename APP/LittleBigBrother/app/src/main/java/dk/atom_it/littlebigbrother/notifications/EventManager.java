package dk.atom_it.littlebigbrother.notifications;

import android.bluetooth.BluetoothDevice;
import android.location.Location;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

import dk.atom_it.littlebigbrother.data.Haversine;

/**
 * Created by Kristian on 10/4/2016.
 */

public class EventManager {

    public static final int BLUETOOTH_ENTER = 0;
    public static final int BLUETOOTH_EXIT = 1;
    public static final int WIFI_ENTER = 2;
    public static final int WIFI_EXIT = 3;
    public static final int LOCATION_ENTER = 4;
    public static final int LOCATION_EXIT = 5;

    //Singleton:
    private static EventManager eventManager;
    protected EventManager(){

    }
    public static EventManager getInstance(){
        if(eventManager == null){
            eventManager = new EventManager();
        }
        return eventManager;
    }

    //Field variables
    private List<WifiEvent> wifiEvents = new ArrayList<>();
    private List<BluetoothEvent> bluetoothEvents = new ArrayList<>();
    private List<LocationEvent> locationEvents = new ArrayList<>();


    //Methods
    public void onBluetoothUpdate(List<BluetoothDevice> devices){
        for(BluetoothEvent event : bluetoothEvents){
            event.onUpdate(devices);
        }
    }

    public void onWifiUpdate(List<ScanResult> results){
        for(WifiEvent event : wifiEvents){
            event.onUpdate(results);
        }
    }


    public void onLocationUpdate(Location location){
        for(LocationEvent event : locationEvents){
            event.onUpdate(location);
        }
    }

    public void queueListener(WifiEvent event){
        wifiEvents.add(event);
    }

    public void queueListener(BluetoothEvent event){
        bluetoothEvents.add(event);
    }

    public void queueListener(LocationEvent event){
        locationEvents.add(event);
    }







}
