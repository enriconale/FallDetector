package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Database.FallDbManager;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Database.SessionDbManager;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Dialogs.DeleteSessionDialog;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Fall;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Session;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.CurrentLocale;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.SessionsLab;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.SessionsListAdapter;

/**
 * @author Enrico Naletto
 * Activity that shows the details of a single session, with all its falls.
 */
public class SessionDetailsActivity extends AppCompatActivity implements DeleteSessionDialog.DeleteSessionDialogListener {
    public static final String FALL_DETAILS = "fall_details";
    public static final String SESSION_DELETE = "session_delete";
    private static SimpleDateFormat mDateFormatter;

    private Session mSession;
    private TextView mSessionName;
    private TextView mSessionCreationDate;
    private TextView mSessionDuration;
    private ImageView mSessionIcon;
    private int mSessionPositionInList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        mDateFormatter = new SimpleDateFormat("dd/MM/yyyy\nHH:mm",
                CurrentLocale.getCurrentLocale(getApplicationContext()));

        mSessionPositionInList = getIntent().getExtras().getInt(SessionsListAdapter.SESSION_DETAILS);
        mSession = SessionsLab.get(getApplicationContext()).getSessions().get(mSessionPositionInList);

        FallDbManager fallDbManager = new FallDbManager(getApplicationContext());
        LinkedList<Fall> listOfFalls = fallDbManager.getFallsFromDatabase(mSession);
        mSession.setListOfFalls(listOfFalls);

        mSessionName = (TextView)findViewById(R.id.session_name);
        mSessionCreationDate = (TextView)findViewById(R.id.date_time);
        mSessionDuration = (TextView)findViewById(R.id.session_duration);
        mSessionIcon = (ImageView)findViewById(R.id.session_icon);


        mSessionName.setText(mSession.getFormattedSessionName());
        mSessionCreationDate.setText(mDateFormatter.format(mSession.getDate()));
        mSessionDuration.setText(mSession.getFormattedDuration());
        mSessionIcon.setImageResource(R.mipmap.recording_icon);
        mSessionIcon.setColorFilter(mSession.getIconColorRgbValue());

        RelativeLayout fallsListContainer = (RelativeLayout)findViewById(R.id.falls_list_container);
        LinearLayout itemsWrapper = new LinearLayout(getApplicationContext());
        itemsWrapper.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        SimpleDateFormat fallItemDateFormatter = new SimpleDateFormat("HH:mm",
                java.util.Locale.getDefault());
        for (int i = 0; i < listOfFalls.size(); i++) {
            final int fallPositionInList = i;
            final View singleFallListItem = getLayoutInflater().inflate(R.layout.single_fall_list_item,
                    itemsWrapper, false);
            singleFallListItem.setId(i);

            TextView fallNameTextView = (TextView)singleFallListItem.findViewById(R.id.fall_id);
            TextView fallHourTextView = (TextView)singleFallListItem.findViewById(R.id.fall_hour);
            fallNameTextView.setText(listOfFalls.get(i).getName());
            fallHourTextView.setText(fallItemDateFormatter.format(listOfFalls.get(i).getDate()));
            singleFallListItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), FallDetailsActivity.class);
                    intent.putExtra(SessionsListAdapter.SESSION_DETAILS, mSessionPositionInList);
                    intent.putExtra(FALL_DETAILS, fallPositionInList);
                    startActivity(intent);
                }
            });
            if (i != 0) {
                lp.addRule(RelativeLayout.BELOW, i - 1);
            }
            itemsWrapper.addView(singleFallListItem);
        }
        fallsListContainer.addView(itemsWrapper);
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
                DialogFragment dialog = new DeleteSessionDialog();
                dialog.show(getSupportFragmentManager(), SESSION_DELETE);
                break;
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteSessionDialogPositiveClick() {
        SessionsLab tmp = SessionsLab.get(getApplicationContext());
        tmp.getSessions().remove(mSessionPositionInList);
        SessionDbManager sessionDbManager = new SessionDbManager(getApplicationContext());
        sessionDbManager.deleteSession(mSession);
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
