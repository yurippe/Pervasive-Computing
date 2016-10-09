package dk.atom_it.littlebigbrother;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by Kristian on 10/9/2016.
 */

public class NotamicusApp extends Application {

        private static NotamicusApp app;

        public static NotamicusApp getInstance(){
            return app;
        }

        public NotamicusApp(){
            super();
            app = this;
        }

        public void runOnUiThread(Runnable runnable){
            new Handler(Looper.getMainLooper()).post(runnable); //Simulates Activity.runOnUiThread()
        }

}
