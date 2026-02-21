package com.databits.androidscouting.fragment;

import android.content.ContentResolver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.databits.androidscouting.R;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepositoryProvider;
import com.databits.androidscouting.databinding.FragmentQRBinding;
import com.databits.androidscouting.util.FileUtils;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.QrCodeGenerator;
import com.databits.androidscouting.util.QrCsvParser;  // Robust CSV parser that trims spaces and supports various separators
import com.databits.androidscouting.util.TeamInfo;
import com.databits.androidscouting.viewmodel.ConfigViewModel;
import com.databits.androidscouting.viewmodel.ConfigViewModelFactory;
import com.travijuu.numberpicker.library.NumberPicker;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * QR.java
 *
 * This Fragment processes and displays scanned QR code data.
 *
 * Optimizations Applied:
 * • Error Handling: Critical parsing sections (e.g., QR data extraction, team number parsing)
 *   are wrapped in try–catch blocks; errors are logged and Toast messages are shown to prevent crashes.
 *
 * • QR Code Content Parsing: Uses QrCsvParser to robustly handle various CSV formats (handles extra spaces,
 *   multiple separators, etc.) and validates the parsed array before processing.
 *
 * • Local Preference Handling: Scanned data is saved into shared preferences efficiently (using PowerPreference)
 *   in a single call to reduce redundant lookups. (This code can later be refactored into a singleton if needed.)
 *
 * • Code Cleanup & Organization: The code is split into logical sections with detailed comments explaining each block.
 *
 * NOTE: File names and dependency references rem   ain unchanged.
 */

public class QR extends Fragment {

    private FragmentQRBinding binding;

    // Utility classes for match/team info, file operations, and QR code generation.
    private MatchInfo matchInfo;
    private TeamInfo teamInfo;
    private FileUtils fileUtils;
    private QrCodeGenerator qrCodeGenerator;

    // Scouting mode and parsed values.
    private boolean mode;
    private int team = 0;
    private int matchCounter = 1; // Initialized to 1

    // Repository for centralized preference access
    private PreferenceRepository repository;
    private ConfigViewModel viewModel;

    // Handler for cycle button animation
    private Handler cycleHandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQRBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("QR", "onViewCreated: Starting QR processing");

        // Initialize ViewModel
        repository = PreferenceRepositoryProvider.get(requireContext());
        ConfigViewModelFactory factory = new ConfigViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(ConfigViewModel.class);

        // --- Initialize Utility Classes ---
        matchInfo = new MatchInfo(repository);
        teamInfo = new TeamInfo(requireContext());
        fileUtils = new FileUtils(requireContext());
        qrCodeGenerator = new QrCodeGenerator(requireContext());

        // --- Full-Screen Mode ---
        View decorView = requireActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        NavController controller = NavHostFragment.findNavController(QR.this);
        Bundle bundle = getArguments();

