package com.example.sleepshaker.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class LightSensorView extends View {
    private Paint backgroundPaint;
    private Paint lightMeterPaint;
    private Paint needlePaint;
    private Paint textPaint;
    private Paint glowPaint;

    private float currentLux = 0f;
    private static final float TARGET_LUX = 500f;
    private static final float MAX_DISPLAY_LUX = 1000f;

    private RectF arcRect;

    public LightSensorView(Context context) {
        super(context);
        init();
    }

    public LightSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LightSensorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Background paint
        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);

        // Light meter arc paint
        lightMeterPaint = new Paint();
        lightMeterPaint.setStyle(Paint.Style.STROKE);
        lightMeterPaint.setStrokeWidth(25);
        lightMeterPaint.setAntiAlias(true);
        lightMeterPaint.setStrokeCap(Paint.Cap.ROUND);

        // Needle paint
        needlePaint = new Paint();
        needlePaint.setColor(Color.parseColor("#D32F2F"));
        needlePaint.setStrokeWidth(8);
        needlePaint.setAntiAlias(true);
        needlePaint.setStrokeCap(Paint.Cap.ROUND);

        // Text paint
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Glow effect paint
        glowPaint = new Paint();
        glowPaint.setAntiAlias(true);
        glowPaint.setStyle(Paint.Style.FILL);

        arcRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int padding = 60;
        arcRect.set(padding, padding, w - padding, h - padding);

        // Create gradient for light meter
        int[] colors = {
                Color.parseColor("#1A237E"), // Dark blue (low light)
                Color.parseColor("#3F51B5"), // Blue
                Color.parseColor("#2196F3"), // Light blue
                Color.parseColor("#00BCD4"), // Cyan
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#FFEB3B"), // Yellow
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#F44336")  // Red (bright light)
        };

        LinearGradient gradient = new LinearGradient(
                0, 0, w, 0,
                colors,
                null,
                Shader.TileMode.CLAMP
        );
        lightMeterPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(centerX, centerY) - 60;

        // Draw dark background circle
        backgroundPaint.setColor(Color.parseColor("#212121"));
        backgroundPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, radius + 20, backgroundPaint);

        // Draw glow effect if light is detected
        if (currentLux > 10) {
            float glowIntensity = Math.min(currentLux / TARGET_LUX, 1.0f);
            int glowAlpha = (int) (glowIntensity * 100);

            // Create radial gradient for glow
            RadialGradient radialGradient = new RadialGradient(
                    centerX, centerY, radius + 40,
                    Color.argb(glowAlpha, 255, 255, 0), // Yellow glow
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
            );
            glowPaint.setShader(radialGradient);
            canvas.drawCircle(centerX, centerY, radius + 40, glowPaint);
        }

        // Draw light meter arc (semicircle)
        canvas.drawArc(arcRect, 180, 180, false, lightMeterPaint);

        // Draw scale marks
        Paint scalePaint = new Paint();
        scalePaint.setColor(Color.WHITE);
        scalePaint.setStrokeWidth(3);
        scalePaint.setAntiAlias(true);

        for (int i = 0; i <= 10; i++) {
            double angle = Math.toRadians(180 + i * 18); // 180 degrees spread
            int startRadius = radius - 15;
            int endRadius = radius - 5;

            float startX = (float) (centerX + Math.cos(angle) * startRadius);
            float startY = (float) (centerY + Math.sin(angle) * startRadius);
            float endX = (float) (centerX + Math.cos(angle) * endRadius);
            float endY = (float) (centerY + Math.sin(angle) * endRadius);

            canvas.drawLine(startX, startY, endX, endY, scalePaint);
        }

        // Draw needle based on current lux
        float luxRatio = Math.min(currentLux / MAX_DISPLAY_LUX, 1.0f);
        double needleAngle = Math.toRadians(180 + luxRatio * 180);

        float needleEndX = (float) (centerX + Math.cos(needleAngle) * (radius - 30));
        float needleEndY = (float) (centerY + Math.sin(needleAngle) * (radius - 30));

        canvas.drawLine(centerX, centerY, needleEndX, needleEndY, needlePaint);

        // Draw center dot
        Paint centerPaint = new Paint();
        centerPaint.setColor(Color.parseColor("#D32F2F"));
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, 12, centerPaint);

        // Draw current lux value
        textPaint.setTextSize(36);
        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(true);
        String luxText = String.format("%.0f LUX", currentLux);
        canvas.drawText(luxText, centerX, centerY + radius - 20, textPaint);

        // Draw target indicator
        textPaint.setTextSize(20);
        textPaint.setColor(Color.parseColor("#4CAF50"));
        String targetText = String.format("Target: %.0f LUX", TARGET_LUX);
        canvas.drawText(targetText, centerX, centerY + radius + 10, textPaint);

        // Draw instruction
        textPaint.setTextSize(18);
        if (currentLux >= TARGET_LUX) {
            textPaint.setColor(Color.parseColor("#4CAF50"));
            canvas.drawText("BRIGHT ENOUGH!", centerX, centerY + radius + 40, textPaint);
        } else {
            textPaint.setColor(Color.parseColor("#FF9800"));
            canvas.drawText("Point phone toward light source", centerX, centerY + radius + 40, textPaint);
        }
    }

    public void updateLux(float lux) {
        this.currentLux = lux;
        invalidate(); // Redraw
    }

    public float getCurrentLux() {
        return currentLux;
    }

    public boolean isTargetReached() {
        return currentLux >= TARGET_LUX;
    }

    // Add pulse animation when target is reached
    public void addSuccessAnimation() {
        animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(200)
                .withEndAction(() ->
                        animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(200)
                                .start()
                )
                .start();
    }
}