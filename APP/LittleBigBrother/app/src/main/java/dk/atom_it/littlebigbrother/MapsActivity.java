package dk.atom_it.littlebigbrother;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.location.LocationListener;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import dk.atom_it.littlebigbrother.api.Endpoint;
import dk.atom_it.littlebigbrother.data.CodeModel;
import dk.atom_it.littlebigbrother.data.Globals;
import dk.atom_it.littlebigbrother.data.User;
import dk.atom_it.littlebigbrother.managers.BluetoothListener;
import dk.atom_it.littlebigbrother.managers.BluetoothManager;
import dk.atom_it.littlebigbrother.managers.WiFiListener;
import dk.atom_it.littlebigbrother.managers.WiFiManager;
import dk.atom_it.littlebigbrother.notifications.AbstractEvent;
import dk.atom_it.littlebigbrother.notifications.BluetoothEvent;
import dk.atom_it.littlebigbrother.notifications.EventManager;
import dk.atom_it.littlebigbrother.notifications.LocationEvent;
import dk.atom_it.littlebigbrother.threading.NetworkingSucks;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, BluetoothListener, WiFiListener, GoogleMap.OnInfoWindowLongClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;

    //private String token = "";
    private Marker myMapMarker;

    private Thread userupdates;

    private NetworkingSucks networkingSucks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        
        Button map_button = (Button) findViewById(R.id.map_button);
        map_button.setText("Events");

        map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent toList = new Intent(getContext(), AddEventListener.class);
                String username = getIntent().getStringExtra("username");
                toList.putExtra("username", username);
                startActivity(toList);

            }
        });

        BluetoothManager.getInstance().scanBluetooth(this);
        WiFiManager.getInstance().scanWifi(this);

        networkingSucks = new NetworkingSucks(this);
    }

    @Override
    protected void onDestroy() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {/*Find better way to confirm permissions ?*/ return;}

        locationManager.removeUpdates(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng pos = new LatLng(0, 0);
        myMapMarker = mMap.addMarker(new MarkerOptions().position(pos).title("You"));
        myMapMarker.setIcon(BitmapDescriptorFactory.defaultMarker(30));
        myMapMarker.setAlpha((float) 0.9);
        myMapMarker.setDraggable(false);

        mMap.setOnInfoWindowLongClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        //if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {/*Find better way to confirm permissions ?*/ return;}

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
            Location lastknown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastknown == null){
                lastknown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if(lastknown != null) {
                onLocationChanged(lastknown);
            }

        } catch(SecurityException e) {
            System.err.println("Exception! No permission");
            Toast.makeText(this, "Lacking permissions to access GPS!", Toast.LENGTH_SHORT).show();
        }

        Globals.getInstance().friendid = new HashSet<Integer>();
        Globals.getInstance().cloudCode = new ArrayList<>();
        if(Globals.getInstance().token != null){
            getCloudFriends();
            getCloudNotes();
            getCloudeCode();
            startUserUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Get our new position
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
        Globals.getInstance().userPosition = pos;

        //Send movement to server
        if(Globals.getInstance().token != null){
            networkingSucks.updatePosition(Globals.getInstance().token, pos.latitude, pos.longitude);
        }

        //Update marker
        myMapMarker.setPosition(pos);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));

        //Trigger event listeners:
        EventManager.getInstance().onLocationUpdate(location);
    }

    @Override
    public void onBackPressed(){
        if(Globals.getInstance().token != null) {
            HashMap<String, String> data = new HashMap<>();
            data.put("token", Globals.getInstance().token);
            JSONObject json = new JSONObject(data);

            Endpoint logout = new Endpoint("/logout");
            logout.call(json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    makeToast("Logout failed", Toast.LENGTH_SHORT);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //The server always goes 200 on logout, but whatever... here it is...
                    try {
                        JSONObject jsonresp = new JSONObject(response.body().string());
                        Globals.getInstance().token = null;
                        if (jsonresp.getInt("status") != 200) {
                            makeToast("Logout failed: " + jsonresp.getString("message"), Toast.LENGTH_SHORT);
                        }
                    } catch (JSONException e) {
                        makeToast("Invalid server response", Toast.LENGTH_SHORT);
                    }
                    response.close(); //Very important
                }
            });
        }
        if(userupdates != null){
            userupdates.interrupt();
        }

        Intent intent = new Intent(this, Login.class);
        this.startActivity(intent);
    }

    public Context getContext(){
        return this;
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        if(marker.getPosition().equals(myMapMarker.getPosition())){
            Intent toList = new Intent(getContext(), UserProfile.class);
            startActivity(toList);
        } else {
            //Why like this? Because Globals.getInstance().usersMarkers.get(marker) doesn't work...

            //Why it is a good idea, that markers are not updated but just recreated? And that the
            //argument is not actually the same as the marker you clicked?
            //I cannot tell you. But, I can tell you, that is completely ridiculous!


            for (User user : Globals.getInstance().usersPosition.values()) {
                Marker m = user.getMarker();
                System.out.println(user.getUserid() + "");
                System.out.println(m.getPosition().toString());
                if (m.getPosition().equals(marker.getPosition())) {
                    final User founduser = user;
                    System.out.println(founduser.getUserid() + "");
                    try {
                        JSONObject data = new JSONObject();
                        data.put("token", Globals.getInstance().token);
                        data.put("friendid", founduser.getUserid());

                        if (user.getIsFriend()) {
                            //Remove from friends
                            Endpoint endpoint = new Endpoint("/deletefriend");
                            endpoint.call(data.toString(), new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    makeToast("Removal from favorites failed");
                                    founduser.setIsFriend(true);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        JSONObject jsonresp = new JSONObject(response.body().string());
                                        if (jsonresp.getInt("status") != 200) {
                                            makeToast("Removal from favorites failed");
                                            Globals.getInstance().friendid.add(founduser.getUserid());
                                            founduser.setIsFriend(true);
                                        } else {
                                            makeToast(jsonresp.getString("message"));
                                            Globals.getInstance().friendid.remove(founduser.getUserid());
                                            founduser.setIsFriend(false);
                                        }
                                    } catch (JSONException e) {
                                        //meh...
                                    }
                                }
                            });
                        } else {
                            //Add to friends
                            Endpoint endpoint = new Endpoint("/addfriend");
                            endpoint.call(data.toString(), new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    makeToast("Adding to favorites failed");
                                    founduser.setIsFriend(false);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        JSONObject jsonresp = new JSONObject(response.body().string());
                                        if (jsonresp.getInt("status") != 200) {
                                            makeToast("Adding to favorites failed");
                                            Globals.getInstance().friendid.remove(founduser.getUserid());
                                            founduser.setIsFriend(false);

                                        } else {
                                            makeToast("Added to favorites");
                                            Globals.getInstance().friendid.add(founduser.getUserid());
                                            founduser.setIsFriend(true);
                                        }
                                    } catch (JSONException e) {
                                        //meh...
                                    }

                                }
                            });
                        }
                    } catch (JSONException e) {
                        //Well, you must like or hate him for a bit longer...
                    }
                break;
                }
            }
        }
    }

    private void startUserUpdates(){
        Globals.getInstance().usersPosition = new HashMap<>();
        Globals.getInstance().usersMarkers = new HashMap<>();

        userupdates = new Thread(new Runnable() {
                @Override
                public void run() {
                    Endpoint endpoint = new Endpoint("/userspos");

                    HashMap<String, String> credentials = new HashMap<>();
                    credentials.put("token", Globals.getInstance().token);
                    String data = new JSONObject(credentials).toString();

                    while (true) {
                        String resp = endpoint.syncCall(data);
                        try {
                            if (resp != null) {
                                JSONArray userarray = new JSONArray(resp);
                                for (int i = 0; i < userarray.length(); i++) {
                                    JSONObject singularUser = userarray.getJSONObject(i);
                                    int uid = singularUser.getInt("userid");
                                    if (Globals.getInstance().usersPosition.containsKey(uid)) {
                                        Globals.getInstance().usersPosition.get(uid).update(singularUser.getDouble("lat"), singularUser.getDouble("lng"),
                                                singularUser.getString("displayname"), singularUser.getInt("online") != 0,
                                                singularUser.getString("lastseen"));
                                    } else {
                                        User newUser = new User(mMap, uid, singularUser.getDouble("lat"), singularUser.getDouble("lng"),
                                                singularUser.getString("displayname"), singularUser.getInt("online") != 0,
                                                singularUser.getString("lastseen"));
                                        Globals.getInstance().usersPosition.put(uid, newUser);
                                        Globals.getInstance().usersMarkers.put(newUser.getMarker(), newUser);
                                    }
                                }
                            } else {
                                //Toast.makeText(tthis, "Server unreachable, will reconnect in 10 sec", Toast.LENGTH_SHORT).show();
                                try {
                                    Thread.sleep(10000);
                                } catch (Exception e) {
                                    //Toast.makeText(tthis, "user updating crashed!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        } catch (JSONException e) {
                            //meh... don't let them know
                        }

                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                            //Toast.makeText(tthis, "user updating crashed!", Toast.LENGTH_SHORT).show();
                            return;
                        }}}
        });
        userupdates.start();
    }

    private void getCloudeCode(){
        Endpoint endpoint = new Endpoint("/code");
        endpoint.call(new JSONObject().toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                makeToast("Could not download code from server");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try{
                    JSONObject jsonresp = new JSONObject(response.body().string());
                    JSONArray codearray = jsonresp.getJSONArray("data");
                    System.out.println("code data: " + codearray.toString());

                    for(int i = 0; i < codearray.length(); i++){
                        JSONObject singleCode = codearray.getJSONObject(i);
                        CodeModel codeModel = new CodeModel(singleCode.getString("title"), singleCode.getString("code"));
                        Globals.getInstance().cloudCode.add(codeModel);

                        System.out.println("Code added (title: " + codeModel.getTitle() + " code: " + codeModel.getCode() + ")");
                    }
                } catch (JSONException e){
                    //Nothing, as always
                }
            }
        });

    }

    private void getCloudNotes(){
        try{
            //Flushing?
            EventManager.getInstance().clearListeners();

            //Get all events saved in the cloud... because clouds are the shit!
            JSONObject data = new JSONObject();
            data.put("token", Globals.getInstance().token);

            Endpoint endpoint = new Endpoint("/getnotes");

            endpoint.call(data.toString(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //Fuck this... I'm out!
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try{
                            JSONObject jsonresp = new JSONObject(response.body().string());
                            JSONArray notearray = jsonresp.getJSONArray("data");

                            for(int i = 0; i < notearray.length(); i++){
                                JSONObject singleNote = notearray.getJSONObject(i);
                                AbstractEvent event = EventManager.getInstance().fromJSON(singleNote);
                                EventManager.getInstance().queueListener(event);
                            }
                        } catch (JSONException e) {
                            //Fuck this... even more
                        }
                    }
            });
        } catch (JSONException e){
            //Meh...
        }
    }

    private void getCloudFriends(){
        try{
            JSONObject data = new JSONObject();
            data.put("token", Globals.getInstance().token);

            Endpoint endpoint = new Endpoint("/getfriends");
            endpoint.call(data.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //No friends for you this time...
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try{
                        JSONObject jsonresp = new JSONObject(response.body().string());
                        JSONArray friendarray = jsonresp.getJSONArray("data");

                        for(int i = 0; i < friendarray.length(); i++){
                            JSONObject friend = friendarray.getJSONObject(i);
                            Globals.getInstance().friendid.add(friend.getInt("userid"));
                        }

                    } catch (JSONException e) {
                        //No friends here either
                    }
                }
            });

        } catch(JSONException e){
            //Meh...
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    @Override
    public void onBluetoothScanStarted() {
        //Toast.makeText(this, "BT scan started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBluetoothScanResults(List<BluetoothDevice> results) {
        /*for(BluetoothDevice device : results){
            Toast.makeText(this, "Found device: " + device.getName() + " - " + device.getAddress(), Toast.LENGTH_SHORT).show();
        }*/
        EventManager.getInstance().onBluetoothUpdate(results);
    }

    @Override
    public void onBluetoothScanCompleted() {
        //Toast.makeText(this, "BT scan ended, restart in 3 secs", Toast.LENGTH_SHORT).show();
        BluetoothManager.getInstance().scanBluetooth(this, 3000);
    }

    @Override
    public void onBluetoothError() {

    }

    @Override
    public void onWiFiScanStarted() {

    }

    @Override
    public void onWiFiScanResults(List<ScanResult> results) {
        EventManager.getInstance().onWifiUpdate(results);
    }

    @Override
    public void onWiFiScanCompleted() {
        WiFiManager.getInstance().scanWifi(this, 7000);
    }

    @Override
    public void onWiFiError() {

    }

    private void makeToast(final String text, final int length) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NotamicusApp.getInstance(), text, length).show();
            }
        });
    }

    private void makeToast(final String text) {
        makeToast(text, Toast.LENGTH_LONG);
    }
}
