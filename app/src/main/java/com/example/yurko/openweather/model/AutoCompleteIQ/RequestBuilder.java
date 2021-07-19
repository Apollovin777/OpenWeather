package com.example.yurko.openweather.model.AutoCompleteIQ;



import com.example.yurko.openweather.BuildConfig;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;

public class RequestBuilder {

    //Login request body
    public static RequestBody LoginBody(String username, String password, String token) {
        return new FormBody.Builder()
                .add("action", "login")
                .add("format", "json")
                .add("username", username)
                .add("password", password)
                .add("logintoken", token)
                .build();
    }

    public static HttpUrl buildURL(String search_string) {
        return new HttpUrl.Builder()
                .scheme("https") //http
                .host("api.locationiq.com/v1/autocomplete.php")
                .addQueryParameter("key", BuildConfig.OPENWEATHER_LOCATIONIQ_TOKEN) //add query parameters to the URL
                .addQueryParameter("q", search_string) //add query parameters to the URL
                .build();
        /**
         * The return URL:
         *  https://www.somehostname.com/pathSegment?param1=value1&encodedName=encodedValue
         */
    }

}