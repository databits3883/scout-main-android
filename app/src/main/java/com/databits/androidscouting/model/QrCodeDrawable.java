package com.databits.androidscouting.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class QrCodeDrawable extends Drawable {
  private Paint boundingRectPaint = new Paint();
  private Paint contentRectPaint = new Paint();
  private Paint contentTextPaint = new Paint();
  private QrCodeViewModel qrCodeViewModel;
  private int contentPadding = 25;
  private int textWidth;

  public QrCodeDrawable(QrCodeViewModel qrCodeViewModel) {
    boundingRectPaint.setStyle(Paint.Style.STROKE);
    boundingRectPaint.setColor(Color.YELLOW);
    boundingRectPaint.setStrokeWidth(5F);
    boundingRectPaint.setAlpha(200);

    contentRectPaint.setStyle(Paint.Style.FILL);
    contentRectPaint.setColor(Color.YELLOW);
    contentRectPaint.setAlpha(255);

    contentTextPaint.setColor(Color.DKGRAY);
    contentTextPaint.setAlpha(255);
    contentTextPaint.setTextSize(36F);

    this.qrCodeViewModel = qrCodeViewModel;
    textWidth = (int) contentTextPaint.measureText(qrCodeViewModel.qrContent);
  }

  @Override
  public void draw(Canvas canvas) {
    // Draw bounding rectangle
    canvas.drawRect(qrCodeViewModel.boundingRect, boundingRectPaint);

    // Draw filled rectangle
    canvas.drawRect(
        qrCodeViewModel.boundingRect.left - contentPadding,
        qrCodeViewModel.boundingRect.top - contentPadding,
        qrCodeViewModel.boundingRect.left + textWidth + contentPadding,
        qrCodeViewModel.boundingRect.top + 2 * contentPadding + contentTextPaint.getTextSize(),
        contentRectPaint);

    // Draw content text
    canvas.drawText(
        qrCodeViewModel.qrContent,
        qrCodeViewModel.boundingRect.left,
        qrCodeViewModel.boundingRect.top + contentPadding + contentTextPaint.getTextSize(),
        contentTextPaint);
  }

  @Override
  public void setAlpha(int alpha) {
    boundingRectPaint.setAlpha(alpha);
    contentRectPaint.setAlpha(alpha);
    contentTextPaint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    boundingRectPaint.setColorFilter(colorFilter);
    contentRectPaint.setColorFilter(colorFilter);
    contentTextPaint.setColorFilter(colorFilter);
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }
}