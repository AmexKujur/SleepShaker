package com.example.sleepshaker.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ShakeProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private Paint centerPaint;
    private int progress = 0;
    private int maxProgress = 100;
    private RectF arcRect;

    // Colors for different progress levels
    private static final int COLOR_LOW = Color.parseColor("#FF5722");     // Red
    private static final int COLOR_MID = Color.parseColor("#FF9800");     // Orange
    private static final int COLOR_HIGH = Color.parseColor("#4CAF50");    // Green

    public ShakeProgressView(Context context) {
        super(context);
        init();
    }

    public ShakeProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShakeProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Background circle paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#E0E0E0"));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(20);
        backgroundPaint.setAntiAlias(true);

        // Progress arc paint
        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setAntiAlias(true);

        // Text paint for percentage
        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#424242"));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(48);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);

        // Center circle paint
        centerPaint = new Paint();
        centerPaint.setColor(Color.parseColor("#F5F5F5"));
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setAntiAlias(true);

        arcRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int padding = 40;
        arcRect.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = (int) (Math.min(centerX, centerY) - 40);

        // Draw center filled circle
        canvas.drawCircle(centerX, centerY, radius - 10, centerPaint);

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // Set progress color based on progress level
        if (progress < 30) {
            progressPaint.setColor(COLOR_LOW);
        } else if (progress < 70) {
            progressPaint.setColor(COLOR_MID);
        } else {
            progressPaint.setColor(COLOR_HIGH);
        }

        // Draw progress arc (starts from top, goes clockwise)
        float sweepAngle = (progress / (float) maxProgress) * 360;
        canvas.drawArc(arcRect, -90, sweepAngle, false, progressPaint);

        // Draw progress text
        String progressText = progress + "%";

        // Calculate text position (center of circle)
        float textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(progressText, centerX, textY, textPaint);

        // Draw "SHAKE!" text below percentage if progress < 100
        if (progress < 100) {
            textPaint.setTextSize(24);
            textPaint.setColor(Color.parseColor("#757575"));
            canvas.drawText("SHAKE!", centerX, textY + 40, textPaint);
            textPaint.setTextSize(48); // Reset text size
            textPaint.setColor(Color.parseColor("#424242")); // Reset color
        } else {
            // Draw "COMPLETE!" when done
            textPaint.setTextSize(24);
            textPaint.setColor(COLOR_HIGH);
            canvas.drawText("COMPLETE!", centerX, textY + 40, textPaint);
            textPaint.setTextSize(48); // Reset text size
            textPaint.setColor(Color.parseColor("#424242")); // Reset color
        }
    }

    // Method to update progress from DismissActivity
    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(progress, maxProgress));
        invalidate(); // Trigger redraw
    }

    public int getProgress() {
        return progress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
        invalidate();
    }

    // Method to reset progress
    public void reset() {
        this.progress = 0;
        invalidate();
    }

    // Add animation effect when shake is detected
    public void addShakeEffect() {
        // Add a slight scale animation or color flash
        animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction(() ->
                        animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start()
                )
                .start();
    }
}