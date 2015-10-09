package com.repkap11.repcast;


import com.repkap11.repcast.cast.refplayer.CastApplication;
import com.repkap11.repcast.cast.refplayer.UpdateAppTask;

/**
 * Created by paul on 9/20/15.
 */
public class AutoUpdateApplication extends CastApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        UpdateAppTask task = new UpdateAppTask(this);
        task.execute();
    }
}
