package com.databits.androidscouting.util;

import android.content.Context;
import android.widget.Toast;
import com.databits.androidscouting.R;
import com.databits.androidscouting.data.repository.PowerPreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TeamInfo {

  Context context;
  private final PreferenceRepository repository;

  public TeamInfo(Context context) {
    this.context = context;
    // Use repository for centralized preference access
    this.repository = PowerPreferenceRepository.getInstance();
  }

  public int getTeam(int match) {
    int pos = repository.getCrowdPosition();

    if (repository.isManualTeamOverrideEnabled()) {
      return repository.getManualTeamOverrideValue();
    } else {
      if (match >= repository.getTeamMatchListSize() || match < 0) {
        // Toast.makeText(context, "No team data found", Toast.LENGTH_LONG).show();
        return 0;
      }
      String teamNumber = repository.getTeamNumber(match, pos);
      return teamNumber != null ? Integer.parseInt(teamNumber) : 0;
    }
  }

  public String getMasterTeam(int match, int pos) {
    if (match >= repository.getTeamMatchListSize() || match < 0) {
      //Toast.makeText(context, "No team data found", Toast.LENGTH_LONG).show();
      return "0";
    }
    String teamNumber = repository.getTeamNumber(match, pos);
    return teamNumber != null ? teamNumber : "0";
  }

  public boolean teamsLoaded() {
    return repository.getTeamMatchListSize() > 0;
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
      try {
        File teams = new File(context.getFilesDir() + "/" + "match.csv");
          CSVReader csvReader = new CSVReader(new FileReader(teams));
          List<String[]> list = csvReader.readAll();
          String[][] dataArr = list.toArray(new String[0][]);
          repository.importTeamSchedule(dataArr);
      } catch (IOException | CsvException e) {
          e.printStackTrace();
      }

  }
}
