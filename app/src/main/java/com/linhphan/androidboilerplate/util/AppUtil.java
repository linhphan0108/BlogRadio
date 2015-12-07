package com.linhphan.androidboilerplate.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by linhphan on 11/11/15.
 */
public class AppUtil {
    private static AppUtil mAppUtil;

    public static AppUtil getInstance(){
        if (mAppUtil == null)
            mAppUtil = new AppUtil();
        return mAppUtil;
    }


    //== version
    public boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }


    public boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }


    public boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }


    public boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }


    public boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }


    public boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }


    public boolean isSupportBigNotification(){
        return hasJellyBean();
    }


    /**
     * determine whether an application is installed or not
     *
     * @param packageName the full package name
     * @return true if the application is installed whereas return false
     */
    public boolean isAppInstalled(Context context, String packageName) {
        boolean isInstalled = false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            isInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return isInstalled;
    }

    /**
     * open galleries app
     * if there are many applications are suitable then the OS will open a chooser dialog to pick one
     */
    public void openGalleryApp(Context context, int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    /**
     * try to retrieve the hash key of an application by package name
     */
    public void retrieveHashKey(Context context, String packageName) {
        // Add code to print out the key hash
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName,PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(e);
        } catch (NoSuchAlgorithmException e) {
            Logger.e(e);
        }
    }

    /**
     * open the volume controller of system, which will let users might turn volume up or down
     */
    public void openVolumeSystem(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_SHOW_UI);
    }

    /**
     *send an broadcast to system to rescan media
     * @param file assign the file will be rescanned
     */
    public void reScanSystemFileAt(Context context, File file){
        Logger.d(getClass().getName(), "rescan file at "+ file.getAbsolutePath());
        if (hasKitKat()){
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
                    context.sendBroadcast(intent);

        }else {
            Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file.getParentFile()));
            context.sendBroadcast(intent);
        }
    }
}
