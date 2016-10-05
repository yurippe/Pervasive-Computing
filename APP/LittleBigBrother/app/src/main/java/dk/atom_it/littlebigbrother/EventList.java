package dk.atom_it.littlebigbrother;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import dk.atom_it.littlebigbrother.data.EventAdapter;
import dk.atom_it.littlebigbrother.notifications.AbstractEvent;
import dk.atom_it.littlebigbrother.notifications.EventManager;

public class EventList extends AppCompatActivity {

    private List<AbstractEvent> events;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        events = new ArrayList<>();
        final ListView listEvents = (ListView) findViewById(R.id.event_list_view);
        adapter = new EventAdapter(this, events);
        listEvents.setAdapter(adapter);
        adapter.addAll(EventManager.getInstance().getAllEvents());

        Button refreshButton = (Button) findViewById(R.id.event_list_refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clear();
                adapter.addAll(EventManager.getInstance().getAllEvents());
            }
        });

    }

}
