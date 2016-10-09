package dk.atom_it.littlebigbrother;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import dk.atom_it.littlebigbrother.api.Endpoint;
import dk.atom_it.littlebigbrother.data.Globals;
import dk.atom_it.littlebigbrother.notifications.AbstractEvent;
import dk.atom_it.littlebigbrother.notifications.BluetoothEvent;
import dk.atom_it.littlebigbrother.notifications.EventManager;
import dk.atom_it.littlebigbrother.notifications.LocationEvent;
import dk.atom_it.littlebigbrother.notifications.WifiEvent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateEventListener extends AppCompatActivity {

    private AbstractEvent event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event_listener);

        //Common values from intent
        event = Globals.getInstance().tempEvent;

        //Locationbased dialog
        final EditText dialogLAT = (EditText) findViewById(R.id.dialog_lat);
        final EditText dialogLNG = (EditText) findViewById(R.id.dialog_lng);
        final EditText dialogRADIUS = (EditText) findViewById(R.id.dialog_radius);

        //Networkbased dialog
        final EditText dialogFILTER = (EditText) findViewById(R.id.dialog_filter);
        final ToggleButton dialogFTYPE = (ToggleButton) findViewById(R.id.dialog_filtertype);

        if(event instanceof BluetoothEvent || event instanceof WifiEvent){
            //Set standard values for changing type over
            dialogLAT.setText(Globals.getInstance().userPosition.latitude + "");
            dialogLNG.setText(Globals.getInstance().userPosition.longitude + "");
            dialogRADIUS.setText("50");

            //What is it really?
            if(event instanceof BluetoothEvent){
                BluetoothEvent cevent = (BluetoothEvent) event;

                dialogFILTER.setText(cevent.getFilter());
                dialogFTYPE.setChecked(cevent.getFilterType() == BluetoothEvent.FILTER_MAC);
            } else if (event instanceof WifiEvent) {
                WifiEvent cevent = (WifiEvent) event;

                dialogFILTER.setText(cevent.getFilter());
                dialogFTYPE.setChecked(cevent.getFilterType() == BluetoothEvent.FILTER_MAC);
            }

            findViewById(R.id.NoteUWIFIBT).setVisibility(View.VISIBLE);
            findViewById(R.id.NoteULoc).setVisibility(View.GONE);

        } else if (event instanceof LocationEvent) {
            //Set standrard values for changing type over
            dialogFILTER.setText("FF:FF:FF:FF:FF:FF");
            dialogFTYPE.setChecked(true);

            LocationEvent cevent = (LocationEvent) event;
            dialogLAT.setText(cevent.getLocation().latitude + "");
            dialogLNG.setText(cevent.getLocation().longitude + "");
            dialogRADIUS.setText(cevent.getRadius() + "");

            findViewById(R.id.NoteULoc).setVisibility(View.VISIBLE);
            findViewById(R.id.NoteUWIFIBT).setVisibility(View.GONE);

        } else {
            //wtf?
        }

        //Map button
        final Button mapButt = (Button) findViewById(R.id.dialog_map);
        mapButt.setOnClickListener(AddEventListener.mapPopUp(this, new LatLng(Double.parseDouble(dialogLAT.getText().toString()), Double.parseDouble(dialogLNG.getText().toString())), dialogLAT, dialogLNG));


        //Common fields
        final EditText jheme = (EditText) findViewById(R.id.NoteUJheme);
        jheme.setText(event.getJhemeCode());

        final Spinner spinner = (Spinner) findViewById((R.id.NoteUSpinner));
        final ArrayAdapter<CharSequence> eventTypeAdapter = ArrayAdapter.createFromResource(this, R.array.event_types, android.R.layout.simple_spinner_item);
        eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(eventTypeAdapter);
        spinner.setSelection(event.getEventType());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == EventManager.BLUETOOTH_ENTER || position == EventManager.BLUETOOTH_EXIT || position == EventManager.WIFI_ENTER || position == EventManager.WIFI_EXIT){
                    findViewById(R.id.NoteUWIFIBT).setVisibility(View.VISIBLE);
                    findViewById(R.id.NoteULoc).setVisibility(View.GONE);
                } else if (position == EventManager.LOCATION_ENTER || position == EventManager.LOCATION_EXIT) {
                    findViewById(R.id.NoteULoc).setVisibility(View.VISIBLE);
                    findViewById(R.id.NoteUWIFIBT).setVisibility(View.GONE);
                } else {
                    //wtf?
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //wtf?
            }
        });

        //Buttons
        final Button DeleteButt = (Button) findViewById(R.id.NoteUDelete);
        final Button UpdateButt = (Button) findViewById(R.id.NoteUUpdate);

        DeleteButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });

        UpdateButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    JSONObject json = new JSONObject();
                    json.put("token", Globals.getInstance().token);
                    json.put("type", spinner.getSelectedItemPosition());
                    json.put("note", jheme.getText().toString());
                    try{
                        json.put("lat", Double.parseDouble(dialogLAT.getText().toString()));
                        json.put("lng", Double.parseDouble(dialogLNG.getText().toString()));
                        json.put("radius", Double.parseDouble(dialogRADIUS.getText().toString()));
                    } catch (Exception e) {
                        makeToast("lat, lng and/or radius is not a number");
                        return;
                    }
                    json.put("filtertype", dialogFTYPE.isChecked() ? 0 : 1);
                    json.put("filter", dialogFILTER.getText().toString());

                    AddEventListener.processJSON(json);
                    delete();
                } catch (JSONException e) {
                    //meh
                }
            }
        });
    }

    private void delete(){
        try{
            JSONObject data = new JSONObject();
            data.put("token", Globals.getInstance().token);
            data.put("noteid", event.getNoteId());

            Endpoint endpoint = new Endpoint("/deletenote");
            endpoint.call(data.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    makeToast("Deletion to server failed");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try{
                        JSONObject resp = new JSONObject(response.body().string());
                        if(resp.getInt("status") != 200){
                            makeToast("Deletion failed: " + resp.getString("message"));
                            response.close();
                            return;
                        }
                        EventManager.getInstance().unqueueListener(event);

                        //Let's get back to the list... in the hackiest way possible
                        finish();

                    } catch (JSONException e){
                        makeToast("Deletion response unreadable");
                    }
                }
            });

        } catch(JSONException e) {
            // meh...
        }
    }

    private void makeToast(final String text){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NotamicusApp.getInstance(), text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
