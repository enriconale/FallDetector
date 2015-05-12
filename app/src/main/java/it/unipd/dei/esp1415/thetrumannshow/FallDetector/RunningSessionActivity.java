package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;


public class RunningSessionActivity extends ActionBarActivity {
    private static SimpleDateFormat mDateFormatter;

    private Session mSession;
    private TextView mSessionName;
    private TextView mSessionCreationDate;
    private TextView mSessionDuration;
    private Handler mHandler = new Handler();
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            mSessionDuration.setText(getApplicationContext().getString(R.string.cardview_duration)
                    + " " + mSession.getFormattedDuration());
            mHandler.postDelayed(mUpdateTimeTask, 100);

        }
    };


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

        RelativeLayout fallsListContainer = (RelativeLayout)findViewById(R.id
                .falls_list_container);
        LinearLayout itemsWrapper = new LinearLayout(getApplicationContext());
        itemsWrapper.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        for (int i = 0; i < 10; i++) {
            final int fallPositionInList = i;
            final View singleFallListItem = getLayoutInflater().inflate(R.layout.single_fall_list_item,
                    itemsWrapper, false);
            singleFallListItem.setId(i);
            singleFallListItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), FallDetailsActivity.class);
                    intent.putExtra(SessionsListAdapter.SESSION_DETAILS, 0);
                    intent.putExtra(SessionDetailsActivity.FALL_DETAILS, fallPositionInList);
                    startActivity(intent);
                }
            });
            if (i != 0) {
                lp.addRule(RelativeLayout.BELOW, i - 1);
            }
            itemsWrapper.addView(singleFallListItem);
        }
        fallsListContainer.addView(itemsWrapper);

        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_running_session, menu);
        if (SessionsLab.get(getApplicationContext()).isRunningSessionPlaying()) {
            menu.getItem(0).setIcon(getResources()
                    .getDrawable(R.mipmap.action_pause_circle_outline));
        } else {
            menu.getItem(0).setIcon(getResources()
                    .getDrawable(R.mipmap.action_play_circle_outline));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_pause_resume_session:
                if (SessionsLab.get(getApplicationContext()).isRunningSessionPlaying()) {
                    item.setIcon(getResources().getDrawable(R.mipmap.action_play_circle_outline));
                    SessionsLab.get(getApplicationContext()).pauseCurrentlyRunningSession();
                } else {
                    item.setIcon(getResources().getDrawable(R.mipmap.action_pause_circle_outline));
                    SessionsLab.get(getApplicationContext()).resumeCurrentlyRunningSession();
                }
                break;
            case R.id.action_stop_session:
                SessionsLab.get(getApplicationContext()).stopCurrentlyRunningSession();
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 0);
    }
}
