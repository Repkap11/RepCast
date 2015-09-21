package com.repkap11.repcast;

import android.app.Application;

/**
 * Created by paul on 9/20/15.
 */
public class AutoUpdateApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UpdateAppTask task = new UpdateAppTask(this);
        task.execute();
    }
}
