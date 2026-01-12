package com.databits.androidscouting.util;

import com.databits.androidscouting.data.repository.PowerPreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.travijuu.numberpicker.library.NumberPicker;

public class MatchInfo {
  private PreferenceRepository repository;

  // Lazy-loaded to avoid premature preference access during field initialization
  private Integer match = null;

  private PreferenceRepository getRepository() {
    if (repository == null) {
      repository = PowerPreferenceRepository.getInstance();
    }
    return repository;
  }

  public int getMatch() {
    if (getRepository().isManualMatchOverrideEnabled()) {
      return getRepository().getManualMatchOverrideValue();
    } else {
      // Lazy load match value on first access
      if (match == null) {
        match = getRepository().getCurrentMatch();
      }
      return match;
    }
  }

  public void setMatch(int val) {
    match = val; // Update cached value
    getRepository().setCurrentMatch(val);
  }

  public void incrementMatch() {
    int newValue = getRepository().getCurrentMatch() + 1;
    match = newValue; // Update cached value
    getRepository().setCurrentMatch(newValue);
  }

  public void setTempMatch(int val) {
    getRepository().setDebugMatch(val);
  }

  public int getTempMatch() {
    return getRepository().getDebugMatch();
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
