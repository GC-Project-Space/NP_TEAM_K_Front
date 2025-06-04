package com.example.np_team_k.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.example.np_team_k.repository.UserRepository;

import com.example.np_team_k.network.PinRequest;
import com.google.android.gms.maps.model.LatLng;
import com.example.np_team_k.network.HomeAPI;  // 추가된 코드
import com.example.np_team_k.network.RetrofitClient;  // 추가된 코드
import com.example.np_team_k.network.PinResponse;
import com.google.gson.Gson;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final HomeAPI homeAPI = RetrofitClient.getClient().create(HomeAPI.class);
    private final UserRepository userRepository;
    private String currentSelectedReaction = null;

    public HomeViewModel(UserRepository userRepository) {
        // 기존 값들 유지
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
        selectedUserId.setValue("myUser");

        this.userRepository = userRepository;

        // LiveData로 변환된 kakaoId를 observe(수집)
        userRepository.getKakaoIdLiveData().observeForever(kakaoId -> {
            Log.d("HomeViewModel", "kakaoId observed: " + kakaoId);
            myKakaoId.postValue(kakaoId);
        });

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

    public LiveData<List<PinResponse.Pin>> getPinsLiveData() {
        return pinList;
    }


    // selectedUserId setter
    public void setSelectedUserId(String userId) {
        previousSelectedUserId.setValue(selectedUserId.getValue()); // 기존 값 옮기고 저장
        selectedUserId.setValue(userId); //사용자 선택 시 이전-현재 ID 갱신 처리
    }

    public void setSelectedUserLatLng(LatLng latLng) {
        selectedUserLatLng.setValue(latLng);
    }

    public void setPreviousSelectedUserId(String userId) {
        previousSelectedUserId.setValue(userId);
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

    public String getCurrentSelectedReaction() {
        return currentSelectedReaction;
    }

    public void setCurrentSelectedReaction(String reaction) {
        this.currentSelectedReaction = reaction;
    }



    // Retrofit을 사용하여 서버에서 핀 데이터를 요청하는 메서드, LiveData에 결과 저장
    public void fetchPins(double latitude, double longitude, String sort, String kakaoId) {
        Log.d("fetchPins", "called with ID: " + kakaoId + ", lat: " + latitude + ", lng: " + longitude);
        Call<List<PinResponse.Pin>> call = homeAPI.getPins(latitude, longitude, sort, kakaoId);

        call.enqueue(new Callback<List<PinResponse.Pin>>() {
            @Override
            public void onResponse(Call<List<PinResponse.Pin>> call, Response<List<PinResponse.Pin>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, PinResponse.Pin> uniquePins = new HashMap<>();
                    for (PinResponse.Pin pin : response.body()) {
                        if (pin.getLocation() == null ||
                                (pin.getLocation().getLatitude() == 0.0 && pin.getLocation().getLongitude() == 0.0)) {
                            Log.e("HomeViewModel", "잘못된 좌표: (0.0, 0.0)");
                            continue;
                        }
                        uniquePins.put(pin.getWriterKakaoId(), pin); // 중복 제거: 마지막 항목이 유지됨

                        if (kakaoId.equals(pin.getWriterKakaoId())) {
                            setMyMessage(pin.getMessage());
                        }
                    }
                    pinList.setValue(new ArrayList<>(uniquePins.values()));
                    Log.d("PinFetch", "중복 제거 및 유효 좌표 적용 후 핀 개수: " + uniquePins.size());
                } else {
                    errorMessage.setValue("핀 목록 요청 실패: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<PinResponse.Pin>> call, Throwable t) {
                errorMessage.setValue("네트워크 오류: " + t.getMessage());
                Log.e("HomeViewModel", "네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void fetchPinsWithMyId(double lat, double lng, String sort) {
        String kakaoId = myKakaoId.getValue();
        if (kakaoId != null) {
            fetchPins(lat, lng, sort, kakaoId);
        } else {
            Log.w("HomeViewModel", "fetchPinsWithMyId: kakaoId is null");
        }
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

    // 이모지 서버 전송 메서드 (리스트와 공통 사용 가능하도록 통일)
    public void sendReactionToServer(String emojiType, String targetUserId) {
        String myId = myKakaoId.getValue();
        if (myId == null || targetUserId == null) {
            Log.e("HomeViewModel", "이모지 전송 실패: 사용자 ID 누락");
            return;
        }

        String pinId = getPinIdByUserId(targetUserId);
        if (pinId == null) {
            Log.e("HomeViewModel", "이모지 전송 실패: 해당 사용자 핀 ID 없음" + targetUserId + ")");
            return;
        }

        Call<ResponseBody> call;
        if (emojiType != null) {
            // 새 이모지 전송
            currentSelectedReaction = emojiType; // 현재 선택 상태 갱신
            Log.d("ReactionDebug", "전송 요청 → pinId=" + pinId + ", emoji=" + emojiType + ", sender=" + myId);
            call = homeAPI.addReaction(pinId, emojiType, myId);
        } else {
            // 삭제 요청 (이전 선택값이 있어야 함)
            if (currentSelectedReaction == null) {
                Log.e("ReactionDebug", "삭제 요청 실패: 선택된 이모지 없음");
                return;
            }
            Log.d("ReactionDebug", "삭제 요청 → pinId=" + pinId + ", emoji=" + currentSelectedReaction + ", sender=" + myId);
            call = homeAPI.removeReaction(pinId, currentSelectedReaction, myId);
            currentSelectedReaction = null; // 상태 초기화
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("ReactionDebug", "이모지 " + (emojiType == null ? "삭제" : "전송") + " 성공");
                } else {
                    Log.e("ReactionDebug", "서버 응답 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ReactionDebug", "이모지 전송 실패: 네트워크 오류", t);
            }
        });
    }

    // wrapper 클래스 (LatLng 사용 시 필요)
    public static class LatLngWrapper {
        public final double lat;
        public final double lng;

        public LatLngWrapper(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    //Pin 서버 전송 로직
    public void sendPinToServer(String message, double latitude, double longitude) {
       // String kakaoId = myKakaoId.getValue();
        String kakaoId = "1234";
        if (kakaoId == null || kakaoId.isEmpty()) {
            errorMessage.setValue("카카오 ID가 설정되지 않았습니다.");
            return;
        }

        //PinRequest 객체 생성
        PinRequest request = new PinRequest(kakaoId, message, latitude, longitude);

        // Retrofit 요청 전송
        HomeAPI api = RetrofitClient.getClient().create(HomeAPI.class);
        api.postStatus(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("PinSend", "메시지 등록 성공");
                    errorMessage.setValue("메시지 등록 성공");
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
