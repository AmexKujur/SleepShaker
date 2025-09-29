package com.example.sleepshaker.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class StepCounterView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private Paint footstepPaint;
    private Paint pathPaint;

    private int currentSteps = 0;
    private int targetSteps = 20;

    private List<FootstepIcon> footsteps;
    private RectF progressRect;

    // Footstep animation
    private float animationProgress = 0f;
    private boolean isAnimating = false;

    public StepCounterView(Context context) {
        super(context);
        init();
    }

    public StepCounterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StepCounterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Background paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#F5F5F5"));
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);

        // Progress bar paint
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Text paint
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Footstep icon paint
        footstepPaint = new Paint();
        footstepPaint.setAntiAlias(true);
        footstepPaint.setStyle(Paint.Style.FILL);

        // Walking path paint
        pathPaint = new Paint();
        pathPaint.setColor(Color.parseColor("#E0E0E0"));
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(8);
        pathPaint.setAntiAlias(true);

        footsteps = new ArrayList<>();
        progressRect = new RectF();

        // Initialize footstep positions
        initializeFootsteps();
    }

    private void initializeFootsteps() {
        footsteps.clear();
        // Create a walking path with alternating left/right footsteps
        int stepsToShow = Math.min(targetSteps, 10); // Show max 10 footsteps visually

        for (int i = 0; i < stepsToShow; i++) {
            float progress = (float) i / (stepsToShow - 1);
            boolean isLeft = i % 2 == 0;
            footsteps.add(new FootstepIcon(progress, isLeft));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Progress bar rectangle
        int progressBarHeight = 20;
        int margin = 40;
        progressRect.set(margin, h - 100, w - margin, h - 100 + progressBarHeight);

        initializeFootsteps();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;

        // Draw background
        canvas.drawRoundRect(0, 0, width, height, 20, 20, backgroundPaint);

        // Draw walking path (curved line)
        drawWalkingPath(canvas, width, height);

        // Draw footsteps
        drawFootsteps(canvas, width, height);

        // Draw step counter (big number)
        textPaint.setTextSize(72);
        textPaint.setColor(Color.parseColor("#2E7D32"));
        textPaint.setFakeBoldText(true);
        String stepText = String.valueOf(currentSteps);
        canvas.drawText(stepText, centerX, height / 2 - 40, textPaint);

        // Draw "STEPS" label
        textPaint.setTextSize(24);
        textPaint.setColor(Color.parseColor("#616161"));
        textPaint.setFakeBoldText(false);
        canvas.drawText("STEPS", centerX, height / 2 - 10, textPaint);

        // Draw target info
        textPaint.setTextSize(20);
        textPaint.setColor(Color.parseColor("#757575"));
        String targetText = "Target: " + targetSteps + " steps";
        canvas.drawText(targetText, centerX, height / 2 + 20, textPaint);

        // Draw progress bar
        drawProgressBar(canvas);

        // Draw motivation text
        drawMotivationText(canvas, centerX, height);
    }

    private void drawWalkingPath(Canvas canvas, int width, int height) {
        Path path = new Path();
        int pathY = height / 2 + 60;
        int margin = 60;

        // Create curved walking path
        path.moveTo(margin, pathY);

        for (int i = 0; i <= 100; i++) {
            float x = margin + (width - 2 * margin) * i / 100f;
            float y = pathY + (float) (Math.sin(i * 0.1) * 15); // Slight wave
            path.lineTo(x, y);
        }

        canvas.drawPath(path, pathPaint);
    }

    private void drawFootsteps(Canvas canvas, int width, int height) {
        int margin = 60;
        int pathY = height / 2 + 60;

        for (int i = 0; i < footsteps.size(); i++) {
            FootstepIcon footstep = footsteps.get(i);

            // Calculate position along the path
            float x = margin + (width - 2 * margin) * footstep.progress;
            float y = pathY + (float) (Math.sin(footstep.progress * 10) * 15);

            // Determine color based on step progress
            if (i < currentSteps) {
                footstepPaint.setColor(Color.parseColor("#4CAF50")); // Green (completed)
            } else {
                footstepPaint.setColor(Color.parseColor("#BDBDBD")); // Gray (not reached)
            }

            // Draw footstep
            drawFootstepIcon(canvas, x, y, footstep.isLeft);
        }
    }

    private void drawFootstepIcon(Canvas canvas, float x, float y, boolean isLeft) {
        // Simple footstep icon (oval shape)
        Paint iconPaint = new Paint(footstepPaint);

        float footWidth = 15;
        float footHeight = 25;

        // Adjust position for left/right foot
        float offsetX = isLeft ? -10 : 10;

        RectF footRect = new RectF(
                x + offsetX - footWidth/2,
                y - footHeight/2,
                x + offsetX + footWidth/2,
                y + footHeight/2
        );

        canvas.drawOval(footRect, iconPaint);

        // Add toe part
        RectF toeRect = new RectF(
                x + offsetX - footWidth/3,
                y - footHeight/2 - 5,
                x + offsetX + footWidth/3,
                y - footHeight/2 + 3
        );
        canvas.drawOval(toeRect, iconPaint);
    }

    private void drawProgressBar(Canvas canvas) {
        // Background bar
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#E0E0E0"));
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);
        canvas.drawRoundRect(progressRect, 10, 10, bgPaint);

        // Progress bar
        float progressRatio = Math.min((float) currentSteps / targetSteps, 1.0f);
        RectF filledRect = new RectF(
                progressRect.left,
                progressRect.top,
                progressRect.left + (progressRect.width() * progressRatio),
                progressRect.bottom
        );

        progressPaint.setColor(currentSteps >= targetSteps ?
                Color.parseColor("#4CAF50") : Color.parseColor("#2196F3"));
        progressPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(filledRect, 10, 10, progressPaint);

        // Progress text on bar
        textPaint.setTextSize(16);
        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(true);
        String progressText = currentSteps + " / " + targetSteps;
        canvas.drawText(progressText,
                progressRect.centerX(),
                progressRect.centerY() + 6,
                textPaint);
    }

    private void drawMotivationText(Canvas canvas, int centerX, int height) {
        textPaint.setTextSize(18);
        textPaint.setFakeBoldText(false);

        if (currentSteps >= targetSteps) {
            textPaint.setColor(Color.parseColor("#4CAF50"));
            canvas.drawText("🎉 TARGET REACHED!", centerX, height - 40, textPaint);
        } else if (currentSteps > targetSteps / 2) {
            textPaint.setColor(Color.parseColor("#FF9800"));
            canvas.drawText("Keep going! You're halfway there!", centerX, height - 40, textPaint);
        } else {
            textPaint.setColor(Color.parseColor("#757575"));
            canvas.drawText("Start walking to dismiss the alarm", centerX, height - 40, textPaint);
        }
    }

    public void updateSteps(int steps) {
        int oldSteps = this.currentSteps;
        this.currentSteps = steps;

        // Trigger animation if steps increased
        if (steps > oldSteps) {
            addStepAnimation();
        }

        invalidate();
    }

    public void setTargetSteps(int target) {
        this.targetSteps = target;
        initializeFootsteps();
        invalidate();
    }

    public boolean isTargetReached() {
        return currentSteps >= targetSteps;
    }

    private void addStepAnimation() {
        animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(150)
                .withEndAction(() ->
                        animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(150)
                                .start()
                )
                .start();
    }

    // Inner class for footstep data
    private static class FootstepIcon {
        float progress; // 0.0 to 1.0 along the path
        boolean isLeft; // true for left foot, false for right foot

        FootstepIcon(float progress, boolean isLeft) {
            this.progress = progress;
            this.isLeft = isLeft;
        }
    }
}