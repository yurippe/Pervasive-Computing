package dk.atom_it.littlebigbrother;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import dk.atom_it.littlebigbrother.JhemeExtensions.JhemeInterpreter;
import dk.atom_it.littlebigbrother.data.Globals;
import dk.atom_it.littlebigbrother.notifications.AbstractEvent;
import dk.atom_it.littlebigbrother.notifications.BluetoothEvent;
import dk.atom_it.littlebigbrother.notifications.EventManager;
import dk.atom_it.littlebigbrother.threading.NetworkingSucks;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddEventListener extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event_listener);

        final AddEventListener tthis = this;

        //Set the dropdown menu for event types
        final Spinner eventTypeSpinner = (Spinner) findViewById(R.id.event_type_spinner);
        final ArrayAdapter<CharSequence> eventTypeAdapter = ArrayAdapter.createFromResource(this, R.array.event_types, android.R.layout.simple_spinner_item);
        final TextView jhemeCode = (TextView) findViewById(R.id.event_code);
        final TextView userLabel = (TextView) findViewById(R.id.user_label);

        String username = getIntent().getStringExtra("username");
        userLabel.setText("Logged in as " + username);
        eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(eventTypeAdapter);

        Button eventTestCodeButton = (Button) findViewById(R.id.event_test);
        eventTestCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JhemeInterpreter interpreter = new JhemeInterpreter(tthis);
                    interpreter.eval(jhemeCode.getText().toString());
                } catch (RuntimeException runtimeEx) {
                    if (runtimeEx.getMessage() != null) {
                        Toast.makeText(tthis, runtimeEx.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(tthis, "Syntax error", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception anyException) {
                    Toast.makeText(tthis, anyException.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button eventLists = (Button) findViewById(R.id.eventLists);
        eventLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toList = new Intent(tthis, EventList.class);
                tthis.startActivity(toList);
            }
        });


        Button eventAddButton = (Button) findViewById(R.id.event_add_button);
        eventAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //{type, note} + (for types 4 & 5) {lat, lng, radius} + (for types 0,1,2,3) {filtertype [range 0-1], filter}
                final int type = eventTypeSpinner.getSelectedItemPosition();
                final String jhemeProgram = jhemeCode.getText().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(tthis);

                if (type == 4 || type == 5) {
                    builder.setTitle("Location");
                    final View inflated = LayoutInflater.from(tthis).inflate(R.layout.dialog_location, (ViewGroup) view.getRootView(), false);
                    builder.setView(inflated);

                    final TextView txt_lat = (TextView) inflated.findViewById(R.id.dialog_lat);
                    final TextView txt_lng = (TextView) inflated.findViewById(R.id.dialog_lng);
                    final TextView txt_radius = (TextView) inflated.findViewById(R.id.dialog_radius);

                    //Map button
                    final Button mapButt = (Button) inflated.findViewById(R.id.dialog_map);
                    mapButt.setOnClickListener(mapPopUp(tthis, Globals.getInstance().userPosition, txt_lat, txt_lng));

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
                                Toast.makeText(tthis, "Latitude is not a valid number", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            try {
                                lng = Double.parseDouble(txt_lng.getText().toString());
                            } catch (Exception exc) {
                                Toast.makeText(tthis, "Longitude is not a valid number", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            try {
                                radius = Double.parseDouble(txt_radius.getText().toString());
                            } catch (Exception exc) {
                                Toast.makeText(tthis, "Radius is not a valid number", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(tthis, "An error occured, please try again", Toast.LENGTH_SHORT).show();
                            }
                            processJSON(json, tthis);
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    });

                } else {
                    builder.setTitle("Filter");
                    View inflated = LayoutInflater.from(tthis).inflate(R.layout.dialog_wifibt, (ViewGroup) view.getRootView(), false);
                    builder.setView(inflated);

                    final TextView txt_filter = (TextView) inflated.findViewById(R.id.dialog_filter);
                    final ToggleButton inp_filtertype = (ToggleButton) inflated.findViewById(R.id.dialog_filtertype);

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
                                Toast.makeText(tthis, "An error occured, please try again", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(tthis, "Event has been added", Toast.LENGTH_LONG).show();
                            processJSON(json, tthis);
                        }
                    });

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

    public static void processJSON(JSONObject json, final Activity activity) {
        final AbstractEvent newEvent = EventManager.getInstance().fromJSON(json, activity);
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
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
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

    public static View.OnClickListener mapPopUp(final Activity tthis, final LatLng pos, final TextView dialogLAT, final TextView dialogLNG){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Map lat lng picker
                final AlertDialog.Builder mapBuilder = new AlertDialog.Builder(tthis);
                mapBuilder.setTitle("Choose location");

                View mapView = LayoutInflater.from(tthis).inflate(R.layout.dialog_map, (ViewGroup) v.getRootView(), false);
                mapBuilder.setView(mapView);

                MapFragment map = (MapFragment) tthis.getFragmentManager().findFragmentById(R.id.dialog_mapFragment);

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

                        mapBuilder.setPositiveButton("set", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogLAT.setText(marker.getPosition().latitude + "");
                                dialogLNG.setText(marker.getPosition().longitude + "");
                                dialog.dismiss();
                            }
                        });

                        mapBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        mapBuilder.show();
                    }
                });
            }
        };
    }
}
