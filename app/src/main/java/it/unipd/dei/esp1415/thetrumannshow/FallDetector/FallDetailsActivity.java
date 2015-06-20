package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Locale;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_fall_details);

        mDateFormatter = new SimpleDateFormat("dd/MM/yyyy\nHH:mm",
                java.util.Locale.getDefault());

        int mSessionPositionInList = getIntent().getExtras().getInt(
                SessionsListAdapter.SESSION_DETAILS);
        mSession = SessionsLab.get(getApplicationContext()).getSessions().get(mSessionPositionInList);
        int mFallPostitionInList = getIntent().getExtras().getInt(SessionDetailsActivity
                .FALL_DETAILS);
        mFall = mSession.getFalls().get(mFallPostitionInList);
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

        mFallNameTextView = (TextView)findViewById(R.id.fall_name);
        mFallNameTextView.setText(getFormattedSessionName(mFall.getName()));

        mSessionNameTextView = (TextView)findViewById(R.id.session_name);
        mSessionNameTextView.setText(mSession.getSessionName());

        mFallDateTextView = (TextView)findViewById(R.id.date_time);
        mFallDateTextView.setText(mDateFormatter.format(mFall.getDate()));

        String resultString = Double.toString(mFall.getLatitude());
        mFallLatitudeTextView = (TextView)findViewById(R.id.latitude);
        mFallLatitudeTextView.setText(resultString);

        resultString = Double.toString(mFall.getLongitude());
        mFallLongitudeTextView = (TextView)findViewById(R.id.longitude);
        mFallLongitudeTextView.setText(resultString);

        mFallLocationHeader = (RelativeLayout)findViewById(R.id.fall_location_header);
        mFallLocationHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", mFall.getLatitude(),
                        mFall.getLongitude());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });

        mGraphContainer = (RelativeLayout)findViewById(R.id.graph_container);
        ViewGroup.LayoutParams params = mGraphContainer.getLayoutParams();
        int height = getWindowManager().getDefaultDisplay().getHeight();
        int width = getWindowManager().getDefaultDisplay().getWidth();
        params.height = height / 3;
        params.width = width;

        MyView v = new MyView(getApplicationContext());
        mGraphContainer.addView(v);
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

    private String getFormattedSessionName(String name) {
        if (name.length() > 30) {
            return name.substring(0, 30) + "...";
        } else {
            return name;
        }
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
}
