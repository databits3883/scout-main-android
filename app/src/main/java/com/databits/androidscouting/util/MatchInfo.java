package com.databits.androidscouting.util;

import com.databits.androidscouting.data.repository.PowerPreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.travijuu.numberpicker.library.NumberPicker;

public class MatchInfo {
  private final PreferenceRepository repository = PowerPreferenceRepository.getInstance();

  // Lazy-loaded to avoid premature preference access during field initialization
  private Integer match = null;

  public int getMatch() {
    if (repository.isManualMatchOverrideEnabled()) {
      return repository.getManualMatchOverrideValue();
    } else {
      // Lazy load match value on first access
      if (match == null) {
        match = repository.getCurrentMatch();
      }
      return match;
    }
  }

  public void setMatch(int val) {
    match = val; // Update cached value
    repository.setCurrentMatch(val);
  }

  public void incrementMatch() {
    int newValue = repository.getCurrentMatch() + 1;
    match = newValue; // Update cached value
    repository.setCurrentMatch(newValue);
  }

  public void setTempMatch(int val) {
    repository.setDebugMatch(val);
  }

  public int getTempMatch() {
    return repository.getDebugMatch();
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
