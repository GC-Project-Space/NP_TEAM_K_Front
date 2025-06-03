package com.example.np_team_k.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.github.mikephil.charting.data.BarData;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.Locale;

public class RoundedBarChartRenderer extends BarChartRenderer {
    private final float mRadius;

    public RoundedBarChartRenderer(BarChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler, float radius) {
        super(chart, animator, viewPortHandler);
        mRadius = radius;
    }

    @Override
    public void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        BarChart barChart = (BarChart) mChart;
        BarData barData = barChart.getBarData();
        mRenderPaint.setColor(dataSet.getColor());

        float phaseY = mAnimator.getPhaseY();

        for (int i = 0; i < dataSet.getEntryCount(); i++) {
            BarEntry entry = dataSet.getEntryForIndex(i);
            float x = entry.getX();
            float y = entry.getY() * phaseY;

            float barWidth = barData.getBarWidth();
            float left = x - barWidth / 2f;
            float right = x + barWidth / 2f;
            float top = Math.max(y, 0);
            float bottom = Math.min(y, 0);

            mBarRect.set(left, bottom, right, top);
            mChart.getTransformer(dataSet.getAxisDependency()).rectToPixelPhase(mBarRect, phaseY);

            c.drawRoundRect(mBarRect, 10f, 10f, mRenderPaint);

            String valueText = String.format(Locale.getDefault(), "%.0f회", entry.getY());
            float textX = (mBarRect.left + mBarRect.right) / 2f;
            float textY = mBarRect.top - 10f;

            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(dataSet.getValueTextColor());
            textPaint.setTextSize(36f);

            textPaint.setTextAlign(Paint.Align.CENTER);

            c.drawText(valueText, textX, textY, textPaint);
        }
    }

    @Override
    public void drawValues(Canvas c) {
    }
}
