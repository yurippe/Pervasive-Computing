package dk.atom_it.littlebigbrother;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import dk.atom_it.littlebigbrother.JhemeExtensions.JhemeInterpreter;

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

        eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(eventTypeAdapter);

        Button eventAddButton = (Button) findViewById(R.id.event_add_button);
        eventAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(tthis, "Selected: " + eventTypeAdapter.getItem(eventTypeSpinner.getSelectedItemPosition()), Toast.LENGTH_LONG).show();
                JhemeInterpreter jheme = new JhemeInterpreter(tthis);
                try {
                    jheme.eval(jhemeCode.getText().toString());
                } catch (Exception e){
                    Toast.makeText(tthis, "Not valid Jheme code", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
