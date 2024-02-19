package com.databits.androidscouting.model;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;

import com.google.mlkit.vision.barcode.common.Barcode;

public class QrCodeViewModel {
  public Rect boundingRect;
  public String qrContent = "";
  public QrCodeTouchCallback qrCodeTouchCallback = (v, e) -> false;

  public QrCodeViewModel(Barcode barcode) {
    boundingRect = barcode.getBoundingBox();
    switch (barcode.getValueType()) {
      case Barcode.TYPE_URL:
        qrContent = barcode.getUrl().getUrl();
        qrCodeTouchCallback = (v, e) -> {
          if (e.getAction() == MotionEvent.ACTION_DOWN && boundingRect.contains((int) e.getX(), (int) e.getY())) {
            Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW);
            openBrowserIntent.setData(Uri.parse(qrContent));
            v.getContext().startActivity(openBrowserIntent);
          }
          return true;
        };
        break;
      default:
        //qrContent = "QR Code Detected";
        qrContent = "QR Code Detected: " + barcode.getRawValue();
    }
  }

  public interface QrCodeTouchCallback {
    boolean onTouch(View v, MotionEvent e);
  }
}