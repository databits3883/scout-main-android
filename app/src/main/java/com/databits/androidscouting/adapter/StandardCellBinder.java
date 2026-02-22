package com.databits.androidscouting.adapter;

import android.content.Context;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.core.content.ContextCompat;
import com.addisonelliott.segmentedbutton.SegmentedButton;
import com.databits.androidscouting.model.CellConfig;
import java.util.ArrayList;
import java.util.List;

final class StandardCellBinder {
    private StandardCellBinder() {
    }

    static void bindYesNo(
        MultiviewTypeAdapter.YesNoTypeViewHolder holder,
        String title,
        Context context,
        int categoryColor
    ) {
        holder.title.setText(title);
        holder.group.setOnPositionChangedListener(position -> performHaptic(holder.group));
        holder.categoryColor.setBackgroundColor(ContextCompat.getColor(context, categoryColor));
    }

    static void bindText(
        MultiviewTypeAdapter.TextTypeViewHolder holder,
        CellConfig config,
        String title,
        Context context,
        int categoryColor
    ) {
        holder.title.setText(title);
        holder.editText.setSingleLine(true);
        holder.textInputLayout.setId(com.databits.androidscouting.R.id.textbox_text_layout);
        if (!config.isTextHidden()) {
            holder.textInputLayout.setHint(config.getTextHint());
        }
        holder.categoryColor.setBackgroundColor(ContextCompat.getColor(context, categoryColor));
    }

    static void bindSegment(
        MultiviewTypeAdapter.SegmentTypeViewHolder holder,
        CellConfig config,
        String title,
        Context context,
        int categoryColor
    ) {
        holder.title.setText(title);
        int segmentCount = config.getSegments();
        SegmentedButton[] segmentedButtons = {
            holder.one,
            holder.two,
            holder.three,
            holder.four,
            holder.five,
            holder.six
        };

        int visibleSegmentCount = Math.min(config.getSegmentLabels().size(), segmentCount);
        for (int i = 0; i < visibleSegmentCount; i++) {
            segmentedButtons[i].setText(config.getSegmentLabels().get(i));
            segmentedButtons[i].setVisibility(View.VISIBLE);
        }
        for (int i = visibleSegmentCount; i < segmentedButtons.length; i++) {
            segmentedButtons[i].setVisibility(View.GONE);
        }

        performHaptic(holder.group);
        holder.categoryColor.setBackgroundColor(ContextCompat.getColor(context, categoryColor));
    }

    static void bindList(
        MultiviewTypeAdapter.ListTypeViewHolder holder,
        CellConfig config,
        String title,
        Context context,
        int categoryColor
    ) {
        holder.title.setText(title);

        List<String> entryLabels = new ArrayList<>();
        for (int i = 0; i < config.getTotalEntries(); i++) {
            entryLabels.add(config.getEntryLabels().get(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            context,
            android.R.layout.simple_spinner_item,
            entryLabels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinner.setAdapter(adapter);
        holder.spinner.setTag("Spinner");
        holder.spinner.setOnItemClickListener((parent, view, position, id) -> performHaptic(view));
        holder.categoryColor.setBackgroundColor(ContextCompat.getColor(context, categoryColor));
    }

    private static void performHaptic(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
    }
}
