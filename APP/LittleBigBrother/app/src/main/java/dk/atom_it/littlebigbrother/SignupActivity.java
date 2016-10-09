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

public class SignupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Context tthis = this;
        final Button signupbutton = (Button) findViewById(R.id.applysignup_button);
        final EditText usernamewidget = (EditText) findViewById(R.id.signup_username);
        final EditText passwordwidget = (EditText) findViewById(R.id.signup_password);

        final API api = new API();

        signupbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View view) {
                Map<String, String> credentials = new HashMap<>();
                credentials.put("username", usernamewidget.getText().toString());
                credentials.put("password", passwordwidget.getText().toString());
                JSONObject data = new JSONObject(credentials);

                Endpoint login = new Endpoint("/signup");
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
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, Login.class);
        this.startActivity(intent);
    }
}
