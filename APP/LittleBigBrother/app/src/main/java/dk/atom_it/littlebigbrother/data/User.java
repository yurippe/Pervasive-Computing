package dk.atom_it.littlebigbrother.data;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

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
    private Date lastseen;

    private boolean isFriend;

    public User(final MapsActivity mActivity, final GoogleMap inmMap, final int userid, final double lat,
                final double lng, final String displayname, final boolean online, final String lastseen){
        this.mActivity = mActivity;
        this.mMap = inmMap;

        this.userid = userid;

        this.lat = lat;
        this.lng = lng;

        this.displayname = displayname;
        this.online = online;
        this.lastseen = new Date(Long.parseLong(lastseen) * 1000);

        if(Globals.getInstance().friendid != null){
            this.isFriend = Globals.getInstance().friendid.contains(userid);
        } else {
            isFriend = false;
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(displayname));
                marker.setDraggable(false);
                marker.setZIndex(-1);

                updateMarker();
            }
        });
    }

    public void update(final double lat, final double lng, final String displayname, final boolean online, final String lastseen){
        this.lat = lat;
        this.lng = lng;

        this.displayname = displayname;
        this.online = online;

        if(this.online = true){
            this.lastseen = new Date(Long.parseLong(lastseen) * 1000);
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateMarker();
            }
        });
    }

    private void updateMarker(){
        marker.setPosition(new LatLng(lat, lng));
        final String title = displayname;
        final String snippet;

        if(Globals.getInstance().friendid != null){
            isFriend = Globals.getInstance().friendid.contains(userid);
        } else {
            isFriend = false;
        }

        if(online){
            marker.setAlpha((float) 0.9);
            snippet = "online";
        } else {
            marker.setAlpha((float) 0.2);
            snippet = "last seen: " + lastseen.toString();
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                marker.setTitle(title);
                marker.setSnippet(snippet);

                if(isFriend){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(120));
                    marker.showInfoWindow();
                } else {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(190));
                }
            }
        });

    }

    public Marker getMarker(){
        return marker;
    }

    public int getUserid() { return userid; }

    public void setIsFriend(final boolean isFriend) {
        this.isFriend = isFriend;

        //Make the color change immediate... hopefully
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isFriend){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(120));
                    marker.showInfoWindow();
                } else {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(190));
                }
            }
        });
    }
    public boolean getIsFriend() { return isFriend; }
}