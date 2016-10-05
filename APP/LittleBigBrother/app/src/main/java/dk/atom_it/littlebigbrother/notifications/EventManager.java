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
    public List<AbstractEvent> getAllEvents(){
        List<AbstractEvent> list = new ArrayList<>();
        list.addAll(getWifiEvents());
        list.addAll(getBluetoothEvents());
        list.addAll(getLocationEvents());
        return list;
    }


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

    public void queueListener(AbstractEvent event) {
        if(event instanceof WifiEvent){queueListener((WifiEvent) event);}
        else if(event instanceof BluetoothEvent){queueListener((BluetoothEvent) event);}
        else if(event instanceof LocationEvent){queueListener((LocationEvent) event);}
        else {/*ok ? */ return;}
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

    public void clearListeners(){
        wifiEvents.clear();
        bluetoothEvents.clear();
        locationEvents.clear();
    }

    public boolean unqueueListener(AbstractEvent event){
        if(event instanceof WifiEvent){
            return wifiEvents.remove(event);
        } else if (event instanceof BluetoothEvent){
            return bluetoothEvents.remove(event);
        } else if (event instanceof LocationEvent){
            return locationEvents.remove(event);
        } else {
            return false;
        }
    }

    public AbstractEvent fromJSON(String data, final Activity activity){
        try{return fromJSON(new JSONObject(data), activity);}
        catch (JSONException e){return null;}
    }

    public AbstractEvent fromJSON(JSONObject data, final Activity activity) {

        //{noteid, type, note} + (for types 4 & 5) {lat, lng, radius} + (for types 0,1,2,3) {filtertype [range 0-1], filter}
        try {
            final int type = data.getInt("type");
            final String jhemeProgram = data.getString("note");
            AbstractEvent returnValue;
            if (type == LOCATION_ENTER) {

                returnValue = new LocationEvent(new LatLng(data.getDouble("lat"), data.getDouble("lng")), data.getDouble("radius")) {
                    @Override
                    public void onEnter() {
                        try {
                            JhemeInterpreter jheme = new JhemeInterpreter(activity);
                            jheme.eval(getJhemeCode());
                        }catch (Exception exception){
                            System.err.println("Scheme Error in onEnterLocation");
                        }
                    }

                    @Override
                    public void onExit() {
                    }

                    @Override
                    public String getJhemeCode() {return jhemeProgram;}
                    @Override
                    public int getEventType(){return type;}

                };

            } else if (type == LOCATION_EXIT) {
                returnValue =  new LocationEvent(new LatLng(data.getDouble("lat"), data.getDouble("lng")), data.getDouble("radius")) {
                    @Override
                    public void onEnter() {
                    }

                    @Override
                    public void onExit() {
                        try{
                        JhemeInterpreter jheme = new JhemeInterpreter(activity);
                        jheme.eval(getJhemeCode());
                        } catch (Exception exception){
                                System.err.println("Scheme Error in onExitLocation");
                        }
                    }

                    @Override
                    public String getJhemeCode() {return jhemeProgram;}
                    @Override
                    public int getEventType(){return type;}
                };

            } else if (type == WIFI_ENTER) {
                returnValue =  new WifiEvent(data.getString("filter"), data.getInt("filtertype")) {
                    @Override
                    public void onEnter() {
                        try{
                            JhemeInterpreter jheme = new JhemeInterpreter(activity);
                            jheme.eval(getJhemeCode());
                        } catch (Exception exception){
                            System.err.println("Scheme Error in onEnterWiFi");
                        }
                    }

                    @Override
                    public void onExit() {
                    }

                    @Override
                    public String getJhemeCode() {return jhemeProgram;}
                    @Override
                    public int getEventType(){return type;}
                };

            } else if (type == WIFI_EXIT) {
                returnValue =  new WifiEvent(data.getString("filter"), data.getInt("filtertype")) {
                    @Override
                    public void onEnter() {
                    }

                    @Override
                    public void onExit() {
                        try{
                            JhemeInterpreter jheme = new JhemeInterpreter(activity);
                            jheme.eval(getJhemeCode());
                        } catch (Exception exception){
                            System.err.println("Scheme Error in onExitWiFi");
                        }
                    }

                    @Override
                    public String getJhemeCode() {return jhemeProgram;}
                    @Override
                    public int getEventType(){return type;}
                };

            } else if (type == BLUETOOTH_ENTER) {
                returnValue =  new BluetoothEvent(data.getString("filter"), data.getInt("filtertype")) {
                    @Override
                    public void onEnter() {
                        try{
                            JhemeInterpreter jheme = new JhemeInterpreter(activity);
                            jheme.eval(getJhemeCode());
                        } catch (Exception exception){
                            System.err.println("Scheme Error in onEnterBluetooth");
                        }
                    }

                    @Override
                    public void onExit() {
                    }

                    @Override
                    public String getJhemeCode() {return jhemeProgram;}
                    @Override
                    public int getEventType(){return type;}
                };

            } else if (type == BLUETOOTH_EXIT) {
                returnValue =  new BluetoothEvent(data.getString("filter"), data.getInt("filtertype")) {
                    @Override
                    public void onEnter() {
                    }

                    @Override
                    public void onExit() {
                        try{
                            JhemeInterpreter jheme = new JhemeInterpreter(activity);
                            jheme.eval(getJhemeCode());
                        } catch (Exception exception){
                            System.err.println("Scheme Error in onExitBluetooth");
                        }
                    }

                    @Override
                    public String getJhemeCode() {return jhemeProgram;}
                    @Override
                    public int getEventType(){return type;}
                };

            } else {
                return null;
            }

            if(data.has("noteid")){
                try{
                    int noteid = data.getInt("noteid");
                    returnValue.setNoteId(noteid);
                } catch (Exception exception){
                    //Fail silently
                }
            }

            return returnValue;

        } catch (JSONException e){
            return null;
        }


    }







}
