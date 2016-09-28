package dk.atom_it.littlebigbrother;

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

    public Device(final Activity activity, final String itoken, final String iaddress, final String iname){
        this.token = itoken;
        this.activity = activity;

        this.address = iaddress;

        HashMap<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("mac", iaddress);

        Endpoint login = new Endpoint(this.activity, "/deviceinfo");
        login.call(data.toString(), new Callback() {
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
                        commit();
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

    public void commit(){

        Endpoint login = new Endpoint(activity, "/updatedevice");
        JSONObject data = new JSONObject();
        try {
            data.put("token", token);
            data.put("mac", address);
            data.put("name", name);
            data.put("owner", owner);
            data.put("lat", "" + lat);
            data.put("lng", "" + lng);
        } catch (JSONException e){

        }
        login.call(data.toString(), new Callback() {
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
                        makeToast("Update successful", Toast.LENGTH_SHORT);
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
