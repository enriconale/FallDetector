package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class SessionsListAdapter extends RecyclerView.Adapter<SessionsListAdapter.MyViewHolder> {
    public static final String SESSION_DETAILS = "session_details";

    private Context mAppContext;
    private ArrayList<Session> mDataset;
    private static SimpleDateFormat mDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",
            java.util.Locale.getDefault());

    //Initialize and control all the views of a single card
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        protected Session mSessionVisualized;
        protected int mPosition;
        protected RelativeLayout mMainCardLayout;
        protected ImageView mSessionIcon;
        protected TextView mSessionName;
        protected TextView mNumOfFalls;
        protected TextView mStartDateTime;
        protected TextView mSessionDuration;

        public MyViewHolder(View v) {
            super(v);
            mMainCardLayout = (RelativeLayout)v.findViewById(R.id.main_card_layout);
            mSessionName = (TextView)v.findViewById(R.id.session_name);
            mSessionIcon = (ImageView)v.findViewById(R.id.session_icon);
            mNumOfFalls = (TextView)v.findViewById(R.id.number_of_falls);
            mStartDateTime = (TextView)v.findViewById(R.id.session_start_date_time);
            mSessionDuration = (TextView)v.findViewById(R.id.session_duration);

            mMainCardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), SessionDetailsActivity.class);
                    i.putExtra(SESSION_DETAILS, mPosition);
                    v.getContext().startActivity(i);
                }
            });
        }
    }

    //Create a new SessionsListAdapter
    public SessionsListAdapter(ArrayList<Session> myDataset, Context appContext) {
        mDataset = myDataset;
        mAppContext = appContext;
    }

    //Nothing more to do here
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.activity_main_cardview, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    //Take data from i-th element of the list (sessions list) and passes them to the views of the
    // card
    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int i) {
        Session tmpSession = mDataset.get(i);
        viewHolder.mSessionName.setText(tmpSession.getSessionName());

        String tmpStrBuilder = mAppContext.getString(R.string.cardview_falls) + " " +
                tmpSession.getFalls().size();
        viewHolder.mNumOfFalls.setText(tmpStrBuilder);
        tmpStrBuilder = mAppContext.getString(R.string.cardview_startdate) + " " +
                mDateFormatter.format(tmpSession.getDate());
        viewHolder.mStartDateTime.setText(tmpStrBuilder);
        tmpStrBuilder = mAppContext.getString(R.string.cardview_duration) + " " +
                tmpSession.getDuration();
        viewHolder.mSessionDuration.setText(tmpStrBuilder);

        viewHolder.mPosition = i;
        viewHolder.mSessionVisualized = tmpSession;
    }

    //Numbers of element in the list
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
