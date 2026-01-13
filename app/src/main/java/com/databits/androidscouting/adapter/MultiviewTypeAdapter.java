package com.databits.androidscouting.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.addisonelliott.segmentedbutton.SegmentedButton;
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.databits.androidscouting.R;
import com.databits.androidscouting.data.repository.PowerPreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.databits.androidscouting.model.Cell;
import com.databits.androidscouting.model.CellConfig;
import com.databits.androidscouting.model.CellType;
import com.databits.androidscouting.model.CellCategory;
import com.databits.androidscouting.model.HelpPictureType;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.TeamInfo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.travijuu.numberpicker.library.NumberPicker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MultiviewTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
    ItemTouchHelperAdapter {

    public List<Cell> mCell;
    List<String> entryLabels = new ArrayList<>();
    private TeamInfo teamInfo;
    private MatchInfo matchInfo;
    private PreferenceRepository repository;
    private LayoutInflater inflater;
    private Balloon.Builder helpBuilder;

    // Cached pit teams remaining list to avoid database access in onBindViewHolder
    private List<String> cachedPitTeamsRemainingList = null;

    // Cached team numbers for SPECIAL cell to avoid database access in onBindViewHolder
    // Index 0 unused, indices 1-6 correspond to positions 1-6 (Blue 1-3, Red 4-6)
    private String[] cachedSpecialTeamNumbers = null;


    public static class YesNoTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        SegmentedButtonGroup group;
        LinearLayout categoryColor;
        public YesNoTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.yesno_title);
            this.group = itemView.findViewById(R.id.buttonGroup_yes_no);
            this.categoryColor = itemView.findViewById(R.id.category_color);
        }
    }

    public static class TextTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextInputLayout textInputLayout;
        TextInputEditText editText;
        ImageButton help;
        LinearLayout categoryColor;
        public TextTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.textbox_title);
            this.textInputLayout = itemView.findViewById(R.id.textbox_text_layout);
            this.editText = itemView.findViewById(R.id.enteredText);
            this.categoryColor = itemView.findViewById(R.id.category_color);
        }
    }

    public static class CounterTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        NumberPicker currentPicker;
        LinearLayout categoryColor;
        public CounterTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.counter_title);
            this.currentPicker = itemView.findViewById(R.id.number_counter_inside);
            this.categoryColor = itemView.findViewById(R.id.category_color);
        }
    }

    public static class DoubleCounterTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        NumberPicker counterOne, counterTwo;
        LinearLayout categoryColor;
        public DoubleCounterTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.double_counter_title);
            this.counterOne = itemView.findViewById(R.id.number_counter_one);
            this.counterTwo = itemView.findViewById(R.id.number_counter_two);
            this.categoryColor = itemView.findViewById(R.id.category_color);
        }
    }

    public static class DualCounterTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        NumberPicker counterOne, counterTwo;
        LinearLayout categoryColor;
        public DualCounterTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.counter_title);
            this.counterOne = itemView.findViewById(R.id.counterOne);
            this.counterTwo = itemView.findViewById(R.id.counterTwo);
            this.categoryColor = itemView.findViewById(R.id.category_color);
        }
    }

    public static class SegmentTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        SegmentedButton one,two,three,four,five,six;
        SegmentedButtonGroup group;
        LinearLayout categoryColor;

        public SegmentTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.segment_title);
            this.one = itemView.findViewById(R.id.button_one);
            this.two = itemView.findViewById(R.id.button_two);
            this.three = itemView.findViewById(R.id.button_three);
            this.four = itemView.findViewById(R.id.button_four);
            this.five = itemView.findViewById(R.id.button_five);
            this.six = itemView.findViewById(R.id.button_six);
            this.group = itemView.findViewById(R.id.buttonGroup_segments);
            this.categoryColor = itemView.findViewById(R.id.category_color);
        }
    }

    public static class ListTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        Spinner spinner;
        LinearLayout categoryColor;
        public ListTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.list_title);
            this.spinner = itemView.findViewById(R.id.spinner);
            this.categoryColor = itemView.findViewById(R.id.category_color);
        }
    }

    public static class TeamSelectTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        Spinner spinner;
        LinearLayout categoryColor;
        public TeamSelectTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.teamselect_title);
            this.spinner = itemView.findViewById(R.id.spinner);
            this.categoryColor = itemView.findViewById(R.id.category_color);
        }
    }

    public static class SpecialTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        LinearLayout categoryColor;
        SegmentedButtonGroup teamSelector;
        NumberPicker algaeMiss;
        NumberPicker algaeSuccess;
        NumberPicker algaeReturned;
        final SegmentedButton[] specialSegmentedButtons;

        public SpecialTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.special_title);
            this.categoryColor = itemView.findViewById(R.id.category_color);
            this.teamSelector = itemView.findViewById(R.id.buttonGroup_segments);
            this.algaeMiss = itemView.findViewById(R.id.counterUI1).findViewById(R.id.number_counter_inside);
            this.algaeSuccess = itemView.findViewById(R.id.counterUI2).findViewById(R.id.number_counter_inside);
            this.algaeReturned = itemView.findViewById(R.id.counterUI3).findViewById(R.id.number_counter_inside);
            this.specialSegmentedButtons = new SegmentedButton[]{
                teamSelector.findViewById(R.id.button_one),
                teamSelector.findViewById(R.id.button_two),
                teamSelector.findViewById(R.id.button_three),
                teamSelector.findViewById(R.id.button_four),
            };
        }
    }

    public MultiviewTypeAdapter(List<Cell> cells) {
        this.mCell = cells;
    }

    /**
     * Load pit teams remaining list on background thread and cache it.
     * Call this method after creating the adapter to populate the cache.
     */
    public void loadPitTeamsRemainingCache() {
        new Thread(() -> {
            if (repository != null) {
                cachedPitTeamsRemainingList = repository.getPitTeamsRemainingList();
            }
        }).start();
    }

    /**
     * Update the cached pit teams remaining list.
     * Call this method when the list changes (e.g., after removing a team).
     */
    public void updatePitTeamsRemainingCache(List<String> newList) {
        cachedPitTeamsRemainingList = newList;
    }

    /**
     * Load team numbers for SPECIAL cell on background thread and cache them.
     * Call this method after creating the adapter to populate the cache.
     * Loads all 6 team positions (1-6) for the current match.
     */
    public void loadSpecialTeamNumbersCache() {
        new Thread(() -> {
            if (teamInfo != null && matchInfo != null) {
                String[] teams = new String[7]; // Index 0 unused, 1-6 for positions
                for (int i = 1; i <= 6; i++) {
                    try {
                        teams[i] = teamInfo.getMasterTeam(matchInfo.getMatch(), i);
                    } catch (Exception e) {
                        teams[i] = "Team " + i; // Fallback on error
                    }
                }
                cachedSpecialTeamNumbers = teams;

                // Notify adapter that data has changed (on main thread)
                if (inflater != null && inflater.getContext() instanceof android.app.Activity) {
                    ((android.app.Activity) inflater.getContext()).runOnUiThread(() -> {
                        notifyDataSetChanged();
                    });
                }
            }
        }).start();
    }

    /**
     * Update the cached special team numbers.
     * Call this method when match changes or schedule is updated.
     */
    public void updateSpecialTeamNumbersCache(String[] newTeamNumbers) {
        if (newTeamNumbers != null && newTeamNumbers.length == 7) {
            cachedSpecialTeamNumbers = newTeamNumbers;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null) {
            Context context = parent.getContext();
            inflater = LayoutInflater.from(context);
            teamInfo = new TeamInfo(context);
            matchInfo = new MatchInfo();
            repository = PowerPreferenceRepository.getInstance();
            helpBuilder = new Balloon.Builder(context)
                .setArrowSize(15)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setArrowPosition(0.5f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setPadding(6)
                .setTextSize(20f)
                .setCornerRadius(4f)
                .setTextColor(ContextCompat.getColor(context, R.color.white))
                .setBalloonAnimation(BalloonAnimation.CIRCULAR)
                .setLayout(R.layout.help_bubble_layout)
                .setDismissWhenClicked(true)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }
        View view;
        switch (viewType) {
            case 0:
                view = inflater.inflate(R.layout.card_yesno,
                    parent,false);
                return new YesNoTypeViewHolder(view);
            case 1:
                view = inflater.inflate(R.layout.card_textbox,
                    parent,false);
                return new TextTypeViewHolder(view);
            case 2:
                view = inflater.inflate(R.layout.card_counter,
                    parent,false);
                return new CounterTypeViewHolder(view);
            case 3:
                view = inflater.inflate(R.layout.card_segment,
                    parent,false);
                return new SegmentTypeViewHolder(view);
            case 4:
                view = inflater.inflate(R.layout.card_list,
                    parent,false);
                return new ListTypeViewHolder(view);
            case 5:
                view = inflater.inflate(R.layout.card_teamselect,
                    parent,false);
                return new TeamSelectTypeViewHolder(view);
            case 6:
                view = inflater.inflate(R.layout.card_dualcounter,
                    parent,false);
                return new DualCounterTypeViewHolder(view);
            case 7:
                view = inflater.inflate(R.layout.card_special,
                    parent,false);
                return new SpecialTypeViewHolder(view);
            case 8:
                view = inflater.inflate(R.layout.card_doublecounter,
                    parent,false);
                return new DoubleCounterTypeViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        CellType cellType = mCell.get(position).getType();
        switch (cellType) {
            case YES_NO:
                return 0;
            case TEXT:
                return 1;
            case COUNTER:
                return 2;
            case SEGMENT:
                return 3;
            case LIST:
                return 4;
            case TEAM_SELECT:
                return 5;
            case DUAL_COUNTER:
                return 6;
            case SPECIAL:
                return 7;
            case DOUBLE_COUNTER:
                return 8;
            case TITLE:
                return 9;
            default:
                return -1;
        }
    }

    private void bindHelpBalloon(ImageButton helpButton, CellConfig object, Drawable helpPicture) {
        helpButton.setOnClickListener(view -> {
            Balloon helpBalloon = helpBuilder.build();
            TextView helpTitle = helpBalloon.getContentView().findViewById(R.id.help_title);
            TextView helpContent = helpBalloon.getContentView().findViewById(R.id.help_content);
            ImageView helpImage = helpBalloon.getContentView().findViewById(R.id.help_image);
            helpTitle.setText(object.getHelpTitle());
            helpContent.setText(object.getHelpText());
            helpImage.setImageDrawable(helpPicture);
            helpBalloon.showAlignBottom(helpButton);
        });
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int listPosition) {
        CellConfig object = mCell.get(listPosition).getConfig();

        String title_text = mCell.get(listPosition).getTitle();

        Context mContext = holder.itemView.getContext();

        // Get category color from enum (eliminates 15-line switch statement)
        CellCategory category = object.getCategory();
        int categoryColor = category.getColorRes();

        // Get help picture from enum (eliminates 30-line switch statement)
        HelpPictureType helpPictureType = object.getHelpPicture();
        Drawable helpPicture = AppCompatResources.getDrawable(mContext, helpPictureType.getDrawableRes());

        if (object != null) {
            CellType cellType = mCell.get(listPosition).getType();
            switch (cellType) {
                case YES_NO:
                    YesNoTypeViewHolder yesnoHolder = (YesNoTypeViewHolder) holder;
                    bindHelpBalloon(yesnoHolder.help, object, helpPicture);
                    yesnoHolder.title.setText(title_text);
                    yesnoHolder.group.setOnPositionChangedListener(position -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            yesnoHolder.group.performHapticFeedback(
                                HapticFeedbackConstants.CONFIRM);
                        } else {
                            yesnoHolder.group.performHapticFeedback(
                                HapticFeedbackConstants.LONG_PRESS);
                        }
                    });
                    yesnoHolder.categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case TEXT:
                    TextTypeViewHolder textHolder = (TextTypeViewHolder) holder;
                    bindHelpBalloon(textHolder.help, object, helpPicture);
                    textHolder.title.setText(title_text);
                    textHolder.editText.setSingleLine(true);
                    textHolder.textInputLayout.setId(R.id.textbox_text_layout);

                    if (!object.isTextHidden()) {
                        textHolder.textInputLayout.setHint(object.getTextHint());
                    }

                    textHolder.categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case COUNTER:
                    CounterTypeViewHolder counterHolder = (CounterTypeViewHolder) holder;
                    bindHelpBalloon(counterHolder.help, object, helpPicture);
                    counterHolder.title.setText(title_text);
                    counterHolder.currentPicker.setMax(object.getMax());
                    counterHolder.currentPicker.setMin(object.getMin());
                    counterHolder.currentPicker.setUnit(object.getUnit());
                    counterHolder.currentPicker.setValue(object.getDefaultValue());
                    counterHolder.currentPicker.setFocusable(false);
                    counterHolder.categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case DOUBLE_COUNTER:
                    DoubleCounterTypeViewHolder doubleCounterHolder = (DoubleCounterTypeViewHolder) holder;
                    bindHelpBalloon(doubleCounterHolder.help, object, helpPicture);
                    doubleCounterHolder.title.setText(title_text);
                    doubleCounterHolder.counterOne.setMax(object.getMax());
                    doubleCounterHolder.counterOne.setMin(object.getMin());
                    doubleCounterHolder.counterOne.setUnit(object.getUnit());
                    doubleCounterHolder.counterOne.setValue(object.getDefaultValue());
                    doubleCounterHolder.counterOne.setFocusable(false);

                    doubleCounterHolder.counterTwo.setMax(object.getMax());
                    doubleCounterHolder.counterTwo.setMin(object.getMin());
                    doubleCounterHolder.counterTwo.setUnit(object.getUnit());
                    doubleCounterHolder.counterTwo.setValue(object.getDefaultValue());
                    doubleCounterHolder.counterTwo.setFocusable(false);
                    doubleCounterHolder.categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case DUAL_COUNTER:
                    DualCounterTypeViewHolder dualCounterHolder = (DualCounterTypeViewHolder) holder;
                    bindHelpBalloon(dualCounterHolder.help, object, helpPicture);
                    dualCounterHolder.title.setText(title_text);
                    dualCounterHolder.counterOne.setMax(object.getMax());
                    dualCounterHolder.counterOne.setMin(object.getMin());
                    dualCounterHolder.counterOne.setUnit(object.getUnit());
                    dualCounterHolder.counterOne.setValue(object.getDefaultValue());
                    dualCounterHolder.counterOne.setFocusable(false);

                    dualCounterHolder.counterTwo.setMax(object.getMax());
                    dualCounterHolder.counterTwo.setMin(object.getMin());
                    dualCounterHolder.counterTwo.setUnit(object.getUnit());
                    dualCounterHolder.counterTwo.setValue(object.getDefaultValue());
                    dualCounterHolder.counterTwo.setFocusable(false);
                    dualCounterHolder.categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case SEGMENT:
                    SegmentTypeViewHolder segmentHolder = (SegmentTypeViewHolder) holder;
                    bindHelpBalloon(segmentHolder.help, object, helpPicture);
                    segmentHolder.title.setText(title_text);

                    int segmentCount = object.getSegments();

                    SegmentedButton[] segmentedButtons = {
                        segmentHolder.one,
                        segmentHolder.two,
                        segmentHolder.three,
                        segmentHolder.four,
                        segmentHolder.five,
                        segmentHolder.six
                    };

                    int visibleSegmentCount = Math.min(object.getSegmentLabels().size(), segmentCount);

                    for (int i = 0; i < visibleSegmentCount; i++) {
                        segmentedButtons[i].setText(object.getSegmentLabels().get(i));
                        segmentedButtons[i].setVisibility(View.VISIBLE);
                    }

                    for (int i = visibleSegmentCount; i < segmentedButtons.length; i++) {
                        segmentedButtons[i].setVisibility(View.GONE);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        segmentHolder.group.performHapticFeedback(
                            HapticFeedbackConstants.CONFIRM);
                    } else {
                        segmentHolder.group.performHapticFeedback(
                            HapticFeedbackConstants.LONG_PRESS);
                    }
                    segmentHolder.categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case LIST:
                    ListTypeViewHolder listHolder = (ListTypeViewHolder) holder;
                    bindHelpBalloon(listHolder.help, object, helpPicture);
                    listHolder.title.setText(title_text);

                    // Loop through add each item to entryLabels
                    for (int i = 0; i < object.getTotalEntries(); i++) {
                        entryLabels.add(object.getEntryLabels().get(i));
                    }
                    ArrayAdapter<String> listspinnerArrayAdapter = new ArrayAdapter<>(mContext,
                        android.R.layout.simple_spinner_item, entryLabels);
                    listspinnerArrayAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                    listHolder.spinner.setAdapter(listspinnerArrayAdapter);
                    listHolder.spinner.setTag("Spinner");
                    listHolder.spinner.setOnItemClickListener((parent, view, position, id) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                        } else {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        }
                    });
                    listHolder.categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case TEAM_SELECT:
                    TeamSelectTypeViewHolder teamSelectHolder = (TeamSelectTypeViewHolder) holder;
                    bindHelpBalloon(teamSelectHolder.help, object, helpPicture);

                    teamSelectHolder.title.setText(R.string.select_team_title);

                    teamSelectHolder.categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));

                    // Use cached pit teams list to avoid database access on main thread
                    if (repository.isPitRemoveEnabled() && cachedPitTeamsRemainingList != null) {
                        entryLabels = cachedPitTeamsRemainingList;
                    } else {
                        entryLabels = Arrays.asList(mContext.getResources().getStringArray(
                            R.array.team_list));;
                    }

                    teamSelectHolder.spinner.setTag("TeamSpinner");

                    ArrayAdapter<String> teamselectspinnerArrayAdapter = new ArrayAdapter<>(mContext,
                        android.R.layout.simple_spinner_item, entryLabels);
                    teamselectspinnerArrayAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                    teamSelectHolder.spinner.setAdapter(
                        teamselectspinnerArrayAdapter);
                    break;
                case SPECIAL:
                    SpecialTypeViewHolder specialHolder = (SpecialTypeViewHolder) holder;
                    bindHelpBalloon(specialHolder.help, object, helpPicture);

                    specialHolder.categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));

                    specialHolder.algaeMiss.setValue(0);
                    specialHolder.algaeSuccess.setValue(0);
                    specialHolder.algaeReturned.setValue(0);

                    int specialSegmentCount = object.getSegments();
                    SegmentedButton[] specialSegmentedButtons = specialHolder.specialSegmentedButtons;


                    int visibleSpecialSegmentCount = Math.min(object.getSegmentLabels().size(),
                        specialSegmentCount);

                    for (int i = 0; i < visibleSpecialSegmentCount; i++) {
                        specialSegmentedButtons[i].setText(object.getSegmentLabels().get(i));
                        specialSegmentedButtons[i].setVisibility(View.VISIBLE);
                    }

                    for (int i = visibleSpecialSegmentCount; i < specialSegmentedButtons.length; i++) {
                        specialSegmentedButtons[i].setVisibility(View.GONE);
                    }

                    specialHolder.teamSelector.setPosition(3,false);
                    String teamColor = object.getSpecialTeamColor();

                    // Use cached team numbers to avoid database access on main thread
                    String teamNumberOne;
                    String teamNumberTwo;
                    String teamNumberThree;
                    String teamNumberFour = "No Team";

                    // Get team numbers from cache based on team color
                    if(cachedSpecialTeamNumbers != null && cachedSpecialTeamNumbers.length == 7){
                        if(teamColor != null && teamColor.equals("Blue")){
                            // Blue alliance: positions 1, 2, 3
                            teamNumberOne = cachedSpecialTeamNumbers[1];
                            teamNumberTwo = cachedSpecialTeamNumbers[2];
                            teamNumberThree = cachedSpecialTeamNumbers[3];
                        } else { // Red alliance: positions 4, 5, 6
                            teamNumberOne = cachedSpecialTeamNumbers[4];
                            teamNumberTwo = cachedSpecialTeamNumbers[5];
                            teamNumberThree = cachedSpecialTeamNumbers[6];
                        }
                    } else {
                        // Fallback if cache not loaded yet
                        teamNumberOne = "Team 1";
                        teamNumberTwo = "Team 2";
                        teamNumberThree = "Team 3";
                    }

                    specialSegmentedButtons[0].setText(teamNumberOne);
                    specialSegmentedButtons[1].setText(teamNumberTwo);
                    specialSegmentedButtons[2].setText(teamNumberThree);
                    specialSegmentedButtons[3].setText(teamNumberFour);

                    specialHolder.title.setText(
                        String.format("%s Team", teamColor));
                    break;

            }
        }
    }

    @Override
    public int getItemCount() {
        return mCell.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // Move the item in the list and notify the adapter that the item has moved
        Collections.swap(mCell, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @Override
    public void onItemSelected() {

    }

    @Override
    public void onItemClear() {

    }
}
