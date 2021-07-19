package com.example.yurko.openweather.presenter;

import androidx.annotation.NonNull;

public interface Presenter {
    void onPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    void viewIsReady();
    void updateScreenData();
    void updateOnClicked();
}
