package com.example.testapp2.ui.learnMore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LearnMoreViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LearnMoreViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}