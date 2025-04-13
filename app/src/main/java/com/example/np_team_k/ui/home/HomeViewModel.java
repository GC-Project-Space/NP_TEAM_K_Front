package com.example.np_team_k.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    //main user id
    private final MutableLiveData<String> selectedUserId = new MutableLiveData<>();
    private final MutableLiveData<String> previousSelectedUserId = new MutableLiveData<>();
    private final MutableLiveData<LatLng> selectedUserLatLng = new MutableLiveData<>();

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");

        //fix my information at start
        selectedUserId.setValue("myUser");
    }

    public LiveData<String> getText() {
        return mText;
    }

    //selectedUserId getter
    public LiveData<String> getSelectedUserId() {
        return selectedUserId;
    }

    //previousSelectedUserId getter
    public LiveData<String> getPreviousSelectedUserId() {
        return previousSelectedUserId;
    }

    public LiveData<LatLng> getSelectedUserLatLng() {
        return selectedUserLatLng;
    }

    //selectedUserId setter
    public void setSelectedUserId(String userId) {
        previousSelectedUserId.setValue(selectedUserId.getValue());//기존 값 옮기고 저장
        selectedUserId.setValue(userId);
    }

    public void setSelectedUserLatLng(LatLng latLng) {
        selectedUserLatLng.setValue(latLng);
    }
}