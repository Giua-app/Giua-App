package com.giua.app.ui.voti;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.giua.webscraper.GiuaScraper;

public class VotiViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public VotiViewModel() {
        mText = new MutableLiveData<>();

        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}