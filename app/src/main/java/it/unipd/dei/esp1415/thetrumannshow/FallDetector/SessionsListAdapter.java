package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

public class SessionsListAdapter extends RecyclerView.Adapter<SessionsListAdapter.MyViewHolder> {
    private ArrayList<Session> mDataset;
    private Date mDate = new Date();

    //Initialize and control all the views of a single card
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        protected ImageView mSessionIcon;
        protected TextView mSessionName;
        protected TextView mNumOfFalls;
        protected TextView mStartDateTime;
        protected TextView mSessionDuration;

        public MyViewHolder(View v) {
            super(v);
            mSessionName = (TextView)v.findViewById(R.id.session_name);
            mSessionIcon = (ImageView)v.findViewById(R.id.session_icon);
            mNumOfFalls = (TextView)v.findViewById(R.id.number_of_falls);
            mStartDateTime = (TextView)v.findViewById(R.id.session_start_date_time);
            mSessionDuration = (TextView)v.findViewById(R.id.session_duration);
        }
    }

    //Create a new SessionsListAdapter
    public SessionsListAdapter(ArrayList<Session> myDataset) {
        mDataset = myDataset;
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
        viewHolder.mSessionName.setText(mDataset.get(i).getSessionName());
        viewHolder.mNumOfFalls.setText(Integer.toString(mDataset.get(i).getFalls().size()));
        viewHolder.mStartDateTime.setText(mDataset.get(i).getDate().toString());
        viewHolder.mSessionDuration.setText(Long.toString(mDataset.get(i).getDuration()));
    }

    //Numbers of element in the list
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
