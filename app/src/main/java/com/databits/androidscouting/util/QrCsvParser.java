package com.databits.androidscouting.util;

import android.util.Log;
import java.util.Arrays;

/**
 * QrCsvParser handles parsing QR codes with better validation, format handling, and error control.
 */
public class QrCsvParser {

  private static final String TAG = "QrCsvParser";

  /**
   * Parses a QR code string into an array of fields.
   * Handles different separators and trims extra spaces.
   *
   * @param qrData    The raw QR code data as a string.
   * @param minFields Minimum number of fields required for valid parsing.
   * @return An array of parsed fields or null if invalid.
   */
  public static String[] parseCsv(String qrData, int minFields) {
    try {
      if (qrData == null || qrData.trim().isEmpty()) {
        Log.e(TAG, "Error: QR data is empty or null.");
        return null;
      }

      // Detect separator dynamically
      String separator = detectSeparator(qrData);
      if (separator == null) {
        Log.e(TAG, "Error: Could not detect separator in QR data.");
        return null;
      }

      // Split and sanitize each field
      String[] fields = qrData.split(separator);
      for (int i = 0; i < fields.length; i++) {
        fields[i] = fields[i].trim(); // Trim spaces
      }

      // Validate minimum fields requirement
      if (fields.length < minFields) {
        Log.e(TAG, "Error: QR data has insufficient fields. Found: " + fields.length);
        return null;
      }

      Log.d(TAG, "Successfully Parsed QR Data: " + Arrays.toString(fields));
      return fields;

    } catch (Exception e) {
      Log.e(TAG, "Exception during QR parsing: " + e.getMessage());
      return null;
    }
  }

  /**
   * Detects the most likely separator used in the QR code data.
   * Supports multiple formats for robustness.
   *
   * @param data The raw QR code data.
   * @return The separator found or null if no valid separator is detected.
   */
  private static String detectSeparator(String data) {
    char[] separators = { ',', ';', '|', '\t' };
    for (char separator : separators) {
      if (data.indexOf(separator) != -1) {
        return String.valueOf(separator);
      }
    }
    return null;
  }
}
