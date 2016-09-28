package dk.atom_it.littlebigbrother;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.view.View;
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

    private final Context context;
    private final String token;

    private String owner;
    private String lastseen;
    private double lat;
    private double lng;

    public Device(final Context icontext, final String itoken, final String iaddress, final String iname){
        this.token = itoken;
        this.context = icontext;

        this.address = iaddress;

        HashMap<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("mac", iaddress);

        Endpoint login = new Endpoint(context, "/deviceinfo");
        login.call(data.toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(context, "Checking server about blueetooth device failed", Toast.LENGTH_SHORT).show();
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
        HashMap<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("mac", address);
        data.put("name", name);
        data.put("owner", owner);
        data.put("lat", "" + lat);
        data.put("lng", "" + lng);

        Endpoint login = new Endpoint(context, "/updatedevice");
        login.call(data.toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(context, "Updating bluetooth device failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonresp = new JSONObject(response.body().string());
                    if(jsonresp.getInt("status") != 200){
                        Toast.makeText(context, "Updating bluetooth device failed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Update successful", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(context, "Couldn't send bluetooth info to server", Toast.LENGTH_SHORT).show();
                }
                response.close(); //Very important
            }
        });
    }







}
