package dk.atom_it.littlebigbrother.data;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import dk.atom_it.littlebigbrother.MapsActivity;

/**
 * An object representing one singular user
 */
public class User {
    private double lat;
    private double lng;

    private final MapsActivity mActivity;
    private final GoogleMap mMap;
    private Marker marker;

    private final int userid;
    private String displayname;
    private boolean online;
    private String lastseen;

    public User(final MapsActivity mActivity, final GoogleMap inmMap, final int userid, final double lat,
                final double lng, final String displayname, final boolean online, final String lastseen){
        this.mActivity = mActivity;
        this.mMap = inmMap;

        this.userid = userid;

        this.lat = lat;
        this.lng = lng;

        this.displayname = displayname;
        this.online = online;
        this.lastseen = lastseen;

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(displayname));
                marker.setDraggable(false);
                marker.setZIndex(-1);

                marker.setPosition(new LatLng(lat, lng));
                String title = "" + displayname;
                if(online){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(190));
                    marker.setAlpha((float) 0.9);
                    title = title + ", online";
                } else {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(200));
                    marker.setAlpha((float) 0.4);
                    title = title + ", offline";
                }
                marker.setTitle(title);
            }
        });

    }

    public void update(final double lat, final double lng, final String displayname, final boolean online, final String lastseen){
        this.lat = lat;
        this.lng = lng;

        if(!this.displayname.equals(displayname)){
            this.displayname = displayname;
        }

        if(this.online != online) {
            this.online = online;
        }

        this.lastseen = lastseen;

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                marker.setPosition(new LatLng(lat, lng));
                String title = "" + displayname;
                if(online){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(190));
                    marker.setAlpha((float) 0.9);
                    title = title + ", online";
                } else {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(200));
                    marker.setAlpha((float) 0.2);
                    title = title + ", offline";
                }
                marker.setTitle(title);
            }
        });

    }

    public Marker getMarker(){
        return marker;
    }
}