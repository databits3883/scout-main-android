package com.databits.androidscouting.util;

import android.content.Context;

import androidx.preference.Preference;

import com.preference.PowerPreference;

// PreferenceManager is a singleton that centralizes access to different Preference files.
// It provides methods to retrieve configuration, debug, list, match, and pit data preferences.
public class PreferenceManager {
  // Singleton instance of PreferenceManager.
  private static PreferenceManager instance;

  // Instance variables for various preference files.
  private final com.preference.Preference configPreference;
  private final com.preference.Preference debugPreference;
  private final com.preference.Preference listPreference;
  private final com.preference.Preference matchPreference;
  private final com.preference.Preference pitDataPreference;

  // Private constructor to initialize the preference files only once.
  private PreferenceManager() {
    // Initialize each preference only once
    //PowerPreference.init(context);
    configPreference = PowerPreference.getFileByName("Config");
    debugPreference = PowerPreference.getFileByName("Debug");
    listPreference = PowerPreference.getFileByName("List");
    matchPreference = PowerPreference.getFileByName("Match");
    pitDataPreference = PowerPreference.getFileByName("PitData");
  }

  /**
   * Returns the singleton instance of PreferenceManager.
   * Ensures that only one instance of PreferenceManager exists throughout the app.
   *
   * @return The singleton instance of PreferenceManager.
   */
  public static synchronized PreferenceManager getInstance() {
    if (instance == null) {
      instance = new PreferenceManager();
    }
    return instance;
  }

  /**
   * Returns the configuration preference file.
   *
   * @return The config preference.
   */
  public com.preference.Preference getConfigPreference() {
    return configPreference;
  }

  /**
   * Returns the debug preference file.
   *
   * @return The debug preference.
   */
  public com.preference.Preference getDebugPreference() {
    return debugPreference;
  }

  /**
   * Returns the list preference file.
   *
   * @return The list preference.
   */
  public com.preference.Preference getListPreference() {
    return listPreference;
  }

  /**
   * Returns the match preference file.
   *
   * @return The match preference.
   */
  public com.preference.Preference getMatchPreference() {
    return matchPreference;
  }

  /**
   * Returns the pit data preference file.
   *
   * @return The pit data preference.
   */
  public com.preference.Preference getPitDataPreference() {return pitDataPreference;}
}

