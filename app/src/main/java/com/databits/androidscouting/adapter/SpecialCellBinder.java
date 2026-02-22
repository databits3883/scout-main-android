package com.databits.androidscouting.adapter;

import android.content.Context;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.addisonelliott.segmentedbutton.SegmentedButton;
import com.databits.androidscouting.model.CellConfig;

final class SpecialCellBinder {
    private SpecialCellBinder() {
    }

    static void bind(
        MultiviewTypeAdapter.SpecialTypeViewHolder holder,
        CellConfig config,
        Context context,
        int categoryColor,
        String[] cachedSpecialTeamNumbers
    ) {
        holder.categoryColor.setBackgroundColor(ContextCompat.getColor(context, categoryColor));

        holder.algaeMiss.setValue(0);
        holder.algaeSuccess.setValue(0);
        holder.algaeReturned.setValue(0);

        int specialSegmentCount = config.getSegments();
        SegmentedButton[] specialSegmentedButtons = holder.specialSegmentedButtons;
        int visibleSpecialSegmentCount = Math.min(config.getSegmentLabels().size(), specialSegmentCount);

        for (int i = 0; i < visibleSpecialSegmentCount; i++) {
            specialSegmentedButtons[i].setText(config.getSegmentLabels().get(i));
            specialSegmentedButtons[i].setVisibility(View.VISIBLE);
        }

        for (int i = visibleSpecialSegmentCount; i < specialSegmentedButtons.length; i++) {
            specialSegmentedButtons[i].setVisibility(View.GONE);
        }

        holder.teamSelector.setPosition(3, false);
        String teamColor = config.getSpecialTeamColor();

        String teamNumberOne;
        String teamNumberTwo;
        String teamNumberThree;
        String teamNumberFour = "No Team";

        if (cachedSpecialTeamNumbers != null && cachedSpecialTeamNumbers.length == 7) {
            if ("Blue".equals(teamColor)) {
                teamNumberOne = cachedSpecialTeamNumbers[1];
                teamNumberTwo = cachedSpecialTeamNumbers[2];
                teamNumberThree = cachedSpecialTeamNumbers[3];
            } else {
                teamNumberOne = cachedSpecialTeamNumbers[4];
                teamNumberTwo = cachedSpecialTeamNumbers[5];
                teamNumberThree = cachedSpecialTeamNumbers[6];
            }
        } else {
            teamNumberOne = "Team 1";
            teamNumberTwo = "Team 2";
            teamNumberThree = "Team 3";
        }

        specialSegmentedButtons[0].setText(teamNumberOne);
        specialSegmentedButtons[1].setText(teamNumberTwo);
        specialSegmentedButtons[2].setText(teamNumberThree);
        specialSegmentedButtons[3].setText(teamNumberFour);

        holder.title.setText(String.format("%s Team", teamColor));
    }
}
