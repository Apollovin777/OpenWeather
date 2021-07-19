package com.example.yurko.openweather.model;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.yurko.openweather.BuildConfig;
import com.example.yurko.openweather.presenter.UpdateJob;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyApplication extends Application {

    private final String APP_KEY = BuildConfig.OPENWEATHER_API_KEY;

    private static Context context;
    private static OpenWeatherAPI openWeatherAPI;
    private static OpenWeatherAPI openWeatherAPIRaw;

    public void onCreate() {
        super.onCreate();

        MyApplication.context = getApplicationContext();

        OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("x-api-key", APP_KEY)
                        .build();
                return chain.proceed(request);
            }
        }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        openWeatherAPI = retrofit.create(OpenWeatherAPI.class);

        Retrofit retrofitRaw = new Retrofit.Builder()
                .client(client)
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .build();
        openWeatherAPIRaw = retrofitRaw.create(OpenWeatherAPI.class);

        scheduleSync();
    }

    public static void scheduleSync() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        if (prefs.getBoolean("sync", false)) {
            JobScheduler mJobScheduler = (JobScheduler)
                    MyApplication.getAppContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                JobInfo jobInfo = mJobScheduler.getPendingJob(UpdateJob.JOB_ID);
                if (jobInfo == null) {
                    JobInfo.Builder builder = new JobInfo.Builder(UpdateJob.JOB_ID,
                            new ComponentName(MyApplication.getAppContext(), UpdateJob.class));
                    int period = Integer.parseInt(prefs.getString("list", "1"));
                    builder.setPeriodic(3600000 * period);
                    builder.setPersisted(true);// in every 1 hour
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // only when network is available
                    mJobScheduler.schedule(builder.build());
                }
            }
        }
    }

    public static OpenWeatherAPI getApi() {
        return openWeatherAPI;
    }

    public static OpenWeatherAPI getApiRAW() {
        return openWeatherAPIRaw;
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
