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
    String rawValue = barcode.getRawValue();

    switch (barcode.getValueType()) {
      case Barcode.TYPE_URL:
        qrContent = "URL: " + barcode.getUrl().getUrl();
        qrCodeTouchCallback = (v, e) -> {
          if (e.getAction() == MotionEvent.ACTION_DOWN && boundingRect.contains((int) e.getX(), (int) e.getY())) {
            Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW);
            openBrowserIntent.setData(Uri.parse(barcode.getUrl().getUrl()));
            v.getContext().startActivity(openBrowserIntent);
          }
          return true;
        };
        break;
      default:
        // Determine QR type from content
        qrContent = determineQrType(rawValue);
    }
  }

  /**
   * Determine the type of QR code based on its content
   * and return a user-friendly label
   */
  private String determineQrType(String rawValue) {
    if (rawValue == null || rawValue.isEmpty()) {
      return "QR Code";
    }

    // Check for Scout-specific QR codes
    if (rawValue.startsWith("ScoutData")) {
      return "Scout Config";
    } else if (rawValue.startsWith("GoogleConfig")) {
      return "Google Sheets Config";
    } else if (rawValue.startsWith("MatchData")) {
      return "Match Schedule";
    } else if (rawValue.startsWith("role")) {
      return "Role Assignment";
    }

    // For team numbers and other data, show first 30 chars
    if (rawValue.length() > 30) {
      return rawValue.substring(0, 27) + "...";
    }

    return rawValue;
  }

  public interface QrCodeTouchCallback {
    boolean onTouch(View v, MotionEvent e);
  }
}