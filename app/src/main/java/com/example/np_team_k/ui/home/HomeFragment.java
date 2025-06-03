package com.example.np_team_k.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.np_team_k.R;
import com.example.np_team_k.databinding.FragmentHomeBinding;
import com.example.np_team_k.databinding.ViewMainUserInfoBinding;
import com.example.np_team_k.network.HomeAPI;
import com.example.np_team_k.network.PinRequest;
import com.example.np_team_k.network.PinResponse;
import com.example.np_team_k.network.ReactionAPI;
import com.example.np_team_k.network.RetrofitClient;
import com.example.np_team_k.repository.UserRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentHomeBinding binding;
    private ViewMainUserInfoBinding mainUserInfoBinding;
    private HomeViewModel homeViewModel;
    private MapView mapView;
    private GoogleMap googleMap;
    private GoogleMap.OnCameraMoveListener cameraMoveListener; // 카메라 움직일 때 listener

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LatLng myCurrentLocation = new LatLng(37.5665, 126.9780);  // 초기값
    private boolean messageSubmitted = false; //중복 등록 방지
    private String currentSelectedReaction = null; // 현재 선택된 reaction 상태를 저장할 변수
    private boolean cameraMoved = false;  // 지도 자동이동 제어 플래그
    private boolean isMapMoving = false; // 지도 이동 여부 추적용 플래그
    boolean pinsAlreadyFetched = false; // 추가: 핀 중복 로딩 방지용
    private final Map<String, View> balloonViewMap = new HashMap<>(); // 추가: 사용자 ID → 말풍선 View 매핑
    private String currentPinUserId = ""; // 현재 선택된 핀의 사용자 ID 저장
    private String myKakaoId = "";
    private final Handler pinCheckHandler = new Handler(Looper.getMainLooper());
    private final Runnable pinCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMapMoving) {
                Log.d("PinCheck", "checkAndUpdatePins() 실행됨");
                checkAndUpdatePins();
            } else { // 지도 이동 중이면 핀 갱신 생략
                Log.d("PinCheck", "지도 이동 중 - 핀 갱신 생략");
            }
            pinCheckHandler.postDelayed(this, 4000); // 5초 주기 반복
        }
    };


    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        Log.d("PinCheck", "onMapReady 호출됨 - googleMap 초기화 완료");

        // ✅ 지도 준비 완료되면 핀 점검 루프 시작
        pinCheckHandler.post(pinCheckRunnable);
        // 사용자가 지도 조작 시 자동 이동 비활성화
        map.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                cameraMoved = true;
            }
        });

        // 카메라 이동 리스너 등록
        cameraMoveListener = () -> {
            Log.d("MapCamera", "Camera moved, updating balloons...");
            updateAllBalloonPositions();
            moveMainUserViews();
        };
        // 지도 이동 중/종료 감지 설정
        googleMap.setOnCameraMoveListener(cameraMoveListener);
        googleMap.setOnCameraMoveListener(() -> {
            isMapMoving = true;
            Log.d("MapCamera", "Camera moved, updating balloons.");
            updateAllBalloonPositions(); // 기존 함수 사용
            moveMainUserViews();         // 메인 유저 UI 이동
        });
        googleMap.setOnCameraIdleListener(() -> {
            isMapMoving = false;
        });

        /*googleMap.setOnCameraIdleListener(() -> {
            isMapMoving = false;
            Log.d("BalloonUpdate", "카메라 이동 완료 → 위치 보정");
            updateAllBalloonPositions();
        });*/

        // 추가: 실시간 위치 업데이트 시작
        startLocationUpdates();


        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            } else {
                locationPermissionRequest.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }


        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {  // 추가된 코드
            if (message != null) {  // 추가된 코드
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();  // 추가된 코드
            }  // 추가된 코드
        });  // 추가된 코드

        // 현재 위치를 기준으로 핀 데이터를 가져옴
        // 현재 위치 설정
        LatLng myLocation = new LatLng(37.5665, 126.9780);

        // 동적으로 파라미터 설정
        double latitude = myLocation.latitude;
        double longitude = myLocation.longitude;
        String sort = "distance";  // 거리순 정렬
        //String kakaoId = requireActivity().getIntent().getStringExtra("kakaoId");  // 카카오 ID
        //homeViewModel.setMyKakaoId(kakaoId); // 실제 카카오 ID 저장
        //homeViewModel.fetchPins(latitude, longitude, sort, kakaoId);
        homeViewModel.getMyKakaoId().observe(getViewLifecycleOwner(), kakaoId -> {
            Log.d("MapReady", "kakaoId observed: " + kakaoId);
            if (kakaoId != null && !kakaoId.isEmpty() && !pinsAlreadyFetched) {
                Log.d("MapReady", "calling fetchPins with kakaoId: " + kakaoId);
                homeViewModel.fetchPins(latitude, longitude, sort, kakaoId);
                pinsAlreadyFetched = true; // 한 번만 호출되도록
            }
        });

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
    }

    private void checkAndUpdatePins() { // 5초마다 실행->업데이트
        Log.d("PinCheck", "checkAndUpdatePins() 실행됨");
        String myId = homeViewModel.getMyKakaoId().getValue();
        if (myId == null) return;

        LatLng currentLatLng = myCurrentLocation;
        Log.d("PinCheck", "서버에 fetchPins 요청");
        homeViewModel.fetchPins(
                currentLatLng.latitude,
                currentLatLng.longitude,
                "distance",
                myId
        );

    }

    // ✅ 추가: 실시간 위치 업데이트 메서드
    private void startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);  // 5초마다 업데이트
        locationRequest.setFastestInterval(2000);  // 최소 2초 간격

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    myCurrentLocation = new LatLng(latitude, longitude);
                    Log.d("HomeFragment", "실시간 위치 업데이트: " + latitude + ", " + longitude);

                    // cameraMoved가 false일 때만 내 위치 핀 갱신
                    if (!cameraMoved) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 15));
                        moveMainUserViews();
                    }
                    moveMainUserViews();
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    // ✅ 추가: 위치 업데이트 중지 메서드
    private void stopLocationUpdates() {
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }



    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if ((fine != null && fine) || (coarse != null && coarse)) {
                    if (googleMap != null) {
                        try {
                            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                googleMap.setMyLocationEnabled(true);
                            }
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UserRepository userRepository = new UserRepository(requireContext());
        HomeViewModelFactory factory = new HomeViewModelFactory(userRepository);
        homeViewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        observeViewModel();
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        binding.setViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        mainUserInfoBinding = binding.includeMainUserInfo;

        FrameLayout pinReactionBox = mainUserInfoBinding.getRoot().findViewById(R.id.pinReactionBox);

        // 이모지 아이콘 뷰 참조
        View pinIconGroup = mainUserInfoBinding.getRoot().findViewById(R.id.pinIconGroup);
        ImageView heart = mainUserInfoBinding.getRoot().findViewById(R.id.icon_heart);
        ImageView funny = mainUserInfoBinding.getRoot().findViewById(R.id.icon_funny);
        ImageView thumb = mainUserInfoBinding.getRoot().findViewById(R.id.icon_thumb);
        ImageView sad = mainUserInfoBinding.getRoot().findViewById(R.id.icon_sad);

        //(핀 + 프로필 불러오기)
        ImageView profileImage = mainUserInfoBinding.includeMainPin.myProfileImage;

        // // 샘플 프로필 이미지 URL
        String profileImageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/React-icon.svg/2048px-React-icon.svg.png";
        // Glide를 이용해서 프로필 이미지 불러오기
        Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.drawable.basicprofile)  // 로딩 중일 때 기본 이미지
                .error(R.drawable.basicprofile)         // 실패했을 때 기본 이미지
                .circleCrop()                            // 동그랗게 자르기
                .into(profileImage);

        TextView nicknameText = mainUserInfoBinding.mainNickname;
        EditText inputField = mainUserInfoBinding.mainMessage;

        //기본 유저아이디 관찰해서 바뀌면 업데이트
        homeViewModel.getSelectedUserId().observe(getViewLifecycleOwner(), userId -> {
            updateMainAndSubViews(userId);

            // 핀 목록에서 선택된 유저와 일치하는 핀을 찾아 닉네임과 메시지 업데이트
            List<PinResponse.Pin> pinList = homeViewModel.getPinList().getValue();
            Log.d("MapReady", "onMapReady에서 핀 리스트 확인: " + (pinList != null ? pinList.size() : 0));
            if (pinList != null) {
                for (PinResponse.Pin pin : pinList) {
                    // writerKakaoId가 userId와 같은 핀이면 해당 핀의 정보를 표시
                    if (userId != null && userId.equals(pin.getWriterKakaoId())) {
                        nicknameText.setText(pin.getWriterKakaoId());
                        inputField.setText(pin.getMessage());
                        inputField.setEnabled(false);// 메시지 업데이트
                        return;
                    }
                }
            }

            // 내 핀(myUser)의 메시지는 ViewModel에서 가져옴 → 내 ID와 내 메시지 사용
            if ("myUser".equals(userId)) {
                homeViewModel.getMyKakaoId().observe(getViewLifecycleOwner(), kakaoId -> {
                    nicknameText.setText(kakaoId);
                });
                inputField.setHint("메시지를 입력하세요 (최대 20자)");
                inputField.setEnabled(true);
                inputField.setText("");
                messageSubmitted = false;
            }

            if (userId != null && pinIconGroup != null) {
                pinIconGroup.setVisibility("myUser".equals(userId) ? View.GONE : View.VISIBLE);
            }
        });

        inputField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String message = inputField.getText().toString().trim();
                if (!messageSubmitted && !message.isEmpty()) {
                    messageSubmitted = true;

                    String kakaoId = homeViewModel.getMyKakaoId().getValue();
                    if (kakaoId == null) return false;

                    LatLng fixedLocation = myCurrentLocation;  // 🔒 고정 위치 저장

                    // 1. 서버로 등록
                    PinRequest pinRequest = new PinRequest(kakaoId, message, fixedLocation.latitude, fixedLocation.longitude);
                    HomeAPI api = RetrofitClient.getClient().create(HomeAPI.class);
                    api.postStatus(pinRequest).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "등록 완료", Toast.LENGTH_SHORT).show();

                                // 2. 입력창 초기화 및 재사용 가능 상태로 되돌림
                                inputField.setEnabled(true);           // 다시 활성화
                                inputField.setText("");                // 입력창 비움
                                messageSubmitted = false;              // 플래그 초기화
                                inputField.clearFocus();

                                // 3. ViewModel 상태 초기화 (현재 메시지는 없는 상태)
                                homeViewModel.setMyMessage("");

                                // 4. 서버에서 전체 pin 재조회
                                homeViewModel.fetchPins(
                                        fixedLocation.latitude, fixedLocation.longitude,
                                        "distance", kakaoId
                                );
                            } else {
                                Toast.makeText(getContext(), "등록 실패", Toast.LENGTH_SHORT).show();
                                messageSubmitted = false;
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            messageSubmitted = false;
                        }
                    });
                }

                return true;
            }
            return false;
        });

        //선택좌표 바뀌면 핀 이동
        homeViewModel.getSelectedUserLatLng().observe(getViewLifecycleOwner(), latLng -> {
            moveMainUserViews();
        });

        binding.getRoot().post(() -> {
            View bubbleContainer = binding.bubbleContainer;
            int[] bubbleContainerPos = new int[2];
            bubbleContainer.getLocationOnScreen(bubbleContainerPos);
            Log.d("BalloonDebug", "bubbleContainer 위치 - x: " + bubbleContainerPos[0] + ", y: " + bubbleContainerPos[1]);
        });

        return root;
    }

    private void observeViewModel() {
        homeViewModel.getMyKakaoId().observe(getViewLifecycleOwner(), myId -> {
            if (myId != null) {
                updateMainAndSubViews(myId);
            }
        });
        homeViewModel.getPinList().observe(getViewLifecycleOwner(), pins -> {
            Log.d("PinObserver", "observe triggered. pins = " + (pins != null ? pins.size() : "null"));
            if (googleMap == null) {
                Log.w("PinObserver", "googleMap is null, cannot render pins");
                return; // map 초기화 전에는 렌더링 생략
            }
            Log.d("PinObserver", "observe triggered. pins = " + (pins != null ? pins.size() : "null"));
            googleMap.clear();
            if (pins != null && !pins.isEmpty()) {
                Log.d("PinObserver", "starting to process pins");
                for (PinResponse.Pin pin : pins) {
                    if (pin.getWriterKakaoId() == null || pin.getLocation() == null ||
                            pin.getLocation().getLatitude() == 0.0 || pin.getLocation().getLongitude() == 0.0 ||
                            "myUser".equals(pin.getWriterKakaoId())) {
                        Log.w("PinFilter", "비정상 핀 필터링됨: " + pin.getId());
                        continue;
                    }

                    String userId = pin.getWriterKakaoId();
                    LatLng latLng = new LatLng(pin.getLocation().getLatitude(), pin.getLocation().getLongitude());
                    String myId = homeViewModel.getMyKakaoId().getValue();
                    String selectedId = homeViewModel.getSelectedUserId().getValue();

                    if (balloonViewMap.containsKey(userId)) {
                        removeBalloonByUserId(userId);
                    }
                    addBalloonView(latLng, pin.getMessage(), userId);
                    // 이모지 UI 확인 로그 추가
                    View balloonView = balloonViewMap.get(userId);
                    View pinView = binding.includeMainUserInfo.getRoot();
                    if (balloonView != null) {
                        View reactionBox = balloonView.findViewById(R.id.reactionBox);
                        View pinReactionBox = pinView.findViewById(R.id.pinReactionBox);
                        if (reactionBox != null) {
                            boolean isMine = userId.equals(myId);
                            boolean isSelected = userId.equals(selectedId);

                            // ✅ 선택된 유저이고 내 핀이 아닐 경우만 VISIBLE 유지
                            if (isSelected && !isMine) {
                                balloonView.setVisibility(View.GONE);
                                //reactionBox.setVisibility(View.VISIBLE);
                                pinReactionBox.setVisibility(View.VISIBLE);
                                Log.d("EmojiUI", "[PinList] 재선택된 reactionBox 유지 → userId: " + userId);
                            } else {
                                reactionBox.setVisibility(View.GONE);
                                Log.d("EmojiUI", "[PinList] reactionBox 숨김 처리 → userId: " + userId + ", isMine: " + isMine);
                            }
                        }
                    }
                }
            } else {
                addBalloonView(myCurrentLocation, "현재 위치입니다", "myUser");
                homeViewModel.setSelectedUserLatLng(myCurrentLocation);
                homeViewModel.setSelectedUserId("myUser");
                Log.d("HomeFragment", "no pins to display");
            }
            binding.bubbleContainer.post(() -> updateAllBalloonPositions()); // pin 전환 후에도 말풍선 정상 표시
        });

        // pin 전환 후 위치 보정: selectedUserId가 바뀌면 위치 보정 실행
        homeViewModel.getSelectedUserId().observe(getViewLifecycleOwner(), userId -> {
            Log.d("BalloonUpdate", "selectedUserId 변경됨: " + userId);
            String myId = homeViewModel.getMyKakaoId().getValue();
            binding.bubbleContainer.post(() -> {
                updateAllBalloonPositions();

                for (Map.Entry<String, View> entry : balloonViewMap.entrySet()) {
                    View balloon = entry.getValue();
                    View reactionBox = balloon.findViewById(R.id.reactionBox);
                    if (reactionBox != null) {
                        String userIdInMap = entry.getKey();   // 해당 말풍선의 사용자 ID


                        boolean isMine = userIdInMap != null && userIdInMap.equals(myId);
                        boolean isSelected = entry.getKey().equals(userId);
                        Log.d("EmojiUI", "선택 비교 → userIdInMap: " + userIdInMap + ", isSelected: " + isSelected + ", isMine: " + isMine);
                        Log.d("EmojiUI", userIdInMap+" "+myId);

                        if (isSelected && !isMine) {
                            reactionBox.setVisibility(View.VISIBLE);
                            Log.d("EmojiUI", "[Select] reactionBox 표시 → userId: " + (userIdInMap != null ? userIdInMap : "null"));
                        } else {
                            reactionBox.setVisibility(View.GONE);
                            Log.d("EmojiUI", "[Select] reactionBox 숨김 → userId: " + (userIdInMap != null ? userIdInMap : "null"));
                        }
                    }
                }
                // ✅ pin형으로 전환된 유저에게도 reactionBox 표시
                View pinView = binding.includeMainUserInfo.getRoot();

                View pinReactionBox = pinView.findViewById(R.id.pinReactionBox);
                if (pinReactionBox != null && !userId.equals(myId)) {
                    pinReactionBox.setVisibility(View.VISIBLE);
                    Log.d("EmojiUI", "[Select→Pin] reactionBox 표시 (pinView) → userId: " + userId);
                }

            });
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void requestLocationPermission() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void addBalloonView(LatLng latLng, String message, String userId) {
        Log.d("BalloonView", "called with: " + latLng + ", message: " + message + ", userId: " + userId);
        if (googleMap == null || binding == null) {
            Log.e("BalloonView", "googleMap or binding is null, aborting");
            return;
        }

        View balloonView = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_speech_bubble, binding.bubbleContainer, false);

        TextView messageText = balloonView.findViewById(R.id.bubbleText);
        messageText.setText(message);
        // 태그로 유저 ID 저장
        balloonView.setTag(userId);

        // 크기 측정 + 레이아웃 강제 수행 (먼저 수행해야 정확한 좌표 계산 가능)
        int containerWidth = binding.bubbleContainer.getWidth();
        int containerHeight = binding.bubbleContainer.getHeight();

        balloonView.measure(
                View.MeasureSpec.makeMeasureSpec(containerWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(containerHeight, View.MeasureSpec.AT_MOST)
        );
        balloonView.layout(0, 0, balloonView.getMeasuredWidth(), balloonView.getMeasuredHeight());

        // 처음 위치 계산 및 배치
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        Point screenPoint = googleMap.getProjection().toScreenLocation(latLng);
        params.leftMargin = screenPoint.x - balloonView.getMeasuredWidth() / 2;
        params.topMargin = screenPoint.y - balloonView.getMeasuredHeight();
        balloonView.setLayoutParams(params);
        binding.bubbleContainer.addView(balloonView); // 말풍선 컨테이너에 먼저 추가
        Log.d("BalloonView", "balloonView added to container for userId: " + userId);



        if ("myUser".equals(userId)) {
            balloonView.setVisibility(View.GONE);
            Log.w("EmojiUI", "reactionBox → GONE if (\"myUser\".equals(userId))");

        }

        balloonView.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Clicked: " + userId, Toast.LENGTH_SHORT).show();
            Log.d("BalloonClick", "Clicked userId: " + userId);
            List<PinResponse.Pin> pinList = homeViewModel.getPinList().getValue();
            if (pinList != null) {
                for (PinResponse.Pin pin : pinList) {
                    if (userId.equals(pin.getWriterKakaoId())) {
                        Log.d("BalloonClick", "Pin Data: "
                                + "lat=" + pin.getLocation().getLatitude()
                                + ", lng=" + pin.getLocation().getLongitude()
                                + ", message=" + pin.getMessage()
                                + ", id=" + pin.getWriterKakaoId());
                        break;
                    }
                }
            }

            homeViewModel.setSelectedUserId(userId);// 클릭된 말풍선의 유저를 메인으로
            homeViewModel.setSelectedUserLatLng(latLng);
        });

        int mapWidth = mapView.getWidth();
        int mapHeight = mapView.getHeight();

        Log.d("BalloonDebug", "balloonView class1: " + balloonView.getClass().getName());
        balloonViewMap.put(userId, balloonView); // ✅ 말풍선 뷰를 ID 기준으로 저장
        Log.d("BalloonDebug", "balloonView class2: " + balloonView.getClass().getName());

        Log.d("BalloonDebug", "Container size: " + containerWidth + "x" + containerHeight);
        Log.d("BalloonDebug", "MapView size: " + mapWidth + "x" + mapHeight);
    }
    //View를 화면 좌표 기준으로 직접 위치시켜주는 방식
    private void updateBalloonPosition(View balloonView, LatLng latLng) {
        if (googleMap == null || balloonView == null || latLng == null) return;

        // 좌표를 지도상의 픽셀 위치로 변환
        Point screenPoint = googleMap.getProjection().toScreenLocation(latLng);
        // 강제 측정 및 레이아웃 적용
        balloonView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        balloonView.layout(0, 0, balloonView.getMeasuredWidth(), balloonView.getMeasuredHeight());

        balloonView.setX(screenPoint.x - balloonView.getMeasuredWidth() / 2f); // 중앙 정렬
        balloonView.setY(screenPoint.y - balloonView.getMeasuredHeight());    // 말풍선 아래 기준 정렬

        //balloonView.post(() -> {
        //    Point screenPoint = googleMap.getProjection().toScreenLocation(latLng);
        //    Log.d("BalloonDebug", "screenPoint = " + screenPoint.x + ", " + screenPoint.y);
        //    balloonView.setX(screenPoint.x - balloonView.getWidth() / 2f);
        //    balloonView.setY(screenPoint.y - balloonView.getHeight() / 2f);
        //});
        // 위치 계산 및 적용


        Log.d("BalloonMove", "Updated balloon at: " + screenPoint.x + ", " + screenPoint.y + " for latLng: " + latLng.latitude + ", " + latLng.longitude);
    }

    private void removeBalloonByUserId(String userId) {
        View oldView = balloonViewMap.remove(userId);
        if (oldView != null && binding != null) {
            binding.bubbleContainer.removeView(oldView);
        }
    }

    private void updateAllBalloonPositions() {
        if (binding == null || googleMap == null) {
            Log.d("BalloonUpdate", "null");
            return;
        }

        List<PinResponse.Pin> pins = homeViewModel.getPinList().getValue();
        if (pins == null) {
            Log.d("BalloonUpdate", "noPin");
            return;
        }

        for (Map.Entry<String, View> entry : balloonViewMap.entrySet()) {
            String userId = entry.getKey();
            View balloon = entry.getValue();

            for (PinResponse.Pin pin : pins) {
                if (userId.equals(pin.getWriterKakaoId())) {
                    double lat = pin.getLocation().getLatitude();
                    double lng = pin.getLocation().getLongitude();
                    updateBalloonPosition(balloon, new LatLng(lat, lng)); // ✅ 정확한 위치 반영
                    Log.d("BalloonUpdate", "screenPoint: " + lat + " : " + lng);
                    break;
                }
            }
        }
    }


    // ✅ 핀, 닉네임, 메세지 이동
    private void moveMainUserViews() {
        if (binding == null || googleMap == null) return;

        LatLng selectedLatLng = homeViewModel.getSelectedUserLatLng().getValue();
        if (selectedLatLng == null) selectedLatLng = myCurrentLocation;

        Point screenPoint = googleMap.getProjection().toScreenLocation(selectedLatLng);
        Log.d("BalloonMove", "screenPoint: " + screenPoint + " for latLng: " + selectedLatLng);

        View mainUserView = binding.includeMainUserInfo.getRoot();
        mainUserView.setX(screenPoint.x - mainUserView.getWidth() / 2f);
        mainUserView.setY(screenPoint.y - mainUserView.getHeight() / 1.2f);
    }

    // (샘플) 사용자별 LatLng 가져오기
    private LatLng getLatLngForUserId(String userId) {
        switch (userId) {
            case "myUser": return new LatLng(37.5665, 126.9780);
            case "userA": return new LatLng(37.5675, 126.9785);
            case "userB": return new LatLng(37.5655, 126.9775);
            default: return null;
        }
    }

    private void updateMainAndSubViews(String newSelectedUserId) {
        String previousUserId = homeViewModel.getPreviousSelectedUserId().getValue();
        String myId = homeViewModel.getMyKakaoId().getValue();

        if (previousUserId != null) {
            View previousSubBalloon = findBalloonViewByUserId(previousUserId);
            if (previousSubBalloon != null) {
                previousSubBalloon.setVisibility(View.VISIBLE);// 이전 메인 -> 다시 서브 말풍선
                if (myId != null && !myId.equals(previousUserId)) {
                    View pinView = binding.includeMainUserInfo.getRoot();
                    View iconGroup = getIconGroupFromBalloon(pinView);
                    if (iconGroup != null) iconGroup.setVisibility(View.GONE); // ✅ 숨기기
                    Log.w("EmojiUI", "reactionBox → GONE if (iconGroup != null) ");

                }
            }
        }

        if (newSelectedUserId != null) {
            View newMainBalloon = findBalloonViewByUserId(newSelectedUserId);
            if (newMainBalloon != null) {
                Log.d("BalloonDebug", "Returned class: " + newMainBalloon.getClass().getName());
                newMainBalloon.setVisibility(View.GONE);  // 서브 말풍선 숨기고
                Log.w("EmojiUI", "newMainBalloon.setVisibility(View.GONE)");

                updateAllBalloonPositions();

                if (myId != null && !myId.equals(newSelectedUserId)) {
                    View pinView = binding.includeMainUserInfo.getRoot();
                    View iconGroup = getIconGroupFromBalloon(pinView);
                    setupEmojiListeners(pinView, newSelectedUserId);
                    if (iconGroup == null) {
                        Log.e("BalloonDebug", "iconGroupPin not found in main user pin view");
                    } else {
                        Log.d("BalloonDebug", "Setting iconGroupPin visible");
                        iconGroup.setVisibility(View.VISIBLE);
                    }
                }
            }

        }
    }

    private View findBalloonViewByUserId(String userId) {//말풍선 탐색
        int count = binding.bubbleContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = binding.bubbleContainer.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof String && tag.equals(userId)) {
                return child;
            }
        }
        return balloonViewMap.get(userId);
    }

    private View getIconGroupFromBalloon(View balloonView) {
        if (balloonView.findViewById(R.id.mainNickname) != null) {
            Log.d("BalloonDebug", "This is a pinView");
        } else {
            Log.d("BalloonDebug", "This is likely a bubbleView");
        }
        View pinReactionBox = balloonView.findViewById(R.id.pinReactionBox); // reactionBox 찾기
        if (pinReactionBox == null) {
            Log.e("BalloonDebug", "reactionBox not found in balloonView");
            return null;
        }

        View pinIconGroup = pinReactionBox.findViewById(R.id.pinIconGroup); // 그 안에서 iconGroupPin 찾기
        if (pinIconGroup == null) {
            Log.e("BalloonDebug", "iconGroupPin not found in pinReactionBox");
            return null;
        }

        return pinIconGroup;
    }


    private void setupEmojiListeners(View balloonView, String userId) {
        //if(balloonView == null){Log.e("EmojiUI", "balloonView null");}
        //else{Log.e("EmojiUI", balloonView.toString());}
        Log.e("EmojiUI", "set "+userId);
        View pinReactionBox = balloonView.findViewById(R.id.pinReactionBox);
        if (pinReactionBox == null) {
            Log.e("EmojiUI", "pinReactionBox null");
            return;
        }

        View iconGroup = pinReactionBox.findViewById(R.id.pinIconGroup);
        if (iconGroup == null) {
            Log.e("EmojiUI", "iconGroupPin null");
            return;
        }
        ImageView heart = iconGroup.findViewById(R.id.icon_heart);
        ImageView funny = iconGroup.findViewById(R.id.icon_funny);
        ImageView thumb = iconGroup.findViewById(R.id.icon_thumb);
        ImageView sad = iconGroup.findViewById(R.id.icon_sad);

        View.OnClickListener reactionClickListener = v -> {
            String clicked = null;
            int fullRes = 0;

            if (v.getId() == R.id.icon_heart) {
                clicked = "best"; fullRes = R.drawable.ic_heart_full;
            } else if (v.getId() == R.id.icon_funny) {
                clicked = "funny"; fullRes = R.drawable.ic_funny_full;
            } else if (v.getId() == R.id.icon_thumb) {
                clicked = "like"; fullRes = R.drawable.ic_thumb_full;
            } else if (v.getId() == R.id.icon_sad) {
                clicked = "sad"; fullRes = R.drawable.ic_sad_full;
            }//선택->clicked에 이름 저장

            if (clicked != null) {//clicked에 이름이 있으면 선택된 상태
                String currentEmoji = homeViewModel.getCurrentEmojiForUser(userId); // 현재 유저에 대한 선택된 이모지를 가져옴
                boolean isSame = clicked.equals(currentEmoji);
                //기존 이모지와 선택한 이모지 비교

                // 모두 초기화
                heart.setImageResource(R.drawable.ic_heart_empty);
                funny.setImageResource(R.drawable.ic_funny_empty);
                thumb.setImageResource(R.drawable.ic_thumb_empty);
                sad.setImageResource(R.drawable.ic_sad_empty);

                if (!isSame) {//기존 이모지와 선택한 이모지 다르면
                    ((ImageView) v).setImageResource(fullRes);//이미지 변경

                    homeViewModel.sendReactionToServer(clicked, userId); // 서버 반영
                    homeViewModel.setCurrentEmojiForUser(userId, clicked);//현재 선택한 이모지 등록
                } else {//같으면
                    currentSelectedReaction = null;
                    //userId: newSelectedUserId
                    if (homeViewModel.getCurrentEmojiForUser(userId) != null) {
                        homeViewModel.sendReactionToServer(null, userId);
                        homeViewModel.setCurrentEmojiForUser(userId, null);
                    }
                }
            }
        };

        // 각 아이콘에 리스너 연결
        heart.setOnClickListener(reactionClickListener);
        funny.setOnClickListener(reactionClickListener);
        thumb.setOnClickListener(reactionClickListener);
        sad.setOnClickListener(reactionClickListener);
    }


    private void handleReactionClick(View v, View balloonView) {
        String clicked = getReactionTypeFromViewId(v.getId());
        if (clicked == null) return;

        int fullRes = getFullIconResource(clicked);
        int emptyRes = getEmptyIconResource(clicked);

        // userId 역탐색
        String userId = null;
        for (Map.Entry<String, View> entry : balloonViewMap.entrySet()) {
            if (entry.getValue() == balloonView) {
                userId = entry.getKey();
                break;
            }
        }

        if (userId == null) {
            Log.e("Reaction", "userId를 찾을 수 없습니다.");
            return;
        }

        boolean isSame = clicked.equals(currentSelectedReaction);

        // 모든 아이콘 초기화
        ((ImageView) balloonView.findViewById(R.id.icon_heart)).setImageResource(R.drawable.ic_heart_empty);
        ((ImageView) balloonView.findViewById(R.id.icon_funny)).setImageResource(R.drawable.ic_funny_empty);
        ((ImageView) balloonView.findViewById(R.id.icon_thumb)).setImageResource(R.drawable.ic_thumb_empty);
        ((ImageView) balloonView.findViewById(R.id.icon_sad)).setImageResource(R.drawable.ic_sad_empty);

        if (!isSame) {
            ((ImageView) v).setImageResource(fullRes);
            currentSelectedReaction = clicked;
            homeViewModel.sendReactionToServer(clicked, userId);
        } else {
            currentSelectedReaction = null;
            homeViewModel.sendReactionToServer(null, userId);
        }
    }

    private String getReactionTypeFromViewId(int id) {
        if (id == R.id.icon_heart) return "heart";
        if (id == R.id.icon_funny) return "funny";
        if (id == R.id.icon_thumb) return "thumb";
        if (id == R.id.icon_sad) return "sad";
        return null;
    }

    private int getFullIconResource(String reaction) {
        switch (reaction) {
            case "heart": return R.drawable.ic_heart_full;
            case "funny": return R.drawable.ic_funny_full;
            case "thumb": return R.drawable.ic_thumb_full;
            case "sad": return R.drawable.ic_sad_full;
            default: return 0;
        }
    }

    private int getEmptyIconResource(String reaction) {
        switch (reaction) {
            case "heart": return R.drawable.ic_heart_empty;
            case "funny": return R.drawable.ic_funny_empty;
            case "thumb": return R.drawable.ic_thumb_empty;
            case "sad": return R.drawable.ic_sad_empty;
            default: return 0;
        }
    }


    private void showMyPinUI() {
        String myId = homeViewModel.getMyKakaoId().getValue();
        if (myId == null) {
            Log.w("HomeFragment", "showMyPinUI(): myKakaoId is null");
            return;
        }

        // 내 pin을 메인으로 전환
        homeViewModel.setSelectedUserId(myId);
        homeViewModel.setSelectedUserLatLng(myCurrentLocation);

        // 말풍선 위치 재조정
        moveMainUserViews();

        // 닉네임 및 메시지 UI 갱신 (onCreateView에서 observe에 있는 것과 유사)
        mainUserInfoBinding.mainNickname.setText(myId);
        mainUserInfoBinding.mainMessage.setHint("메시지를 입력하세요 (최대 20자)");
        mainUserInfoBinding.mainMessage.setEnabled(true);
        mainUserInfoBinding.mainMessage.setText("");
        currentSelectedReaction = null;

        // 이모지 박스 숨기기
        View pinReactionBox = mainUserInfoBinding.getRoot().findViewById(R.id.pinReactionBox);
        if (pinReactionBox != null) pinReactionBox.setVisibility(View.GONE);
        Log.w("EmojiUI", "pinReactionBox → GONE if (pinReactionBox != null)");


        Log.d("HomeFragment", "showMyPinUI(): 내 pin으로 복원됨");
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        isMapMoving = false;
        cameraMoved = false;
        Log.d("PinCheck", "onResume 호출됨 - 핀 점검 시작"); // 로그 추가
        if (googleMap != null) {
            Log.d("PinCheck", "GoogleMap 준비됨 - 핀 점검 시작");
            pinCheckHandler.post(pinCheckRunnable); // 5초 루프 시작(지도 로딩 기다린다)
        } else {
            Log.w("PinCheck", "GoogleMap 아직 준비되지 않음 - 핀 점검 지연");
            // 지연 초기화 시도를 onMapReady에서 수행
        }
        // LiveData에서 내 Kakao ID 받아오기
        String myId = homeViewModel.getMyKakaoId().getValue();
        if (myId == null) {
            Log.w("HomeFragment", "myKakaoId is null in onResume");
            return;
        }
        // 현재 선택된 핀이 내 것이 아니라면 복원
        if (!currentPinUserId.equals(myId)) {
            showMyPinUI();
        }

        pinCheckHandler.postDelayed(pinCheckRunnable, 5000); // 핀 갱신 시작
        startLocationUpdates();  // 추가: 위치 업데이트 재개
    }
    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();  // 수정: 위치 업데이트 중지
        pinCheckHandler.removeCallbacks(pinCheckRunnable); // 핀 갱신 중단
        mapView.onPause();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        stopLocationUpdates();  // ✅ 수정: 위치 업데이트 중지
        if (mapView != null) {
            mapView.onDestroy();
        }
        binding = null;
        // ✅ 권한 체크 및 예외 처리 추가
        if (googleMap != null) {
            try {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(false);
                    Log.d("HomeFragment", "위치 권한 허용 상태에서 MyLocation 비활성화 성공");
                } else {
                    Log.w("HomeFragment", "위치 권한이 없어 MyLocation 비활성화 불가");
                }
            } catch (SecurityException e) {
                Log.e("HomeFragment", "위치 권한 설정 중 오류 발생: " + e.getMessage());
            }
        }
        binding = null;
    }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}
