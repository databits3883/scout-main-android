package com.databits.androidscouting.util;

import android.content.Context;
import android.widget.Toast;
import com.databits.androidscouting.R;
import com.databits.androidscouting.data.repository.PowerPreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TeamInfo {

  Context context;
  private final PreferenceRepository repository;

  String[][] dataArr;

  public TeamInfo(Context context) {
    this.context = context;
    // Use repository for centralized preference access
    this.repository = PowerPreferenceRepository.getInstance();
  }

  public int getTeam(int match) {
    dataArr = repository.getTeamMatchData();
    int pos = repository.getCrowdPosition();

    if (repository.isManualTeamOverrideEnabled()) {
      return repository.getManualTeamOverrideValue();
    } else {
      if (match >= repository.getTeamMatchListSize() || match < 0) {
        Toast.makeText(context, "No team data found", Toast.LENGTH_LONG).show();
        return 0;
      }
      return Integer.parseInt(dataArr[match][pos]);
    }
  }

  public String getMasterTeam(int match, int pos) {
    dataArr = repository.getTeamMatchData();
    if (match >= repository.getTeamMatchListSize() || match < 0) {
      //Toast.makeText(context, "No team data found", Toast.LENGTH_LONG).show();
      return "0";
    }
    return dataArr[match][pos];
  }

  public boolean teamsLoaded() {
    return repository.getTeamMatchData() != null;
  }

  public void setTeam(int val) {
    // Note: team_number is not in repository interface, keeping direct access for now
    // TODO: Add to repository if this is a core preference
    repository.setManualTeamOverrideValue(val);
  }

  public int getTeamCount() {
    return repository.getTeamMatchListSize();
  }

  public int getPitTeamCount() {
    List<String> entryLabels = Arrays.asList(
        context.getResources().getStringArray(R.array.team_list));
    int size = entryLabels.size();
    repository.setPitTeamListSize(size);
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
    return repository.getCurrentScouter();
  }

  // Read the team data for validator from match.csv
  public void read_teams() {
      String[][] dataArr;
      try {
        File teams = new File(context.getFilesDir() + "/" + "match.csv");
          CSVReader csvReader = new CSVReader(new FileReader(teams));
          List<String[]> list = csvReader.readAll();
          int size = list.size();
          repository.setTeamMatchListSize(size);
          dataArr = new String[size][];
          dataArr = list.toArray(dataArr);
          repository.setTeamMatchData(dataArr);
      } catch (IOException e) {
          e.printStackTrace();
      }

  }
}
