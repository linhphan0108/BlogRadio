package com.linhphan.androidboilerplate.ui.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.linhphan.androidboilerplate.util.Logger;
import com.linhphan.blogradio.R;

/**
 * Created by linhphan on 11/13/15.
 */
public class BaseActivity extends AppCompatActivity implements Handler.Callback{

    public final static int REPLACING_FRAGMENT = 11;

    protected Handler mBaseHandler = new Handler(this);
    protected int mContainerResource;//the id if the fragment container

    public Handler getBaseHandler() {
        return mBaseHandler;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_sliding, R.anim.animation_sliding_down);
    }

    //handle message from handler
    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what){
            case REPLACING_FRAGMENT:
                replaceFragment(mContainerResource, (Fragment) msg.obj);
                break;
        }

        return false;
    }



    private void replaceFragment(int container, Fragment fragment){
        if (container == 0 || fragment == null){
            Logger.e(getClass().getName(), "container was null or fragment was null");
            return;
        }
        FragmentTransaction fragmentTransaction = getFragmentTransaction();
        fragmentTransaction.replace(container, fragment);
        fragmentTransaction.commit();
    }

    /**
     * get an instant of FragmentTransaction which was setup an custom animation
     * @return FragmentTransaction object
     */
    protected FragmentTransaction getFragmentTransaction(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.animation_sliding_in_right_left, R.anim.no_sliding);
        return fragmentTransaction;
    }
}
