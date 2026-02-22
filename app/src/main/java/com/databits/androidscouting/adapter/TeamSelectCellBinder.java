package com.databits.androidscouting.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import androidx.core.content.ContextCompat;
import com.databits.androidscouting.R;
import com.databits.androidscouting.data.repository.ScheduleStore;
import com.databits.androidscouting.model.CellConfig;
import java.util.Arrays;
import java.util.List;

final class TeamSelectCellBinder {
    private TeamSelectCellBinder() {
    }

    static void bind(
        MultiviewTypeAdapter.TeamSelectTypeViewHolder holder,
        CellConfig config,
        Context context,
        int categoryColor,
        ScheduleStore scheduleStore,
        List<String> cachedPitTeamsRemainingList
    ) {
        holder.title.setText(R.string.select_team_title);
        holder.categoryColor.setBackgroundColor(ContextCompat.getColor(context, categoryColor));

        List<String> entryLabels;
        if (scheduleStore != null && scheduleStore.isPitRemoveEnabled() && cachedPitTeamsRemainingList != null) {
            entryLabels = cachedPitTeamsRemainingList;
        } else {
            entryLabels = Arrays.asList(context.getResources().getStringArray(R.array.team_list));
        }

        holder.spinner.setTag("TeamSpinner");
        ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(
            context,
            android.R.layout.simple_spinner_item,
            entryLabels
        );
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinner.setAdapter(teamAdapter);
    }
}
