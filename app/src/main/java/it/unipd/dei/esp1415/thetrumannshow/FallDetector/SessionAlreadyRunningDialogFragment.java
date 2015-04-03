package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * @author Enrico Naletto
 */
public class SessionAlreadyRunningDialogFragment extends DialogFragment {

    public interface SessionAlreadyRunningDialogFragmentListener {
        public void onSessionAlreadyRunningDialogPositiveClick();
        public void onSessionAlreadyRunningDialogNegativeClick();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage("Session already running. Stop it?");
        AlertDialog dialog = builder.create();
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SessionAlreadyRunningDialogFragmentListener activity =
                        (SessionAlreadyRunningDialogFragmentListener) getActivity();
                activity.onSessionAlreadyRunningDialogPositiveClick();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SessionAlreadyRunningDialogFragmentListener activity =
                        (SessionAlreadyRunningDialogFragmentListener) getActivity();
                activity.onSessionAlreadyRunningDialogNegativeClick();
            }
        });

        return builder.create();
    }
}
