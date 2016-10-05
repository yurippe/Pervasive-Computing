package dk.atom_it.littlebigbrother.notifications;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.location.Location;
import android.net.wifi.ScanResult;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dk.atom_it.littlebigbrother.JhemeExtensions.JhemeInterpreter;
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

    public static String typeToString(int type){
        switch (type){
            case 0: return "Bluetooth Enter";
            case 1: return "Bluetooth Exit";
            case 2: return "Wifi Enter";
            case 3: return "Wifi Exit";
            case 4: return "Location Enter";
            case 5: return "Location Exit";
            default: return "Unknown (" + type + ")";
        }
    }

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

    public List<WifiEvent> getWifiEvents(){return this.wifiEvents;}
    public List<BluetoothEvent> getBluetoothEvents(){return this.bluetoothEvents;}
    public List<LocationEvent> getLocationEvents(){return this.locationEvents;}


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

    public AbstractEvent fromJSON(String data, final Activity activity){
        try{return fromJSON(new JSONObject(data), activity);}
        catch (JSONException e){return null;}
    }

    public AbstractEvent fromJSON(JSONObject data, final Activity activity) {

        //{noteid, type, note} + (for types 4 & 5) {lat, lng, radius} + (for types 0,1,2,3) {filtertype [range 0-1], filter}
        try {
            int type = data.getInt("type");
            final String jhemeProgram = data.getString("note");

            if (type == LOCATION_ENTER) {

                return new LocationEvent(new LatLng(data.getDouble("lat"), data.getDouble("lng")), data.getDouble("radius")) {
                    @Override
                    public void onEnter() {
                        try {
                            JhemeInterpreter jheme = new JhemeInterpreter(activity);
                            jheme.eval(jhemeProgram);
                        }catch (Exception exception){
                            System.err.println("Scheme Error in onEnterLocation");
                        }
                    }

                    @Override
                    public void onExit() {
                    }
                };

            } else if (type == LOCATION_EXIT) {
                return new LocationEvent(new LatLng(data.getDouble("lat"), data.getDouble("lng")), data.getDouble("radius")) {
                    @Override
                    public void onEnter() {
                    }

                    @Override
                    public void onExit() {
                        try{
                        JhemeInterpreter jheme = new JhemeInterpreter(activity);
                        jheme.eval(jhemeProgram);
                        } catch (Exception exception){
                                System.err.println("Scheme Error in onExitLocation");
                        }
                    }
                };

            } else if (type == WIFI_ENTER) {
                return new WifiEvent(data.getString("filter"), data.getInt("filtertype")) {
                    @Override
                    public void onEnter() {
                        try{
                            JhemeInterpreter jheme = new JhemeInterpreter(activity);
                            jheme.eval(jhemeProgram);
                        } catch (Exception exception){
                            System.err.println("Scheme Error in onEnterWiFi");
                        }
                    }

                    @Override
                    public void onExit() {
                    }
                };

            } else if (type == WIFI_EXIT) {
                return new WifiEvent(data.getString("filter"), data.getInt("filtertype")) {
                    @Override
                    public void onEnter() {
                    }

                    @Override
                    public void onExit() {
                        try{
                            JhemeInterpreter jheme = new JhemeInterpreter(activity);
                            jheme.eval(jhemeProgram);
                        } catch (Exception exception){
                            System.err.println("Scheme Error in onExitWiFi");
                        }
                    }
                };

            } else if (type == BLUETOOTH_ENTER) {
                return new BluetoothEvent(data.getString("filter"), data.getInt("filtertype")) {
                    @Override
                    public void onEnter() {
                        try{
                            JhemeInterpreter jheme = new JhemeInterpreter(activity);
                            jheme.eval(jhemeProgram);
                        } catch (Exception exception){
                            System.err.println("Scheme Error in onEnterBluetooth");
                        }
                    }

                    @Override
                    public void onExit() {
                    }
                };

            } else if (type == BLUETOOTH_EXIT) {
                return new BluetoothEvent(data.getString("filter"), data.getInt("filtertype")) {
                    @Override
                    public void onEnter() {
                    }

                    @Override
                    public void onExit() {
                        try{
                            JhemeInterpreter jheme = new JhemeInterpreter(activity);
                            jheme.eval(jhemeProgram);
                        } catch (Exception exception){
                            System.err.println("Scheme Error in onExitBluetooth");
                        }
                    }
                };

            } else {
                return null;
            }
        } catch (JSONException e){
            return null;
        }


    }







}
