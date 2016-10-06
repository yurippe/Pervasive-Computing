package dk.atom_it.littlebigbrother;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dk.atom_it.littlebigbrother.data.DeviceModel;
import dk.atom_it.littlebigbrother.data.DeviceAdapter;
import dk.atom_it.littlebigbrother.managers.BluetoothListener;
import dk.atom_it.littlebigbrother.managers.BluetoothManager;
import dk.atom_it.littlebigbrother.threading.ASyncSucks;

public class Blist extends AppCompatActivity implements BluetoothListener {

    //For questions:
    //https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView

    private ArrayList<DeviceModel> devices;
    private DeviceAdapter adapter;
    private ASyncSucks aSyncSucks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blist);

        devices = new ArrayList<>();

        final ListView listBluetooth = (ListView) findViewById(R.id.listLayout);
        adapter = new DeviceAdapter(this, devices);
        listBluetooth.setAdapter(adapter);

        BluetoothManager.getInstance(this).scanBluetooth(this);

        Button refreshButton = (Button) findViewById(R.id.bluetooth_refresh);
        final Blist tthis = this;
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothManager.getInstance(tthis).scanBluetooth(tthis);
            }
        });
    }

    @Override
    public void onBluetoothScanStarted() {
        adapter.clear();
        Toast.makeText(this, "Scan started", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBluetoothScanResults(List<BluetoothDevice> results) {

        for(BluetoothDevice device : results) {
            DeviceModel devicemodel = new DeviceModel(device.getAddress(), device.getName());
            if (devices.contains(devicemodel)) {
                return;
            }
            adapter.add(devicemodel);
        }
    }

    @Override
    public void onBluetoothScanCompleted() {
        Toast.makeText(this, "Scan complete", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBluetoothError() {
        Toast.makeText(this, "Could not access Bluetooth on this device", Toast.LENGTH_LONG).show();
    }
}
