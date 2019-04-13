package com.example.yurko.openweather.presenter;

import android.support.annotation.NonNull;

public interface Presenter {
    void onPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    void viewIsReady();
    void updateCurrentConditionData();
    void updateClicked();
}
