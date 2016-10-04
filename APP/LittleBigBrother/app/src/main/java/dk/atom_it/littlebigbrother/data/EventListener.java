package dk.atom_it.littlebigbrother.data;

import org.json.JSONObject;

/**
 * Created by Kristian on 10/4/2016.
 */

public class EventListener {

    public static final int BLUETOOTH_ENTER = 0;
    public static final int BLUETOOTH_EXIT = 1;
    public static final int WIFI_ENTER = 2;
    public static final int WIFI_EXIT = 3;
    public static final int LOCATION_ENTER = 4;
    public static final int LOCATION_EXIT = 5;

    private int type;

    public EventListener(int type){
        this.type = type;
    }

    public void notify(int eventType){

    }

    public String toJson(){
        JSONObject data = new JSONObject();
        try{

            data.put("type", this.type);

        } catch (Exception e){
            return "";
        }
        return data.toString();
    }



}
