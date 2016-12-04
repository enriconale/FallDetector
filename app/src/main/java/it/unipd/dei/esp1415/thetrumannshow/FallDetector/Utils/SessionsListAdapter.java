package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities.RunningSessionActivity;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities.SessionDetailsActivity;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Session;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;

/**
 * @author Enrico Naletto
 *         Adapter that builds every card (with the appropriate informations)in the MainActivity
 *         CardListView.
 */
public class SessionsListAdapter extends RecyclerView.Adapter<SessionsListAdapter.MyViewHolder> {
    public static final String SESSION_DETAILS = "session_details";

    private Context mAppContext;
    private ArrayList<Session> mDataset;
    private static SimpleDateFormat mDateFormatter;

    //Initialize and control all the views of a single card
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        protected Context appContext;
        protected Session session;
        protected RelativeLayout mMainCardLayout;
        protected ImageView mSessionIcon;
        protected TextView mSessionName;
        protected TextView mNumOfFalls;
        protected TextView mStartDateTime;
        protected TextView mSessionDuration;

        protected Handler mHandler = new Handler();
        protected Runnable mUpdateTimeTask = new Runnable() {
            public void run() {
                mSessionDuration.setText(session.getFormattedDuration());
                mNumOfFalls.setText(Integer.toString(session.getFalls().size()));

                mHandler.postDelayed(mUpdateTimeTask, 200);

            }
        };

        public MyViewHolder(View v) {
            super(v);
            mMainCardLayout = (RelativeLayout) v.findViewById(R.id.main_card_layout);
            mSessionName = (TextView) v.findViewById(R.id.session_name);
            mSessionIcon = (ImageView) v.findViewById(R.id.session_icon);
            mNumOfFalls = (TextView) v.findViewById(R.id.number_of_falls);
            mStartDateTime = (TextView) v.findViewById(R.id.session_start_date_time);
            mSessionDuration = (TextView) v.findViewById(R.id.session_duration);
        }
    }

    //Create a new SessionsListAdapter
    public SessionsListAdapter(ArrayList<Session> myDataset, Context appContext) {
        mDataset = myDataset;
        mAppContext = appContext;
        mDateFormatter = SessionsLab.get(appContext).getDateFormat();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.activity_main_cardview, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    //Take data from i-th element of the list (sessions list) and passes them to the views of the
    // card
    @Override
    public void onBindViewHolder(final MyViewHolder viewHolder, int i) {
        final int x = i;
        Session tmpSession = mDataset.get(x);
        viewHolder.session = tmpSession;
        viewHolder.appContext = mAppContext;


        viewHolder.mSessionName.setText(Helper.getFormattedSessionName(tmpSession.getSessionName()));
        viewHolder.mNumOfFalls.setText(Integer.toString(tmpSession.getNumberOfFalls()));
        viewHolder.mStartDateTime.setText(mDateFormatter.format(tmpSession.getDate()));
        viewHolder.mSessionDuration.setText(tmpSession.getFormattedDuration());
        viewHolder.mSessionIcon.clearColorFilter();

        if (SessionsLab.get(mAppContext).hasRunningSession() && x == 0) {
            viewHolder.mSessionIcon.setImageResource(R.mipmap.recording_icon);
            viewHolder.mHandler.postDelayed(viewHolder.mUpdateTimeTask, 0);
            viewHolder.mMainCardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.mHandler.removeCallbacks(viewHolder.mUpdateTimeTask);
                    Intent intent = new Intent(v.getContext(), RunningSessionActivity.class);
                    v.getContext().startActivity(intent);
                }
            });
        } else {
            viewHolder.mSessionIcon.setImageResource(R.mipmap.recording_icon);
            viewHolder.mSessionIcon.setColorFilter(Color.rgb(tmpSession.getColor1(), tmpSession.getColor2(), tmpSession.getColor3()));
            viewHolder.mMainCardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), SessionDetailsActivity.class);
                    intent.putExtra(SESSION_DETAILS, x);
                    v.getContext().startActivity(intent);
                }
            });
        }

    }

    //Number of elements in the list
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}