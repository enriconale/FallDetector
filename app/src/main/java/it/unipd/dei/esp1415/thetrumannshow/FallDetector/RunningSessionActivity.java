package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;


public class RunningSessionActivity extends ActionBarActivity {
    private static SimpleDateFormat mDateFormatter;

    private Session mSession;
    private TextView mSessionName;
    private TextView mSessionCreationDate;
    private TextView mSessionDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_session);

        mSession = SessionsLab.get(getApplicationContext()).getRunningSession();
        mDateFormatter = SessionsLab.get(getApplicationContext()).getDateFormat();

        mSessionName = (TextView)findViewById(R.id.session_name);
        mSessionCreationDate = (TextView)findViewById(R.id.date_time);
        mSessionDuration = (TextView)findViewById(R.id.session_duration);

        mSessionName.setText(mSession.getSessionName());
        mSessionCreationDate.setText(mDateFormatter.format(mSession.getDate()));
        mSessionDuration.setText(getApplicationContext().getString(R.string.cardview_duration)
                + " " + mSession.getFormattedDuration());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_running_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
