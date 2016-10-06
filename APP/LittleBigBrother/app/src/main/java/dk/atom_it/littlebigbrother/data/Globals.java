package dk.atom_it.littlebigbrother.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Set;

import dk.atom_it.littlebigbrother.notifications.AbstractEvent;

/**
 * Created by Kristian on 10/5/2016.
 */

public class Globals {

    public String token = null;

    public LatLng userPosition = null;

    public HashMap<Integer, User> usersPosition = null;
    public HashMap<Marker, User> usersMarkers = null;

    public AbstractEvent tempEvent = null;

    public Set<Integer> friendid = null;

    //Singleton stuff:
    private static Globals instance;
    protected Globals(){}
    public static Globals getInstance(){
        if(instance == null){
            instance = new Globals();
        }
        return instance;
    }

}
