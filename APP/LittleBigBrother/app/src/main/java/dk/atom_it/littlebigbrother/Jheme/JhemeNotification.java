package dk.atom_it.littlebigbrother.Jheme;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import dk.atom_it.littlebigbrother.R;
import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;
import dk.atomit.Jheme.SchemeTypes.SchemeString;

/**
 * Created by Kristian on 10/4/2016.
 */

public class JhemeNotification extends SchemeProcedure {

    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e){
        i.assertArgCountEqual(this, args, 2);

        String title = ""; String text = "";

        if(args[0] instanceof SchemeString) {
            title = ((SchemeString) args[0]).getValue();
        } else {
            title = args[0].getStringValue();
        }

        if(args[1] instanceof SchemeString) {
            text = ((SchemeString) args[1]).getValue();
        } else {
            text = args[1].getStringValue();
        }


        JhemeInterpreter interp = (JhemeInterpreter) i;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(interp.getActivity());
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);

        NotificationManager manager = (NotificationManager) interp.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(UniqueCounter.getNextInt(), mBuilder.build());
        return new EvaluationResult(e);
    }
}
