package com.example.np_team_k.ui.home;

import static com.example.np_team_k.BR.viewModel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.np_team_k.R;
import com.example.np_team_k.databinding.FragmentHomeBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentHomeBinding binding;

    private MapView mapView;
    private GoogleMap googleMap;

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        googleMap.getUiSettings().setZoomControlsEnabled(true);

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

        //지도 좌표(LatLng)를 스크린 좌표(point)로 변환
        //sample 실제 데이터 받을 때는 loadMessagesFromViewModel();
        LatLng sampleLatLng = new LatLng(37.5665, 126.9780);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleLatLng, 15));
        addBalloonView(sampleLatLng, "안녕하세요!");

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
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        binding.setViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return root;
    }

    private void requestLocationPermission() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void addBalloonView(LatLng latLng, String message) {
        if (googleMap == null || binding == null) return;

        View balloonView = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_speech_bubble, binding.bubbleContainer, false);

        TextView messageText = balloonView.findViewById(R.id.bubbleText);
        messageText.setText(message);

        // 처음 위치 계산 및 배치
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        binding.bubbleContainer.addView(balloonView); // 먼저 추가

        //View를 화면 좌표 기준으로 직접 위치시켜주는 방식
        Runnable updateBalloonPosition = () -> {
            Point screenPoint = googleMap.getProjection().toScreenLocation(latLng);
            balloonView.setX(screenPoint.x);
            balloonView.setY(screenPoint.y);
            Log.d("HomeFragment", "🛰️ 말풍선 위치 갱신됨: x=" + screenPoint.x + ", y=" + screenPoint.y);
        };

        // 초기 위치 설정
        updateBalloonPosition.run();

        // 지도 카메라 이동 시 위치 업데이트, 지도가 움직일 때마다 updateBalloonPosition을 호출
        googleMap.setOnCameraMoveListener(() -> updateBalloonPosition.run());
    }



    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDestroy();
        }
        binding = null;
    }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}
