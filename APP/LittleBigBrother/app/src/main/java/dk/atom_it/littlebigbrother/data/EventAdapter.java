package dk.atom_it.littlebigbrother.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import dk.atom_it.littlebigbrother.R;
import dk.atom_it.littlebigbrother.notifications.AbstractEvent;
import dk.atom_it.littlebigbrother.notifications.BluetoothEvent;
import dk.atom_it.littlebigbrother.notifications.EventManager;
import dk.atom_it.littlebigbrother.notifications.LocationEvent;
import dk.atom_it.littlebigbrother.notifications.WifiEvent;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends ArrayAdapter<AbstractEvent> {

    public EventAdapter(Context context, List<AbstractEvent> events){
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        AbstractEvent event = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_list_item, parent, false);
        }

        TextView typeText = (TextView) convertView.findViewById(R.id.NoteTypeText);
        TextView label1 = (TextView) convertView.findViewById(R.id.Note1Label);
        TextView label2 = (TextView) convertView.findViewById(R.id.Note2Label);

        TextView value1 = (TextView) convertView.findViewById(R.id.Note1Text);
        TextView value2 = (TextView) convertView.findViewById(R.id.Note2Text);

        TextView idlabel = (TextView) convertView.findViewById(R.id.NoteIDLabel);

        TextView jhemeCode = (TextView) convertView.findViewById(R.id.NoteJheme);

        if(event instanceof LocationEvent) {
            LocationEvent cevent = (LocationEvent) event;
            label1.setText("Position: ");
            label2.setText("Radius: ");
            value1.setText(cevent.getLocation().toString());
            value2.setText(cevent.getRadius().toString());
        } else if (event instanceof WifiEvent){
            WifiEvent cevent = (WifiEvent) event;
            label1.setText("Filter: ");
            label2.setText("Filter type: ");
            value1.setText(cevent.getFilter());
            value2.setText(cevent.getFilterTypeAsString());
        } else if (event instanceof BluetoothEvent){
            BluetoothEvent cevent = (BluetoothEvent) event;
            label1.setText("Filter: ");
            label2.setText("Filter type: ");
            value1.setText(cevent.getFilter());
            value2.setText(cevent.getFilterTypeAsString());
        }

        jhemeCode.setText(event.getJhemeCode());
        idlabel.setText(""+event.getNoteId());
        typeText.setText(EventManager.typeToString(event.getEventType()));

        return convertView;

    }
}
