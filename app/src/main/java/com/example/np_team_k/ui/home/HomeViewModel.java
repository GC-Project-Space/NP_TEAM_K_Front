package com.example.np_team_k.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.example.np_team_k.network.HomeAPI;  // 추가된 코드
import com.example.np_team_k.network.RetrofitClient;  // 추가된 코드
import com.example.np_team_k.network.PinResponse;

import retrofit2.Call;  // 추가된 코드
import retrofit2.Callback;  // 추가된 코드
import retrofit2.Response;  // 추가된 코드
import java.util.List;  // 추가된 코드

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    // main user id
    private final MutableLiveData<String> selectedUserId = new MutableLiveData<>();
    private final MutableLiveData<String> previousSelectedUserId = new MutableLiveData<>();
    private final MutableLiveData<LatLng> selectedUserLatLng = new MutableLiveData<>();

    // 추가된 코드: 핀 목록을 저장할 LiveData
    private final MutableLiveData<List<PinResponse.Pin>> pinList = new MutableLiveData<>();  // 추가된 코드
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();  // 추가된 코드

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");

        // fix my information at start
        selectedUserId.setValue("myUser");
    }

    public LiveData<String> getText() {
        return mText;
    }

    // selectedUserId getter
    public LiveData<String> getSelectedUserId() {
        return selectedUserId;
    }

    // previousSelectedUserId getter
    public LiveData<String> getPreviousSelectedUserId() {
        return previousSelectedUserId;
    }

    public LiveData<LatLng> getSelectedUserLatLng() {
        return selectedUserLatLng;
    }

    // selectedUserId setter
    public void setSelectedUserId(String userId) {
        previousSelectedUserId.setValue(selectedUserId.getValue()); // 기존 값 옮기고 저장
        selectedUserId.setValue(userId);
    }

    public void setSelectedUserLatLng(LatLng latLng) {
        selectedUserLatLng.setValue(latLng);
    }

    // 추가된 코드: 핀 목록의 LiveData를 반환
    public LiveData<List<PinResponse.Pin>> getPinList() {  // 추가된 코드
        return pinList;  // 추가된 코드
    }  // 추가된 코드

    // 추가된 코드: 에러 메시지의 LiveData를 반환
    public LiveData<String> getErrorMessage() {  // 추가된 코드
        return errorMessage;  // 추가된 코드
    }  // 추가된 코드

    // ✅ 수정된 코드: Retrofit을 사용하여 핀 데이터를 불러오는 메서드
    public void fetchPins(double latitude, double longitude, String sort, String kakaoId) {
        HomeAPI api = RetrofitClient.getClient().create(HomeAPI.class);

        api.getPins(latitude, longitude, sort, kakaoId).enqueue(new Callback<PinResponse>() {
            @Override
            public void onResponse(Call<PinResponse> call, Response<PinResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PinResponse.Pin> pins = response.body().getPins();

                    // ✅ 빈 배열이 아닌지 확인
                    if (pins == null || pins.isEmpty()) {
                        errorMessage.setValue("서버로부터 핀 데이터를 가져오지 못했습니다.");
                        Log.e("HomeViewModel", "빈 배열 또는 null 반환");
                        return;
                    }

                    // ✅ 유효하지 않은 좌표 처리
                    for (PinResponse.Pin pin : pins) {
                        if (pin.getLatitude() == 0.0 && pin.getLongitude() == 0.0) {
                            Log.e("HomeViewModel", "잘못된 좌표 (0.0, 0.0)로 인한 핀 추가 방지");
                            continue;
                        }
                    }

                    pinList.setValue(pins);
                    Log.d("HomeViewModel", "핀 목록 불러오기 성공: " + pins.size() + "개");
                } else {
                    errorMessage.setValue("핀 데이터를 불러오지 못했습니다. 상태 코드: " + response.code());
                    Log.e("HomeViewModel", "응답 오류: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PinResponse> call, Throwable t) {
                errorMessage.setValue("네트워크 오류: " + t.getMessage());
                Log.e("HomeViewModel", "네트워크 오류: " + t.getMessage());
            }
        });
    }


}
