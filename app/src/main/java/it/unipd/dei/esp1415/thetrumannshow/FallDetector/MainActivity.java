package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        String[] myDataset = {"Session 1", "Session 2", "Session 3", "Session 4", "Session 5",
                "Session 6", "Session 7"};
        mAdapter = new SessionsListAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        // Eikes soon-to-be-removed test code
        new Thread(){
            public void run(){
                DataAcquisitionUnit dau = new DataAcquisitionUnit(getApplicationContext());
                try{
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {}
                dau.writeToFile("testData.csv");
                android.widget.Toast.makeText(getApplicationContext(),
                        getApplicationContext().getFilesDir().toString(),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        }.run();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
