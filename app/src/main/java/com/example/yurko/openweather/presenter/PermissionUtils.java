package com.example.yurko.openweather.presenter;

import android.content.Context;
import android.content.SharedPreferences;

public class PermissionUtils {

    public static final String PERMISSION_REQUEST_COUNT = "permission_request_count";

    public static void incrementPermissionsRequestsCount(final Context context, final String permission) {
        SharedPreferences genPrefs = context.getSharedPreferences(PERMISSION_REQUEST_COUNT, Context.MODE_PRIVATE);
        int count = getPermissionsRequestsCount(context,permission);
        SharedPreferences.Editor editor = genPrefs.edit();
        editor.putInt(permission, ++count);
        editor.commit();
    }
    public static int getPermissionsRequestsCount(final Context context, final String permission) {
        SharedPreferences genPrefs = context.getSharedPreferences(PERMISSION_REQUEST_COUNT, Context.MODE_PRIVATE);
        return genPrefs.getInt(permission, 0);
    }
}
