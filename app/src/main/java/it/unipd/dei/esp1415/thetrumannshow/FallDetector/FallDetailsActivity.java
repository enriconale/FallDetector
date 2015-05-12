package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class FallDetailsActivity extends ActionBarActivity {
    private ImageView mSessionIcon;
    private Session mSession;
    private Fall mFall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_fall_details);

        int mSessionPositionInList = getIntent().getExtras().getInt(
                SessionsListAdapter.SESSION_DETAILS);
        mSession = SessionsLab.get(getApplicationContext()).getSessions().get(mSessionPositionInList);
        int mFallPostitionInList = getIntent().getExtras().getInt(SessionDetailsActivity
                .FALL_DETAILS);
        //mFall = mSession.getFalls().get(mFallPostitionInList);
        mSessionIcon = (ImageView)findViewById(R.id.session_icon);
        mSessionIcon.setImageResource(R.mipmap.recording_icon);
        if (SessionsLab.get(getApplicationContext()).hasRunningSession()) {
            if (mSessionPositionInList != 0) {
                mSessionIcon.setColorFilter(Color.rgb(mSession.getColor1(), mSession.getColor2(),
                        mSession.getColor3()));
            }
        } else {
            mSessionIcon.setColorFilter(Color.rgb(mSession.getColor1(), mSession.getColor2(),
                    mSession.getColor3()));
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fall_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
