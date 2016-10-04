package dk.atom_it.littlebigbrother.notifications;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import dk.atom_it.littlebigbrother.data.Haversine;

/**
 * Created by Kristian on 10/4/2016.
 */

public abstract class LocationEvent implements AbstractEvent{

    private boolean entered = false;
    private LatLng location;
    private Double radius;

    public LocationEvent(LatLng loc, Double radius){
        this.location = loc;
        this.radius = radius;
    }

    public void onUpdate(Location result){
        //Only update on enter and exit
        if(isInside(result)){
            if(!entered){
                this.onEnter();
                this.entered = true;
            }
        } else {
            if(entered){
                this.onExit();
                this.entered = false;
            }
        }
    }


    private boolean isInside(Location loc){
        return Haversine.HaversineInM(this.location.latitude, this.location.longitude, loc.getLatitude(), loc.getLongitude()) < this.radius;
    }



}