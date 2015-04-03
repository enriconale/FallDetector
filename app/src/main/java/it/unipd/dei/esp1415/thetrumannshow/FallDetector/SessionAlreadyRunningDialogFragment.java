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
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.session_running_dialog_title);
        builder.setMessage(R.string.session_running_dialog_message);
        builder.setPositiveButton(R.string.session_running_dialog_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SessionAlreadyRunningDialogFragmentListener activity =
                        (SessionAlreadyRunningDialogFragmentListener) getActivity();
                activity.onSessionAlreadyRunningDialogPositiveClick();
            }
        });
        builder.setNegativeButton(R.string.session_running_dialog_negative_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getDialog().cancel();
            }
        });

        return builder.create();
    }
}
