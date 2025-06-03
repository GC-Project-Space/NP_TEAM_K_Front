package com.example.np_team_k.ui.my;

import com.example.np_team_k.network.ReportAPI;
import com.example.np_team_k.network.EmotionResponse;
import com.example.np_team_k.network.ReactionResponse;
import com.example.np_team_k.network.WeeklyActivityResponse;
import com.example.np_team_k.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.np_team_k.R;
import com.example.np_team_k.ui.RoundedBarChartRenderer;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class StatusReportActivity extends AppCompatActivity {

    private PieChart statusPieChart;
    private PieChart empathyPieChart;
    private BarChart activityBarChart;

    private String kakaoId = "1234";
    private ReportAPI reportAPI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_report);

        statusPieChart = findViewById(R.id.statusPieChart);
        empathyPieChart = findViewById(R.id.empathyPieChart);
        activityBarChart = findViewById(R.id.activityBarChart);

        reportAPI = RetrofitClient.getClient().create(ReportAPI.class);

        getEmotionData();
        getReactionData();
        getActivityData();

    }

    private void getEmotionData() {
        reportAPI.getEmotion(kakaoId).enqueue(new Callback<EmotionResponse>() {
            @Override
            public void onResponse(Call<EmotionResponse> call, Response<EmotionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmotionResponse emotionResponse = response.body();
                    int total = emotionResponse.getTotal();
                    EmotionResponse.EmotionCounts emotionCounts = emotionResponse.getEmotionCounts();

                    if (emotionCounts != null) {
                        Map<String, Integer> counts = new HashMap<>();
                        counts.put("슬픔", emotionCounts.getSad());
                        counts.put("불안", emotionCounts.getAnxious());
                        counts.put("행복", emotionCounts.getHappy());
                        counts.put("놀람", emotionCounts.getSurprise());
                        counts.put("외로움", emotionCounts.getLonely());
                        counts.put("화남", emotionCounts.getAngry());

                        updateStatusPieChart(counts, total);
                    } else {
                        Log.e("API", "EmotionCounts가 null입니다.");
                    }

                    Log.d("API", "응답 성공 여부: " + response.isSuccessful());
                    Log.d("API", "응답: " + response.toString());
                } else {
                    Log.e("API", "응답 실패");
                    Log.d("API", "응답 성공 여부: " + response.isSuccessful());
                    Log.d("API", "응답: " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<EmotionResponse> call, Throwable t) {
                Log.e("API", "네트워크 오류: " + t.getMessage());
            }
        });
    }




    // 감정 차트 수정 - 0이면 표시 안되게
    private void updateStatusPieChart(Map<String, Integer> counts, int total) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Integer value = entry.getValue();
            if (value != null && value != 0) {
                entries.add(new PieEntry(value, entry.getKey()));
            }
        }

        // PieDataSet 설정
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#04bfda"), // 슬픔
                Color.parseColor("#9b88ed"), // 불안
                Color.parseColor("#fb67ca"), // 행복
                Color.parseColor("#ffa84a"), // 놀람
                Color.parseColor("#5dca18"), // 외로움
                Color.parseColor("#F85C5C")  // 화남
        );

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f%%", value);
            }
        });

        statusPieChart.setUsePercentValues(true);
        statusPieChart.setData(data);

        SpannableString centerText = new SpannableString("상태 공유 횟수\n" + total);
        centerText.setSpan(new RelativeSizeSpan(1.6f), 8, centerText.length(), 0); // 숫자 크게
        centerText.setSpan(new StyleSpan(Typeface.BOLD), 8, centerText.length(), 0); // 숫자 굵게
        statusPieChart.setCenterText(centerText);

        statusPieChart.setDrawEntryLabels(false);
        statusPieChart.getDescription().setEnabled(false);
        statusPieChart.getLegend().setEnabled(false);
        statusPieChart.setHoleRadius(60f);
        statusPieChart.setTransparentCircleRadius(0f);

        statusPieChart.invalidate();
        setupStatusLegend();

    }

    private void setupStatusLegend() {
        LinearLayout legendLayout = findViewById(R.id.statusLegendLayout);
        legendLayout.removeAllViews(); // 중복 제거

        String[] labels = {"슬픔", "불안", "행복", "놀람", "외로움", "화남"};
        String[] colors = {"#04bfda", "#9b88ed", "#fb67ca", "#ffa84a", "#5dca18", "#F85C5C"};

        for (int i = 0; i < labels.length; i++) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setPadding(12, 8, 12, 8);
            item.setGravity(Gravity.CENTER_VERTICAL);

            View colorDot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(24, 24);
            dotParams.setMargins(0, 0, 8, 0);
            colorDot.setLayoutParams(dotParams);

            GradientDrawable circle = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.legend_circle).mutate();
            circle.setColor(Color.parseColor(colors[i]));
            colorDot.setBackground(circle);

            TextView label = new TextView(this);
            label.setText(labels[i]);
            label.setTextSize(14f);
            label.setTextColor(Color.DKGRAY);

            item.addView(colorDot);
            item.addView(label);

            legendLayout.addView(item);
        }
    }

    private void getReactionData() {
        reportAPI.getReaction(kakaoId).enqueue(new Callback<ReactionResponse>() {
            @Override
            public void onResponse(Call<ReactionResponse> call, Response<ReactionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReactionResponse reactionResponse = response.body();

                    int total = reactionResponse.getTotal();
                    Map<String, Integer> counts = reactionResponse.getReceivedReactions();

                    updateEmpathyPieChart(counts, total);
                } else {
                    Log.e("API", "공감 API 응답 실패");
                }
            }

            @Override
            public void onFailure(Call<ReactionResponse> call, Throwable t) {
                Log.e("API", "공감 API 네트워크 오류: " + t.getMessage());
            }
        });
    }
    private void updateEmpathyPieChart(Map<String, Integer> counts, int total) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Integer value = entry.getValue();
            if (value != null && value != 0) {
                entries.add(new PieEntry(value, entry.getKey()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#04bfda"),
                Color.parseColor("#9b88ed"),
                Color.parseColor("#fb67ca"),
                Color.parseColor("#ffa84a")
        );

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f%%", value);
            }
        });

        empathyPieChart.setUsePercentValues(true);
        empathyPieChart.setData(data);
        empathyPieChart.setDrawEntryLabels(false);
        empathyPieChart.getDescription().setEnabled(false);
        empathyPieChart.getLegend().setEnabled(false);
        empathyPieChart.setHoleRadius(0f);
        empathyPieChart.setTransparentCircleRadius(0f);
        empathyPieChart.invalidate();

        TextView empathyCenterText = findViewById(R.id.empathyCenterText);
        SpannableString empathyText = new SpannableString("총 받은 공감수\n" + total);
        empathyText.setSpan(new RelativeSizeSpan(1.6f), 9, empathyText.length(), 0);
        empathyText.setSpan(new StyleSpan(Typeface.BOLD), 9, empathyText.length(), 0);
        empathyCenterText.setText(empathyText);

        setupEmpathyLegend();
    }

    private void setupEmpathyLegend() {
        LinearLayout legendLayout = findViewById(R.id.empathyLegendLayout);
        legendLayout.removeAllViews();

        String[] labels = {"공감해요", "슬퍼요", "최고", "웃겨요"};
        String[] colors = {"#04bfda", "#9b88ed", "#fb67ca", "#ffa84a"};

        for (int i = 0; i < labels.length; i++) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setPadding(12, 8, 12, 8);
            item.setGravity(Gravity.CENTER_VERTICAL);

            View colorDot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(24, 24);
            dotParams.setMargins(0, 0, 8, 0);
            colorDot.setLayoutParams(dotParams);

            GradientDrawable circle = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.legend_circle).mutate();
            circle.setColor(Color.parseColor(colors[i]));
            colorDot.setBackground(circle);

            TextView label = new TextView(this);
            label.setText(labels[i]);
            label.setTextSize(14f);
            label.setTextColor(Color.DKGRAY);

            item.addView(colorDot);
            item.addView(label);

            legendLayout.addView(item);
        }
    }





    private void getActivityData() {
        reportAPI.getWeeklyActivity(kakaoId).enqueue(new Callback<WeeklyActivityResponse>() {
            @Override
            public void onResponse(Call<WeeklyActivityResponse> call, Response<WeeklyActivityResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeeklyActivityResponse weeklyActivityResponse = response.body();

                    updateActivityBarChart(weeklyActivityResponse);

                } else {
                    Log.e("API", "Activity API 응답 실패");
                }
            }

            @Override
            public void onFailure(Call<WeeklyActivityResponse> call, Throwable t) {
                Log.e("API", "Activity API 네트워크 오류: " + t.getMessage());
            }
        });
    }

    private void updateActivityBarChart(WeeklyActivityResponse response) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, response.getMon()));
        entries.add(new BarEntry(1, response.getTue()));
        entries.add(new BarEntry(2, response.getWed()));
        entries.add(new BarEntry(3, response.getThu()));
        entries.add(new BarEntry(4, response.getFri()));
        entries.add(new BarEntry(5, response.getSat()));
        entries.add(new BarEntry(6, response.getSun()));

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#FF9800")); // 막대 색상
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return (int) barEntry.getY() + "회";
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        activityBarChart.setData(data);
        activityBarChart.setFitBars(true);
        activityBarChart.getDescription().setEnabled(false);
        activityBarChart.getLegend().setEnabled(false);
        activityBarChart.setExtraBottomOffset(10f);

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        XAxis xAxis = activityBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(days.length);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.DKGRAY);

        YAxis leftAxis = activityBarChart.getAxisLeft();
        leftAxis.setEnabled(false);
        activityBarChart.getAxisRight().setEnabled(false);

        activityBarChart.setRenderer(new RoundedBarChartRenderer(
                activityBarChart,
                activityBarChart.getAnimator(),
                activityBarChart.getViewPortHandler(),
                30f
        ));

        activityBarChart.invalidate();
    }



}
