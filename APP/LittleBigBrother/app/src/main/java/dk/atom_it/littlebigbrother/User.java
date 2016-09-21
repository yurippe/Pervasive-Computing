package dk.atom_it.littlebigbrother;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * An object representing one singular user
 */
public class User {
    private double lat;
    private double lng;

    private GoogleMap mMap;
    private Marker marker;

    private String userid;
    private String displayname;
    private boolean online;
    private String lastseen;

    public User(GoogleMap mMap, String userid, double lat, double lng, String displayname, boolean online, String lastseen){
        this.mMap = mMap;

        this.userid = userid;

        this.lat = lat;
        this.lng = lng;

        this.displayname = displayname;
        this.online = online;
        this.lastseen = lastseen;

        LatLng pos = new LatLng(lat, lng);
        marker = mMap.addMarker(new MarkerOptions().position(pos).title(displayname));
        marker.setDraggable(false);
    }

    public void update(double lat, double lng, String displayname, boolean online, String lastseen){
        this.lat = lat;
        this.lng = lng;
        marker.setPosition(new LatLng(lat, lng));

        if(!this.displayname.equals(displayname)){
            this.displayname = displayname;
            marker.setTitle(displayname);
        }

        if(this.online != online){
            this.online = online;
            if(online){
                if(online){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(190));
                    marker.setAlpha((float) 0.9);
                } else {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(200));
                    marker.setAlpha((float) 0.4);
                }
            }
        }

        this.lastseen = lastseen;
    }

    public Marker getMarker(){
        return marker;
    }
}