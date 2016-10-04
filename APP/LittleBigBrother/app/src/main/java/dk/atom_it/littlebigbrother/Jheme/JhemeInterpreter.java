package dk.atom_it.littlebigbrother.Jheme;

import android.app.Activity;

/**
 * Created by Kristian on 10/4/2016.
 */

public class JhemeInterpreter extends dk.atomit.Jheme.Interpreter.Interpreter {

    private Activity activity;
    private int uniqueInt = 0;

    public JhemeInterpreter(Activity activity){
        this.activity = activity;
        environment.put("toast", new JhemeToast());
        environment.put("notify", new JhemeNotification());
    }

    public Activity getActivity(){
        return activity;
    }

}
