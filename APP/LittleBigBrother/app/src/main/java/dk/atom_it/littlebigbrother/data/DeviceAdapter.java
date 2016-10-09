package dk.atom_it.littlebigbrother.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import dk.atom_it.littlebigbrother.R;

/**
 * Created by Kristian on 9/28/2016.
 */

public class DeviceAdapter extends ArrayAdapter<DeviceModel> {

    public DeviceAdapter(Context context, ArrayList<DeviceModel> devices){
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        DeviceModel device = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.wifibt_list_item, parent, false);
        }

        TextView mac = (TextView) convertView.findViewById(R.id.device_mac);
        TextView name = (TextView) convertView.findViewById(R.id.device_name);

        /*Button claimbutton = (Button) convertView.findViewById(R.id.device_select);

        claimbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Claimed", Toast.LENGTH_SHORT).show();
            }
        });*/

        mac.setText(device.mac);
        name.setText(device.name);

        return convertView;
    }
}
