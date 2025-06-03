package com.example.np_team_k.ui.list;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.np_team_k.network.HomeAPI;
import com.example.np_team_k.network.MyPageAPI;
import com.example.np_team_k.network.PinResponse;
import com.example.np_team_k.network.RetrofitClient;
import com.example.np_team_k.network.StatusMineResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListPageViewModel extends ViewModel {

    private final MutableLiveData<List<ListItem>> itemList = new MutableLiveData<>();

    public ListPageViewModel() {
    }

    public LiveData<List<ListItem>> getItemList() {
        return itemList;
    }

    // api 연결
    public void fetchStatusList(double lat, double lon, String sort, String kakaoId) {
        Log.d("ViewModel", "fetchStatusList() 호출됨");

        HomeAPI api = RetrofitClient.getClient().create(HomeAPI.class);
        api.getPins(lat, lon, sort, kakaoId).enqueue(new Callback<List<PinResponse.Pin>>() {
            @Override
            public void onResponse(Call<List<PinResponse.Pin>> call, Response<List<PinResponse.Pin>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PinResponse.Pin> pins = response.body();
                    List<ListItem> convertedList = new ArrayList<>();

                    for (PinResponse.Pin pin : pins) {
                        ListItem item = new ListItem(pin.getId(), pin.getMessage());
                        item.setSelectedReaction(pin.getMyReaction());  // 초기 리액션 상태도 반영
                        convertedList.add(item);
                    }
                    Log.e("ViewModel", "API 응답:" + convertedList);

                    itemList.setValue(convertedList);  // LiveData 갱신
                } else {
                    Log.e("ViewModel", "API 응답 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PinResponse.Pin>> call, Throwable t) {
                // 실패 시: 필요하면 로그 출력
            }
        });
    }

    public void updateReaction(String itemId, String reaction) {
        if (itemList.getValue() == null) return;

        List<ListItem> updated = new ArrayList<>();
        for (ListItem item : itemList.getValue()) {
            if (item.getId().equals(itemId)) {
                item.setSelectedReaction(
                        reaction.equals(item.getSelectedReaction()) ? null : reaction
                );
            }
            updated.add(item);
        }
        itemList.setValue(updated);  // LiveData 갱신
    }
}
