package com.linhphan.androidboilerplate.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

/**
 * Created by linhphan on 11/11/15.
 */
public class ServiceUtil {

    /**
     * determine whether the special service is running or not
     * @param serviceClassName the name of service class
     * @return true if the special service is running otherwise return false
     */
    public static boolean isServiceRunning(Context context, Class serviceClassName){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if (service.getClass().getName().equals(serviceClassName))
                return true;
        }
        return false;
    }
}
