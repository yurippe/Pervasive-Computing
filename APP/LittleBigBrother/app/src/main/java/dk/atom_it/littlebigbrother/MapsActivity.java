package dk.atom_it.littlebigbrother;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
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
import java.util.HashMap;
import java.util.List;

import dk.atom_it.littlebigbrother.api.Endpoint;
import dk.atom_it.littlebigbrother.data.Globals;
import dk.atom_it.littlebigbrother.data.User;
import dk.atom_it.littlebigbrother.managers.BluetoothListener;
import dk.atom_it.littlebigbrother.managers.BluetoothManager;
import dk.atom_it.littlebigbrother.managers.WiFiListener;
import dk.atom_it.littlebigbrother.managers.WiFiManager;
import dk.atom_it.littlebigbrother.notifications.BluetoothEvent;
import dk.atom_it.littlebigbrother.notifications.EventManager;
import dk.atom_it.littlebigbrother.notifications.LocationEvent;
import dk.atom_it.littlebigbrother.threading.NetworkingSucks;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, BluetoothListener, WiFiListener {

    private GoogleMap mMap;
    private LocationManager locationManager;

    //private String token = "";
    private Marker myMapMarker;

    private final MapsActivity tthis = this;
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
        Globals.getInstance().token = getIntent().getStringExtra("token");
        
        Button map_button = (Button) findViewById(R.id.map_button);
        map_button.setText("Events");

        map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent toList = new Intent(tthis, AddEventListener.class);
                tthis.startActivity(toList);

            }
        });

        BluetoothManager.getInstance(this).scanBluetooth(this);
        WiFiManager.getInstance(this).scanWifi(this);

        networkingSucks = new NetworkingSucks(this);


        //Example Location Event
        EventManager.getInstance().queueListener(new LocationEvent(new LatLng(10.0, 10.0), 2000.0) {
            @Override
            public void onEnter() {
                Toast.makeText(tthis, "OnEnter 10.0 ; 10.0", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onExit() {
                Toast.makeText(tthis, "OnExit 10.0 ; 10.0", Toast.LENGTH_LONG).show();
            }
        });

        //Example Bluetooth Event
        EventManager.getInstance().queueListener(new BluetoothEvent("9C:D2:1E:61:B3:C1", BluetoothEvent.FILTER_MAC) {
            @Override
            public void onEnter() {
                Toast.makeText(tthis, "Found Kristians PC", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onExit() {
                Toast.makeText(tthis, "Oh noes, lost Kristians PC", Toast.LENGTH_LONG).show();
            }
        });

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
        myMapMarker = mMap.addMarker(new MarkerOptions().position(pos).title("Me"));
        myMapMarker.setIcon(BitmapDescriptorFactory.defaultMarker(30));
        myMapMarker.setAlpha((float) 0.9);
        myMapMarker.setDraggable(false);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
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
        if(Globals.getInstance().token != null){
            userupdates = new Thread(new Runnable() {
                @Override
                public void run() {
                    HashMap<Integer, User> users = new HashMap<>();
                    Endpoint endpoint = new Endpoint(tthis, "/userspos");

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
                                    if (users.containsKey(uid)) {
                                        users.get(uid).update(singularUser.getDouble("lat"), singularUser.getDouble("lng"),
                                                singularUser.getString("displayname"), singularUser.getInt("online") != 0,
                                                singularUser.getString("lastseen"));
                                    } else {
                                        users.put(uid, new User(tthis, mMap, uid, singularUser.getDouble("lat"), singularUser.getDouble("lng"),
                                                singularUser.getString("displayname"), singularUser.getInt("online") != 0,
                                                singularUser.getString("lastseen")));
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
                        }
                    }
                }
            });
            userupdates.start();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Get our new position
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

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

            Endpoint logout = new Endpoint(this, "/logout");
            logout.call(json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(tthis, "Logout failed", Toast.LENGTH_SHORT);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //The server always goes 200 on logout, but whatever... here it is...
                    try {
                        JSONObject jsonresp = new JSONObject(response.body().string());
                        Globals.getInstance().token = null;
                        if (jsonresp.getInt("status") != 200) {
                            Toast.makeText(tthis, "Logout failed: " + jsonresp.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(tthis, "Invalid server response", Toast.LENGTH_SHORT).show();
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
        BluetoothManager.getInstance(this).scanBluetooth(this, 3000);
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
        WiFiManager.getInstance(this).scanWifi(this, 7000);
    }

    @Override
    public void onWiFiError() {

    }
}
