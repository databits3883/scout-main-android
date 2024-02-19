package com.databits.androidscouting.util;

import com.preference.PowerPreference;
import com.preference.Preference;
import com.travijuu.numberpicker.library.NumberPicker;

public class MatchInfo {
  Preference configPreference = PowerPreference.getFileByName("Config");
  Preference debugPreference = PowerPreference.getFileByName("Debug");

  int match = configPreference.getInt("current_match", 1);

  public int getMatch() {
    if (debugPreference.getBoolean("manual_match_override_toggle")) {
      return debugPreference.getInt("manual_match_override_value");
    } else {
      return match;
    }
  }

  public void setMatch(int val) {
    configPreference.setInt("current_match", val);
  }

  public void incrementMatch() {
    configPreference.setInt("current_match", configPreference.getInt("current_match", 1) + 1);
  }

  public void setTempMatch(int val) {
    debugPreference.setInt("debug_match", val);
  }

  public int getTempMatch() {
    return debugPreference.getInt("debug_match", getMatch());
  }

  // Default configuration for the match number picker
  public NumberPicker configurePicker(NumberPicker numberPicker) {
    numberPicker.setMin(1);
    numberPicker.setMax(100);
    numberPicker.setUnit(1);
    numberPicker.setValue(getMatch());
    numberPicker.setFocusable(false);
    numberPicker.setValueChangedListener((value, action) -> setMatch(value));
    return numberPicker;
  }
}
