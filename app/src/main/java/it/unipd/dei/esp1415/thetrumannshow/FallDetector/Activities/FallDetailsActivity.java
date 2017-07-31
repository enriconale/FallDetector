package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Fall;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Session;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.CurrentLocale;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.SessionsLab;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.SessionsListAdapter;

/**
 * @author Enrico Naletto
 * Activity that shows the details of a single fall.
 */
public class FallDetailsActivity extends AppCompatActivity {
    //Variables needed to draw the graph
    private static int NUMBER_OF_TIME_MOMENTS = 150;
    private static int NUMBER_OF_ACCELERATION_UNITS = 100;

    private SimpleDateFormat mDateFormatter;
    private TextView mFallNameTextView;
    private TextView mSessionNameTextView;
    private TextView mFallDateTextView;
    private RelativeLayout mFallLocationHeader;
    private TextView mFallLatitudeTextView;
    private TextView mFallLongitudeTextView;
    private ImageView mSessionIcon;
    private RelativeLayout mGraphContainer;
    private Session mSession;
    private Fall mFall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.activity_fall_details);

        mDateFormatter = new SimpleDateFormat("dd/MM/yyyy\nHH:mm",
                CurrentLocale.getCurrentLocale(getApplicationContext()));

        int mSessionPositionInList = getIntent().getExtras().getInt(
                SessionsListAdapter.SESSION_DETAILS);
        mSession = SessionsLab.get(getApplicationContext()).getSessions().get(mSessionPositionInList);
        int mFallPositionInList = getIntent().getExtras().getInt(SessionDetailsActivity
                .FALL_DETAILS);
        mFall = mSession.getFalls().get(mFallPositionInList);
        mSessionIcon = (ImageView)findViewById(R.id.session_icon);
        mSessionIcon.setImageResource(R.mipmap.recording_icon);
        if (SessionsLab.get(getApplicationContext()).hasRunningSession()) {
            if (mSessionPositionInList != 0) {
                mSessionIcon.setColorFilter(mSession.getIconColorRgbValue());
            }
        } else {
            mSessionIcon.setColorFilter(mSession.getIconColorRgbValue());
        }

        mFallNameTextView = (TextView)findViewById(R.id.fall_name);
        mFallNameTextView.setText(mFall.getName());

        mSessionNameTextView = (TextView)findViewById(R.id.session_name);
        mSessionNameTextView.setText(mSession.getSessionName());

        mFallDateTextView = (TextView)findViewById(R.id.date_time);
        mFallDateTextView.setText(mDateFormatter.format(mFall.getDate()));

//        String resultString = Double.toString(mFall.getLatitude());
//        mFallLatitudeTextView = (TextView)findViewById(R.id.latitude);
//        mFallLatitudeTextView.setText(resultString);
//
//        resultString = Double.toString(mFall.getLongitude());
//        mFallLongitudeTextView = (TextView)findViewById(R.id.longitude);
//        mFallLongitudeTextView.setText(resultString);
//
//        mFallLocationHeader = (RelativeLayout)findViewById(R.id.fall_location_header);
//        mFallLocationHeader.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", mFall.getLatitude(),
//                        mFall.getLongitude());
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//                startActivity(intent);
//            }
//        });

        mGraphContainer = (RelativeLayout)findViewById(R.id.graph_container);
        ViewGroup.LayoutParams params = mGraphContainer.getLayoutParams();
        @SuppressWarnings("deprecation")
        int height = getWindowManager().getDefaultDisplay().getHeight();
        @SuppressWarnings("deprecation")
        int width = getWindowManager().getDefaultDisplay().getWidth();
        params.height = height / 3;
        params.width = width;

        MyView v = new MyView(getApplicationContext());
        mGraphContainer.addView(v);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fall_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class MyView extends View {
        protected Paint paint = new Paint();

        public MyView(Context context)
        {
            super(context);
        }

        @Override
        @SuppressWarnings("deprecation")
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(3);
            paint.setColor(Color.parseColor("red"));
            int screenWidth = getWidth();
            int screenHeight = getHeight();
            double[] totalAcceleration = getTotalAcceleration(mFall.getXAcceleration(), mFall
                            .getYAcceleration(),
                    mFall.getZAcceleration());
            NUMBER_OF_TIME_MOMENTS = totalAcceleration.length;
            NUMBER_OF_ACCELERATION_UNITS = (int)(getMaximum(totalAcceleration) + getMinimum
                    (totalAcceleration)) + 50;
            int pixelsPerTimeUnit = getNumOfHorizontalPixelsPerTimeUnit(screenWidth);
            int pixelsPerAccUnit = getNumOfVerticalPixelsPerAccelerationUnit(screenHeight);
            int endOfPrecedentLine = screenHeight/2;
            double precedentStopY = totalAcceleration[0];
            int j = 1;
            for (int i = 1; i < screenWidth; i += pixelsPerTimeUnit) {
                double startY = precedentStopY;
                double stopY;
                try {
                    stopY = totalAcceleration[j++];
                } catch (ArrayIndexOutOfBoundsException e) {
                    stopY = totalAcceleration[totalAcceleration.length - 1];
                }

                int offset = getPixelOffset(startY, stopY, pixelsPerAccUnit);
                canvas.drawLine(i,endOfPrecedentLine,i+pixelsPerTimeUnit,screenHeight/2 - (int)
                                startY + offset,
                        paint);
                endOfPrecedentLine = screenHeight/2 - (int)startY + offset;
                precedentStopY = stopY;
            }
            canvas.scale(getWindowManager().getDefaultDisplay().getWidth(), 0);
        }
    }

    private int getNumOfHorizontalPixelsPerTimeUnit(int screenWidth) {
        return screenWidth / NUMBER_OF_TIME_MOMENTS;
    }

    private int getNumOfVerticalPixelsPerAccelerationUnit(int screenHeight) {
        return screenHeight / NUMBER_OF_ACCELERATION_UNITS;
    }

    private int getPixelOffset(double startPointY, double stopPointY, int
            pixelsPerAccelerationUnit) {
        double difference = Math.abs(stopPointY - startPointY);
        return (int)Math.round(pixelsPerAccelerationUnit * difference);
    }

    private double[] getTotalAcceleration(float[] xAcc, float[] yAcc, float[] zAcc) {
        double[] result = new double[xAcc.length];
        for (int i = 0; i < xAcc.length; i++) {
            double value = Math.sqrt(xAcc[i]*xAcc[i] + yAcc[i]*yAcc[i] + zAcc[i]*zAcc[i]);
            result[i] = value;
        }
        return result;
    }

    private double getMaximum(double[] array) {
        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    private double getMinimum(double[] array) {
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
