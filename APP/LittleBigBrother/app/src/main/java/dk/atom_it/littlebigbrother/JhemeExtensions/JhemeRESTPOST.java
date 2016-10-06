package dk.atom_it.littlebigbrother.JhemeExtensions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeBoolean;
import dk.atomit.Jheme.SchemeTypes.SchemeJavaObject;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;
import dk.atomit.Jheme.SchemeTypes.SchemeString;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Kristian on 10/5/2016.
 */

public class JhemeRESTPOST extends SchemeProcedure {

    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e){
        String url = "";
        String mediatype = "application/json; charset=utf-8";
        String data = "";
        SchemeProcedure onSuccess;
        SchemeProcedure onFailure = null;

        if(args.length == 5){
            i.assertIsType(this, args[0], SchemeString.class);
            i.assertIsType(this, args[1], SchemeString.class);
            i.assertIsType(this, args[2], SchemeString.class);
            if(!(args[3] instanceof SchemeProcedure)){throw new RuntimeException("Expected argument to be of type: SchemeProcedure");}
            if(!(args[4] instanceof SchemeProcedure)){throw new RuntimeException("Expected argument to be of type: SchemeProcedure");}

            url = ((SchemeString) args[0]).getValue();
            mediatype = ((SchemeString) args[1]).getValue();
            data = ((SchemeString) args[2]).getValue();
            onSuccess = (SchemeProcedure) args[3];
            onFailure = (SchemeProcedure) args[4];

        } else if (args.length == 4){
            //if string string string procedure
            if(args[2] instanceof SchemeString){
                i.assertIsType(this, args[0], SchemeString.class);
                i.assertIsType(this, args[1], SchemeString.class);
                i.assertIsType(this, args[2], SchemeString.class);
                if(!(args[3] instanceof SchemeProcedure)){throw new RuntimeException("Expected argument to be of type: SchemeProcedure");}

                url = ((SchemeString) args[0]).getValue();
                mediatype = ((SchemeString) args[1]).getValue();
                data = ((SchemeString) args[2]).getValue();
                onSuccess = (SchemeProcedure) args[3];

            } else /* if string string procedure procedure*/ {
                i.assertIsType(this, args[0], SchemeString.class);
                i.assertIsType(this, args[1], SchemeString.class);
                if(!(args[2] instanceof SchemeProcedure)){throw new RuntimeException("Expected argument to be of type: SchemeProcedure");}
                if(!(args[3] instanceof SchemeProcedure)){throw new RuntimeException("Expected argument to be of type: SchemeProcedure");}

                url = ((SchemeString) args[0]).getValue();
                data = ((SchemeString) args[1]).getValue();
                onSuccess = (SchemeProcedure) args[2];
                onFailure = (SchemeProcedure) args[3];

            }
        } else {
            i.assertArgCountEqual(this, args, 3);
            i.assertIsType(this, args[0], SchemeString.class);
            i.assertIsType(this, args[1], SchemeString.class);
            if(!(args[2] instanceof SchemeProcedure)){throw new RuntimeException("Expected argument to be of type: SchemeProcedure");}

            url = ((SchemeString) args[0]).getValue();
            data = ((SchemeString) args[1]).getValue();
            onSuccess = (SchemeProcedure) args[2];
        }

        //(rest "http://url" ["media type" default json] "data" onSuccess [onFailure])

        MediaType JSON = MediaType.parse(mediatype);

        JSONObject jsonObj = new JSONObject();

        RequestBody body = RequestBody.create(JSON, data);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);

        final SchemeProcedure failure = onFailure;
        final SchemeProcedure success = onSuccess;
        final Interpreter interpreter = i;
        final Environment environment = e;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                ((JhemeInterpreter) interpreter).getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(failure != null){
                            SchemeObject[] supargs = {new SchemeJavaObject(call), new SchemeJavaObject(e)};
                            failure.call(supargs, interpreter, environment);
                        }
                    }
                });
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {

                final String body = response.body().string();
                response.close();

                ((JhemeInterpreter) interpreter).getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //SchemeObject[] supargs = {new SchemeJavaObject(call), new SchemeJavaObject(response)};
                        //success.call(supargs, interpreter, environment);
                        SchemeObject[] args = {new SchemeString(body)};
                        success.call(args, interpreter, environment);
                        //response.close();
                    }
                });

            }
        });

        return new EvaluationResult(e);
    }

}
