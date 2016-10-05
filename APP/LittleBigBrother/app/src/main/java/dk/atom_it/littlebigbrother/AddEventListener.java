package dk.atom_it.littlebigbrother;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
                    Toast.makeText(tthis, "Everything a-ok", Toast.LENGTH_LONG).show();
                } catch (RuntimeException runtimeEx){
                    if(runtimeEx.getMessage() != null) {
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

                if(type == 4 || type == 5) {
                    builder.setTitle("Location");
                    View inflated = LayoutInflater.from(tthis).inflate(R.layout.dialog_location, (ViewGroup) view.getRootView(), false);
                    builder.setView(inflated);

                    final TextView txt_lat = (TextView) inflated.findViewById(R.id.dialog_lat);
                    final TextView txt_lng = (TextView) inflated.findViewById(R.id.dialog_lng);
                    final TextView txt_radius = (TextView) inflated.findViewById(R.id.dialog_radius);


                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                            double lat; double lng; double radius;
                            try{lat = Double.parseDouble(txt_lat.getText().toString());}
                            catch (Exception exc){Toast.makeText(tthis, "Latitude is not a valid number", Toast.LENGTH_SHORT).show(); return;}
                            try{lng = Double.parseDouble(txt_lng.getText().toString());}
                            catch (Exception exc){Toast.makeText(tthis, "Longitude is not a valid number", Toast.LENGTH_SHORT).show(); return;}
                            try{radius = Double.parseDouble(txt_radius.getText().toString());}
                            catch (Exception exc){Toast.makeText(tthis, "Radius is not a valid number", Toast.LENGTH_SHORT).show(); return;}

                            JSONObject json = new JSONObject();
                            try {
                                json.put("token", Globals.getInstance().token);
                                json.put("type", type);
                                json.put("note", jhemeProgram);
                                json.put("lat", lat);
                                json.put("lng", lng);
                                json.put("radius", radius);
                            } catch (JSONException jsonexcept){
                                Toast.makeText(tthis, "An error occured, please try again", Toast.LENGTH_SHORT).show();
                            }

                            processJSON(json);

                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
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

                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener(){
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
                            } catch (JSONException jsonexcept){
                                Toast.makeText(tthis, "An error occured, please try again", Toast.LENGTH_SHORT).show();
                            }

                            processJSON(json);
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    });
                }

                builder.show();

            }
        });




    }

    private void processJSON(JSONObject json){
        final AbstractEvent newEvent = EventManager.getInstance().fromJSON(json, this);
        //Update server
        if(Globals.getInstance().token != null) {
            final AddEventListener tthis = this;
            NetworkingSucks.addNote(json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    AnErrorOccured("Failed to update server with notification, notification will not be saved");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject resp = new JSONObject(response.body().string());
                        if(resp.getInt("status") != 200){
                            response.close();
                            AnErrorOccured("Server error (" + resp.getInt("status") + ") ; Note was not saved");
                        }
                        int noteid = resp.getJSONObject("data").getInt("noteid");
                        newEvent.setNoteId(noteid);
                        response.close();
                    } catch (JSONException exception){
                        AnErrorOccured(exception.getMessage() + " ; Note was not saved");
                        response.close();
                    }
                }

                private void AnErrorOccured(final String message){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(tthis, message, Toast.LENGTH_LONG).show();
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

}
