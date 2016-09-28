package dk.atom_it.littlebigbrother.data;

import android.app.Activity;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import dk.atom_it.littlebigbrother.api.Endpoint;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Steffan Sølvsten on 28-09-2016.
 */

public class Device {
    private String address;
    private String name;

    private final Activity activity;
    private final String token;

    private String owner;
    private String lastseen;
    private double lat;
    private double lng;

    public Device(final Activity activity, final String itoken, final String iaddress, final String iname, final Double lat, final Double lng){


        this.token = itoken;
        this.activity = activity;

        this.address = iaddress;

        this.lat = lat;
        this.lng = lng;

        Endpoint endpoint = new Endpoint(this.activity, "/deviceinfo");
        JSONObject data = new JSONObject();
        try{
            data.put("token", token);
            data.put("mac", iaddress);
        } catch (JSONException e) {
            return;
        }

        endpoint.call(data.toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                makeToast("Checking server about blueetooth device failed", Toast.LENGTH_SHORT);
                setName(iname);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonresp = new JSONObject(response.body().string());
                    if(jsonresp.getInt("status") == 404){
                        setName(iname);
                        addDevice();
                        return;
                    } else if(jsonresp.getInt("status") == 200){
                        setName(jsonresp.getString("name"));
                        setOwner(jsonresp.getString("owner"));
                        setLastSeen(jsonresp.getString("lastseen"));
                    } else {
                        return;
                    }

                } catch (JSONException e) {
                    setName(iname);
                    return;
                }

                response.close(); //Very important
            }
        });
    }

    public void setName(String name){
        this.name = name;
    }

    public void setOwner(String owner){
        this.owner = owner;
    }

    public void setLastSeen(String lastseen){
        this.lastseen = lastseen;
    }

    public void setLatLng(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }

    public void addDevice(){
        Endpoint endpoint = new Endpoint(this.activity, "/adddevice");
        JSONObject data = new JSONObject();
        try{
            data.put("token", token);
            data.put("mac", address);
            data.put("name", name);
            data.put("lat", lat);
            data.put("lng", lng);
        } catch (JSONException e) {
            return;
        }

        endpoint.call(data.toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                makeToast("Adding bluetooth device failed", Toast.LENGTH_SHORT);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonresp = new JSONObject(response.body().string());
                    if(jsonresp.getInt("status") != 200){
                        makeToast("Adding bluetooth device failed", Toast.LENGTH_SHORT);
                        return;
                    }

                } catch (JSONException e) {
                    makeToast("Couldn't send bluetooth info to server", Toast.LENGTH_SHORT);
                    return;
                }

                response.close(); //Very important
            }
        });

    }

    public void commit(){
        //TODO: Don't automatically claim...
        claimDevice();

        Endpoint endpoint = new Endpoint(activity, "/updatedevice");
        JSONObject data = new JSONObject();
        try {
            data.put("token", token);
            data.put("mac", address);
            data.put("name", name);
            data.put("lat", "" + lat);
            data.put("lng", "" + lng);
        } catch (JSONException e){
            return;
        }

        endpoint.call(data.toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                makeToast("Updating bluetooth device failed", Toast.LENGTH_SHORT);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonresp = new JSONObject(response.body().string());
                    if(jsonresp.getInt("status") != 200){
                        makeToast("Updating bluetooth device failed", Toast.LENGTH_SHORT);
                    } else {
                        //makeToast("Update successful", Toast.LENGTH_SHORT);
                    }

                } catch (JSONException e) {
                    makeToast("Couldn't send bluetooth info to server", Toast.LENGTH_SHORT);
                }
                response.close(); //Very important
            }
        });
    }

    public void claimDevice(){
        Endpoint endpoint = new Endpoint(activity, "/claimdevice");
        JSONObject data = new JSONObject();
        try {
            data.put("token", token);
            data.put("mac", address);
        } catch (JSONException e){
            return;
        }

        endpoint.call(data.toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                makeToast("Claiming bluetooth device failed", Toast.LENGTH_SHORT);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonresp = new JSONObject(response.body().string());
                    if(jsonresp.getInt("status") != 200){
                        makeToast("Claiming bluetooth device failed", Toast.LENGTH_SHORT);
                    } else {
                        //makeToast("Update successful", Toast.LENGTH_SHORT);
                    }

                } catch (JSONException e) {
                    makeToast("Couldn't send bluetooth info to server", Toast.LENGTH_SHORT);
                }
                response.close(); //Very important
            }
        });
    }

    public void makeToast(final String msg, final int length){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, length).show();
            }
        });
    }



}
