package com.example.np_team_k.ui.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.np_team_k.domain.model.StatusResponse;
import com.example.np_team_k.network.RetrofitClient;
import com.example.np_team_k.network.StatusAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListPageViewModel extends ViewModel {

    private final MutableLiveData<List<ListItem>> listItems = new MutableLiveData<>();
    private final MutableLiveData<List<ListItem>> sortedList = new MutableLiveData<>();

    public LiveData<List<ListItem>> getListItems() {
        return listItems;
    }

    public LiveData<List<ListItem>> getSortedList() {
        return sortedList;
    }

    public void fetchStatusList(double lat, double lng, String sort, String kakaoId) {
        StatusAPI api = RetrofitClient.getClient().create(StatusAPI.class);

        api.getStatusList(lat, lng, sort, kakaoId).enqueue(new Callback<List<StatusResponse>>() {
            @Override
            public void onResponse(Call<List<StatusResponse>> call, Response<List<StatusResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ListItem> items = new ArrayList<>();
                    for (StatusResponse res : response.body()) {
                        int likes = res.getReactionCounts().getLike()
                                + res.getReactionCounts().getFunny()
                                + res.getReactionCounts().getSad()
                                + res.getReactionCounts().getBest();

                        double distance = calculateDistance(lat, lng,
                                res.getLocation().getLatitude(),
                                res.getLocation().getLongitude());

                        items.add(new ListItem(res.getMessage(), likes, distance, res.getMyReaction()));
                    }

                    listItems.setValue(items);
                    applySort(items, sort);  // 🔥 정렬된 결과를 sortedList에 저장
                }
            }

            @Override
            public void onFailure(Call<List<StatusResponse>> call, Throwable t) {
                // 실패 로그 찍기
            }
        });
    }

    private void applySort(List<ListItem> items, String sort) {
        List<ListItem> sorted = new ArrayList<>(items);
        if ("popular".equals(sort)) {
            Collections.sort(sorted, (a, b) -> Integer.compare(b.getLikes(), a.getLikes()));
        } else if ("distance".equals(sort)) {
            Collections.sort(sorted, Comparator.comparingDouble(ListItem::getDistance));
        }
        sortedList.setValue(sorted);
    }

    // 위도, 경도 거리 계산 (Haversine 공식)
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
