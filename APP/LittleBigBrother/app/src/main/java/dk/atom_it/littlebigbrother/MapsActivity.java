package dk.atom_it.littlebigbrother;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.location.LocationListener;
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

import dk.atom_it.littlebigbrother.api.Endpoint;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;

    private String token = "";
    private Marker myMapMarker;

    final Context tthis = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        token = getIntent().getStringExtra("token");

        //TODO: Make the button be based on displayname of the marker selected
        Button map_button = (Button) findViewById(R.id.map_button);
        map_button.setText(getIntent().getStringExtra("username"));

        Thread userupdates = new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, User> users = new HashMap<>();
                Endpoint endpoint = new Endpoint(tthis, "/userspos");

                HashMap<String, String> credentials = new HashMap<>();
                credentials.put("token", token);
                String data = new JSONObject(credentials).toString();

                while(true){
                    String resp = endpoint.syncCall(data);
                    try{
                        JSONArray userarray = new JSONArray(resp);
                        for(int i = 0; i < userarray.length() ; i++){
                            JSONObject singularUser = (JSONObject) userarray.get(i);
                            String uid = singularUser.getString("userid");
                            if(users.containsKey(uid)){
                                users.get(uid).update(singularUser.getDouble("lat"), singularUser.getDouble("lng"),
                                        singularUser.getString("displayname"), singularUser.getInt("online") != 0,
                                        singularUser.getString("lastseen"));
                            } else {
                                users.put(uid, new User(mMap, uid, singularUser.getDouble("lat"), singularUser.getDouble("lng"),
                                        singularUser.getString("displayname"), singularUser.getInt("online") != 0,
                                        singularUser.getString("lastseen")));
                            }
                        }
                    } catch(JSONException e) {
                        //meh... don't let them know
                    }

                    try{Thread.sleep(5000);
                    } catch(Exception e){
                        Toast.makeText(tthis, "user updating crashed!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });
        userupdates.run();
    }

    @Override
    protected void onDestroy() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {/*Find better way to confirm permissions ?*/ return;}

        locationManager.removeUpdates(this);
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
        } catch(SecurityException e) {
            System.err.println("Exception! No permission");
            Toast.makeText(this, "Lacking permissions to access GPS!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Get our new position
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

        //Send movement to server
        Endpoint loc = new Endpoint(this, "/updatepos");
        HashMap<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("lat", "" + pos.latitude);
        data.put("lng", "" + pos.longitude);
        JSONObject json = new JSONObject(data);
        loc.call(json.toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(tthis, "Could not update server on our location", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();
            }
        });

        //Update marker
        myMapMarker.setPosition(pos);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }

    @Override
    public void onBackPressed(){
        HashMap<String, String> data = new HashMap<>();
        data.put("token", token);
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
                    if(jsonresp.getInt("status") != 200){
                        Toast.makeText(tthis, "Logout failed: " + jsonresp.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(tthis, "Invalid server response", Toast.LENGTH_SHORT).show();
                }
                response.close(); //Very important
            }
        });

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
}
