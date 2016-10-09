package dk.atom_it.littlebigbrother;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import dk.atom_it.littlebigbrother.api.Endpoint;
import dk.atom_it.littlebigbrother.data.Globals;
import dk.atom_it.littlebigbrother.data.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Activity tthis = this;

        //Set text
        TextView username = (TextView) findViewById(R.id.profile_username);
        username.setText(Globals.getInstance().username);

        TextView lat = (TextView) findViewById(R.id.profile_lat);
        lat.setText(Globals.getInstance().userPosition.latitude + "");

        TextView lng = (TextView) findViewById(R.id.profile_lng);
        lng.setText(Globals.getInstance().userPosition.longitude + "");

        //Displayname is editable
        final EditText displayname = (EditText) findViewById(R.id.profile_dp);
        displayname.setText(Globals.getInstance().displayname);

        //Update button
        Button update = (Button) findViewById(R.id.profile_update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject data = new JSONObject();
                    data.put("token", Globals.getInstance().token);
                    data.put("displayname", displayname.getText());

                    Endpoint endpoint = new Endpoint(tthis, "/updatedisplayname");
                    endpoint.call(data.toString(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            makeToast("Could not connect to server");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject jsonresp = new JSONObject(response.body().string());
                                if (jsonresp.getInt("status") != 200) {
                                    makeToast("Update failed: " + jsonresp.getString("message"));
                                }
                                makeToast("Update successful");

                            } catch (JSONException e) {
                                makeToast("Server response unreadable");
                            }
                        }
                    });

                } catch (JSONException e) {
                    //Shut up, JSON!
                }
            }
        });
    }

    private void makeToast(final String text) {
        final UserProfile tthis = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(tthis, text, Toast.LENGTH_LONG).show();
           }
        });
    }
}
