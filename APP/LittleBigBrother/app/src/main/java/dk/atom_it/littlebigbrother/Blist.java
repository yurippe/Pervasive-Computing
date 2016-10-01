package dk.atom_it.littlebigbrother;

import android.bluetooth.BluetoothDevice;
import android.net.wifi.ScanResult;
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
import dk.atom_it.littlebigbrother.threading.ASyncSucks;

public class Blist extends AppCompatActivity {

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

        aSyncSucks = new ASyncSucks(this) {
            @Override
            public void onBluetoothSetupError() {
                Toast.makeText(this.activity, "Could not access Bluetooth on this device", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onBluetoothStartError() {
                Toast.makeText(this.activity, "Could not access Bluetooth on this device", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onBluetoothDeviceDiscovery(BluetoothDevice device) {
                DeviceModel devicemodel = new DeviceModel(device.getAddress(), device.getName());
                if(devices.contains(devicemodel)){
                    return;
                }
                adapter.add(devicemodel);
            }

            @Override
            public void onBluetoothDiscoveryStarted() {
                adapter.clear();
            }

            @Override
            public void onBluetoothDiscoveryCompleted() {
                Toast.makeText(this.activity, "Scan complete", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onWiFiSetupError() {

            }

            @Override
            public void onWiFiStartError() {

            }

            @Override
            public void onWiFiScanResults(List<ScanResult> APs) {

            }

            @Override
            public void onWiFiScanStarted() {

            }

            @Override
            public void onWiFiScanCompleted() {

            }
        };

        aSyncSucks.startBluetoothDiscovery();

        Button refreshButton = (Button) findViewById(R.id.bluetooth_refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(aSyncSucks != null) {
                    aSyncSucks.startBluetoothDiscovery();
                }
            }
        });
    }
}
