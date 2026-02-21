package com.databits.androidscouting.fragment;

import androidx.fragment.app.Fragment;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.databits.androidscouting.util.TeamInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

final class ScannerTeamScheduleController {

    void ensureTeamsLoaded(
        Fragment fragment,
        ExecutorService backgroundExecutor,
        PreferenceRepository repository,
        TeamInfo teamInfo,
        Runnable onTeamsLoaded
    ) {
        backgroundExecutor.execute(() -> {
            if (repository.getTeamMatchListSize() == 0) {
                teamInfo.read_teams();
            }

            boolean teamsAreLoaded = teamInfo.teamsLoaded();
            if (!fragment.isAdded()) {
                return;
            }

            fragment.requireActivity().runOnUiThread(() -> {
                if (fragment.isAdded() && teamsAreLoaded) {
                    onTeamsLoaded.run();
                }
            });
        });
    }

    void setupTeamDisplay(
        Fragment fragment,
        ExecutorService backgroundExecutor,
        TeamInfo teamInfo,
        int match,
        String[] cachedTeamNumbers
    ) {
        backgroundExecutor.execute(() -> {
            String[] teams = new String[6];
            for (int i = 0; i < 6; i++) {
                teams[i] = teamInfo.getMasterTeam(match, i + 1);
                cachedTeamNumbers[i + 1] = teams[i];
            }

            if (!fragment.isAdded()) {
                return;
            }

            fragment.requireActivity().runOnUiThread(() -> {
                if (!fragment.isAdded() || fragment.getView() == null) {
                    return;
                }
                ScannerTeamUiHelper.applyDefaultTeamDisplay(fragment.requireView(), fragment.getResources(), teams);
            });
        });
    }

    List<String> getScoutingTeamSlots(String[] cachedTeamNumbers) {
        List<String> slots = new ArrayList<>(6);
        for (int i = 1; i <= 6; i++) {
            slots.add(cachedTeamNumbers[i]);
        }
        return slots;
    }
}
