package com.databits.androidscouting.util;

import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import com.databits.androidscouting.data.repository.AppRepositories;
import com.databits.androidscouting.data.repository.PreferenceRepositoryProvider;
import com.travijuu.numberpicker.library.NumberPicker;

public class MatchInfo {
  private final ProvisionSettingsStore repository;

  // Lazy-loaded to avoid premature preference access during field initialization
  private Integer match = null;

  public MatchInfo(ProvisionSettingsStore repository) {
    this.repository = repository;
  }

  public MatchInfo(android.content.Context context) {
    AppRepositories graph = PreferenceRepositoryProvider.graph(context);
    this.repository = graph.provisionSettingsStore;
  }

  public int getMatch() {
    if (repository.isManualMatchOverrideEnabled()) {
      int override = repository.getManualMatchOverrideValue();
      if (override > 0) {
        return override;
      }
    } else {
      // Lazy load match value on first access
      if (match == null) {
        match = repository.getCurrentMatch();
      }
    }

    if (match == null) {
      match = repository.getCurrentMatch();
    }
    return match;
  }

  public void setMatch(int val) {
    int safeValue = Math.max(1, val);
    match = safeValue; // Update cached value
    repository.setCurrentMatch(safeValue);
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
