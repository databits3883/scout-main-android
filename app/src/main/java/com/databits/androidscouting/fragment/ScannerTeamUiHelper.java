package com.databits.androidscouting.fragment;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import com.databits.androidscouting.R;

final class ScannerTeamUiHelper {
    private static final int[] TEAM_VIEW_IDS = {
        R.id.blue1,
        R.id.blue2,
        R.id.blue3,
        R.id.red1,
        R.id.red2,
        R.id.red3,
    };

    private ScannerTeamUiHelper() {
    }

    static int getTeamViewId(int slot) {
        if (slot < 1 || slot > TEAM_VIEW_IDS.length) {
            throw new IllegalArgumentException("Unknown team slot: " + slot);
        }
        return TEAM_VIEW_IDS[slot - 1];
    }

    static void applyDefaultTeamDisplay(View root, Resources resources, String[] teams) {
        for (int i = 0; i < TEAM_VIEW_IDS.length; i++) {
            TextView teamView = root.findViewById(TEAM_VIEW_IDS[i]);
            if (teamView == null) {
                continue;
            }

            teamView.setText(teams[i]);
            if (i <= 2) {
                teamView.setBackgroundTintList(resources.getColorStateList(android.R.color.holo_blue_light, null));
            } else {
                teamView.setBackgroundTintList(resources.getColorStateList(android.R.color.holo_red_light, null));
            }
        }
    }

    static void highlightMatchedSlot(View root, Resources resources, int slot) {
        int teamId = getTeamViewId(slot);
        TextView teamView = root.findViewById(teamId);
        if (teamView != null) {
            teamView.setBackgroundTintList(resources.getColorStateList(R.color.green_900, null));
        }
    }
}
