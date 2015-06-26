package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * @author Enrico Naletto
 * A simple dialog that is shown when a user starts a new session. Basically it asks the user to
 * insert a name for the new session.
 */
public class NewSessionNameDialogFragment extends DialogFragment {
    private String mInsertedSessionName;
    private EditText mSessionNameEditText;

    public interface NewSessionNameDialogFragmentListener {
        void onReturnValueFromNewSessionNameDialog(String sessionName);
    }

    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_new_session, null, false);
        builder.setView(v);
        mSessionNameEditText = (EditText)v.findViewById(R.id.new_session_name);
        mSessionNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams
                            .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        builder.setTitle(R.string.new_session_name_dialog_title);

        builder.setPositiveButton(R.string.new_session_name_dialog_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NewSessionNameDialogFragmentListener activity =
                        (NewSessionNameDialogFragmentListener) getActivity();
                mInsertedSessionName = mSessionNameEditText.getText().toString();
                activity.onReturnValueFromNewSessionNameDialog(mInsertedSessionName);
            }
        });

        builder.setNegativeButton(R.string.new_session_name_dialog_negative_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getDialog().cancel();
            }
        });

        return builder.create();
    }
}
