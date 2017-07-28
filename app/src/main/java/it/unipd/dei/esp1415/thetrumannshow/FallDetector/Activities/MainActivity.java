package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities;

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

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.FallObjectCreator;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Dialogs.NewSessionNameDialogFragment;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Services.RunningSessionService;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Session;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Dialogs.SessionAlreadyRunningDialogFragment;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.EmailValidator;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.PersistentNotificationManager;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.SessionsLab;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.SessionsListAdapter;


public class MainActivity extends AppCompatActivity implements NewSessionNameDialogFragment.NewSessionNameDialogFragmentListener, SessionAlreadyRunningDialogFragment.SessionAlreadyRunningDialogFragmentListener {

    private static final String NEW_SESSION_DIALOG = "new_session_dialog";
    private static final String SESSION_RUNNING_DIALOG = "session_running_dialog";

    private RecyclerView.Adapter mAdapter;
    private TextView mEmptyListMessage;
    private SharedPreferences mSharedPreferences;

    private static Activity mLastActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLastActivity = this;
        setContentView(R.layout.activity_main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (EmailValidator.hasIncorrectEmailSettings(mSharedPreferences)) {
            Toast.makeText(getApplicationContext(), R.string.insert_valid_email,
                    Toast.LENGTH_LONG).show();
        }

        mEmptyListMessage = (TextView) findViewById(R.id.empty_list_message);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        ArrayList<Session> mSessionsList = SessionsLab.get(getApplicationContext()).getSessions();

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
                if (EmailValidator.hasIncorrectEmailSettings(mSharedPreferences)) {
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
        Session newSession = new Session.Builder().sessionName(sessionName).build();
        SessionsLab.get(getApplicationContext()).getSessions().add(0, newSession);
        SessionsLab.get(getApplicationContext()).createNewRunningSession(newSession);
        Intent intent = new Intent(this, RunningSessionService.class);
        startService(intent);
        FallObjectCreator.resetFallNameCounter();
        SessionsLab.get(getApplicationContext()).saveRunningSessionInDatabase();
        PersistentNotificationManager.createPersistentNotificationForRunningSession(getApplicationContext());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSessionAlreadyRunningDialogPositiveClick() {
        SessionsLab.get(getApplicationContext()).stopCurrentlyRunningSession();
        mAdapter.notifyDataSetChanged();
        DialogFragment dialog = new NewSessionNameDialogFragment();
        dialog.show(getSupportFragmentManager(), NEW_SESSION_DIALOG);
    }

    public static Activity getLastActivity() {
        return mLastActivity;
    }
}
