package com.example.testapp2.ui.onBoarding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OnBoardingViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public OnBoardingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}