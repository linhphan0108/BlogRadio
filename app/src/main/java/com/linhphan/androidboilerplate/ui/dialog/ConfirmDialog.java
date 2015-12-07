package com.linhphan.androidboilerplate.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.linhphan.androidboilerplate.callback.ConfirmDialogCallback;
import com.linhphan.androidboilerplate.util.Constant;

/**
 * Created by linhphan on 11/11/15.
 */
public class ConfirmDialog extends DialogFragment {

    ConfirmDialogCallback callback;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        String title = null;
        String message = null;
        if (bundle != null) {
            title = bundle.getString(Constant.DIALOG_TITLE, "");
            message = bundle.getString(Constant.DIALOG_MESSAGE, "");
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null)
                            callback.onOk();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    /**
     * set up callback listeners to the dialog fragment
     * @param callback the callback mHandler
     */
    public void registerCallback(ConfirmDialogCallback callback){
        this.callback = callback;
    }
}
