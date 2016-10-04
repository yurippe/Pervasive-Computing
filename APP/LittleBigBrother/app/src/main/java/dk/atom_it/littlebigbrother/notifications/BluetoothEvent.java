package dk.atom_it.littlebigbrother.notifications;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by Kristian on 10/4/2016.
 */

public abstract class BluetoothEvent implements AbstractEvent{

    public void onUpdate(List<BluetoothDevice> results){


    }

}