package com.databits.androidscouting.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;
import com.databits.androidscouting.model.CellConfig;

final class PickerCellBinder {
    private PickerCellBinder() {
    }

    static void bindCounter(
        MultiviewTypeAdapter.CounterTypeViewHolder holder,
        CellConfig config,
        String title,
        Context context,
        int categoryColor
    ) {
        holder.title.setText(title);
        holder.currentPicker.setMax(config.getMax());
        holder.currentPicker.setMin(config.getMin());
        holder.currentPicker.setUnit(config.getUnit());
        holder.currentPicker.setValue(config.getDefaultValue());
        holder.currentPicker.setFocusable(false);
        holder.categoryColor.setBackgroundColor(ContextCompat.getColor(context, categoryColor));
    }

    static void bindDoubleCounter(
        MultiviewTypeAdapter.DoubleCounterTypeViewHolder holder,
        CellConfig config,
        String title,
        Context context,
        int categoryColor
    ) {
        holder.title.setText(title);
        holder.counterOne.setMax(config.getMax());
        holder.counterOne.setMin(config.getMin());
        holder.counterOne.setUnit(config.getUnit());
        holder.counterOne.setValue(config.getDefaultValue());
        holder.counterOne.setFocusable(false);

        holder.counterTwo.setMax(config.getMax());
        holder.counterTwo.setMin(config.getMin());
        holder.counterTwo.setUnit(config.getUnit());
        holder.counterTwo.setValue(config.getDefaultValue());
        holder.counterTwo.setFocusable(false);
        holder.categoryColor.setBackgroundColor(ContextCompat.getColor(context, categoryColor));
    }

    static void bindDualCounter(
        MultiviewTypeAdapter.DualCounterTypeViewHolder holder,
        CellConfig config,
        String title,
        Context context,
        int categoryColor
    ) {
        holder.title.setText(title);
        holder.counterOne.setMax(config.getMax());
        holder.counterOne.setMin(config.getMin());
        holder.counterOne.setUnit(config.getUnit());
        holder.counterOne.setValue(config.getDefaultValue());
        holder.counterOne.setFocusable(false);

        holder.counterTwo.setMax(config.getMax());
        holder.counterTwo.setMin(config.getMin());
        holder.counterTwo.setUnit(config.getUnit());
        holder.counterTwo.setValue(config.getDefaultValue());
        holder.counterTwo.setFocusable(false);
        holder.categoryColor.setBackgroundColor(ContextCompat.getColor(context, categoryColor));
    }
}