        // --- QR Code Parsing & Data Extraction ---
        if (bundle != null) {
            String data = bundle.getString("qrData");
            mode = bundle.getBoolean("mode");
            Log.d("QR", "Raw QR data: " + data + ", Mode: " + mode);

            if (data == null || data.trim().isEmpty()) {
                Log.e("QR", "QR data is empty or null.");
                Toast.makeText(requireContext(), "Invalid QR code data!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse the CSV data using QrCsvParser.
            String[] fields = QrCsvParser.parseCsv(data, 1);
            if (fields == null || fields.length == 0) {
                Log.e("QR", "Malformed or empty QR data: " + data);
                Toast.makeText(requireContext(), "Invalid QR code data!", Toast.LENGTH_SHORT).show();
                return;
            }

            // In Pit mode, extract the team number from the first field.
            if (mode) {
                try {
                    team = Integer.parseInt(fields[0]);
                } catch (NumberFormatException e) {
                    Log.e("QR", "Invalid team number in QR data: " + fields[0]);
                    Toast.makeText(requireContext(), "Team number is invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Update temporary match info using current match.
            matchInfo.setTempMatch(matchInfo.getMatch());
            Log.d("QR", "Parsed values - Team: " + team + ", Temp Match: " + matchInfo.getTempMatch());

            // --- Update UI ---
            updateUI();

            // Generate and display the QR code image.
            ImageView qr_img = binding.qrImg;
            qr_img.setImageBitmap(qrCodeGenerator.generateQRCode(data, 1000, 35, true));

            // Save scanned data into local preferences.
            saveData(data, mode);
            checkMode(mode);

            // Optionally, store special data.
            ArrayList<String> specialData = new ArrayList<>();
            specialData.add(data);
            viewModel.updateSpecialScoutData(specialData);
        } else {
            Log.d("QR", "No QR data received; setting default values.");
            updateUI();
        }

        // --- Navigation & Event Handlers ---
        binding.buttonBack.setOnClickListener(v -> {
            if (mode) {
                controller.navigate(R.id.action_QRFragment_to_pitScoutFragment);
            } else {
                controller.navigate(R.id.action_QRFragment_to_crowdScoutFragment);
            }
        });

        binding.buttonNext.setOnClickListener(v -> {
            viewModel.updateManualTeamOverride(false);
            viewModel.updateManualMatchOverride(false);
            repository.removeManualTeamOverrideValue();
            matchInfo.incrementMatch();
            matchInfo.setTempMatch(matchInfo.getMatch());
            controller.navigateUp();
        });

        // --- Additional UI Setup ---
        matchSelector();
        setMatchText();
        checkBrightBar();

        if (mode) {
            binding.buttonNext.setText(R.string.next_team);
        } else {
            binding.buttonNext.setText(R.string.next_match);
        }

        binding.cycleButton.setOnClickListener(v -> {
            NumberPicker matchPicker = requireView().findViewById(R.id.number_counter_inside);
            viewModel.updateMatchBackup(matchInfo.getMatch());
            matchPicker.setValue(1);
            matchInfo.setMatch(1);
            matchInfo.setTempMatch(1);
            setTeamText(mode, team);
            // Cycle through stored data (for demonstration purposes).
            // Clean up any existing handler first
            if (cycleHandler != null) {
                cycleHandler.removeCallbacksAndMessages(null);
            }
            cycleHandler = new Handler();
            Map<String, ?> pitData = repository.getAllPitData();
            Map<String, ?> matchData = repository.getAllMatchData();
            cycleHandler.postDelayed(new Runnable() {
                int i = 0;
                public void run() {
                    cycleHandler.postDelayed(this, 250);
                    matchPicker.increment();
                    if (mode) {
                        if (pitData != null && i == pitData.size()) {
                            cycleHandler.removeCallbacks(this);
                        }
                    } else {
                        if (matchData != null && i == matchData.size()) {
                            cycleHandler.removeCallbacks(this);
                            matchInfo.setMatch(viewModel.getMatchBackupSync());
                        }
                    }
                    i++;
                }
            }, 250);
        });
    }

    @Override
    public void onDestroyView() {
        // Clean up handler to prevent memory leaks
        if (cycleHandler != null) {
            cycleHandler.removeCallbacksAndMessages(null);
            cycleHandler = null;
        }
        super.onDestroyView();
        binding = null; // Prevent memory leaks.
    }

    // --- UI Update & Helper Methods ---

    /**
     * Updates UI elements (team and match text) with the latest values.
     */
    private void updateUI() {
        Log.d("QR", "Updating UI - Team: " + team + ", Temp Match: " + matchInfo.getTempMatch());
        int tempMatch = matchInfo.getTempMatch();
        // Load team data on background thread
        new Thread(() -> {
            int teamNumber = teamInfo.getTeam(tempMatch);
            requireActivity().runOnUiThread(() -> {
                binding.qrTeamText.setText(String.format(Locale.US, "Team: %d", teamNumber));
                binding.qrMatchText.setText(String.format(Locale.US, "Match: %d", tempMatch));
            });
        }).start();
    }

    /**
     * Adjusts UI elements based on the current scouting mode.
     *
     * @param mode True for Pit mode, false for Crowd mode.
     */
    public void checkMode(boolean mode) {
        Log.d("QR", "Checking mode: " + mode);
        if (mode) {
            // Pit Mode UI adjustments.
            binding.matchCounter.numberCounterInside.setVisibility(View.VISIBLE);
            binding.qrMatchCounterText.setText(R.string.match_selector);
            binding.qrMatchText.setVisibility(View.VISIBLE);
            binding.cycleButton.setVisibility(View.GONE);
        } else {
            // Crowd Mode UI adjustments.
            binding.matchCounter.numberCounterInside.setVisibility(View.VISIBLE);
            binding.qrMatchCounterText.setText(R.string.match_selector);
            binding.qrMatchText.setVisibility(View.VISIBLE);
            binding.cycleButton.setVisibility(View.GONE);
        }
    }

    /**
     * Checks if the app can modify system brightness and configures the brightness bar.
     */
    private void checkBrightBar() {
        if (Settings.System.canWrite(requireActivity())) {
            binding.qrBrightBarText.setText(R.string.brightbar_activated_title);
            binding.brightBar.setEnabled(true);
            brightBar();
        } else {
            binding.qrBrightBarText.setText(R.string.brightbar_deactivated_title);
            binding.qrBrightBarText.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            binding.brightBar.setEnabled(false);
        }
    }

    /**
     * Configures the brightness bar listener to update system brightness.
     */
    private void brightBar() {
        final ContentResolver[] conResolver = new ContentResolver[1];
        Window window = requireActivity().getWindow();
        conResolver[0] = requireActivity().getContentResolver();

        final int[] brightness = {0};
        SeekBar bright_bar = requireView().findViewById(R.id.bright_bar);

        try {
            brightness[0] = Settings.System.getInt(conResolver[0], Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }

        bright_bar.setProgress(brightness[0]);

        bright_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                Settings.System.putInt(conResolver[0], Settings.System.SCREEN_BRIGHTNESS, brightness[0]);
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.screenBrightness = brightness[0] / (float) 255;
                window.setAttributes(layoutParams);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness[0] = Math.max(progress, 20);
            }
        });
    }

    /**
     * Updates the team text display using current match and team information.
     * In Pit mode, if a valid team number is provided (via QR), it will override the default.
     *
     * @param mode True if Pit mode is active.
     * @param team The team number to override (if non-zero).
     */
    private void setTeamText(boolean mode, int team) {
        int tempMatch = matchInfo.getTempMatch();
        // If Pit mode is active and an override team is provided, use it directly.
        if (mode && team != 0) {
            binding.qrTeamText.setText(String.format(Locale.US, "Team: %d", team));
        } else {
            // Load team data on background thread for default display
            new Thread(() -> {
                int teamNumber = teamInfo.getTeam(tempMatch);
                requireActivity().runOnUiThread(() -> {
                    binding.qrTeamText.setText(String.format(Locale.US, "Team: %d", teamNumber));
                });
            }).start();
        }
    }

    /**
     * Updates the match text display using temporary match information.
     */
    private void setMatchText() {
        binding.qrMatchText.setText(String.format(Locale.US, "Match: %d", matchInfo.getTempMatch()));
    }

    /**
     * Configures the match selector NumberPicker and updates the QR code when the match changes.
     */
    private void matchSelector() {
        NumberPicker matchPicker = requireView().findViewById(R.id.number_counter_inside);
        matchPicker = matchInfo.configurePicker(matchPicker);
        matchPicker.setValueChangedListener((value, action) -> {
            matchInfo.setTempMatch(value);
            binding.qrMatchText.setText(String.format(Locale.US, "Match: %d", value));
            setTeamText(false, 0);
            String matchData;
            if (repository.isPitRemoveEnabled()) {
                matchData = repository.getPitMatchData(value);
            } else {
                matchData = repository.getMatchData(value);
            }
            if ("No Data".equals(matchData)) {
                binding.qrImg.setImageBitmap(textAsBitmap("No Data", 100, R.color.green_900));
            } else {
                ImageView qr_img = requireView().findViewById(R.id.qr_img);
                qr_img.setImageBitmap(qrCodeGenerator.generateQRCode(matchData, 1000, 35, true));
            }
        });
    }

    /**
     * Converts a text string into a Bitmap image.
     *
     * @param text      The text to convert.
     * @param textSize  The desired text size.
     * @param textColor The resource ID for the text color.
     * @return A Bitmap image representing the text.
     */
    public Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent();
        int width = (int) (paint.measureText(text) + 0.5f);
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    /**
     * Saves the scanned QR data into local preferences based on scouting mode.
     * In Pit mode, also extracts and updates the team number from the CSV data.
     *
     * @param data The raw QR code data.
     * @param mode True if Pit mode is active.
     */
    private void saveData(String data, boolean mode) {
        try {
            if (mode) {
                repository.setPitMatchData(matchInfo.getTempMatch(), data);
                // Parse team number from CSV data.
                String[] fields = QrCsvParser.parseCsv(data, 1);
                if (fields != null && fields.length > 0) {
                    teamInfo.setTeam(Integer.parseInt(fields[0]));
                } else {
                    Log.e("QR", "Failed to parse team number from data");
                }
            } else {
                repository.setMatchData(matchInfo.getTempMatch(), data);
            }
        } catch (Exception e) {
            Log.e("QR", "Error saving data: " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkBrightBar();
    }
}
