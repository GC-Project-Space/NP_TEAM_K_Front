package com.example.np_team_k.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.np_team_k.network.PinResponse;
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

import com.bumptech.glide.Glide;


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


    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // 카메라 이동 리스너 등록
        cameraMoveListener = () -> {
            updateAllBalloonPositions();
            moveMainUserViews();
        };
        googleMap.setOnCameraMoveListener(cameraMoveListener);

        // ✅ 추가: 실시간 위치 업데이트 시작
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

        homeViewModel.getPinList().observe(getViewLifecycleOwner(), pins -> {
            if (pins != null && !pins.isEmpty()) {
                boolean hasValidPin = false;

                for (PinResponse.Pin pin : pins) {
                    double lat = pin.getLatitude();
                    double lng = pin.getLongitude();

                    // ✅ 수정: 좌표 유효성 검사 추가
                    if (lat != 0.0 && lng != 0.0) {
                        addBalloonView(new LatLng(lat, lng), pin.getMessage(), pin.getWriterKakaoId());
                        hasValidPin = true;
                    } else {
                        Log.e("HomeFragment", "잘못된 좌표: " + lat + ", " + lng);
                    }
                }

                // ✅ 수정: 유효한 핀이 없을 경우 내 위치에 기본 핀 추가
                if (!hasValidPin) {
                    addBalloonView(myCurrentLocation, "현재 위치입니다", "myUser");
                    homeViewModel.setSelectedUserLatLng(myCurrentLocation);
                    homeViewModel.setSelectedUserId("myUser");
                }
            } else {
                // ✅ 수정: 핀이 없을 경우 내 위치에 기본 핀 추가
                addBalloonView(myCurrentLocation, "현재 위치입니다", "myUser");
                homeViewModel.setSelectedUserLatLng(myCurrentLocation);
                homeViewModel.setSelectedUserId("myUser");
            }
        });




        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {  // 추가된 코드
            if (message != null) {  // 추가된 코드
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();  // 추가된 코드
            }  // 추가된 코드
        });  // 추가된 코드

        // 현재 위치를 기준으로 핀 데이터를 가져옴
        // 현재 위치 설정
        LatLng myLocation = new LatLng(37.5665, 126.9780);

// ✅ 동적으로 파라미터 설정
        double latitude = myLocation.latitude;
        double longitude = myLocation.longitude;
        String sort = "distance";  // 거리순 정렬
        String kakaoId = "1234";  // 카카오 ID
        homeViewModel.fetchPins(latitude, longitude, sort, kakaoId);  // 추가된 코드

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
        /* //작동 테스트용 데이터
        //지도 좌표(LatLng)를 스크린 좌표(point)로 변환
        //sample 실제 데이터 받을 때는 loadMessagesFromViewModel();
        LatLng myLocation = new LatLng(37.5665, 126.9780);
        LatLng userALocation = new LatLng(37.5675, 126.9785);
        LatLng userBLocation = new LatLng(37.5655, 126.9775);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
        addBalloonView(myLocation, "내 메시지", "myUser");
        addBalloonView(userALocation, "userA 메시지", "userA");
        addBalloonView(userBLocation, "userB 메시지", "userB");

        homeViewModel.setSelectedUserLatLng(myLocation);
        homeViewModel.setSelectedUserId("myUser");*/
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

                    // ✅ 수정: 내 위치 핀 갱신
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 15));
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
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        binding.setViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        mainUserInfoBinding = binding.includeMainUserInfo;

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

        //기본 유저아이디 관찰해서 바뀌면 업데이트
        homeViewModel.getSelectedUserId().observe(getViewLifecycleOwner(), userId -> {
            updateMainAndSubViews(userId);
        });

        //선택좌표 바뀌면 핀 이동
        homeViewModel.getSelectedUserLatLng().observe(getViewLifecycleOwner(), latLng -> {
            moveMainUserViews();
        });

        return root;
    }

    private void requestLocationPermission() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void addBalloonView(LatLng latLng, String message, String userId) {
        if (googleMap == null || binding == null) return;

        View balloonView = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_speech_bubble, binding.bubbleContainer, false);

        TextView messageText = balloonView.findViewById(R.id.bubbleText);
        messageText.setText(message);

        balloonView.setTag(userId);

        // 처음 위치 계산 및 배치
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        balloonView.setLayoutParams(params);
        binding.bubbleContainer.addView(balloonView); // 먼저 추가

        updateBalloonPosition(balloonView, latLng);

        if (userId.equals("myUser")) {
            balloonView.setVisibility(View.GONE);
        }

        balloonView.setOnClickListener(v -> {
            homeViewModel.setSelectedUserId(userId);// 클릭된 말풍선의 유저를 메인으로
            homeViewModel.setSelectedUserLatLng(latLng);
        });
    }
    //View를 화면 좌표 기준으로 직접 위치시켜주는 방식
    private void updateBalloonPosition(View balloonView, LatLng latLng) {
        if (googleMap == null) return;
        balloonView.post(() -> {
            Point screenPoint = googleMap.getProjection().toScreenLocation(latLng);
            balloonView.setX(screenPoint.x - balloonView.getWidth() / 2f);
            balloonView.setY(screenPoint.y - balloonView.getHeight() / 2f);
        });
    }

    // 말풍선 + 핀/닉네임 모두 이동
    private void updateAllBalloonPositions() {
        if (binding == null || googleMap == null) return;

        int count = binding.bubbleContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View balloon = binding.bubbleContainer.getChildAt(i);
            Object tag = balloon.getTag();
            if (tag instanceof String) {
                LatLng latLng = getLatLngForUserId((String) tag);
                if (latLng != null) {
                    // GONE 상태면 계산이 안됨->강제로 레이아웃 한번 해서 계산
                    balloon.measure(
                            View.MeasureSpec.UNSPECIFIED,
                            View.MeasureSpec.UNSPECIFIED
                    );
                    balloon.layout(0, 0, balloon.getMeasuredWidth(), balloon.getMeasuredHeight());

                    updateBalloonPosition(balloon, latLng);
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

        if (previousUserId != null) {
            View previousSubBalloon = findBalloonViewByUserId(previousUserId);
            if (previousSubBalloon != null) {
                previousSubBalloon.setVisibility(View.VISIBLE);  // 이전 메인 -> 다시 서브 말풍선
            }
        }

        if (newSelectedUserId != null) {
            View newMainBalloon = findBalloonViewByUserId(newSelectedUserId);
            if (newMainBalloon != null) {
                newMainBalloon.setVisibility(View.GONE);  // 서브 말풍선 숨기고
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
        return null;
    }






    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        startLocationUpdates();  // ✅ 추가: 위치 업데이트 재개
    }
    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();  // ✅ 수정: 위치 업데이트 중지
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
