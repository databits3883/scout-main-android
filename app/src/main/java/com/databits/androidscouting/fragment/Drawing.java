package com.databits.androidscouting.fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentDrawingMapBinding;
import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import com.mihir.drawingcanvas.drawingView;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.travijuu.numberpicker.library.NumberPicker;
import java.util.Objects;

public class Drawing extends Fragment {
  Preference debugPreference = PowerPreference.getFileByName("Debug");

  private FragmentDrawingMapBinding binding;

  drawingView mDrawView;

  int counter = 1;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState
  ) {
    refreshActionBar();
    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    binding = FragmentDrawingMapBinding.inflate(inflater, container, false);
    return binding.getRoot();

  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Go Full screen
    View decorView = requireActivity().getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    decorView.setSystemUiVisibility(uiOptions);

    mDrawView = binding.canvasView;
    // Create an array of the button IDs
    int[] buttonIds = {R.id.buttonPlace1, R.id.buttonPlace2, R.id.buttonPlace3, R.id.buttonPlace4,
        R.id.buttonPlace5, R.id.buttonPlace6};

    // Register the click event and default image for each button using a loop
    for (int buttonId : buttonIds) {
      ImageButton buttonPlace = requireView().findViewById(buttonId);
      buttonPlace.setImageResource(R.drawable.android_x);
      buttonPlace.setOnClickListener(viewId -> buttonChanger(buttonPlace));
    }

    NavController controller = NavHostFragment.findNavController(Drawing.this);

    binding.buttonBackMap.setOnClickListener(view1 -> controller.navigateUp());

    binding.redButton.setOnClickListener(view1 -> red());
    binding.greenButton.setOnClickListener(view1 -> green());
    binding.blueButton.setOnClickListener(view1 -> blue());
    binding.clearButton.setOnClickListener(view1 -> clear());
    binding.undoButton.setOnClickListener(view1 -> undo());
    binding.redoButton.setOnClickListener(view1 -> redo());
    binding.colorpickerButton.setOnClickListener(view1 -> colorPicker());
    binding.sizeButton.setOnClickListener(view1 -> sizePicker());

    // Configure Drawing
    mDrawView.setBrushAlpha(255);// values from 0-255
    mDrawView.setBrushColor(R.color.green_900);
    mDrawView.setSizeForBrush(debugPreference.getInt("map_brush_size", 15));
    mDrawView.setSizeForBrush(15); // takes value from 0-200
  }

  private void buttonChanger(ImageButton buttonPlace) {
    // Use a switch statement to change the image on the button in a cycle
    switch (counter) {
      case 0:
        buttonPlace.setImageResource(R.drawable.android_x);
        break;
      case 1:
        buttonPlace.setImageResource(R.drawable.cageshallow);
        break;
      case 2:
        buttonPlace.setImageResource(R.drawable.cagedeep);
        break;
    }
    // Increment counter by 1 and use modulo to cycle through the drawables (0,1,2)
    counter = (counter + 1) % 3;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void red(){
    mDrawView.setBrushColor(getResources()
        .getColor(R.color.map_red,null));
  }

  private void green(){
    mDrawView.setBrushColor(getResources()
        .getColor(R.color.green_900,null));
  }

  private void blue(){
    mDrawView.setBrushColor(getResources()
        .getColor(R.color.map_blue,null));
  }

  private void undo(){
    mDrawView.undo();
  }

  private void redo(){
    mDrawView.redo();
  }

  private void clear(){
    mDrawView.clearDrawingBoard();
  }

  private void colorPicker() {
    new ColorPickerDialog.Builder(requireContext())
        .setTitle("Color Picker")
        .setColorShape(ColorShape.SQAURE)
        .setDefaultColor(R.color.green_900)
        .setColorListener((color, colorHex) -> {
          // Handle Color
          mDrawView.setBrushColor(color);
          binding.colorpickerButton.setBackgroundColor(color);
        })
        .show();
  }

  public void refreshActionBar() {
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    assert activity != null;
    ActionBar actionBar = activity.getSupportActionBar();
    Objects.requireNonNull(actionBar).hide();
  }

  private void sizePicker() {
    new AlertDialog.Builder(requireContext())
        .setTitle("Brush Size Selector")
        .setView(R.layout.popup_brush_size)
        .setPositiveButton("OK", (dialog, which) -> {
          NumberPicker picker = ((AlertDialog) dialog).findViewById(R.id.brush_size_picker);
          assert picker != null;
          mDrawView.setSizeForBrush(picker.getValue());
          debugPreference.putInt("map_brush_size", picker.getValue());
        })
        .setNegativeButton("Cancel", (dialog, which) -> {
          // Do Nothing Cancel
        }).show();

  }
}