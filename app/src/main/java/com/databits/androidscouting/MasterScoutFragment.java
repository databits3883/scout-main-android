package com.databits.androidscouting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.databits.androidscouting.databinding.FragmentMasterScoutBinding;

public class MasterScoutFragment extends Fragment {

    private FragmentMasterScoutBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentMasterScoutBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController controller = NavHostFragment.findNavController(MasterScoutFragment.this);

        binding.buttonScanner.setOnClickListener(view1 -> controller
                .navigate(R.id.action_masterScoutFragment_to_ScannerFragment));

        binding.buttonBack.setOnClickListener(view1 -> controller
                .navigate(R.id.action_masterScoutFragment_to_StartFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}