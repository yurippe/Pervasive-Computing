package dk.atom_it.littlebigbrother.threading;

import android.app.Activity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import dk.atom_it.littlebigbrother.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Kristian on 10/1/2016.
 */

public class NetworkingSucks {

    private Activity activity;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static String baseURL;
    private static String e_updatePosition = "/updatepos";
    private static String e_addNote = "/addnote";

    private HashMap<String, Call> calls = new HashMap<>();

    public NetworkingSucks(final Activity activity){
        this.activity = activity;
        baseURL = activity.getString(R.string.base_url);

        this.calls.put(e_updatePosition, null);
    }

    public Call updatePosition(String token, Double lat, Double lng){
        return updatePosition(token, lat, lng, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                calls.put(e_updatePosition, null);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();
                calls.put(e_updatePosition, null);
            }
        });
    }

    public Call updatePosition(String token, Double lat, Double lng, Callback callback){
        Call prevcall = calls.get(e_updatePosition);
        if(prevcall != null) {
            //Ongoing
            prevcall.cancel();
        }
        JSONObject data = new JSONObject();
        try {data.put("token", token);} catch (JSONException e) {}
        try {data.put("lat", lat);} catch (JSONException e) {}
        try {data.put("lng", lng);} catch (JSONException e) {}

        Call call = makeCall(e_updatePosition, data.toString(), callback);
        calls.put(e_updatePosition, call);
        return call;

    }

    public static Call addNote(String jsonNote, Callback callback){
        return makeCall(e_addNote, jsonNote, callback);
    }

    public static Call makeCall(String url, String data, Callback callback){
        RequestBody body = RequestBody.create(JSON, data);
        Request request = new Request.Builder()
                .url(baseURL + url)
                .post(body)
                .build();
        Call call = new OkHttpClient().newCall(request);
        call.enqueue(callback);
        return call;
    }



}
