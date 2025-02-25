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
import com.databits.androidscouting.model.Cell;
import com.databits.androidscouting.model.CellParam;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.TeamInfo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.preference.PowerPreference;
import com.preference.Preference;
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

        public SpecialTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.special_title);
            this.categoryColor = itemView.findViewById(R.id.category_color);
            this.teamSelector = itemView.findViewById(R.id.buttonGroup_segments);
            this.algaeMiss = itemView.findViewById(R.id.counterUI1).findViewById(R.id.number_counter_inside);
            this.algaeSuccess = itemView.findViewById(R.id.counterUI2).findViewById(R.id.number_counter_inside);
            this.algaeReturned = itemView.findViewById(R.id.counterUI3).findViewById(R.id.number_counter_inside);
        }
    }

    public MultiviewTypeAdapter(List<Cell> cells) {
        this.mCell = cells;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_yesno,
                    parent,false);
                return new YesNoTypeViewHolder(view);
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_textbox,
                    parent,false);
                return new TextTypeViewHolder(view);
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_counter,
                    parent,false);
                return new CounterTypeViewHolder(view);
            case 3:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_segment,
                    parent,false);
                return new SegmentTypeViewHolder(view);
            case 4:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_list,
                    parent,false);
                return new ListTypeViewHolder(view);
            case 5:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_teamselect,
                    parent,false);
                return new TeamSelectTypeViewHolder(view);
            case 6:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_dualcounter,
                    parent,false);
                return new DualCounterTypeViewHolder(view);
            case 7:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_special,
                    parent,false);
                return new SpecialTypeViewHolder(view);
            case 8:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_doublecounter,
                    parent,false);
                return new DoubleCounterTypeViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        switch (mCell.get(position).getType()) {
            case "YesNo":
                return 0;
            case "Text":
                return 1;
            case "Counter":
                return 2;
            case "Segment":
                return 3;
            case "List":
                return 4;
            case "TeamSelect":
                return 5;
            case "DualCounter":
                return 6;
            case "Special":
                return 7;
            case "DoubleCounter":
                return 8;
            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int listPosition) {
        CellParam object = mCell.get(listPosition).getParam();

        String title_text = mCell.get(listPosition).getTitle();

        Context mContext = holder.itemView.getContext();

        TeamInfo teamInfo = new TeamInfo(mContext);
        MatchInfo matchInfo = new MatchInfo();

        Preference configPreference = PowerPreference.getFileByName("Config");

        // Common Help balloon settings for all cells
        Balloon.Builder helpBuilder = new Balloon.Builder(mContext)
            .setArrowSize(15)
            .setArrowOrientation(ArrowOrientation.TOP)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowPosition(0.5f)
            .setWidth(BalloonSizeSpec.WRAP)
            .setHeight(BalloonSizeSpec.WRAP)
            .setPadding(6)
            .setTextSize(20f)
            .setCornerRadius(4f)
            .setTextColor(ContextCompat.getColor(mContext, R.color.white))
            .setBalloonAnimation(BalloonAnimation.CIRCULAR)
            .setLayout(R.layout.help_bubble_layout)
            .setDismissWhenClicked(true)
            .setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));

        int categoryColor = 0;

        switch (object.getCellCategory()){
            case "Auto":
                categoryColor = R.color.auto_category;
                break;
            case "Teleop":
                categoryColor = R.color.teleop_category;
                break;
            case "Endgame":
                categoryColor = R.color.endgame_category;
                break;
        }
        Drawable helpPicture = null;
        switch (object.getHelpPictureSelector()){
            case "Speaker":
                helpPicture = AppCompatResources.getDrawable(mContext,R.drawable.speaker);
                break;
            case "Amp":
                helpPicture = AppCompatResources.getDrawable(mContext,R.drawable.amp);
                break;
            case "Stage":
                helpPicture = AppCompatResources.getDrawable(mContext,R.drawable.stage);
                break;
            case "Endgame":
                helpPicture = AppCompatResources.getDrawable(mContext,R.drawable.endgame);
                break;
            case "Feeder":
                helpPicture = AppCompatResources.getDrawable(mContext,R.drawable.feeder);
                break;
            case "Auto":
                helpPicture = AppCompatResources.getDrawable(mContext,R.drawable.auto);
                break;
            case "None":
                helpPicture = AppCompatResources.getDrawable(mContext,
                    com.anggrayudi.storage.R.drawable.md_transparent);
                break;
        }

        if (object != null) {
            switch (object.getType()) {
                case "YesNo":
                    Balloon yesnoHelp = helpBuilder
                        .build();

                    TextView yesnoTitle = yesnoHelp.getContentView().findViewById(R.id.help_title);
                    TextView yesnoContent = yesnoHelp.getContentView().findViewById(R.id.help_content);
                    ImageView yesnoImage = yesnoHelp.getContentView().findViewById(R.id.help_image);
                    yesnoTitle.setText(object.getHelpTitle());
                    yesnoContent.setText(object.getHelpText());
                    yesnoImage.setImageDrawable(helpPicture);

                    ((YesNoTypeViewHolder) holder).title.setText(title_text);
                    ((YesNoTypeViewHolder) holder).help.setOnClickListener(view ->
                        yesnoHelp.showAlignBottom(((YesNoTypeViewHolder) holder).help));
                    ((YesNoTypeViewHolder) holder).group.setOnPositionChangedListener(position -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            ((YesNoTypeViewHolder) holder).group.performHapticFeedback(
                                HapticFeedbackConstants.CONFIRM);
                        } else {
                            ((YesNoTypeViewHolder) holder).group.performHapticFeedback(
                                HapticFeedbackConstants.LONG_PRESS);
                        }
                    });
                    ((YesNoTypeViewHolder) holder).categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case "Text":
                    Balloon textHelp = helpBuilder
                        .build();

                    TextView textTitle = textHelp.getContentView().findViewById(R.id.help_title);
                    TextView textContent = textHelp.getContentView().findViewById(R.id.help_content);
                    ImageView textImage = textHelp.getContentView().findViewById(R.id.help_image);
                    textTitle.setText(object.getHelpTitle());
                    textContent.setText(object.getHelpText());
                    textImage.setImageDrawable(helpPicture);

                    ((TextTypeViewHolder) holder).title.setText(title_text);
                    ((TextTypeViewHolder) holder).editText.setSingleLine(true);
                    ((TextTypeViewHolder) holder).textInputLayout.setId(R.id.textbox_text_layout);

                    if (!object.isTextHidden()) {
                        ((TextTypeViewHolder) holder).textInputLayout.setHint(object.getTextHint());
                    }

                    ((TextTypeViewHolder) holder).help.setOnClickListener(view ->
                        textHelp.showAlignBottom(((TextTypeViewHolder) holder).help));
                    ((TextTypeViewHolder) holder).categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case "Counter":
                    Balloon counterHelp = helpBuilder
                        .build();

                    TextView counterTitle = counterHelp.getContentView().findViewById(R.id.help_title);
                    TextView counterContent = counterHelp.getContentView().findViewById(R.id.help_content);
                    ImageView counterImage = counterHelp.getContentView().findViewById(R.id.help_image);
                    counterTitle.setText(object.getHelpTitle());
                    counterContent.setText(object.getHelpText());
                    counterImage.setImageDrawable(helpPicture);

                    ((CounterTypeViewHolder) holder).title.setText(title_text);
                    ((CounterTypeViewHolder) holder).help.setOnClickListener(view ->
                        counterHelp.showAlignBottom(((CounterTypeViewHolder) holder).help));
                    ((CounterTypeViewHolder) holder).currentPicker.setMax(object.getMax());
                    ((CounterTypeViewHolder) holder).currentPicker.setMin(object.getMin());
                    ((CounterTypeViewHolder) holder).currentPicker.setUnit(object.getUnit());
                    ((CounterTypeViewHolder) holder).currentPicker.setValue(object.getDefault());
                    ((CounterTypeViewHolder) holder).currentPicker.setFocusable(false);
                    ((CounterTypeViewHolder) holder).categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case "DoubleCounter":
                    Balloon doublecounterHelp = helpBuilder
                        .build();

                    TextView doublecounterTitle = doublecounterHelp.getContentView().findViewById(R.id.help_title);
                    TextView doublecounterContent = doublecounterHelp.getContentView().findViewById(R.id.help_content);
                    ImageView doublecounterImage = doublecounterHelp.getContentView().findViewById(R.id.help_image);
                    doublecounterTitle.setText(object.getHelpTitle());
                    doublecounterContent.setText(object.getHelpText());
                    doublecounterImage.setImageDrawable(helpPicture);

                    ((DoubleCounterTypeViewHolder) holder).title.setText(title_text);
                    ((DoubleCounterTypeViewHolder) holder).help.setOnClickListener(view ->
                        doublecounterHelp.showAlignBottom(((DoubleCounterTypeViewHolder) holder).help));
                    ((DoubleCounterTypeViewHolder) holder).counterOne.setMax(object.getMax());
                    ((DoubleCounterTypeViewHolder) holder).counterOne.setMin(object.getMin());
                    ((DoubleCounterTypeViewHolder) holder).counterOne.setUnit(object.getUnit());
                    ((DoubleCounterTypeViewHolder) holder).counterOne.setValue(object.getDefault());
                    ((DoubleCounterTypeViewHolder) holder).counterOne.setFocusable(false);

                    ((DoubleCounterTypeViewHolder) holder).counterTwo.setMax(object.getMax());
                    ((DoubleCounterTypeViewHolder) holder).counterTwo.setMin(object.getMin());
                    ((DoubleCounterTypeViewHolder) holder).counterTwo.setUnit(object.getUnit());
                    ((DoubleCounterTypeViewHolder) holder).counterTwo.setValue(object.getDefault());
                    ((DoubleCounterTypeViewHolder) holder).counterTwo.setFocusable(false);
                    ((DoubleCounterTypeViewHolder) holder).categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case "DualCounter":
                    Balloon dualCounterHelp = helpBuilder
                        .build();

                    TextView dualcounterTitle = dualCounterHelp.getContentView().findViewById(R.id.help_title);
                    TextView dualcounterContent = dualCounterHelp.getContentView().findViewById(R.id.help_content);
                    ImageView dualcounterImage = dualCounterHelp.getContentView().findViewById(R.id.help_image);
                    dualcounterTitle.setText(object.getHelpTitle());
                    dualcounterContent.setText(object.getHelpText());
                    dualcounterImage.setImageDrawable(helpPicture);

                    ((DualCounterTypeViewHolder) holder).title.setText(title_text);
                    ((DualCounterTypeViewHolder) holder).help.setOnClickListener(view ->
                        dualCounterHelp.showAlignBottom(((DualCounterTypeViewHolder) holder).help));
                    ((DualCounterTypeViewHolder) holder).counterOne.setMax(object.getMax());
                    ((DualCounterTypeViewHolder) holder).counterOne.setMin(object.getMin());
                    ((DualCounterTypeViewHolder) holder).counterOne.setUnit(object.getUnit());
                    ((DualCounterTypeViewHolder) holder).counterOne.setValue(object.getDefault());
                    ((DualCounterTypeViewHolder) holder).counterOne.setFocusable(false);

                    ((DualCounterTypeViewHolder) holder).counterTwo.setMax(object.getMax());
                    ((DualCounterTypeViewHolder) holder).counterTwo.setMin(object.getMin());
                    ((DualCounterTypeViewHolder) holder).counterTwo.setUnit(object.getUnit());
                    ((DualCounterTypeViewHolder) holder).counterTwo.setValue(object.getDefault());
                    ((DualCounterTypeViewHolder) holder).counterTwo.setFocusable(false);
                    ((DualCounterTypeViewHolder) holder).categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case "Segment":
                    Balloon segmentHelp = helpBuilder
                        .build();

                    TextView segmentTitle = segmentHelp.getContentView().findViewById(R.id.help_title);
                    TextView segmentContent = segmentHelp.getContentView().findViewById(R.id.help_content);
                    ImageView segmentImage = segmentHelp.getContentView().findViewById(R.id.help_image);
                    segmentTitle.setText(object.getHelpTitle());
                    segmentContent.setText(object.getHelpText());
                    segmentImage.setImageDrawable(helpPicture);

                    ((SegmentTypeViewHolder) holder).title.setText(title_text);
                    ((SegmentTypeViewHolder) holder).help.setOnClickListener(view ->
                        segmentHelp.showAlignBottom(((SegmentTypeViewHolder) holder).help));

                    int segmentCount = object.getSegments();

                    SegmentedButton[] segmentedButtons = {
                        ((SegmentTypeViewHolder) holder).one,
                        ((SegmentTypeViewHolder) holder).two,
                        ((SegmentTypeViewHolder) holder).three,
                        ((SegmentTypeViewHolder) holder).four,
                        ((SegmentTypeViewHolder) holder).five,
                        ((SegmentTypeViewHolder) holder).six
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
                        ((SegmentTypeViewHolder) holder).group.performHapticFeedback(
                            HapticFeedbackConstants.CONFIRM);
                    } else {
                        ((SegmentTypeViewHolder) holder).group.performHapticFeedback(
                            HapticFeedbackConstants.LONG_PRESS);
                    }
                    ((SegmentTypeViewHolder) holder).categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case "List":
                    Balloon listHelp = helpBuilder
                        .build();

                    TextView listTitle = listHelp.getContentView().findViewById(R.id.help_title);
                    TextView listContent = listHelp.getContentView().findViewById(R.id.help_content);
                    ImageView listImage = listHelp.getContentView().findViewById(R.id.help_image);
                    listTitle.setText(object.getHelpTitle());
                    listContent.setText(object.getHelpText());
                    listImage.setImageDrawable(helpPicture);

                    ((ListTypeViewHolder) holder).title.setText(title_text);
                    ((ListTypeViewHolder) holder).help.setOnClickListener(view ->
                        listHelp.showAlignBottom(((ListTypeViewHolder) holder).help));

                    // Loop through add each item to entryLabels
                    for (int i = 0; i < object.getTotalEntries(); i++) {
                        entryLabels.add(object.getEntryLabels().get(i));
                    }
                    ArrayAdapter<String> listspinnerArrayAdapter = new ArrayAdapter<>(mContext,
                        android.R.layout.simple_spinner_item, entryLabels);
                    listspinnerArrayAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                    ((ListTypeViewHolder) holder).spinner.setAdapter(listspinnerArrayAdapter);
                    ((ListTypeViewHolder) holder).spinner.setTag("Spinner");
                    ((ListTypeViewHolder) holder).spinner.setOnItemClickListener((parent, view, position, id) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                        } else {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        }
                    });
                    ((ListTypeViewHolder) holder).categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));
                    break;
                case "TeamSelect":
                    Balloon teamSelectHelp = helpBuilder
                        .build();
                    Preference listPreference = PowerPreference.getFileByName("List");
                    TextView teamSelectTitle = teamSelectHelp.getContentView().findViewById(R.id.help_title);
                    TextView teamSelectContent = teamSelectHelp.getContentView().findViewById(R.id.help_content);
                    ImageView teamSelectImage = teamSelectHelp.getContentView().findViewById(R.id.help_image);
                    teamSelectTitle.setText(object.getHelpTitle());
                    teamSelectContent.setText(object.getHelpText());
                    teamSelectImage.setImageDrawable(helpPicture);

                    ((TeamSelectTypeViewHolder) holder).title.setText(R.string.select_team_title);
                    ((TeamSelectTypeViewHolder) holder).help.setOnClickListener(view ->
                        teamSelectHelp.showAlignBottom(((TeamSelectTypeViewHolder) holder)
                            .help));

                    ((TeamSelectTypeViewHolder) holder).categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));

                    ArrayList<String> remainingList = listPreference.getObject(
                        "pit_teams_remaining_list", ArrayList.class);
                    if (listPreference.getBoolean("pit_remove_enabled")) {
                        entryLabels = remainingList;
                    } else {
                        entryLabels = Arrays.asList(mContext.getResources().getStringArray(
                            R.array.team_list));;
                    }

                    ((TeamSelectTypeViewHolder) holder).spinner.setTag("TeamSpinner");

                    ArrayAdapter<String> teamselectspinnerArrayAdapter = new ArrayAdapter<>(mContext,
                        android.R.layout.simple_spinner_item, entryLabels);
                    teamselectspinnerArrayAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                    ((TeamSelectTypeViewHolder) holder).spinner.setAdapter(
                        teamselectspinnerArrayAdapter);
                    //((TeamSelectTypeViewHolder) holder).spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    //    @Override
                    //    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                    //        } else {
                    //            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    //        }                        }

                        //@Override
                        //public void onNothingSelected(AdapterView<?> parent) {
                        //
                        //}
                    //});
                    break;
                case "Special":

                    Balloon specialHelp = helpBuilder
                        .build();

                    TextView specialTitle = specialHelp.getContentView().findViewById(R.id.help_title);
                    TextView specialContent = specialHelp.getContentView().findViewById(R.id.help_content);
                    ImageView specialImage = specialHelp.getContentView().findViewById(R.id.help_image);
                    specialTitle.setText(object.getHelpTitle());
                    specialContent.setText(object.getHelpText());
                    specialImage.setImageDrawable(helpPicture);
                    ((SpecialTypeViewHolder) holder).categoryColor.setBackgroundColor(
                        ContextCompat.getColor(mContext, categoryColor));

                    ((SpecialTypeViewHolder) holder).algaeMiss.setValue(0);
                    ((SpecialTypeViewHolder) holder).algaeSuccess.setValue(0);
                    ((SpecialTypeViewHolder) holder).algaeReturned.setValue(0);

                    int specialSegmentCount = object.getSegments();

                    SegmentedButton[] specialSegmentedButtons = {
                        ((SpecialTypeViewHolder) holder).teamSelector.findViewById(R.id.button_one),
                        ((SpecialTypeViewHolder) holder).teamSelector.findViewById(R.id.button_two),
                        ((SpecialTypeViewHolder) holder).teamSelector.findViewById(R.id.button_three),
                        ((SpecialTypeViewHolder) holder).teamSelector.findViewById(R.id.button_four),
                    };


                    int visibleSpecialSegmentCount = Math.min(object.getSegmentLabels().size(),
                        specialSegmentCount);

                    for (int i = 0; i < visibleSpecialSegmentCount; i++) {
                        specialSegmentedButtons[i].setText(object.getSegmentLabels().get(i));
                        specialSegmentedButtons[i].setVisibility(View.VISIBLE);
                    }

                    for (int i = visibleSpecialSegmentCount; i < specialSegmentedButtons.length; i++) {
                        specialSegmentedButtons[i].setVisibility(View.GONE);
                    }

                    ((SpecialTypeViewHolder) holder).teamSelector.setPosition(3,false);
                    String teamColor = object.getCellSpecialTeamColorTitle();

                    String teamNumberOne;
                    String teamNumberTwo;
                    String teamNumberThree;
                    String teamNumberFour = "No Team";

                    // Get team numbers based on team color
                    if(teamColor.equals("Blue")){
                        teamNumberOne = teamInfo.getMasterTeam(matchInfo.getMatch(),1);
                        teamNumberTwo = teamInfo.getMasterTeam(matchInfo.getMatch(),2);
                        teamNumberThree = teamInfo.getMasterTeam(matchInfo.getMatch(),3);
                    } else { // Red
                        teamNumberOne = teamInfo.getMasterTeam(matchInfo.getMatch(),4);
                        teamNumberTwo = teamInfo.getMasterTeam(matchInfo.getMatch(),5);
                        teamNumberThree = teamInfo.getMasterTeam(matchInfo.getMatch(),6);
                    }

                    specialSegmentedButtons[0].setText(teamNumberOne);
                    specialSegmentedButtons[1].setText(teamNumberTwo);
                    specialSegmentedButtons[2].setText(teamNumberThree);
                    specialSegmentedButtons[3].setText(teamNumberFour);

                    ((SpecialTypeViewHolder) holder).title.setText(
                        String.format("%s Team", teamColor));
                    ((SpecialTypeViewHolder) holder).help.setOnClickListener(view ->
                        specialHelp.showAlignBottom(((SpecialTypeViewHolder) holder)
                            .help));
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
