package com.example.np_team_k.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.np_team_k.network.PinRequest;
import com.google.android.gms.maps.model.LatLng;
import com.example.np_team_k.network.HomeAPI;  // 추가된 코드
import com.example.np_team_k.network.RetrofitClient;  // 추가된 코드
import com.example.np_team_k.network.PinResponse;
import com.google.gson.Gson;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class HomeViewModel extends ViewModel {

    // 기본 텍스트
    private final MutableLiveData<String> mText;

    // main user id
    private final MutableLiveData<String> selectedUserId = new MutableLiveData<>(); //현재 지도에서 선택된 유저(말풍선 클릭 등) 식별
    private final MutableLiveData<String> previousSelectedUserId = new MutableLiveData<>(); //이전에 선택했던 유저 저장 (UI 전환 처리용)
    private final MutableLiveData<LatLng> selectedUserLatLng = new MutableLiveData<>(); //선택된 유저의 위치 정보 저장

    // 서버에서 받아온 핀 목록 및 에러 메시지를 저장할 LiveData
    private final MutableLiveData<List<PinResponse.Pin>> pinList = new MutableLiveData<>();  // 서버에서 받아온 모든 핀 데이터를 저장
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();  // API 실패 메시지를 저장 (Toast 용)

    // ✅ 추가: 내 Kakao ID와 내 메시지
    private final MutableLiveData<String> myKakaoId = new MutableLiveData<>(); //로그인한 내 사용자 ID 저장
    private final MutableLiveData<String> myMessage = new MutableLiveData<>(); //내 메시지를 서버로부터 추출해 저장

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");

        // fix my information at start
        selectedUserId.setValue("myUser");
    }

    // Getter & Setter
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
        selectedUserId.setValue(userId); //사용자 선택 시 이전-현재 ID 갱신 처리
    }

    public void setSelectedUserLatLng(LatLng latLng) {
        selectedUserLatLng.setValue(latLng);
    }

    // 핀 목록의 LiveData를 반환
    public LiveData<List<PinResponse.Pin>> getPinList() {
        return pinList;
    }

    // 에러 메시지의 LiveData를 반환
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getMyKakaoId() { return myKakaoId; }
    public void setMyKakaoId(String id) { myKakaoId.setValue(id); }

    public LiveData<String> getMyMessage() { return myMessage; }
    public void setMyMessage(String message) { myMessage.setValue(message); }



    // Retrofit을 사용하여 서버에서 핀 데이터를 요청하는 메서드, LiveData에 결과 저장
    public void fetchPins(double latitude, double longitude, String sort, String kakaoId) {
        HomeAPI api = RetrofitClient.getClient().create(HomeAPI.class);

        api.getPins(latitude, longitude, sort, kakaoId).enqueue(new Callback<PinResponse>() {
            @Override
            public void onResponse(Call<PinResponse> call, Response<PinResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PinResponse.Pin> pins = response.body().getPins();
                    Log.d("HTTP_RESPONSE_BODY", new Gson().toJson(response.body()));

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

                    // 내 핀의 메시지를 추출해서 저장
                    for (PinResponse.Pin pin : pins) {
                        if (pin.getWriterKakaoId().equals(kakaoId)) {
                            setMyMessage(pin.getMessage());
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

    // userId에 해당하는 pinId를 반환
    public String getPinIdByUserId(String userId) {
        List<PinResponse.Pin> pins = pinList.getValue();
        if (pins != null) {
            for (PinResponse.Pin pin : pins) {
                if (pin.getWriterKakaoId().equals(userId)) {
                    return pin.getId();
                }
            }
        }
        return null;
    }

    //Pin 서버 전송 로직
    public void sendPinToServer(String message, double latitude, double longitude) {
        String kakaoId = myKakaoId.getValue();
        if (kakaoId == null || kakaoId.isEmpty()) {
            errorMessage.setValue("카카오 ID가 설정되지 않았습니다.");
            return;
        }

        // ✅ createdAt 자동 생성 (ISO 8601 형식)
        String createdAt = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // ✅ 초기 감정 카운트: 0으로 세팅
        PinRequest.ReactionCounts reactions = new PinRequest.ReactionCounts(0, 0, 0, 0);

        // ✅ PinRequest 객체 생성
        PinRequest request = new PinRequest(kakaoId, message, latitude, longitude);

        // ✅ Retrofit 요청 전송
        HomeAPI api = RetrofitClient.getClient().create(HomeAPI.class);
        api.postStatus(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("PinSend", "메시지 등록 성공");
                    // 서버 반영 후 최신 핀 리스트 다시 불러오기
                    fetchPins(latitude, longitude, "distance", kakaoId);
                } else {
                    errorMessage.setValue("서버 응답 실패: " + response.code());
                    Log.e("PinSend", "응답 실패: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                errorMessage.setValue("네트워크 오류: " + t.getMessage());
                Log.e("PinSend", "네트워크 오류: " + t.getMessage());
            }
        });
    }


}
