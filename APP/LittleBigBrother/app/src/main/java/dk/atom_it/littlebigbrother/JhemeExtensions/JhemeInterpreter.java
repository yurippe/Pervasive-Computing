package dk.atom_it.littlebigbrother.JhemeExtensions;

import android.app.Activity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kristian on 10/4/2016.
 */

public class JhemeInterpreter extends dk.atomit.Jheme.Interpreter.Interpreter {

    private Activity activity;

    private Map<String, Object> extras = new HashMap<>();

    public JhemeInterpreter(Activity activity){
        this.activity = activity;
        environment.put("toast", new JhemeToast());
        environment.put("notify", new JhemeNotification());
        environment.put("extra", new JhemeGetExtra());
        environment.put("restpost", new JhemeRESTPOST());
        environment.put("readresponse", new JhemeReadResponse());
        environment.put("gettoken", new JhemeGetToken());
        environment.put("test", new JhemeTest());
    }

    public Activity getActivity(){
        return activity;
    }

    public void setExtra(String key, Object value){
        extras.put(key, value);
    }

    public Object getExtra(String key){
        if(extras.containsKey(key)){
            return extras.get(key);
        } else {
            return null;
        }
    }

}
