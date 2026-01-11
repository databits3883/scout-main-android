package com.databits.androidscouting.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.databits.androidscouting.R;
import com.databits.androidscouting.adapter.MultiviewTypeAdapter;
import com.databits.androidscouting.adapter.SimpleItemTouchHelperCallback;
import com.databits.androidscouting.model.Cell;
import com.databits.androidscouting.model.CellParam;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.travijuu.numberpicker.library.NumberPicker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ScoutUtils {

  String[] cellTypes = {"YesNo", "Counter","DoubleCounter", "Segment", "List", "Text", "Special"};
  String[] cellTitles = {"YesNo_title", "Counter_Title", "DoubleCounter_Title", "Segment_Title", "List_Title", "Textbox_title"};

  public static final int REQUEST_CODE_PERMISSIONS = 10;
  public static final String[] REQUIRED_PERMISSIONS = { android.Manifest.permission.CAMERA };

  Preference configPreference = PowerPreference.getFileByName("Config");
  Preference debugPreference = PowerPreference.getFileByName("Debug");
  Preference listPreference = PowerPreference.getFileByName("List");

  MatchInfo matchInfo;
  TeamInfo teamInfo;

  List<Cell> cellList = new ArrayList<>();

  Context context;

  public ScoutUtils(Context context) {
    this.context = context;
  }

  public String exportCell(RecyclerView recyclerView) {
    MultiviewTypeAdapter recyclerAdapter = (MultiviewTypeAdapter) recyclerView.getAdapter();
    StringBuilder finalString = new StringBuilder();

    if (recyclerAdapter != null) {
      for (int i = 0; i < recyclerView.getChildCount(); i++) {
        String cellType = recyclerAdapter.mCell.get(i).getType();
        View v = recyclerView.getChildAt(i);

          finalString.append(",");
        switch (cellType) {
          case "YesNo":
            SegmentedButtonGroup YesNoButtonGroup = v.findViewById(R.id.buttonGroup_yes_no);
            finalString.append(YesNoButtonGroup.getPosition())/*.append(",")*/;
            break;
          case "Counter":
            NumberPicker numberPicker = v.findViewById(R.id.number_counter_inside);
            finalString.append(numberPicker.getValue())/*.append(",")*/;
            break;
          case "DoubleCounter":
            NumberPicker numberPicker1 = v.findViewById(R.id.number_counter_one);
            finalString.append(numberPicker1.getValue()).append(",");
            NumberPicker numberPicker2 = v.findViewById(R.id.number_counter_two);
            finalString.append(numberPicker2.getValue())/*.append(",")*/;
            break;
          case "DualCounter":
            NumberPicker picker1 = v.findViewById(R.id.counterOne);
            NumberPicker picker2 = v.findViewById(R.id.counterTwo);
            finalString.append(picker1.getValue()).append(",");
            finalString.append(picker2.getValue())/*.append(",")*/;
            break;
          case "Segment":
            SegmentedButtonGroup multiSegment = v.findViewById(R.id.buttonGroup_segments);
            finalString.append(multiSegment.getPosition())/*.append(",")*/;
            break;
          case "List":
            Spinner listSpinner = v.findViewWithTag("Spinner");
            finalString.append(listSpinner.getSelectedItem())/*.append(",")*/;
            break;
          case "Text":
            EditText enteredText = v.findViewById(R.id.enteredText);
            // Remove commas from the string to prevent the spreadsheet from breaking
            finalString.append(enteredText.getText().toString().replaceAll(",", "."))/*.append(",")*/;
            break;
          case "Title":
            // Do nothing this cell type contains only internal data
            break;
          case "TeamSelect":
            Spinner teamSpinner = v.findViewWithTag("TeamSpinner");
            finalString.append(teamSpinner.getSelectedItem())/*.append(",")*/;
            break;
          case "Special":
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
    if (debugPreference.getBoolean("manual_team_override_toggle")) {
      team = debugPreference.getInt("manual_team_override_value");
    } else if (teamInfo.teamsLoaded() || listPreference.getBoolean("pit_remove_enabled")) {
      team = teamInfo.getTeam(match);
    }

    //#TODO figure out why there is a comma at the beginning of the string, substring removes it for now
    if (listPreference.getBoolean("pit_remove_enabled")) {
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

  public List<Cell> testCells (int cells) {
    for (int i = 0; i < cells; i++) {
      String cellType = cellTypes[i];
      CellParam cellParam = new CellParam(cellType);
      switch (cellType) {
        case "YesNo":
          cellParam.setType(context.getString(R.string.YesNoType));
          break;
        case "Counter":
          cellParam.setType(context.getString(R.string.CounterType));
          cellParam.setDefault(3);
          cellParam.setMax(5);
          cellParam.setMin(0);
          cellParam.setUnit(1);
        case "Segment":
          cellParam.setType(context.getString(R.string.SegmentType));
          cellParam.setSegments(6);
          cellParam.setSegmentLabels(
              Arrays.asList("One", "2", "Three", "4", "Five", "6"));
          break;
        case "List":
          cellParam.setType(context.getString(R.string.ListType));
          cellParam.setTotalEntries(3);
          cellParam.setEntryLabels(Arrays.asList("Java", "C++", "Labview"));
          break;
        case "Text":
          cellParam.setType(context.getString(R.string.TextType));
          cellParam.setTextHidden(false);
          cellParam.setTextHint("Enter life here");
          break;
      }
      Cell cell = new Cell(i, cellTitles[i], cellType, cellParam);
      cellList.add(cell);
    }
    return cellList;
  }

  // import cells from json string
  public void import_cells(String optional_json, RecyclerView mRecyclerView) {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<MultiviewTypeAdapter> jsonAdapter = moshi.adapter(MultiviewTypeAdapter.class);
    MultiviewTypeAdapter mRecyclerViewAdapter;
    try {
      MultiviewTypeAdapter config = jsonAdapter.fromJson(optional_json);
      mRecyclerViewAdapter = new MultiviewTypeAdapter(Objects.requireNonNull(config).mCell);
      mRecyclerView.setAdapter(mRecyclerViewAdapter);
      //Log.d("Dynamic", "import: " + optional_json);
    } catch (IOException e) {
      Log.e("Dynamic", "Error parsing JSON", e);
      mRecyclerViewAdapter = new MultiviewTypeAdapter(Collections.emptyList());
    }
    mRecyclerViewAdapter.notifyDataSetChanged();
    mRecyclerView.post(() -> {
      setupTitle(mRecyclerView);
    });
  }

  public void setupTitle(RecyclerView mRecyclerView) {
    MultiviewTypeAdapter recyclerAdapter = (MultiviewTypeAdapter) mRecyclerView.getAdapter();
    matchInfo = new MatchInfo();
    teamInfo = new TeamInfo(context);
    if (recyclerAdapter != null) {
      if (recyclerAdapter.mCell.size() > 0) {
        // Update the team number and match number in all "Title" cells
        for (int i = 0; i < recyclerAdapter.mCell.size(); i++) {
          if (Objects.equals(recyclerAdapter.mCell.get(i).getType(), "Title")) {
            View v = mRecyclerView.getChildAt(i);
            TextView teamNumber = v.findViewById(R.id.team_number);
            TextView matchNumber = v.findViewById(R.id.match_number);

            if (!teamInfo.teamsLoaded()) {
              teamNumber.setText("No Team");
            } else {
              teamNumber.setText(String.valueOf(teamInfo.getTeam(matchInfo.getMatch())));
            }
            matchNumber.setText(String.valueOf(matchInfo.getMatch()));
          }
        }
      }
    }
  }

  private MultiviewTypeAdapter makeAdapter() {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<MultiviewTypeAdapter> jsonAdapter = moshi.adapter(MultiviewTypeAdapter.class);

    MultiviewTypeAdapter myAdapter = new MultiviewTypeAdapter(testCells(0));
    String init = jsonAdapter.toJson(myAdapter);

    MultiviewTypeAdapter mRecyclerViewAdapter;
    try {
      MultiviewTypeAdapter config = jsonAdapter.fromJson(init);
      mRecyclerViewAdapter = new MultiviewTypeAdapter(Objects.requireNonNull(config).mCell);
      //Log.d("Dynamic", "import: " + init);
    } catch (IOException e) {
      Log.e("Dynamic", "Error parsing JSON", e);
      mRecyclerViewAdapter = new MultiviewTypeAdapter(Collections.emptyList());
    }
    return mRecyclerViewAdapter;
  }

  public RecyclerView makeRecyclerView(Context context, View v, int viewId) {
    MultiviewTypeAdapter mAdapter = makeAdapter();
    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);;
    ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);

    RecyclerView mRecyclerView = v.findViewById(viewId);

    // Turn on and off the ability to drag and drop
    if (configPreference.getBoolean("reorder_cells_toggle", false)) {
      mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    } else {
      mItemTouchHelper.attachToRecyclerView(null);
    }

    // Turn on and off the grid layout based on the saved value
    if (configPreference.getBoolean("grid_toggle", true)) {
      mRecyclerView.setLayoutManager(new GridLayoutManager(context, 2) {
        @Override
        public boolean canScrollVertically() {
          return true;
        }
      });
    } else {
      mRecyclerView.setLayoutManager(new LinearLayoutManager(context) {
        @Override
        public boolean canScrollVertically() {
          return true;
        }
      });
    }

    RecyclerView.ItemDecoration itemDecoration = new
        DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
    mRecyclerView.addItemDecoration(itemDecoration);
    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.getRecycledViewPool().setMaxRecycledViews(1,0);
    return mRecyclerView;
  }

  public void layoutMaker(String import_json, View v, RecyclerView mRecyclerView) {
    String layoutJson = import_json.split("\\^")[0];
    import_cells(layoutJson, mRecyclerView);
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