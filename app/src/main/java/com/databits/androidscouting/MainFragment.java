package com.databits.androidscouting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.databits.androidscouting.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController controller = NavHostFragment.findNavController(MainFragment.this);

        binding.buttonMaster.setOnClickListener(view1 -> controller
                .navigate(R.id.action_StartFragment_to_masterScoutFragment));
        binding.buttonCrowd.setOnClickListener(view1 -> controller
                .navigate(R.id.action_StartFragment_to_crowdScoutFragment));
        binding.buttonPit.setOnClickListener(view1 -> controller
                .navigate(R.id.action_StartFragment_to_pitScoutFragment));
        binding.buttonDriveTeam.setOnClickListener(view1 -> controller
                .navigate(R.id.action_StartFragment_to_driveTeamFragment));
        binding.buttonDemo.setOnClickListener(view1 -> controller
                .navigate(R.id.action_StartFragment_to_demoFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}