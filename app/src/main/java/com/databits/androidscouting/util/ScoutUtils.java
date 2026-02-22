package com.databits.androidscouting.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.databits.androidscouting.R;
import com.databits.androidscouting.adapter.MultiviewTypeAdapter;
import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import com.databits.androidscouting.data.repository.ScheduleStore;
import com.databits.androidscouting.model.CellType;
import com.travijuu.numberpicker.library.NumberPicker;

public class ScoutUtils {
  public static final int REQUEST_CODE_PERMISSIONS = 10;
  public static final String[] REQUIRED_PERMISSIONS = { android.Manifest.permission.CAMERA };

  private final ProvisionSettingsStore provisionStore;
  private final ScheduleStore scheduleStore;

  public MatchInfo matchInfo;
  public TeamInfo teamInfo;

  Context context;

  public ScoutUtils(Context context, ProvisionSettingsStore provisionStore, ScheduleStore scheduleStore) {
    this.context = context;
    this.provisionStore = provisionStore;
    this.scheduleStore = scheduleStore;
  }

  public String exportCell(RecyclerView recyclerView) {
    MultiviewTypeAdapter recyclerAdapter = (MultiviewTypeAdapter) recyclerView.getAdapter();
    StringBuilder finalString = new StringBuilder();

    if (recyclerAdapter != null) {
      for (int i = 0; i < recyclerView.getChildCount(); i++) {
        CellType cellType = recyclerAdapter.mCell.get(i).getType();
        View v = recyclerView.getChildAt(i);

          finalString.append(",");
        switch (cellType) {
          case YES_NO:
            SegmentedButtonGroup YesNoButtonGroup = v.findViewById(R.id.buttonGroup_yes_no);
            finalString.append(YesNoButtonGroup.getPosition())/*.append(",")*/;
            break;
          case COUNTER:
            NumberPicker numberPicker = v.findViewById(R.id.number_counter_inside);
            finalString.append(numberPicker.getValue())/*.append(",")*/;
            break;
          case DOUBLE_COUNTER:
            NumberPicker numberPicker1 = v.findViewById(R.id.number_counter_one);
            finalString.append(numberPicker1.getValue()).append(",");
            NumberPicker numberPicker2 = v.findViewById(R.id.number_counter_two);
            finalString.append(numberPicker2.getValue())/*.append(",")*/;
            break;
          case DUAL_COUNTER:
            NumberPicker picker1 = v.findViewById(R.id.counterOne);
            NumberPicker picker2 = v.findViewById(R.id.counterTwo);
            finalString.append(picker1.getValue()).append(",");
            finalString.append(picker2.getValue())/*.append(",")*/;
            break;
          case SEGMENT:
            SegmentedButtonGroup multiSegment = v.findViewById(R.id.buttonGroup_segments);
            finalString.append(multiSegment.getPosition())/*.append(",")*/;
            break;
          case LIST:
            Spinner listSpinner = v.findViewWithTag("Spinner");
            finalString.append(listSpinner.getSelectedItem())/*.append(",")*/;
            break;
          case TEXT:
            EditText enteredText = v.findViewById(R.id.enteredText);
            // Remove commas from the string to prevent the spreadsheet from breaking
            finalString.append(enteredText.getText().toString().replaceAll(",", "."))/*.append(",")*/;
            break;
          case TITLE:
            // Do nothing this cell type contains only internal data
            break;
          case TEAM_SELECT:
            Spinner teamSpinner = v.findViewWithTag("TeamSpinner");
            finalString.append(teamSpinner.getSelectedItem())/*.append(",")*/;
            break;
          case SPECIAL:
            SegmentedButtonGroup teamSelector = v.findViewById(R.id.teamSelector_segment).findViewById(R.id.buttonGroup_segments);
            // Gets the title of the selected button instead of just the position as normal
            finalString.append(teamSelector.getButton(teamSelector.getPosition()).getText()).append(",");

            NumberPicker algaeMiss = v.findViewById(R.id.counterUI1).findViewById(R.id.number_counter_inside);
            finalString.append(algaeMiss.getValue()).append(",");

            NumberPicker algaeSuccess = v.findViewById(R.id.counterUI2).findViewById(R.id.number_counter_inside);
            finalString.append(algaeSuccess.getValue()).append(",");

            NumberPicker algaeReturned = v.findViewById(R.id.counterUI3).findViewById(R.id.number_counter_inside);
            finalString.append(algaeReturned.getValue());
            break;

        }
      }
    }

    return finalString.toString();
  }

  // Save the table and cell data to a string
  public String saveData(View v, boolean special) {
    String cellData;

    int match = matchInfo.getMatch();
    int team = 9999;
    if (provisionStore.isManualTeamOverrideEnabled()) {
      team = provisionStore.getManualTeamOverrideValue();
    } else if (teamInfo.teamsLoaded() || scheduleStore.isPitRemoveEnabled()) {
      team = teamInfo.getTeam(match);
    }

    //#TODO figure out why there is a comma at the beginning of the string, substring removes it for now
    if (scheduleStore.isPitRemoveEnabled()) {
      cellData = exportCell(v.findViewById(R.id.recycler_view)).substring(1) + "," +
          teamInfo.getScouterName();
    } else if (special){
      cellData = exportCell(v.findViewById(R.id.recycler_view)).substring(1) + "," +
          teamInfo.getScouterName();
    } else {
      cellData = team + "," + match + "," + exportCell(v.findViewById(R.id.recycler_view))
          .substring(1) + "," + teamInfo.getScouterName();
    }
    return cellData;
  }

  public boolean allPermissionsGranted() {
    for (String permission : REQUIRED_PERMISSIONS) {
      if (ContextCompat.checkSelfPermission(context, permission)
          != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  public void setButtonStatus(Button button, boolean condition, String trueText, String falseText) {
    button.setText(condition ? trueText : falseText);
    button.setBackgroundColor(ContextCompat.getColor(context, condition ? android.R.color.holo_green_light : android.R.color.holo_red_light));
  }
}
