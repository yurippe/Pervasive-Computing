package dk.atom_it.littlebigbrother.managers;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by Kristian on 10/3/2016.
 */

public interface BluetoothListener {


        public void onBluetoothScanStarted();
        public void onBluetoothScanResults(List<BluetoothDevice> results);
        public void onBluetoothScanCompleted();
        public void onBluetoothError();

}
