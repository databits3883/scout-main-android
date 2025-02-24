package com.databits.androidscouting.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import com.databits.androidscouting.R;
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer;
import com.github.sumimakito.awesomeqr.RenderResult;
import com.github.sumimakito.awesomeqr.option.RenderOption;
import com.github.sumimakito.awesomeqr.option.color.ColorQR;
import com.github.sumimakito.awesomeqr.option.logo.Logo;

public class QrCodeGenerator {
  Context context;

  public QrCodeGenerator(Context context) {
    this.context = context;
  }
  /**
   * Generates a QR code bitmap from the given text.
   *
   * @param text   The text to encode in the QR code.
   * @param size  The overall size of the QR code image.
   * @param borderWidth The width of the empty space around the QR code.
   * @param logo Should the QR code contain a logo?
   * @return A Bitmap representing the generated QR code, or null if an error occurs.
   */
  public Bitmap generateQRCode(String text, int size, int borderWidth, boolean logo) {
    Bitmap logoBitmap = BitmapFactory
        .decodeResource(context.getResources(), R.drawable.logo);

    // Logo for the QR code
    Logo qrLogo = new Logo();
    qrLogo.setBitmap(logoBitmap);
    // scale for the logo in the middle of the QR code
    qrLogo.setScale(0.25f);
    // crop the logo image before applying it to the QR code
    qrLogo.setClippingRect(new RectF(0, 0, 200, 200));

    // Color for the QR code
    ColorQR color = new ColorQR();
    // for blank spaces
    color.setLight(context.getResources().getColor(R.color.white, null));
    // for non-blank spaces
    color.setDark(context.getResources().getColor(R.color.black, null));
    // for the background (will be overridden by background images, if set)
    color.setBackground(context.getResources().getColor(R.color.white, null));

    // Final library render option to generate the QR code
    RenderOption renderOption = new RenderOption();
    renderOption.setContent(text);
    renderOption.setSize(size);
    renderOption.setRoundedPatterns(false);
    renderOption.setBorderWidth(borderWidth);
    renderOption.setQrCodeVersion(12);
    renderOption.setPatternScale(0.9f);
    // if set to true, the background will NOT be drawn on the border area
    renderOption.setClearBorder(false);
    renderOption.setColorQR(color);
    if (logo) {
      renderOption.setLogo(qrLogo);
    }
    try {
      RenderResult render = AwesomeQrRenderer.render(renderOption);
      return render.getBitmap();
    } catch (Exception e) {
      // Make an alert dialog to inform the user that there was an error
      AlertDialog alertDialog = new AlertDialog.Builder(context).create();
      alertDialog.setTitle("There was an error generating the QR code.");
      alertDialog.setMessage(e.getMessage());
      alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
          (dialog, which) -> dialog.dismiss());
      alertDialog.show();
      return null;
    }
  }
}