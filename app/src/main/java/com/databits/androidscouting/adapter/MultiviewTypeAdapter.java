package com.databits.androidscouting.adapter;

import android.content.Context;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.addisonelliott.segmentedbutton.SegmentedButton;
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.databits.androidscouting.R;
import com.databits.androidscouting.model.Cell;
import com.databits.androidscouting.model.CellParam;
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
    List<String> segmentLabels = new ArrayList<>();
    List<String> entryLabels = new ArrayList<>();

    public static class YesNoTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        SegmentedButtonGroup group;
        public YesNoTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.yesno_title);
            this.group = itemView.findViewById(R.id.buttonGroup_yes_no);
        }
    }

    public static class TextTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextInputLayout textInputLayout;
        TextInputEditText editText;
        ImageButton help;
        public TextTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.textbox_title);
            this.textInputLayout = itemView.findViewById(R.id.textbox_text_layout);
            this.editText = itemView.findViewById(R.id.enteredText);
        }
    }

    public static class CounterTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        NumberPicker currentPicker;
        public CounterTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.counter_title);
            this.currentPicker = itemView.findViewById(R.id.number_counter_inside);
        }
    }

    public static class DualCounterTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        NumberPicker counterOne, counterTwo;
        public DualCounterTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.counter_title);
            this.counterOne = itemView.findViewById(R.id.counterOne);
            this.counterTwo = itemView.findViewById(R.id.counterTwo);
        }
    }

    public static class SegmentTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        SegmentedButton one,two,three,four,five,six;
        SegmentedButtonGroup group;

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
        }
    }

    public static class ListTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        Spinner spinner;
        public ListTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.list_title);
            this.spinner = itemView.findViewById(R.id.spinner);
        }
    }

    public static class TeamSelectTypeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton help;
        Spinner spinner;
        public TeamSelectTypeViewHolder(View itemView) {
            super(itemView);
            this.help = itemView.findViewById(R.id.help_button);
            this.title = itemView.findViewById(R.id.teamselect_title);
            this.spinner = itemView.findViewById(R.id.spinner);
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
            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int listPosition) {
        CellParam object = mCell.get(listPosition).getParam();

        String title_text = mCell.get(listPosition).getTitle();

        Context mContext = holder.itemView.getContext();

        // Common Help balloon settings for all cells
        Balloon.Builder helpBuilder = new Balloon.Builder(mContext)
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
            .setTextColor(ContextCompat.getColor(mContext, R.color.white))
            .setBalloonAnimation(BalloonAnimation.FADE);

        if (object != null) {
            switch (object.getType()) {
                case "YesNo":
                    Balloon yesnoHelp = helpBuilder
                        .setText(object.getHelpText())
                        .build();
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
                    break;
                case "Text":
                    ((TextTypeViewHolder) holder).title.setText(title_text);
                    ((TextTypeViewHolder) holder).editText.setSingleLine(true);
                    ((TextTypeViewHolder) holder).textInputLayout.setId(R.id.textbox_text_layout);

                    if (!object.isTextHidden()) {
                        ((TextTypeViewHolder) holder).textInputLayout.setHint(object.getTextHint());
                    }
                    Balloon textHelp = helpBuilder
                        .setText(object.getHelpText())
                        .build();
                    ((TextTypeViewHolder) holder).help.setOnClickListener(view ->
                        textHelp.showAlignBottom(((TextTypeViewHolder) holder).help));
                    break;
                case "Counter":
                    Balloon counterHelp = helpBuilder
                        .setText(object.getHelpText())
                        .build();
                    ((CounterTypeViewHolder) holder).title.setText(title_text);
                    ((CounterTypeViewHolder) holder).help.setOnClickListener(view ->
                        counterHelp.showAlignBottom(((CounterTypeViewHolder) holder).help));
                    ((CounterTypeViewHolder) holder).currentPicker.setMax(object.getMax());
                    ((CounterTypeViewHolder) holder).currentPicker.setMin(object.getMin());
                    ((CounterTypeViewHolder) holder).currentPicker.setUnit(object.getUnit());
                    ((CounterTypeViewHolder) holder).currentPicker.setValue(object.getDefault());
                    ((CounterTypeViewHolder) holder).currentPicker.setFocusable(false);
                    break;
                case "DualCounter":
                    Balloon dualCounterHelp = helpBuilder
                        .setText(object.getHelpText())
                        .build();
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
                    break;
                case "Segment":
                    Balloon segmentHelp = helpBuilder
                        .setText(object.getHelpText())
                        .build();
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

                    //Maybe need -1
                    for (int i = 0; i < segmentCount; i++) {
                        segmentLabels.add(object.getSegmentLabels().get(i));
                    }

                    for (int i = 0; i < segmentLabels.size() & segmentCount > i; i++) {
                        segmentedButtons[i].setText(segmentLabels.get(i));
                    }

                    for (int i = 0; i < segmentedButtons.length; i++) {
                        segmentedButtons[i].setVisibility(i < segmentCount ? View.VISIBLE :
                            View.GONE);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ((SegmentTypeViewHolder) holder).group.performHapticFeedback(
                            HapticFeedbackConstants.CONFIRM);
                    } else {
                        ((SegmentTypeViewHolder) holder).group.performHapticFeedback(
                            HapticFeedbackConstants.LONG_PRESS);
                    }
                    break;
                case "List":
                    Balloon listHelp = helpBuilder
                        .setText(object.getHelpText())
                        .build();
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
                    break;
                case "TeamSelect":
                    Balloon teamSelectHelp = helpBuilder
                        .setText(object.getHelpText())
                        .build();
                    ((TeamSelectTypeViewHolder) holder).title.setText(R.string.select_team_title);
                    ((TeamSelectTypeViewHolder) holder).help.setOnClickListener(view ->
                        teamSelectHelp.showAlignBottom(((TeamSelectTypeViewHolder) holder)
                            .help));
                    entryLabels = Arrays.asList(
                        mContext.getResources().getStringArray(R.array.team_list));
                    ArrayAdapter<String> teamselectspinnerArrayAdapter =new ArrayAdapter<>(mContext,
                        android.R.layout.simple_spinner_item, entryLabels);
                    teamselectspinnerArrayAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                    ((TeamSelectTypeViewHolder) holder).spinner.setAdapter(
                        teamselectspinnerArrayAdapter);
                    ((TeamSelectTypeViewHolder) holder).spinner.setTag("TeamSpinner");
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
