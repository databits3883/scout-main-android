package com.databits.androidscouting.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class QrCodeDrawable extends Drawable {
  private Paint boundingRectPaint = new Paint();
  private Paint cornerPaint = new Paint();
  private Paint contentRectPaint = new Paint();
  private Paint contentTextPaint = new Paint();
  private QrCodeViewModel qrCodeViewModel;
  private int contentPadding = 25;
  private int textWidth;
  private int cornerLength = 60;
  private int cornerThickness = 12;

  public QrCodeDrawable(QrCodeViewModel qrCodeViewModel) {
    // Main bounding box - thin line
    boundingRectPaint.setStyle(Paint.Style.STROKE);
    boundingRectPaint.setColor(Color.rgb(0, 255, 0)); // Bright green
    boundingRectPaint.setStrokeWidth(3F);
    boundingRectPaint.setAlpha(180);

    // Corner markers - thick and bright
    cornerPaint.setStyle(Paint.Style.STROKE);
    cornerPaint.setColor(Color.rgb(0, 255, 0)); // Bright green
    cornerPaint.setStrokeWidth(cornerThickness);
    cornerPaint.setStrokeCap(Paint.Cap.ROUND);
    cornerPaint.setAlpha(255);

    // Label background
    contentRectPaint.setStyle(Paint.Style.FILL);
    contentRectPaint.setColor(Color.rgb(0, 200, 0)); // Darker green
    contentRectPaint.setAlpha(220);

    // Label text
    contentTextPaint.setColor(Color.WHITE);
    contentTextPaint.setAlpha(255);
    contentTextPaint.setTextSize(32F);
    contentTextPaint.setAntiAlias(true);

    this.qrCodeViewModel = qrCodeViewModel;
    textWidth = (int) contentTextPaint.measureText(qrCodeViewModel.qrContent);

    // CRITICAL FIX: Set bounds for the drawable to ensure it renders
    // Expand bounds to include label and corner markers
    if (qrCodeViewModel.boundingRect != null) {
      int expandedLeft = qrCodeViewModel.boundingRect.left - contentPadding;
      int expandedTop = qrCodeViewModel.boundingRect.top - (contentPadding * 3 + (int) contentTextPaint.getTextSize());
      int expandedRight = Math.max(
          qrCodeViewModel.boundingRect.right + contentPadding,
          qrCodeViewModel.boundingRect.left + textWidth + contentPadding * 3
      );
      int expandedBottom = qrCodeViewModel.boundingRect.bottom + contentPadding;

      setBounds(expandedLeft, expandedTop, expandedRight, expandedBottom);
    }
  }

  @Override
  public void draw(Canvas canvas) {
    RectF rect = new RectF(qrCodeViewModel.boundingRect);

    // Draw thin bounding rectangle
    canvas.drawRect(rect, boundingRectPaint);

    // Draw corner markers (professional scanner look)
    drawCorners(canvas, rect);

    // Draw label background (positioned above the QR code)
    float labelTop = rect.top - contentPadding - contentTextPaint.getTextSize() - contentPadding;
    float labelBottom = rect.top - contentPadding;
    float labelLeft = rect.left - contentPadding;
    float labelRight = rect.left + textWidth + contentPadding * 2;

    // Ensure label stays on screen
    if (labelTop < 0) {
      labelTop = rect.bottom + contentPadding;
      labelBottom = labelTop + contentTextPaint.getTextSize() + contentPadding * 2;
    }

    RectF labelRect = new RectF(labelLeft, labelTop, labelRight, labelBottom);
    canvas.drawRoundRect(labelRect, 8, 8, contentRectPaint);

    // Draw content text
    canvas.drawText(
        qrCodeViewModel.qrContent,
        labelLeft + contentPadding,
        labelBottom - contentPadding,
        contentTextPaint);
  }

  /**
   * Draw corner markers at each corner of the bounding box
   * This creates the classic "scanner frame" look
   */
  private void drawCorners(Canvas canvas, RectF rect) {
    // Top-left corner
    canvas.drawLine(rect.left, rect.top, rect.left + cornerLength, rect.top, cornerPaint);
    canvas.drawLine(rect.left, rect.top, rect.left, rect.top + cornerLength, cornerPaint);

    // Top-right corner
    canvas.drawLine(rect.right - cornerLength, rect.top, rect.right, rect.top, cornerPaint);
    canvas.drawLine(rect.right, rect.top, rect.right, rect.top + cornerLength, cornerPaint);

    // Bottom-left corner
    canvas.drawLine(rect.left, rect.bottom - cornerLength, rect.left, rect.bottom, cornerPaint);
    canvas.drawLine(rect.left, rect.bottom, rect.left + cornerLength, rect.bottom, cornerPaint);

    // Bottom-right corner
    canvas.drawLine(rect.right - cornerLength, rect.bottom, rect.right, rect.bottom, cornerPaint);
    canvas.drawLine(rect.right, rect.bottom - cornerLength, rect.right, rect.bottom, cornerPaint);
  }

  @Override
  public void setAlpha(int alpha) {
    boundingRectPaint.setAlpha(alpha);
    cornerPaint.setAlpha(alpha);
    contentRectPaint.setAlpha(alpha);
    contentTextPaint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    boundingRectPaint.setColorFilter(colorFilter);
    cornerPaint.setColorFilter(colorFilter);
    contentRectPaint.setColorFilter(colorFilter);
    contentTextPaint.setColorFilter(colorFilter);
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }
}