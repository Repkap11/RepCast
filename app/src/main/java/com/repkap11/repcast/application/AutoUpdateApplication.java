package com.repkap11.repcast.application;


/**
 * Created by paul on 9/20/15.
 */
public class AutoUpdateApplication extends CastApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //TODO should it automatically update
        //UpdateAppTask task = new UpdateAppTask(this, false);
        //task.execute();
    }
}
