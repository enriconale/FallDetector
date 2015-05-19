package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;


public class RunningSessionActivity extends AppCompatActivity implements SensorEventListener {
    private static SimpleDateFormat mDateFormatter;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Session mSession;
    private ImageView mEditSessionNameImageView;
    private TextView mSessionName;
    private EditText mEditSessionNameEditText;
    private TextView mSessionCreationDate;
    private TextView mSessionDuration;
    private TextView mAccXAxis;
    private TextView mAccYAxis;
    private TextView mAccZAxis;
    private Handler mHandler = new Handler();
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            mSessionDuration.setText(getApplicationContext().getString(R.string.cardview_duration)
                    + " " + mSession.getFormattedDuration());
            mHandler.postDelayed(mUpdateTimeTask, 100);

        }
    };

    private boolean mEditingName = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_session);

        mSession = SessionsLab.get(getApplicationContext()).getRunningSession();
        mDateFormatter = SessionsLab.get(getApplicationContext()).getDateFormat();

        mSessionName = (TextView)findViewById(R.id.session_name);
        mEditSessionNameEditText = (EditText)findViewById(R.id.edit_session_name);
        mSessionCreationDate = (TextView)findViewById(R.id.date_time);
        mSessionDuration = (TextView)findViewById(R.id.session_duration);
        mEditSessionNameImageView = (ImageView)findViewById(R.id.modify_session_name);

        mSessionName.setText(mSession.getSessionName());
        mEditSessionNameEditText.setText(mSession.getSessionName());
        mSessionCreationDate.setText(mDateFormatter.format(mSession.getDate()));

        mAccXAxis = (TextView)findViewById(R.id.acc_x_axis);
        mAccYAxis = (TextView)findViewById(R.id.acc_y_axis);
        mAccZAxis = (TextView)findViewById(R.id.acc_z_axis);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mEditSessionNameImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEditingName) {
                    showVirtualKeyboard();
                    mEditSessionNameImageView.setImageResource(R.mipmap.action_done);
                    mSessionName.setVisibility(View.GONE);
                    mEditSessionNameEditText.setVisibility(View.VISIBLE);
                    mEditingName = true;
                } else {
                    hideVirtualKeyboard();
                    mEditSessionNameImageView.setImageResource(R.mipmap.action_edit);
                    mSessionName.setVisibility(View.VISIBLE);
                    mEditSessionNameEditText.setVisibility(View.GONE);
                    String newName = mEditSessionNameEditText.getText().toString();
                    mSession.setSessionName(newName);
                    mSessionName.setText(newName);
                    mEditSessionNameEditText.setText(newName);
                    mEditingName = false;
                }
            }
        });

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
        mSensorManager.unregisterListener(this);
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 0);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mAccXAxis.setText(Float.toString(event.values[0]));
        mAccYAxis.setText(Float.toString(event.values[1]));
        mAccZAxis.setText(Float.toString(event.values[2]));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing here.
    }

    private void hideVirtualKeyboard(){
        if(getCurrentFocus() != null && getCurrentFocus() instanceof EditText){
            InputMethodManager imm =
                    (InputMethodManager)this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void showVirtualKeyboard() {
            InputMethodManager imm =
                    (InputMethodManager)this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mEditSessionNameEditText, InputMethodManager.SHOW_FORCED);
    }
}
