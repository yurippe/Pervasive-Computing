package dk.atom_it.littlebigbrother;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import dk.atom_it.littlebigbrother.api.API;
import dk.atom_it.littlebigbrother.api.Endpoint;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Activity for logging onto the server and obtaining the unique key.
 */
public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Context tthis = this;
        final Button loginbutton = (Button) findViewById(R.id.login_button);
        final Button signupbutton = (Button) findViewById(R.id.signup_button);
		final Button offlinebutton = (Button) findViewById(R.id.offline_button);
        final EditText usernamewidget = (EditText) findViewById(R.id.login_username);
        final EditText passwordwidget = (EditText) findViewById(R.id.login_password);

        final API api = new API();

        loginbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View view) {
                Map<String, String> credentials = new HashMap<>();
                credentials.put("username", usernamewidget.getText().toString());
                credentials.put("password", passwordwidget.getText().toString());
                JSONObject data = new JSONObject(credentials);

                Endpoint login = new Endpoint(tthis, "/login");
                login.call(data.toString(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Snackbar.make(view, "Could not connect to servers, please try again later", Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            JSONObject jsonresp = new JSONObject(response.body().string());
                            if(jsonresp.getInt("status") != 200){
                                Snackbar.make(view, jsonresp.getString("message"), Snackbar.LENGTH_LONG).show();
                                response.close();
                                return;
                            }

                            Intent toMaps = new Intent(tthis, MapsActivity.class);
                            toMaps.putExtra("token", jsonresp.getJSONObject("data").getString("token"));
                            toMaps.putExtra("username", jsonresp.getJSONObject("data").getString("username"));
                            tthis.startActivity(toMaps);

                        } catch (JSONException e) {
                            Snackbar.make(view, "Invalid server response", Snackbar.LENGTH_LONG).show();
                        }

                        response.close(); //Very important
                    }
                });
            }
        });


        signupbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View view) {
                Intent toSignUp = new Intent(tthis, SignupActivity.class);
                toSignUp.putExtra("username", usernamewidget.getText().toString());
                toSignUp.putExtra("password", passwordwidget.getText().toString());
                tthis.startActivity(toSignUp);
            }});

		offlinebutton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(final View view) {
                 Intent toMaps = new Intent(tthis, MapsActivity.class);

                 String s = usernamewidget.getText().toString();
                 if(s.equals("")) {
                     s = "Offline Mode";
                 }

                 toMaps.putExtra("username", s);
                 tthis.startActivity(toMaps);
             }
        });
    }

    @Override
    public void onBackPressed(){
        //Weeee, patchy fixes!!
    }
}