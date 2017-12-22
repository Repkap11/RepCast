package com.repkap11.repcast.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.repkap11.repcast.R;

/**
 * Created by paul on 12/22/17.
 */

public class ConfigureBackendDialogFragment extends DialogFragment {

    private static final String TAG = ConfigureBackendDialogFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.configure_backend, null, false);
        final EditText edittext = rootView.findViewById(R.id.configure_backend_edit_text);
        alert.setMessage(R.string.configure_backend_message);
        alert.setTitle(R.string.configure_backend_title);
        alert.setView(rootView);
        alert.setPositiveButton(R.string.configure_backend_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                //Editable YouEditTextValue = edittext.getText();
                //OR
                String YouEditTextValue = edittext.getText().toString();
                Log.e(TAG, "YES");

            }
        });

        alert.setNegativeButton(R.string.configure_backend_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                Log.e(TAG, "NO");
            }
        });
        AlertDialog dialog = alert.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;

    }
}


