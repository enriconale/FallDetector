package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;


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

    private DataAcquisitionUnit dau;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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

        // starting data acquisition, for now only till pause
        dau = new DataAcquisitionUnit(getApplicationContext());
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        dau.detach();
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
                if (SessionsLab.get(getApplicationContext()).hasRunningSession()) {
                    DialogFragment dialog = new SessionAlreadyRunningDialogFragment();
                    dialog.show(getSupportFragmentManager(), SESSION_RUNNING_DIALOG);
                } else {
                    DialogFragment dialog = new NewSessionNameDialogFragment();
                    dialog.show(getSupportFragmentManager(), NEW_SESSION_DIALOG);
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
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSessionAlreadyRunningDialogPositiveClick() {
        SessionsLab.get(getApplicationContext()).stopCurrentlyRunningSession();
        mAdapter.notifyDataSetChanged();
        DialogFragment dialog = new NewSessionNameDialogFragment();
        dialog.show(getSupportFragmentManager(), NEW_SESSION_DIALOG);
    }

    public void sendMail(){
        Intent send = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode("softwaretest@eiketrumann.de") +
                "?subject=" + Uri.encode("Sono Caduto") +
                "&body=" + Uri.encode("Vieni a prendermi");
        Uri uri = Uri.parse(uriText);
        send.setData(uri);
        startActivity(Intent.createChooser(send, "Send mail..."));
    }
}
