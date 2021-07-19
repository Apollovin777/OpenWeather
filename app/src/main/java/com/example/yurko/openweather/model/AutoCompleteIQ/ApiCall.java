package com.example.yurko.openweather.model.AutoCompleteIQ;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiCall {

    private static final String LOG_TAG = "ApiCall";

    OkHttpClient mClient;

    public ApiCall() {
        mClient = new OkHttpClient();
    }

    private void getWebservice(String search_string) {
        HttpUrl URL = RequestBuilder.buildURL(search_string);
        final Request request = new Request.Builder().url(URL).build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(LOG_TAG,e.getMessage());
            }
            @Override
            public void onResponse(Call call, final Response response) {
                Log.i(LOG_TAG,response.message());
            }
        });
    }

}