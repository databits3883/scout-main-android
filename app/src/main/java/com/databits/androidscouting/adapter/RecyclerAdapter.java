package com.databits.androidscouting.adapter;

import android.annotation.SuppressLint;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.addisonelliott.segmentedbutton.SegmentedButton;
import com.databits.androidscouting.fragment.Pit;
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
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.travijuu.numberpicker.library.NumberPicker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements
    ItemTouchHelperAdapter {
    public List<Cell> mCell;
    public RecyclerAdapter(List<Cell> cells) {
        mCell = cells;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        Cell cell = mCell.get(position);
        try {
            holder.bind(cell);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
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
        // Remove the item from the list and notify the adapter that the item has been removed
        //mCell.remove(position);
        //notifyItemRemoved(position);
    }

    @Override public void onItemSelected() {
        // Do nothing
    }

    @Override public void onItemClear() {
        // Do nothing
    }

    @SuppressLint("InflateParams")
    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView mCellTitleTextView;
        private final View mRootView;
        Moshi moshi = new Moshi.Builder().build();
        List<String> entryLabels = new ArrayList<>();
        List<String> segmentLabels = new ArrayList<>();

        public ViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
            mCellTitleTextView = itemView.findViewById(R.id.cell_title);
        }

        public void bind(Cell cell) throws JSONException, IOException {
            mCellTitleTextView.setGravity(Gravity.CENTER);
            mCellTitleTextView.setText(cell.getTitle());

            ImageButton help = mRootView.findViewById(R.id.help_button);

            JsonAdapter<CellParam> paramAdapter = moshi.adapter(CellParam.class);
            CellParam cellParams = paramAdapter.fromJson(paramAdapter.toJson(cell.getParam()));

            Balloon.Builder helpBuilder = new Balloon.Builder(mRootView.getContext())
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
                    .setTextColor(ContextCompat.getColor(mRootView.getContext(), R.color.white))
                    .setBalloonAnimation(BalloonAnimation.FADE);

            String cellType = cellParams.getType();
            LinearLayout cellContainerLayout = mRootView.findViewById(R.id.cell_container);
            cellContainerLayout.removeAllViews();
            switch (cellType) {
                case "YesNo":
                    View YesNo = LayoutInflater.from(mRootView.getContext()).inflate(R.layout.ui_yes_no_segmented, null);
                    cellContainerLayout.addView(YesNo);
                    Balloon yesnoHelp = helpBuilder
                            .setText(cellParams.getHelpText())
                            .build();

                    help.setOnClickListener(view -> yesnoHelp.showAlignBottom(help));
                    break;
                case "Counter":
                    View Counter = LayoutInflater.from(mRootView.getContext()).inflate(R.layout.ui_number_picker, null);
                    NumberPicker currentPicker = Counter.findViewById(R.id.number_counter_inside);
                    currentPicker.setMax(cellParams.getMax());
                    currentPicker.setMin(cellParams.getMin());
                    currentPicker.setUnit(cellParams.getUnit());
                    currentPicker.setValue(cellParams.getDefault());
                    currentPicker.setFocusable(false);
                    cellContainerLayout.addView(Counter);
                    Balloon counterHelp = helpBuilder
                        .setText(cellParams.getHelpText())
                        .build();

                    help.setOnClickListener(view -> counterHelp.showAlignBottom(help));
                    break;
                case "Segment":
                    View inner = LayoutInflater.from(mRootView.getContext()).inflate(R.layout.ui_multi_segmented, null);

                    int segmentCount = cellParams.getSegments();

                    SegmentedButton[] buttons = {
                        inner.findViewById(R.id.button_one),
                        inner.findViewById(R.id.button_two),
                        inner.findViewById(R.id.button_three),
                        inner.findViewById(R.id.button_four),
                        inner.findViewById(R.id.button_five),
                        inner.findViewById(R.id.button_six)
                    };

                    for (int i = 0; i < segmentCount; i++) {
                        segmentLabels.add(cellParams.getSegmentLabels().get(i));
                    }

                    for (int i = 0; i < segmentLabels.size() & segmentCount > i; i++) {
                        buttons[i].setText(segmentLabels.get(i));
                    }

                    for (int i = 0; i < buttons.length; i++) {
                        buttons[i].setVisibility(i < segmentCount ? View.VISIBLE : View.GONE);
                    }

                    cellContainerLayout.addView(inner);

                    Balloon segmentHelp = helpBuilder
                        .setText(cellParams.getHelpText())
                        .build();

                    help.setOnClickListener(view -> segmentHelp.showAlignBottom(help));
                    break;
                case "List":
                    Spinner spinner = new Spinner(mRootView.getContext());
                    // Loop through add each item to entryLabels
                    for (int i = 0; i < cellParams.getTotalEntries(); i++) {
                        entryLabels.add(cellParams.getEntryLabels().get(i));
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(mRootView.getContext(),
                        android.R.layout.simple_spinner_item, entryLabels);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(spinnerArrayAdapter);
                    spinner.setTag("Spinner");
                    cellContainerLayout.addView(spinner);
                    Balloon listHelp = helpBuilder
                        .setText(cellParams.getHelpText())
                        .build();

                    help.setOnClickListener(view -> listHelp.showAlignBottom(help));
                    break;
                case "Text":
                    View textInner = LayoutInflater.from(mRootView.getContext()).inflate(R.layout.ui_textbox_maker, null);

                    TextInputLayout textInputLayout = textInner.findViewById(R.id.textbox_text_layout);
                    TextInputEditText editText = textInputLayout.findViewById(R.id.enteredText);
                    editText.setMaxLines(1);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);

                    textInputLayout.setId(R.id.textbox_text_layout);
                    if (!cellParams.isTextHidden()) {
                        textInputLayout.setHint(cellParams.getTextHint());
                    }

                    cellContainerLayout.addView(textInner);

                    Balloon textHelp = helpBuilder
                        .setText(cellParams.getHelpText())
                        .build();

                    help.setOnClickListener(view -> textHelp.showAlignBottom(help));
                    break;
                case "TeamSelect":
                    mCellTitleTextView.setText("Select Team");
                    Spinner selSpinner = new Spinner(mRootView.getContext());

                    entryLabels = Arrays.asList(
                        mRootView.getContext().getResources().getStringArray(R.array.team_list));
                    ArrayAdapter<String> selSpinnerArrayAdapter = new ArrayAdapter<>(mRootView.getContext(),
                        android.R.layout.simple_spinner_item, entryLabels);
                    selSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    selSpinner.setAdapter(selSpinnerArrayAdapter);
                    selSpinner.setTag("TeamSpinner");
                    cellContainerLayout.addView(selSpinner);
                    Pit pitScoutFragment = new Pit();
                    pitScoutFragment.teamSpinner("", false, mRootView.getContext(), mRootView);
                    Balloon teamSelectHelp = helpBuilder
                        .setText(cellParams.getHelpText())
                        .build();

                    help.setOnClickListener(view -> teamSelectHelp.showAlignBottom(help));
                    break;
                case "Title":
                    View titleInner = LayoutInflater.from(mRootView.getContext()).inflate(R.layout.ui_title, null);
                    mCellTitleTextView.setText("Team             Match");
                    cellContainerLayout.addView(titleInner);
                    Balloon titleHelp = helpBuilder
                        .setText(cellParams.getHelpText())
                        .build();

                    help.setOnClickListener(view -> titleHelp.showAlignBottom(help));
                    break;
                default:
                    TextView defaultTextView = new TextView(mRootView.getContext());
                    defaultTextView.setText(R.string.unrecognized_cell_type);
                    cellContainerLayout.addView(defaultTextView);
                    Balloon defaultHelp = helpBuilder
                        .setText("This cell type is not recognized. Please contact the developers.")
                        .build();

                    help.setOnClickListener(view -> defaultHelp.showAlignBottom(help));
                    break;
            }
        }
    }
}