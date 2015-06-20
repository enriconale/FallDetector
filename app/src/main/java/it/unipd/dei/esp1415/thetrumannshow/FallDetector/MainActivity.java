package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements NewSessionNameDialogFragment
        .NewSessionNameDialogFragmentListener, SessionAlreadyRunningDialogFragment
.SessionAlreadyRunningDialogFragmentListener {

    private static final String NEW_SESSION_DIALOG = "new_session_dialog";
    private static final String SESSION_RUNNING_DIALOG = "session_running_dialog";


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Session> mSessionsList;
    private TextView mEmptyListMessage;
    private SharedPreferences mSharedPreferences;

    private static Activity mLastActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLastActivity = this;
        setContentView(R.layout.activity_main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (hasCorrectEmailSettings()) {
            Toast.makeText(getApplicationContext(), R.string.insert_valid_email,
                    Toast.LENGTH_LONG).show();
        }

        mEmptyListMessage = (TextView)findViewById(R.id.empty_list_message);
        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mSessionsList = SessionsLab.get(getApplicationContext()).getSessions();

        mAdapter = new SessionsListAdapter(mSessionsList, getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);

        if (mAdapter.getItemCount() == 0) {
            mEmptyListMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_start_session:
                if (hasCorrectEmailSettings()) {
                    Toast.makeText(getApplicationContext(), R.string.insert_valid_email,
                            Toast.LENGTH_LONG).show();
                } else {
                    if (SessionsLab.get(getApplicationContext()).hasRunningSession()) {
                        DialogFragment dialog = new SessionAlreadyRunningDialogFragment();
                        dialog.show(getSupportFragmentManager(), SESSION_RUNNING_DIALOG);
                    } else {
                        DialogFragment dialog = new NewSessionNameDialogFragment();
                        dialog.show(getSupportFragmentManager(), NEW_SESSION_DIALOG);
                    }
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onReturnValueFromNewSessionNameDialog(String sessionName) {
        mEmptyListMessage.setVisibility(View.GONE);
        Session newSession = new Session();
        newSession.setSessionName(sessionName);
        SessionsLab.get(getApplicationContext()).getSessions().add(0, newSession);
        SessionsLab.get(getApplicationContext()).createNewRunningSession(newSession);
        Intent intent = new Intent(this, RunningSessionService.class);
        startService(intent);
        FallObjectCreator.resetFallNameCounter();
        SessionsLab.get(getApplicationContext()).saveRunningSessionInDatabase();

        boolean userWantsOnGoingNotification = mSharedPreferences.getBoolean(SettingsActivity
                .PREF_ONGOING_NOTIFICATION, true);

        if (userWantsOnGoingNotification) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.notification_icon)
                            .setContentTitle(getApplicationContext().getString(R.string.notification_title_text))
                            .setContentText(getApplicationContext().getString(R.string
                                    .notification_content_text))
                            .setOngoing(true);

            Intent resultIntent = new Intent(this, RunningSessionActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(RunningSessionActivity.class);
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    SessionsLab.get(getApplicationContext()).getNotificationManager();
            mNotificationManager.notify(1, mBuilder.build());
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSessionAlreadyRunningDialogPositiveClick() {
        SessionsLab.get(getApplicationContext()).stopCurrentlyRunningSession();
        mAdapter.notifyDataSetChanged();
        DialogFragment dialog = new NewSessionNameDialogFragment();
        dialog.show(getSupportFragmentManager(), NEW_SESSION_DIALOG);
    }

    private boolean isValidEmailAddress(String email) {
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean hasCorrectEmailSettings() {
        String emailString1 = mSharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS1, "");
        String emailString2 = mSharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS2, "");
        String emailString3 = mSharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS3, "");
        String emailString4 = mSharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS4, "");
        String emailString5 = mSharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS5, "");
        return ((!isValidEmailAddress(emailString1)) && (!isValidEmailAddress(emailString2))
                && (!isValidEmailAddress(emailString3)) && (!isValidEmailAddress
                (emailString4)) && (!isValidEmailAddress(emailString5)));
    }

    /**
     * Gives a reference to the last opened MainActivity to permit code execution on the ui thread
     * @return the last used MainActivity
     */
    public static Activity getLastActivity(){
        return mLastActivity;
    }
}
