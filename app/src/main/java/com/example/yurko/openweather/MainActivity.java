package com.example.yurko.openweather;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    String mCurrent;
    String mForecast;

    TextView mTextView;
    TextView mCurrTemp;
    TextView mCurrDesc;

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mTextView = findViewById(R.id.main_text);
        mCurrTemp = findViewById(R.id.current_temp);
        mCurrDesc = findViewById(R.id.current_desc);

        Log.i("JRequest","onCreate");

        new AsyncTask<Void,Void,Void>(){

            JRequest mJRequest;
            @Override
            protected Void doInBackground(Void... voids) {
                mJRequest = new JRequest("London",false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Log.i("JRequest",mJRequest.getResult());
                mCurrent = mJRequest.getResult();
                updateCurrent();
            }
        }.execute();
    }

    private void updateCurrent(){
        if (mCurrent != null){
            JSONParseCurrent current = null;
            try {
                JSONObject rootObject = new JSONObject(mCurrent);
                current = new JSONParseCurrent(rootObject);
                StringBuilder builder = new StringBuilder();
                builder.append(current.getCityName() + '\n');
                builder.append(current.getDate().toString()+ '\n');
                builder.append(String.valueOf(current.getHumidity()) + '\n');
                builder.append(String.valueOf(current.getPressure()) + '\n');

                mTextView.setText(builder.toString());
                mCurrTemp.setText(String.valueOf(current.getTemp()));
                mCurrDesc.setText(current.getDesc());

            }
            catch (JSONException e){
                Log.i("JRequest",e.getMessage());
            }




        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }
}
