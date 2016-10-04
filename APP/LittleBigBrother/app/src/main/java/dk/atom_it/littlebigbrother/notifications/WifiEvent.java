package dk.atom_it.littlebigbrother.notifications;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by Kristian on 10/4/2016.
 */

public abstract class WifiEvent implements AbstractEvent {

    public void onUpdate(List<ScanResult> results){

    }

}