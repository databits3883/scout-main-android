package com.databits.androidscouting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.databits.androidscouting.databinding.FragmentDriveTeamBinding;

public class DriveTeamFragment extends Fragment {

    private FragmentDriveTeamBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentDriveTeamBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController controller = NavHostFragment.findNavController(DriveTeamFragment.this);

        binding.buttonBack.setOnClickListener(view1 -> controller
                .navigate(R.id.action_driveTeamFragment_to_StartFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}