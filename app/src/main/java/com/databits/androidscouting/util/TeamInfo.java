package com.databits.androidscouting.util;

import android.content.Context;
import android.widget.Toast;
import com.databits.androidscouting.R;
import com.opencsv.CSVReader;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TeamInfo {

  Context context;

  public TeamInfo(Context context) {this.context = context;}

  Preference configPreference = PowerPreference.getFileByName("Config");
  Preference debugPreference = PowerPreference.getFileByName("Debug");
  Preference listPreference = PowerPreference.getFileByName("List");

  String[][] dataArr;

  public int getTeam(int match) {
    dataArr = listPreference.getObject("team_match",String[][].class);
    int pos =  configPreference.getInt("crowd_position");

    if (debugPreference.getBoolean("manual_team_override_toggle")) {
      return debugPreference.getInt("manual_team_override_value");
    } else {
      if (match >= debugPreference.getInt("team_match_list_size") || match < 0) {
        Toast.makeText(context, "No team data found", Toast.LENGTH_LONG).show();
        return 0;
      }
      return Integer.parseInt(dataArr[match][pos]);
    }
  }

  public String getMasterTeam(int match, int pos) {
    dataArr = listPreference.getObject("team_match",String[][].class);
    if (match >= debugPreference.getInt("team_match_list_size") || match < 0) {
      //Toast.makeText(context, "No team data found", Toast.LENGTH_LONG).show();
      return "0";
    }
    return dataArr[match][pos];
  }

  public boolean teamsLoaded() {
    return listPreference.getObject("team_match", String[][].class) != null;
  }

  public void setTeam(int val) {
    debugPreference.setInt("team_number", val);
  }

  public int getTeamCount() {
    return debugPreference.getInt("team_match_list_size", 0);
  }

  public int getPitTeamCount() {
    List<String> entryLabels = Arrays.asList(
        context.getResources().getStringArray(R.array.team_list));
    int size = entryLabels.size();
    debugPreference.setInt("pit_team_list_size", size);
    return size;
  }

  public int getPitTeam(int pos) {
    List<String> entryLabels = Arrays.asList(
        context.getResources().getStringArray(R.array.team_list));
    if (pos >= entryLabels.size() || pos < 0) {
      Toast.makeText(context, "No team data found", Toast.LENGTH_LONG).show();
      return 0;
    }
    return Integer.parseInt(entryLabels.get(pos));
  }

  public String getScouterName() {
    return configPreference.getString("current_scouter", "Scouter");
  }

  // Read the team data for validator from match.csv
  public void read_teams() {
      String[][] dataArr;
      try {
        File teams = new File(context.getFilesDir() + "/" + "match.csv");
          CSVReader csvReader = new CSVReader(new FileReader(teams));
          List<String[]> list = csvReader.readAll();
          int size = list.size();
          debugPreference.setInt("team_match_list_size", size);
          dataArr = new String[size][];
          dataArr = list.toArray(dataArr);
          listPreference.setObject("team_match", dataArr);
      } catch (IOException e) {
          e.printStackTrace();
      }

  }
}
