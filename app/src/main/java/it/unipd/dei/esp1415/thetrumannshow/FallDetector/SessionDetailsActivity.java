package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;


public class SessionDetailsActivity extends ActionBarActivity {
    private static SimpleDateFormat mDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",
            java.util.Locale.getDefault());

    private Session mSession;
    private TextView mSessionName;
    private TextView mCreationDate;
    private int mPositionInList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        mPositionInList = getIntent().getExtras().getInt(SessionsListAdapter.SESSION_DETAILS);
        mSession = SessionsLab.get(getApplicationContext()).getSessions().get(mPositionInList);

        mSessionName = (TextView)findViewById(R.id.session_name);
        mCreationDate = (TextView)findViewById(R.id.date_time);

        mSessionName.setText(mSession.getSessionName());
        mCreationDate.setText(mDateFormatter.format(mSession.getDate()));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_session_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete_session:
                SessionsLab.get(getApplicationContext()).getSessions().remove(mPositionInList);
                NavUtils.navigateUpFromSameTask(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
