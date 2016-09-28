package dk.atom_it.littlebigbrother;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class Blist extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blist);

        final ListView listBluetooth = (ListView) findViewById(R.id.listLayout);
        String[] st = {"enhed1","enhed2","osv."};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, st);
        listBluetooth.setAdapter(adapter);

    }
}
