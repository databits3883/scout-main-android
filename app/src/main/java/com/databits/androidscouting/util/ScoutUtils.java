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
import android.widget.TableLayout;
import android.widget.TableRow;
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

  public static final int NONE = 0;
  public static final int AUTO = 1;
  public static final int TELEOP = 2;
  public static final int BOTH = 3;

  String[] cellTypes = {"YesNo", "Counter","DoubleCounter", "Segment", "List", "Text", "Special"};
  String[] cellTitles = {"YesNo_title", "Counter_Title", "DoubleCounter_Title", "Segment_Title", "List_Title", "Textbox_title"};
  String[] topTitles = { "Left\nSide", "Autonomous\nCenter Side", "Right\nSide" };
  String[] botTitles = { "Left\nSide", "Teleop\nCenter Side", "Right\nSide" };

  public static final int REQUEST_CODE_PERMISSIONS = 10;
  public static final String[] REQUIRED_PERMISSIONS = { android.Manifest.permission.CAMERA };

  Preference configPreference = PowerPreference.getFileByName("Config");
  Preference debugPreference = PowerPreference.getFileByName("Debug");
  Preference listPreference = PowerPreference.getFileByName("List");

  MatchInfo matchInfo;
  TeamInfo teamInfo;

  TableLayout[] tables;

  List<Cell> cellList = new ArrayList<>();

  Context context;

  public ScoutUtils(Context context) {
    this.context = context;
  }

  private TableLayout[] provideTables (View v) {
    View[] topTables = {v.findViewById(R.id.left_table), v.findViewById(R.id.center_table), v.findViewById(R.id.right_table)};
    View[] botTables = {v.findViewById(R.id.bot_left_table), v.findViewById(R.id.bot_center_table), v.findViewById(R.id.bot_right_table)};

    tables = new TableLayout[]{
        botTables[0].findViewById(R.id.inner_table),
        botTables[1].findViewById(R.id.inner_table),
        botTables[2].findViewById(R.id.inner_table),
        topTables[0].findViewById(R.id.inner_table),
        topTables[1].findViewById(R.id.inner_table),
        topTables[2].findViewById(R.id.inner_table),
    };

    return tables;
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

  public String exportTable(View v) {
    // Get the tables
    tables = provideTables(v);

    // Export the table as a string
    StringBuilder export = new StringBuilder();
    for (TableLayout table : tables) {
      // 0 is the title row, so start at 1
      for (int i = 1; i < table.getChildCount(); i++) {
        TableRow row = (TableRow) table.getChildAt(i);
        for (int j = 0; j < row.getChildCount(); j++) {
          ImageButton button = (ImageButton) row.getChildAt(j);
          Drawable.ConstantState curDraw = button.getDrawable().getConstantState();
          int id = 0;
          // Loop through all buttons and set id based on which drawable is currently set
          if (!curDraw.equals(
              ContextCompat.getDrawable(context, R.drawable.android_x).getConstantState())) {
            if (curDraw.equals(ContextCompat.getDrawable(context, R.drawable.red_cube44).getConstantState())
                || curDraw.equals(ContextCompat.getDrawable(context, R.drawable.cube44).getConstantState())) {
              id = 1;
            } else if (curDraw.equals(ContextCompat.getDrawable(context, R.drawable.red_cone44).getConstantState())
                || curDraw.equals(ContextCompat.getDrawable(context, R.drawable.cone44).getConstantState())) {
              id = 2;
            }
          }

          // Append the id to the export string with a comma separator except for the last table in the list
          if (table == tables[tables.length - 1] && i == table.getChildCount() - 1 && j == row.getChildCount() - 1) {
            export.append(id);
          } else {
            export.append(id).append(",");
          }
        }
      }
    }
    return export.toString();
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
      cellData = exportCell(v.findViewById(R.id.recycler_view_top)).substring(1) + "," +
          teamInfo.getScouterName();
    } else if (special){
      cellData = exportCell(v.findViewById(R.id.recycler_view_top)).substring(1) + "," +
          teamInfo.getScouterName();
    } else {
      cellData = team + "," + match + "," + exportCell(v.findViewById(R.id.recycler_view_top))
          .substring(1) + "," + teamInfo.getScouterName();
    }
    return cellData;
  }

  public void tableSorter(int table_status, View v, RecyclerView mRecyclerViewTop,
      RecyclerView mRecyclerViewBot) {
    RecyclerView recyclerViewTop = v.findViewById(R.id.recycler_view_top);
    ViewGroup.LayoutParams topParam = recyclerViewTop.getLayoutParams();
    ConstraintLayout constraintLayout = v.findViewById(R.id.Dynamic_layout);
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(constraintLayout);

    // Get the tables
    tables = provideTables(v);

    TextView title;
    //Switch statement to sort the tables based on the table status
    switch (table_status) {
      case NONE:
        // recyclerViewTop attached to parent top
        // recyclerViewBot GONE
        constraintSet.connect(R.id.recycler_view_top, ConstraintSet.TOP, ConstraintSet.PARENT_ID,
            ConstraintSet.TOP, 0);
        constraintSet.setVisibility(R.id.recycler_view_bot, View.VISIBLE);
        topParam.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        for (int j = 0; j < 6; j++) {
          tables[j].setVisibility(View.GONE);
        }
        title = tables[4].getChildAt(0).findViewById(R.id.table_title);
        title.setText("Auto\nCenter Side");
        mRecyclerViewTop.setVisibility(View.VISIBLE);
        mRecyclerViewBot.setVisibility(View.GONE);
        break;
      case AUTO:
        // recyclerViewTop attached to parent top
        // recyclerViewBot GONE
        constraintSet.connect(R.id.recycler_view_top, ConstraintSet.TOP, R.id.bot_center_table,
            ConstraintSet.BOTTOM, 0);
        constraintSet.setVisibility(R.id.recycler_view_bot, View.GONE);
        topParam.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        for (int j = 3; j < 6; j++) {
          tables[j].setVisibility(View.GONE);
          for (int k = 0; k < 3; k++) {
            tables[k].setVisibility(View.VISIBLE);
          }
          title = tables[4].getChildAt(0).findViewById(R.id.table_title);
          title.setText("Auto\nCenter Side");
          mRecyclerViewTop.setVisibility(View.VISIBLE);
          mRecyclerViewBot.setVisibility(View.GONE);
        }
        break;
      case TELEOP:
        // recyclerViewTop attached to parent top
        // recyclerViewBot GONE
        constraintSet.connect(R.id.recycler_view_top, ConstraintSet.TOP, R.id.bot_center_table,
            ConstraintSet.BOTTOM, 0);
        constraintSet.setVisibility(R.id.recycler_view_bot, View.GONE);
        topParam.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        for (int j = 3; j < 6; j++) {
          tables[j].setVisibility(View.GONE);
          for (int k = 0; k < 3; k++) {
            tables[k].setVisibility(View.VISIBLE);
          }
          title = tables[4].getChildAt(0).findViewById(R.id.table_title);
          title.setText("Teleop\nCenter Side");
          mRecyclerViewTop.setVisibility(View.VISIBLE);
          mRecyclerViewBot.setVisibility(View.GONE);
        }
        break;
      case BOTH:
        // recyclerViewTop attached to parent top
        // recyclerViewBot attached to center_table
        constraintSet.connect(R.id.recycler_view_top, ConstraintSet.TOP, R.id.center_table,
            ConstraintSet.BOTTOM, 0);
        constraintSet.connect(R.id.recycler_view_bot, ConstraintSet.TOP, R.id.bot_center_table,
            ConstraintSet.BOTTOM, 0);
        //FIXME Do not tie directly to crowdScoutFragment and remove table support
        constraintSet.connect(R.id.inner_table, ConstraintSet.TOP, R.id.crowdScoutFragment,
            ConstraintSet.TOP, 0);
        constraintSet.setVisibility(R.id.recycler_view_top, View.VISIBLE);
        constraintSet.setVisibility(R.id.recycler_view_bot, View.VISIBLE);
        topParam.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        for (int j = 0; j < 6; j++) {
          tables[j].setVisibility(View.VISIBLE);
        }
        mRecyclerViewTop.setVisibility(View.VISIBLE);
        mRecyclerViewBot.setVisibility(View.VISIBLE);
        break;
    }
  }

  public void setupTables(View v) {
    View[] topTables = {
        v.findViewById(R.id.left_table), v.findViewById(R.id.center_table),
        v.findViewById(R.id.right_table)
    };
    View[] botTables = {
        v.findViewById(R.id.bot_left_table), v.findViewById(R.id.bot_center_table),
        v.findViewById(R.id.bot_right_table)
    };

    tables = new TableLayout[] {
        topTables[0].findViewById(R.id.inner_table),
        topTables[1].findViewById(R.id.inner_table),
        topTables[2].findViewById(R.id.inner_table),
        botTables[0].findViewById(R.id.inner_table),
        botTables[1].findViewById(R.id.inner_table),
        botTables[2].findViewById(R.id.inner_table),
    };

    // Set Table Auto/Teleop titles
    for (int i = 0; i < 3; i++) {
      TextView topText = topTables[i].findViewById(R.id.table_title);
      TextView botText = botTables[i].findViewById(R.id.table_title);
      topText.setTextSize(20);
      topText.setTextColor(Color.WHITE);
      botText.setTextSize(20);
      botText.setTextColor(Color.WHITE);
      topText.setText(topTitles[i]);
      botText.setText(botTitles[i]);
    }

    // Set on click listeners for all buttons to cycle between icons
    // Uses the team color to determine which icon to use
    for (TableLayout table : tables) {
      if (debugPreference.getBoolean("isRedteam")) {
        updateTableColor(table, android.R.color.holo_red_dark);
      } else {
        updateTableColor(table, R.color.map_blue);
      }
      for (int i = 1; i < table.getChildCount(); i++) {
        TableRow row = (TableRow) table.getChildAt(i);
        for (int j = 0; j < row.getChildCount(); j++) {
          ImageButton button = (ImageButton) row.getChildAt(j);

          if (i == 3) {
            button.setTag("Both");
          } else {
            if (j == 0) {
              button.setTag("Cone");
            } else if (j == 1) {
              button.setTag("Cube");
            } else if (j == 2) {
              button.setTag("Cone");
            }
          }

          button.setImageResource(R.drawable.android_x);
          final int[] counter = { 1 };
          button.setOnClickListener(v1 -> {
            boolean red = debugPreference.getBoolean("isRedteam");
            String curTag = String.valueOf(button.getTag());
            switch (curTag) {
              case "Both":
                if (counter[0] == 0) {
                  button.setImageResource(R.drawable.android_x);
                  counter[0]++;
                } else if (counter[0] == 1) {
                  if (red) {
                    button.setImageResource(R.drawable.red_cube44);
                  } else {
                    button.setImageResource(R.drawable.cube44);
                  }
                  counter[0]++;
                } else if (counter[0] == 2) {
                  if (red) {
                    button.setImageResource(R.drawable.red_cone44);
                  } else {
                    button.setImageResource(R.drawable.cone44);
                  }
                  counter[0] = 0;
                }
                break;
              case "Cone":
                if (counter[0] == 0) {
                  button.setImageResource(R.drawable.android_x);
                  counter[0]++;
                } else if (counter[0] == 1) {
                  if (red) {
                    button.setImageResource(R.drawable.red_cone44);
                  } else {
                    button.setImageResource(R.drawable.cone44);
                  }
                  counter[0] = 0;
                }
                break;
              case "Cube":
                if (counter[0] == 0) {
                  button.setImageResource(R.drawable.android_x);
                  counter[0]++;
                } else if (counter[0] == 1) {
                  if (red) {
                    button.setImageResource(R.drawable.red_cube44);
                  } else {
                    button.setImageResource(R.drawable.cube44);
                  }
                  counter[0] = 0;
                }
                break;
            }
          });
        }
      }
    }
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

  public void updateTableColor(TableLayout table, int colorResId) {
    // All row background color set
    TableRow row = (TableRow) table.getChildAt(0);
    row.setBackgroundColor(ContextCompat.getColor(context, colorResId));

    // Title in top row
    TextView title = row.findViewById(R.id.table_title);
    title.setTextColor(ContextCompat.getColor(context, R.color.white));

    ImageView icon = row.findViewById(R.id.table_image);
    icon.setOnClickListener(v -> {
      Balloon.Builder helpBuilder = new Balloon.Builder(context)
          .setArrowSize(10)
          .setArrowOrientation(ArrowOrientation.TOP)
          .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
          .setArrowPosition(0.5f)
          .setWidth(BalloonSizeSpec.WRAP)
          .setHeight(BalloonSizeSpec.WRAP)
          .setPadding(6)
          .setTextSize(20f)
          .setCornerRadius(4f)
          .setAlpha(0.8f)
          .setTextColor(ContextCompat.getColor(context, R.color.white))
          .setBalloonAnimation(BalloonAnimation.FADE)
          .setText("This is the top side where the drivers stand");
       helpBuilder.build().showAlignBottom(icon);
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

  public void layoutMaker(int table_status, String import_json, View v, RecyclerView mRecyclerViewTop, RecyclerView mRecyclerViewBot) {
    String top;
    String bot;

    tableSorter(table_status, v, mRecyclerViewTop, mRecyclerViewBot);

    if (table_status == NONE || table_status == AUTO || table_status == TELEOP) {
      top = import_json.split("\\^")[0];
      import_cells(top, mRecyclerViewTop);
    } else {
      top = import_json.split("\\^")[0];
      bot = import_json.split("\\^")[1];
      import_cells(top, mRecyclerViewTop);
      import_cells(bot, mRecyclerViewBot);
    }

    Button importButton = v.findViewById(R.id.importButton);
    Button loadButton = v.findViewById(R.id.loadButton);
    //if (!import_json.equals("")) {
    //  importButton.setVisibility(View.GONE);
    //  loadButton.setVisibility(View.GONE);
    //
    //} else {
    //  importButton.setVisibility(View.VISIBLE);
    //  loadButton.setVisibility(View.VISIBLE);
    //}
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