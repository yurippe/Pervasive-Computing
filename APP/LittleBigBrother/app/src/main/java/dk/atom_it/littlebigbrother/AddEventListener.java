package dk.atom_it.littlebigbrother;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dk.atom_it.littlebigbrother.JhemeExtensions.JhemeInterpreter;
import dk.atom_it.littlebigbrother.data.CodeAdapter;
import dk.atom_it.littlebigbrother.data.CodeModel;
import dk.atom_it.littlebigbrother.data.DeviceAdapter;
import dk.atom_it.littlebigbrother.data.DeviceModel;
import dk.atom_it.littlebigbrother.data.Globals;
import dk.atom_it.littlebigbrother.managers.BluetoothListener;
import dk.atom_it.littlebigbrother.managers.BluetoothManager;
import dk.atom_it.littlebigbrother.managers.WiFiListener;
import dk.atom_it.littlebigbrother.notifications.AbstractEvent;
import dk.atom_it.littlebigbrother.notifications.BluetoothEvent;
import dk.atom_it.littlebigbrother.notifications.EventManager;
import dk.atom_it.littlebigbrother.threading.NetworkingSucks;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddEventListener extends AppCompatActivity implements BluetoothListener, WiFiListener {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public Context getContext(){
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event_listener);

        //Instantiate if necessary the global device and network list
        if(Globals.getInstance().devices == null){
            Globals.getInstance().devices = new ArrayList<>();
        }
        if(Globals.getInstance().networks == null){
            Globals.getInstance().networks = new ArrayList<>();
        }

        BluetoothManager.getInstance().scanBluetooth(this);

        //Set the dropdown menu for event types
        final Spinner eventTypeSpinner = (Spinner) findViewById(R.id.event_type_spinner);
        final ArrayAdapter<CharSequence> eventTypeAdapter = ArrayAdapter.createFromResource(this, R.array.event_types, android.R.layout.simple_spinner_item);
        final TextView jhemeCode = (TextView) findViewById(R.id.event_code);
        final TextView userLabel = (TextView) findViewById(R.id.user_label);

        String username = Globals.getInstance().username;
        userLabel.setText("Logged in as " + username);
        eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(eventTypeAdapter);

        Button eventTestCodeButton = (Button) findViewById(R.id.event_test);
        eventTestCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JhemeInterpreter interpreter = new JhemeInterpreter();
                    interpreter.eval(jhemeCode.getText().toString());
                } catch (RuntimeException runtimeEx) {
                    if (runtimeEx.getMessage() != null) {
                        Toast.makeText(NotamicusApp.getInstance(), runtimeEx.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(NotamicusApp.getInstance(), "Syntax error", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception anyException) {
                    Toast.makeText(NotamicusApp.getInstance(), anyException.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button eventCodeLib = (Button) findViewById(R.id.event_codelist);
        eventCodeLib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Choose code");

                final View inflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_code_list, (ViewGroup) view.getRootView(), false);
                builder.setView(inflated);

                ArrayList<CodeModel> codeList = Globals.getInstance().cloudCode;

                ListView list = (ListView) inflated.findViewById(R.id.code_list);
                CodeAdapter adapter = new CodeAdapter(inflated.getContext(), codeList);
                list.setAdapter(adapter);

                list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        CodeModel codeModel = Globals.getInstance().cloudCode.get(position);
                        jhemeCode.setText(codeModel.getCode());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        Button eventLists = (Button) findViewById(R.id.eventLists);
        eventLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toList = new Intent(getContext(), EventList.class);
                startActivity(toList);
            }
        });

        Button eventAddButton = (Button) findViewById(R.id.event_add_button);


        eventAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //{type, note} + (for types 4 & 5) {lat, lng, radius} + (for types 0,1,2,3) {filtertype [range 0-1], filter}
                final int type = eventTypeSpinner.getSelectedItemPosition();
                final String jhemeProgram = jhemeCode.getText().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                if (type == 4 || type == 5) {
                    builder.setTitle("Location");
                    final View inflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_location, (ViewGroup) view.getRootView(), false);
                    builder.setView(inflated);

                    final TextView txt_lat = (TextView) inflated.findViewById(R.id.dialog_lat);
                    final TextView txt_lng = (TextView) inflated.findViewById(R.id.dialog_lng);
                    final TextView txt_radius = (TextView) inflated.findViewById(R.id.dialog_radius);

                    //Map button
                    final Button mapButt = (Button) inflated.findViewById(R.id.dialog_map);
                    mapButt.setOnClickListener(mapPopUp(Globals.getInstance().userPosition, txt_lat, txt_lng));

                    //Add button
                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                            double lat;
                            double lng;
                            double radius;
                            try {
                                lat = Double.parseDouble(txt_lat.getText().toString());
                            } catch (Exception exc) {
                                Toast.makeText(NotamicusApp.getInstance(), "Latitude is not a valid number", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            try {
                                lng = Double.parseDouble(txt_lng.getText().toString());
                            } catch (Exception exc) {
                                Toast.makeText(NotamicusApp.getInstance(), "Longitude is not a valid number", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            try {
                                radius = Double.parseDouble(txt_radius.getText().toString());
                            } catch (Exception exc) {
                                Toast.makeText(NotamicusApp.getInstance(), "Radius is not a valid number", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            JSONObject json = new JSONObject();
                            try {
                                json.put("token", Globals.getInstance().token);
                                json.put("type", type);
                                json.put("note", jhemeProgram);
                                json.put("lat", lat);
                                json.put("lng", lng);
                                json.put("radius", radius);
                            } catch (JSONException jsonexcept) {
                                Toast.makeText(NotamicusApp.getInstance(), "An error occured, please try again", Toast.LENGTH_SHORT).show();
                            }
                            processJSON(json);
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    });

                } else {
                    scanBluetooth();
                    //WifiManager.

                    builder.setTitle("Filter");
                    View inflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_wifibt, (ViewGroup) view.getRootView(), false);
                    builder.setView(inflated);

                    final TextView txt_filter = (TextView) inflated.findViewById(R.id.dialog_filter);
                    final ToggleButton inp_filtertype = (ToggleButton) inflated.findViewById(R.id.dialog_filtertype);

                    //List button
                    final Button devicesButt = (Button) inflated.findViewById(R.id.dialog_macList);
                    devicesButt.setOnClickListener(devicesPopUp(txt_filter, inp_filtertype.isChecked(), eventTypeSpinner));

                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();

                            JSONObject json = new JSONObject();
                            try {
                                json.put("token", Globals.getInstance().token);
                                json.put("type", type);
                                json.put("note", jhemeProgram);
                                json.put("filtertype", (inp_filtertype.isChecked() ? 0 : 1));
                                json.put("filter", txt_filter.getText().toString());
                            } catch (JSONException jsonexcept) {
                                Toast.makeText(NotamicusApp.getInstance(), "An error occured, please try again", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(NotamicusApp.getInstance(), "Event has been added", Toast.LENGTH_LONG).show();
                            processJSON(json);
                        }
                    });

                    boolean bluetooth = false;
                    if(type == EventManager.BLUETOOTH_ENTER || type == EventManager.BLUETOOTH_EXIT){
                        bluetooth = true;
                    }
                    final Button list_button = (Button) inflated.findViewById(R.id.dialog_macList);
                    //list_button.setOnClickListener(devicesPopUp(tthis, txt_filter, inp_filtertype.isChecked(), bluetooth));

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    });
                }

                builder.show();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public static void processJSON(JSONObject json) {
        final AbstractEvent newEvent = EventManager.getInstance().fromJSON(json);
        //Update server
        if (Globals.getInstance().token != null) {
            //final AddEventListener tthis = this;
            NetworkingSucks.addNote(json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    AnErrorOccured("Failed to update server with notification, notification will not be saved");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject resp = new JSONObject(response.body().string());
                        if (resp.getInt("status") != 200) {
                            response.close();
                            AnErrorOccured("Server error (" + resp.getInt("status") + ") ; Note was not saved");
                        }
                        int noteid = resp.getJSONObject("data").getInt("noteid");
                        newEvent.setNoteId(noteid);
                        response.close();
                    } catch (JSONException exception) {
                        AnErrorOccured(exception.getMessage() + " ; Note was not saved");
                        response.close();
                    }
                }

                private void AnErrorOccured(final String message) {
                    NotamicusApp.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NotamicusApp.getInstance(), message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            //Queue it anyways ? maybe put this into onResponse idk
            EventManager.getInstance().queueListener(newEvent);
        } else {
            //No token, we arent logged in, so just schedule the event
            EventManager.getInstance().queueListener(newEvent);
        }
        //Add it to the scheduler
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("AddEventListener Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private View.OnClickListener mapPopUp(final LatLng pos, final TextView dialogLAT, final TextView dialogLNG){
        return mapPopUp(this, pos, dialogLAT, dialogLNG);
    }
    public static View.OnClickListener mapPopUp(final Activity activity, final LatLng pos, final TextView dialogLAT, final TextView dialogLNG){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Map lat lng picker
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Choose location");

                View mapView = LayoutInflater.from(activity).inflate(R.layout.dialog_map, (ViewGroup) v.getRootView(), false);
                builder.setView(mapView);

                MapFragment map = (MapFragment) activity.getFragmentManager().findFragmentById(R.id.dialog_mapFragment);

                map.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(final GoogleMap googleMap) {
                        final Marker marker = googleMap.addMarker(new MarkerOptions().position(pos).title("Move Me!"));
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(30));
                        marker.setAlpha((float) 0.9);
                        marker.setDraggable(false);

                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));


                        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener(){
                            @Override
                            public void onCameraMove() {
                                LatLng newpos = googleMap.getCameraPosition().target;
                                marker.setPosition(newpos);
                            }
                        });

                        builder.setPositiveButton("set", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String lat = marker.getPosition().latitude + "";
                                String lng = marker.getPosition().longitude + "";

                                dialogLAT.setText(lat);
                                dialogLNG.setText(lng);
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                });
            }
        };
    }

    private View.OnClickListener devicesPopUp(final TextView dialogFILTER, final boolean checked, final Spinner spinner){
        return devicesPopUp(this, dialogFILTER, checked, spinner);
    }

    public static View.OnClickListener devicesPopUp(final Activity activity, final TextView dialogFILTER, final boolean checked, final Spinner spinner){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                if(spinner.getSelectedItemPosition() == BluetoothEvent.FILTER_MAC || spinner.getSelectedItemPosition() == BluetoothEvent.FILTER_NAME){
                    builder.setTitle("Choose device");
                } else {
                    builder.setTitle("Choose network");
                }

                final View inflated = LayoutInflater.from(activity).inflate(R.layout.dialog_wifibt_list, (ViewGroup) view.getRootView(), false);
                builder.setView(inflated);

                ListView list = (ListView) inflated.findViewById(R.id.wifibt_list);
                if(spinner.getSelectedItemPosition() == BluetoothEvent.FILTER_MAC || spinner.getSelectedItemPosition() == BluetoothEvent.FILTER_NAME){
                    DeviceAdapter adapter = new DeviceAdapter(inflated.getContext(), Globals.getInstance().devices);
                    list.setAdapter(adapter);

                } else {
                    DeviceAdapter adapter = new DeviceAdapter(inflated.getContext(), Globals.getInstance().networks);
                    list.setAdapter(adapter);
                }

                list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        DeviceModel deviceModel = Globals.getInstance().devices.get(position);
                        if(checked){
                            dialogFILTER.setText(deviceModel.mac);
                        } else {
                            dialogFILTER.setText(deviceModel.name);
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        };
    }

    @Override
    public void onBluetoothScanStarted() {

    }

    @Override
    public void onBluetoothScanResults(List<BluetoothDevice> results) {
        for(BluetoothDevice device : results) {
            DeviceModel devicemodel = new DeviceModel(device.getAddress(), device.getName());
            if (Globals.getInstance().devices.contains(devicemodel)) {
                return;
            }
            Globals.getInstance().devices.add(devicemodel);
        }
    }

    @Override
    public void onBluetoothScanCompleted() {

    }

    @Override
    public void onBluetoothError() {

    }

    @Override
    public void onWiFiScanStarted() {

    }

    @Override
    public void onWiFiScanResults(List<ScanResult> results) {
        for(ScanResult wifi : results) {
            DeviceModel devicemodel = new DeviceModel(wifi.BSSID, wifi.SSID);
            if (Globals.getInstance().networks.contains(devicemodel)) {
                return;
            }
            Globals.getInstance().networks.add(devicemodel);
        }
    }

    @Override
    public void onWiFiScanCompleted() {

    }

    @Override
    public void onWiFiError() {

    }

    private void scanBluetooth(){
        BluetoothManager.getInstance().scanBluetooth(this);
    }
}
