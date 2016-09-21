package dk.atom_it.littlebigbrother.api;

import android.content.Context;

import java.io.IOException;

import dk.atom_it.littlebigbrother.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Class handling parsing and setting up uses of the API.
 * Created by Kristian on 9/4/2016.
 */

public class Endpoint {

    private String baseURL;
    private String endpoint;
    private Context context;

    public Endpoint(Context context, String endpoint){
        baseURL = context.getString(R.string.base_url);
        this.endpoint = endpoint;
        this.context = context;
    }

    public Call callAndThrow(String data, Callback callback) throws IOException{
        API api = new API();
        return api.post(baseURL + endpoint, data, callback);
    }

    public Call call(String data, Callback callback){
        try{
            return callAndThrow(data, callback);
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public String syncCall(String data) {
        try {
            API api = new API();
            return api.syncronizedPost(baseURL + endpoint, data);
        } catch(IOException e){
            return null;
        }
    }
}
