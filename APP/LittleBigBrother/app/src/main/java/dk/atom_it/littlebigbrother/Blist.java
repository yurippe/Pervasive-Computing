package dk.atom_it.littlebigbrother;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import dk.atom_it.littlebigbrother.data.DeviceModel;
import dk.atom_it.littlebigbrother.data.DeviceAdapter;

public class Blist extends AppCompatActivity {

    private ArrayList<DeviceModel> devices;
    private DeviceAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blist);

        devices = new ArrayList<>();

        final ListView listBluetooth = (ListView) findViewById(R.id.listLayout);
        adapter = new DeviceAdapter(this, devices);
        listBluetooth.setAdapter(adapter);

        adapter.add(new DeviceModel("mac", "name"));

    }
}
