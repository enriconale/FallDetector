package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SessionsListAdapter extends RecyclerView.Adapter<SessionsListAdapter.MyViewHolder> {
    private String[] mDataset;

    //Initialize and control all the views of a single card
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTestTextView;

        public MyViewHolder(View v) {
            super(v);
            mTestTextView = (TextView)v.findViewById(R.id.test_textview);
        }
    }

    //Create a new SessionsListAdapter
    public SessionsListAdapter(String[] myDataset) {
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
        viewHolder.mTestTextView.setText("Hello");
    }

    //Numbers of element in the list
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
