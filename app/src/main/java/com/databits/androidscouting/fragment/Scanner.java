package com.databits.androidscouting.fragment;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.databits.androidscouting.data.repository.PowerPreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.databits.androidscouting.databinding.FragmentScannerBinding;
import com.databits.androidscouting.model.QrCodeDrawable;
import com.databits.androidscouting.model.QrCodeViewModel;
import com.databits.androidscouting.util.GoogleAuthActivity;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.ScoutUtils;
import com.databits.androidscouting.util.SheetsUpdateTask;
import com.databits.androidscouting.util.TeamInfo;
import com.databits.androidscouting.viewmodel.ConfigViewModel;
import com.databits.androidscouting.viewmodel.ConfigViewModelFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.opencsv.CSVWriter;
import com.preference.PowerPreference;
import com.travijuu.numberpicker.library.NumberPicker;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED;

public class Scanner extends Fragment implements SheetsUpdateTask.UiCallback {

    protected BarcodeScanner qrScanner;
    protected ExecutorService cameraExecutor;

    private FragmentScannerBinding binding;
    private ConfigViewModel viewModel;

    private PreferenceRepository repository;

    MatchInfo matchInfo;
    TeamInfo teamInfo;
    ScoutUtils scoutUtils;

    int match;

    PreviewView preview;

    LifecycleCameraController camController;

    private SheetsUpdateTask sheetsUpdateTask;
    private ActivityResultLauncher<Intent> googleAuthLauncher;
    private ActivityResultLauncher<Intent> authorizationLauncher;

    // Camera control state
    private boolean isTorchOn = false;
    private boolean isScanningPaused = false;
    private int totalScans = 0;
    private int successfulScans = 0;
    private long frameCount = 0;
    private long lastFpsTime = System.currentTimeMillis();

    // Feedback objects
    private Vibrator vibrator;
    private ToneGenerator toneGenerator;

    // Camera capability info
    private android.util.Range<Integer> exposureRange;
    private android.util.Range<Float> zoomRange;

    // Cached team data to avoid database access in analyzer callback
    private final String[] cachedTeamNumbers = new String[7]; // Index 0 unused, 1-6 for team positions

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewModel
        repository = PowerPreferenceRepository.getInstance(requireContext());
        ConfigViewModelFactory factory = new ConfigViewModelFactory(repository);
        viewModel = new ViewModelProvider(requireActivity(), factory).get(ConfigViewModel.class);

        googleAuthLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                String accountName = result.getData().getStringExtra(GoogleAuthActivity.EXTRA_ACCOUNT_NAME);
                viewModel.updateGoogleAccountName(accountName);
                // Retry the upload after getting the account
                call_sheets();
            } else {
                Toast.makeText(getContext(), "Google Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });

        authorizationLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // Retry the upload after authorization
                call_sheets();
            } else {
                Toast.makeText(getContext(), "Authorization was denied.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.scanner_menu, menu);
                // Add/Modify menu items Here
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id == R.id.action_change_view) {
                    viewModel.updateIsMaster(!viewModel.getIsMasterSync());
                    refreshUI();
                }

                // Open camera settings dialog
                if (id == R.id.action_camera_settings) {
                    CameraSettingsDialogFragment dialog = new CameraSettingsDialogFragment();
                    dialog.setOnSettingsAppliedListener(() -> {
                        // Apply camera settings immediately when OK is pressed
                        applyCameraSettings();
                        // Refresh UI elements that depend on settings
                        refreshUI();
                    });
                    dialog.show(getParentFragmentManager(), "CameraSettings");
                }

                // Launch the Power Preference debug screen
                if (id == R.id.action_debug) {
                    PowerPreference.showDebugScreen(true);
                }

                return false;
            }
        }, this.getViewLifecycleOwner(), Lifecycle.State.CREATED);
        binding = FragmentScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Go Full screen
        View decorView = requireActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        matchInfo = new MatchInfo();
        teamInfo = new TeamInfo(getContext());
        scoutUtils = new ScoutUtils(getContext());
        sheetsUpdateTask = new SheetsUpdateTask(requireContext(), this);

        match = matchInfo.getMatch();

        String role = viewModel.getDeviceRoleSync();

        NavController controller = NavHostFragment.findNavController(Scanner.this);

        binding.buttonBack.setOnClickListener(view1 -> controller
            .navigateUp());

        binding.buttonUpload.setOnClickListener(view1 -> {
            call_sheets();
        });

        binding.buttonGroupUploadMode.setOnPositionChangedListener(position -> {
            switch (position) {
                case 0:
                    viewModel.updateUploadMode("Crowd");
                    break;
                case 1:
                    viewModel.updateUploadMode("Speciality");
                    break;
                case 2:
                    viewModel.updateUploadMode("Pit");
                    break;

            }
        });

        binding.testButton.setOnClickListener(view1 ->
            PowerPreference.showDebugScreen(true));

        NumberPicker matchCounter = binding.uiInsideNumberPicker;

        matchCounter = matchInfo.configurePicker(matchCounter);

        // Check team list size on background thread
        new Thread(() -> {
            int teamMatchListSize = repository.getTeamMatchListSize();
            if (teamMatchListSize == 0) {
                teamInfo.read_teams();
            }

            // Check if teams are loaded (also accesses database)
            boolean teamsAreLoaded = teamInfo.teamsLoaded();

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (isAdded() && teamsAreLoaded) {
                        setupTeamDisplay(match);
                    }
                });
            }
        }).start();

        //Override the default listener to configure the ui
        matchCounter.setValueChangedListener((value, action) -> {
            matchInfo.setMatch(value);
            setupTeamDisplay(value);
        });

        if (role != null) {
            if (viewModel.getRoleLockedSync() && (!role.equals("master"))) {
                viewModel.updateIsMaster(false);
            } else if (role.equals("master")) {
                viewModel.updateIsMaster(true);
                binding.buttonBack.setVisibility(View.INVISIBLE);
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        preview = binding.previewView;
        refreshUI();
        openCamera();

        // Setup camera controls and auto-settings
        setupCameraControls();
        setupAutoSettings();
    }

    protected void openCamera() {

        // Add the option to detect all types of QR Codes
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
            //.setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build();

        qrScanner = BarcodeScanning.getClient(options);
        camController = new LifecycleCameraController(requireContext());
        camController.setCameraSelector(new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build());

        camController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(requireContext()),
            getQrCodeAnalyzer(preview));

        camController.setTapToFocusEnabled(true);
        camController.setPinchToZoomEnabled(true);

        camController.bindToLifecycle(this);

        // Apply camera settings from preferences
        applyCameraSettings();

        preview.setController(camController);

        // Update resolution display once preview is ready
        preview.post(() -> {
            if (repository.isCameraShowResolutionEnabled()) {
                updateResolutionDisplay();
            }
        });

        refreshActionBar();
    }

    protected MlKitAnalyzer getQrCodeAnalyzer(PreviewView preview) {
        return new MlKitAnalyzer(
            Collections.singletonList(qrScanner), COORDINATE_SYSTEM_VIEW_REFERENCED,
            ContextCompat.getMainExecutor(requireContext()),
            result -> {
                // Update FPS counter on every frame
                updateFpsCounter();

                List<Barcode> qrResList = result.getValue(qrScanner);
                if (qrResList == null || qrResList.isEmpty() || qrResList.get(0) == null) {
                    preview.getOverlay().clear();
                    //if there is no result
                    return;
                }

                Barcode qr = qrResList.get(0);

                // Apply ML Kit enhancements - size and position filtering
                if (!checkBarcodeSize(qr)) {
                    preview.getOverlay().clear();
                    return; // Too small, ignore
                }
                if (!checkBarcodePosition(qr, preview)) {
                    preview.getOverlay().clear();
                    return; // Not centered, ignore
                }

                // Check if scanning is paused
                if (isScanningPaused) {
                    preview.getOverlay().clear();
                    return; // Scanning paused
                }

                String bar_string = qr.getRawValue();
                preview.getOverlay().clear();

                QrCodeViewModel qrCodeViewModel = new QrCodeViewModel(qr);
                QrCodeDrawable qrCodeDrawable = new QrCodeDrawable(qrCodeViewModel);

                preview.getOverlay().add(qrCodeDrawable);

                assert bar_string != null;

                // Show feedback and animation for successful scan
                showScanFeedback();
                animateScanSuccess();

                // Update statistics
                totalScans++;
                successfulScans++;
                if (repository.isCameraShowStatsEnabled()) {
                    updateScanStatistics();
                }

                if (bar_string.startsWith("ScoutData")) {
                    String[] scouterList = bar_string.split(",");
                    viewModel.updateScouterList(Arrays.asList(scouterList));
                }  else if (bar_string.startsWith("GoogleConfig")) {
                    String[] parts = bar_string.split(",");
                    // parts[0] is "GoogleConfig"
                    viewModel.updateWorkbookId(parts[1]);
                    viewModel.updateCrowdRange(parts[2]);
                    viewModel.updatePitRange(parts[3]);
                    viewModel.updateSpecialtyRange(parts[4]);
                } else if (bar_string.startsWith("MatchData")) {
                    List<String[]> matchData = splitMatchData(bar_string);
                    if (!matchData.isEmpty()) {
                        // Import new match data into Room database
                        // importTeamSchedule handles deduplication via unique index
                        String[][] newMatchDataArray = matchData.toArray(new String[0][0]);
                        viewModel.importTeamSchedule(newMatchDataArray);
                        setupTeamDisplay(match);
                    }
                } else if (bar_string.startsWith("role")) {
                    process_qr(bar_string);
                } else {
                    // Check if scanned QR matches any team in this match
                    android.util.Log.d("Scanner", "Checking QR: " + bar_string);
                    android.util.Log.d("Scanner", "Cached teams: " + java.util.Arrays.toString(cachedTeamNumbers));

                    boolean teamFound = false;
                    if (cachedTeamNumbers[1] != null && bar_string.contains(cachedTeamNumbers[1])) {
                        android.util.Log.d("Scanner", "Matched Blue 1: " + cachedTeamNumbers[1]);
                        set_team(R.id.blue1);
                        saveData(bar_string);
                        teamFound = true;
                    } else if (cachedTeamNumbers[2] != null && bar_string.contains(cachedTeamNumbers[2])) {
                        android.util.Log.d("Scanner", "Matched Blue 2: " + cachedTeamNumbers[2]);
                        set_team(R.id.blue2);
                        saveData(bar_string);
                        teamFound = true;
                    } else if (cachedTeamNumbers[3] != null && bar_string.contains(cachedTeamNumbers[3])) {
                        android.util.Log.d("Scanner", "Matched Blue 3: " + cachedTeamNumbers[3]);
                        set_team(R.id.blue3);
                        saveData(bar_string);
                        teamFound = true;
                    } else if (cachedTeamNumbers[4] != null && bar_string.contains(cachedTeamNumbers[4])) {
                        android.util.Log.d("Scanner", "Matched Red 1: " + cachedTeamNumbers[4]);
                        set_team(R.id.red1);
                        saveData(bar_string);
                        teamFound = true;
                    } else if (cachedTeamNumbers[5] != null && bar_string.contains(cachedTeamNumbers[5])) {
                        android.util.Log.d("Scanner", "Matched Red 2: " + cachedTeamNumbers[5]);
                        set_team(R.id.red2);
                        saveData(bar_string);
                        teamFound = true;
                    } else if (cachedTeamNumbers[6] != null && bar_string.contains(cachedTeamNumbers[6])) {
                        android.util.Log.d("Scanner", "Matched Red 3: " + cachedTeamNumbers[6]);
                        set_team(R.id.red3);
                        saveData(bar_string);
                        teamFound = true;
                    }

                    if (!teamFound && !bar_string.startsWith("role")) {
                        android.util.Log.d("Scanner", "No team match found, saving data anyway");
                        saveData(bar_string);
                    }
                }
            }
        );
    }

    public List<String[]> splitMatchData(String matchDataString) {
        List<String[]> result = new ArrayList<>();

        // Check if the string starts with "MatchData" and has the correct format
        if (!matchDataString.startsWith("MatchData")) {
            System.err.println("Invalid data format: String does not start with 'MatchData'");
            return result; // Return empty list for invalid format
        }

        // Split the string into parts based on the first two commas
        String[] parts = matchDataString.split(",", 3);
        if (parts.length != 3) {
            System.err.println("Invalid data format: Incorrect number of commas");
            return result; // Return empty list for invalid format
        }

        // Extract the chunk index
        int chunkIndex;
        try {
            chunkIndex = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid data format: Chunk index is not a number");
            return result; // Return empty list for invalid format
        }
        // Check if this chunk has already been processed
        if (repository.hasProcessedChunk(chunkIndex)) {
            System.out.println("Duplicate chunk detected: " + chunkIndex + ". Skipping.");
            return result; // Return empty list to indicate no new data
        }

        // Mark the chunk as processed
        viewModel.markChunkProcessed(chunkIndex);

        // Split the data string into individual match entries
        String[] matchEntries = parts[2].split("(?<=])(?=\\[)");

        // Process each match entry
        for (String entry : matchEntries) {
            // Remove any remaining brackets and split by comma
            String cleanedEntry = entry.replaceAll("[\\[\\]]", "");
            String[] values = cleanedEntry.split(",");

            // Trim whitespace from each value
            for (int i = 0; i < values.length; i++) {
                values[i] = values[i].trim();
            }

            result.add(values);
        }

        return result;
    }

    private void makeUploadFile(String bar_string) {
        File file = new File(requireContext().getFilesDir() + "/" + "upload.csv");
        try {
            FileWriter uploadFile = new FileWriter(file, true);
            CSVWriter uploader = new CSVWriter(uploadFile, CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
            List<String[]> upload_data = new ArrayList<>();
            String timeStamp = new SimpleDateFormat("MM-dd-yy hh:mmaaa",
                Locale.getDefault()).format(new Date());

            upload_data.add(new String[] {bar_string + "," + timeStamp});

            uploader.writeAll(upload_data);
            uploader.flush();
            uploader.close();

            //binding.buttonBack.setVisibility(View.INVISIBLE);
            //final Handler handler = new Handler();
            //handler.postDelayed(() -> binding.buttonBack.setVisibility(View.VISIBLE),
            //    3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveData(String bar_string) {

        String uploadMode = repository.getUploadMode();
        if (uploadMode == null) {
            uploadMode = "Crowd";
        }

        String dataType;
        switch (uploadMode) {
            case "Crowd":
                dataType = "CROWD";
                break;
            case "Speciality":
                dataType = "SPECIALTY";
                break;
            case "Pit":
                dataType = "PIT";
                break;
            default:
                dataType = "CROWD";
                break;
        }

        // Make upload.csv for debugging
        makeUploadFile(bar_string);

        // Check for duplicate using Room seen_lines in background thread
        final String finalDataType = dataType;
        new Thread(() -> {
            // Check if line has been seen (Room database access)
            if (!repository.hasSeenLine(bar_string, finalDataType) && !bar_string.contains("Role")) {
                // Mark as seen
                repository.markLineSeen(bar_string, finalDataType);

                // Add to upload queue
                com.databits.androidscouting.data.entity.UploadQueueItem item =
                    new com.databits.androidscouting.data.entity.UploadQueueItem();
                item.uploadType = finalDataType;
                item.dataCsv = bar_string;

                // Extract match and team number from CSV string if possible
                String[] parts = bar_string.split(",");
                if (parts.length > 0) {
                    try {
                        item.matchNumber = Integer.parseInt(parts[0]);
                    } catch (NumberFormatException e) {
                        item.matchNumber = null;
                    }
                }
                if (parts.length > 1) {
                    item.teamNumber = parts[1];
                }

                viewModel.addUploadItem(item);
            }
        }).start();
    }

    private void set_team(int id) {
        android.util.Log.d("Scanner", "set_team called for id: " + getResources().getResourceEntryName(id));
        if (!isAdded()) return; // Fragment not attached to activity
        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) return; // Double-check inside runOnUiThread
            TextView text = requireView().findViewById(id);
            if (text != null) {
                text.setBackgroundTintList(getResources().getColorStateList(R.color.green_900, null));
                android.util.Log.d("Scanner", "Team box turned green for: " + text.getText());
            } else {
                android.util.Log.w("Scanner", "TextView not found for id: " + id);
            }
        });
    }

    private void setupTeamDisplay(int match) {
        int[] teamIds = new int[] {R.id.blue1, R.id.blue2, R.id.blue3, R.id.red1, R.id.red2,
            R.id.red3};

        // Load team data on background thread
        new Thread(() -> {
            String[] teams = new String[6];
            for (int i = 0; i < 6; i++) {
                teams[i] = teamInfo.getMasterTeam(match, i+1);
                // Cache team numbers for use in barcode analyzer (avoid database access on main thread)
                cachedTeamNumbers[i+1] = teams[i];
            }

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return; // Fragment not attached to activity
                for (int i = 0; i < teamIds.length; i++) {
                    TextView team = requireView().findViewById(teamIds[i]);
                    team.setText(teams[i]);
                    if (i == 0 || i == 1 || i == 2) {
                        team.setBackgroundTintList(getResources().getColorStateList(
                            android.R.color.holo_blue_light,null));
                    } else {
                        team.setBackgroundTintList(getResources().getColorStateList(
                            android.R.color.holo_red_light, null));
                    }
                }
            });
        }).start();
    }

    private void process_qr(String raw_qr) {
        NavController controller = NavHostFragment.findNavController(Scanner.this);
        String[] qr_data = raw_qr.split(",");

        // QR Data goes up by 2 to skip the next text field
        String role = qr_data[1];
        //String crowd_pos = qr_data[3];
        int crowd_num = Integer.parseInt(qr_data[3]);
        String name = qr_data[5];
        boolean locked = Boolean.parseBoolean(qr_data[7]);
        int match = Integer.parseInt(qr_data[9]);
        boolean delete_data = Boolean.parseBoolean(qr_data[11]);
        boolean special_selector = Boolean.parseBoolean(qr_data[13]);

        if (delete_data) {
            PowerPreference.clearAllData();
            //teamInfo.read_teams();
        }

        viewModel.updateDeviceRole(role);
        viewModel.updateCrowdPosition(crowd_num);
        viewModel.updateCurrentScouter(name);
        viewModel.updateRoleLocked(locked);
        viewModel.updateSpecialSwitch(special_selector);
    }

    public static void restartApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    protected void call_sheets() {
        if (!isAdded()) return; // Fragment not attached to activity

        if (repository.getGoogleAccountName() == null) {
            googleAuthLauncher.launch(GoogleAuthActivity.newIntent(requireContext()));
        } else {
            String spreadsheetId = repository.getWorkbookId();
            if (spreadsheetId == null) {
                spreadsheetId = "1ksCFboY3RF0d6eCQHtH2bdrPWFHXZXnicNJGz6pXndM";
            }
            sheetsUpdateTask.execute(spreadsheetId);
        }
    }

    public void refreshActionBar() {
        if (!isAdded()) return; // Fragment not attached to activity

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null) return;

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Scanner");
            actionBar.setSubtitle("");
        }
    }

    private void refreshUI() {
        //toggle isMaster on press
        if (!viewModel.getIsMasterSync()) {
            binding.teamListDisplay.getRoot().setVisibility(View.GONE);
            binding.uiInsideNumberPicker.setVisibility(View.GONE);
            binding.scanPrompt.setText(R.string.role_qr_title);
            binding.scanPrompt.setVisibility(View.VISIBLE);
            binding.buttonBack.setText(R.string.back);
            binding.matchSelectorText.setVisibility(View.GONE);
            binding.buttonUpload.setVisibility(View.GONE);
            binding.buttonGroupUploadMode.setVisibility(View.GONE);
        } else {
            binding.teamListDisplay.getRoot().setVisibility(View.VISIBLE);
            binding.uiInsideNumberPicker.setVisibility(View.VISIBLE);
            binding.scanPrompt.setVisibility(View.GONE);
            binding.buttonBack.setText(R.string.back);
            binding.matchSelectorText.setVisibility(View.VISIBLE);
            binding.buttonUpload.setVisibility(View.VISIBLE);
            binding.buttonGroupUploadMode.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onAuthorizationRequired(UserRecoverableAuthIOException e) {
        authorizationLauncher.launch(e.getIntent());
    }

    @Override
    public void onUploadSuccess(String updatedRange) {
        if (!isAdded()) return; // Fragment not attached to activity
        requireActivity().runOnUiThread(() -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Upload successful: " + updatedRange, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onUploadFailed() {
        if (!isAdded()) return; // Fragment not attached to activity
        requireActivity().runOnUiThread(() -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Upload failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNoDataToUpload() {
        if (!isAdded()) return; // Fragment not attached to activity
        requireActivity().runOnUiThread(() -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "No data to upload.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDuplicateData() {
        if (!isAdded()) return; // Fragment not attached to activity
        requireActivity().runOnUiThread(() -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Data is a duplicate and was not uploaded.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==================== Camera Control Methods ====================

    /**
     * Setup all camera controls and observers
     */
    private void setupCameraControls() {
        setupTorchButton();
        setupZoomButtons();
        setupPauseScanButton();
        observeCameraSettings();
    }

    /**
     * Setup torch toggle button
     */
    private void setupTorchButton() {
        binding.torchButton.setOnClickListener(v -> {
            isTorchOn = !isTorchOn;
            if (camController != null) {
                camController.enableTorch(isTorchOn);
            }
            viewModel.updateCameraTorch(isTorchOn);
            binding.torchButton.setImageResource(isTorchOn ?
                R.drawable.ic_flashlight_on : R.drawable.ic_flashlight_off);
            binding.torchButton.setContentDescription(getString(isTorchOn ?
                R.string.torch_on : R.string.torch_off));
        });

        // Restore torch state from preferences
        viewModel.getCameraTorch().observe(getViewLifecycleOwner(), enabled -> {
            if (enabled != null && enabled != isTorchOn) {
                isTorchOn = enabled;
                if (camController != null) {
                    camController.enableTorch(enabled);
                }
                binding.torchButton.setImageResource(enabled ?
                    R.drawable.ic_flashlight_on : R.drawable.ic_flashlight_off);
            }
        });
    }

    /**
     * Setup zoom buttons with smooth animations and long press support
     */
    private void setupZoomButtons() {
        final float ZOOM_STEP = 0.25f; // Zoom increment per button press
        final float ZOOM_CONTINUOUS_STEP = 0.05f; // Smaller step for continuous zoom
        final float MIN_ZOOM = 1.0f;
        final float MAX_ZOOM = 4.0f;
        final int REPEAT_DELAY = 50; // ms between zoom steps during long press

        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        final boolean[] isLongPressing = {false};

        // Zoom In button
        binding.zoomInButton.setOnClickListener(v -> {
            if (!isLongPressing[0] && camController != null) {
                float currentZoom = camController.getZoomState().getValue().getZoomRatio();
                float newZoom = Math.min(currentZoom + ZOOM_STEP, MAX_ZOOM);
                animateZoom(newZoom);
                viewModel.updateCameraZoomLevel(newZoom);
            }
        });

        binding.zoomInButton.setOnLongClickListener(v -> {
            isLongPressing[0] = true;
            final Runnable zoomRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isLongPressing[0] && camController != null) {
                        float currentZoom = camController.getZoomState().getValue().getZoomRatio();
                        float newZoom = Math.min(currentZoom + ZOOM_CONTINUOUS_STEP, MAX_ZOOM);
                        if (newZoom < MAX_ZOOM) {
                            camController.setZoomRatio(newZoom);
                            handler.postDelayed(this, REPEAT_DELAY);
                        } else {
                            camController.setZoomRatio(MAX_ZOOM);
                            isLongPressing[0] = false;
                        }
                    }
                }
            };
            handler.post(zoomRunnable);
            return true;
        });

        binding.zoomInButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                if (isLongPressing[0]) {
                    isLongPressing[0] = false;
                    handler.removeCallbacksAndMessages(null);
                    if (camController != null) {
                        float finalZoom = camController.getZoomState().getValue().getZoomRatio();
                        viewModel.updateCameraZoomLevel(finalZoom);
                    }
                }
            }
            return false; // Allow other listeners to handle the event
        });

        // Zoom Out button
        binding.zoomOutButton.setOnClickListener(v -> {
            if (!isLongPressing[0] && camController != null) {
                float currentZoom = camController.getZoomState().getValue().getZoomRatio();
                float newZoom = Math.max(currentZoom - ZOOM_STEP, MIN_ZOOM);
                animateZoom(newZoom);
                viewModel.updateCameraZoomLevel(newZoom);
            }
        });

        binding.zoomOutButton.setOnLongClickListener(v -> {
            isLongPressing[0] = true;
            final Runnable zoomRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isLongPressing[0] && camController != null) {
                        float currentZoom = camController.getZoomState().getValue().getZoomRatio();
                        float newZoom = Math.max(currentZoom - ZOOM_CONTINUOUS_STEP, MIN_ZOOM);
                        if (newZoom > MIN_ZOOM) {
                            camController.setZoomRatio(newZoom);
                            handler.postDelayed(this, REPEAT_DELAY);
                        } else {
                            camController.setZoomRatio(MIN_ZOOM);
                            isLongPressing[0] = false;
                        }
                    }
                }
            };
            handler.post(zoomRunnable);
            return true;
        });

        binding.zoomOutButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                if (isLongPressing[0]) {
                    isLongPressing[0] = false;
                    handler.removeCallbacksAndMessages(null);
                    if (camController != null) {
                        float finalZoom = camController.getZoomState().getValue().getZoomRatio();
                        viewModel.updateCameraZoomLevel(finalZoom);
                    }
                }
            }
            return false; // Allow other listeners to handle the event
        });

        // Restore zoom level from preferences
        viewModel.getCameraZoomLevel().observe(getViewLifecycleOwner(), zoom -> {
            if (zoom != null && camController != null) {
                camController.setZoomRatio(zoom);
            }
        });
    }

    /**
     * Animate zoom changes smoothly
     */
    private void animateZoom(float targetZoom) {
        if (camController == null || camController.getZoomState().getValue() == null) return;

        Float currentZoom = camController.getZoomState().getValue().getZoomRatio();
        if (currentZoom == null) currentZoom = 1.0f;

        ValueAnimator animator = ValueAnimator.ofFloat(currentZoom, targetZoom);
        animator.addUpdateListener(anim ->
            camController.setZoomRatio((Float) anim.getAnimatedValue())
        );
        animator.setDuration(200);
        animator.start();
    }

    /**
     * Setup pause/resume scanning button
     */
    private void setupPauseScanButton() {
        binding.pauseScanButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isScanningPaused = isChecked;
            if (isChecked) {
                // Scanning is paused
                binding.pauseScanButton.setText(R.string.resume_scanning);
            } else {
                // Scanning is resumed
                binding.pauseScanButton.setText(R.string.pause_scanning);
            }
        });
    }

    /**
     * Observe camera settings and update UI accordingly
     */
    private void observeCameraSettings() {
        viewModel.getCameraShowReticle().observe(getViewLifecycleOwner(), enabled -> {
            if (enabled != null) {
                binding.scanReticle.setVisibility(enabled ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getCameraShowStats().observe(getViewLifecycleOwner(), enabled -> {
            if (enabled != null) {
                binding.statsText.setVisibility(enabled ? View.VISIBLE : View.GONE);
                binding.scanControlBar.setVisibility(enabled ? View.VISIBLE : View.GONE);
                if (enabled) {
                    updateScanStatistics();
                }
            }
        });

        viewModel.getCameraShowFps().observe(getViewLifecycleOwner(), enabled -> {
            if (enabled != null) {
                binding.fpsText.setVisibility(enabled ? View.VISIBLE : View.GONE);
                updateDebugInfoPanelVisibility();
            }
        });

        viewModel.getCameraShowResolution().observe(getViewLifecycleOwner(), enabled -> {
            if (enabled != null) {
                binding.resolutionText.setVisibility(enabled ? View.VISIBLE : View.GONE);
                if (enabled) {
                    updateResolutionDisplay();
                }
                updateDebugInfoPanelVisibility();
            }
        });

        // Observe exposure compensation changes
        viewModel.getCameraExposureCompensation().observe(getViewLifecycleOwner(), exposure -> {
            if (exposure != null && camController != null) {
                if (exposureRange != null && exposureRange.contains(exposure)) {
                    camController.getCameraControl().setExposureCompensationIndex(exposure);
                }
            }
        });
    }

    /**
     * Setup auto-settings (brightness, keep screen on, orientation lock)
     */
    private void setupAutoSettings() {
        // Auto-brightness
        if (repository.isCameraAutoBrightnessEnabled()) {
            Window window = requireActivity().getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = 1.0f; // Max brightness
            window.setAttributes(layoutParams);
        }

        // Keep screen on
        if (repository.isCameraKeepScreenOnEnabled()) {
            requireActivity().getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        // Orientation lock to portrait
        requireActivity().setRequestedOrientation(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );
    }

    /**
     * Apply camera settings from preferences (exposure compensation, etc.)
     */
    private void applyCameraSettings() {
        if (camController == null) return;

        // Get camera info and capability ranges
        androidx.camera.core.CameraInfo cameraInfo = camController.getCameraInfo();
        if (cameraInfo != null) {
            // Get exposure compensation range
            androidx.camera.core.ExposureState exposureState = cameraInfo.getExposureState();
            if (exposureState != null) {
                exposureRange = exposureState.getExposureCompensationRange();
            }

            // Get zoom range
            androidx.lifecycle.LiveData<androidx.camera.core.ZoomState> zoomState = cameraInfo.getZoomState();
            if (zoomState != null && zoomState.getValue() != null) {
                zoomRange = new android.util.Range<>(
                    zoomState.getValue().getMinZoomRatio(),
                    zoomState.getValue().getMaxZoomRatio()
                );
            }
        }

        // Apply exposure compensation from preferences
        int exposureCompensation = repository.getCameraExposureCompensation();
        if (exposureRange != null && exposureRange.contains(exposureCompensation)) {
            camController.getCameraControl().setExposureCompensationIndex(exposureCompensation);
        }
    }

    /**
     * Show scan feedback (haptic + audio + visual)
     */
    private void showScanFeedback() {
        if (!isAdded()) return; // Fragment not attached to activity

        // Haptic feedback
        if (repository.isCameraHapticFeedbackEnabled()) {
            if (vibrator == null) {
                vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }

        // Audio feedback
        if (repository.isCameraAudioFeedbackEnabled()) {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
            }
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
        }
    }

    /**
     * Animate scan success with green flash
     */
    private void animateScanSuccess() {
        if (!repository.isCameraShowSuccessAnimationEnabled()) return;
        if (!isAdded()) return; // Fragment not attached to activity

        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) return; // Double-check inside runOnUiThread
            binding.scanSuccessOverlay.setVisibility(View.VISIBLE);
            binding.scanSuccessOverlay.setAlpha(0.4f);
            binding.scanSuccessOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    if (isAdded()) {
                        binding.scanSuccessOverlay.setVisibility(View.GONE);
                    }
                })
                .start();
        });
    }

    /**
     * Update scan statistics display
     */
    private void updateScanStatistics() {
        if (totalScans == 0) {
            binding.statsText.setText("Scanned: 0/0 (0%)");
        } else {
            double successRate = (successfulScans * 100.0) / totalScans;
            binding.statsText.setText(String.format(Locale.US,
                "Scanned: %d/%d (%.1f%%)", successfulScans, totalScans, successRate));
        }
    }

    /**
     * Update FPS counter
     */
    private void updateFpsCounter() {
        if (!repository.isCameraShowFpsEnabled()) return;
        if (!isAdded()) return; // Fragment not attached to activity

        frameCount++;
        long now = System.currentTimeMillis();
        if (now - lastFpsTime >= 1000) {
            double fps = frameCount / ((now - lastFpsTime) / 1000.0);
            requireActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    binding.fpsText.setText(String.format(Locale.US, "FPS: %.1f", fps));
                }
            });
            frameCount = 0;
            lastFpsTime = now;
        }
    }

    /**
     * Update resolution display
     */
    private void updateResolutionDisplay() {
        if (!repository.isCameraShowResolutionEnabled()) return;
        if (camController == null || preview == null) return;
        if (!isAdded()) return; // Fragment not attached to activity

        requireActivity().runOnUiThread(() -> {
            if (isAdded()) {
                int width = preview.getWidth();
                int height = preview.getHeight();
                binding.resolutionText.setText(String.format(Locale.US, "Resolution: %dx%d", width, height));
            }
        });
    }

    /**
     * Update debug info panel visibility based on whether any debug info is shown
     */
    private void updateDebugInfoPanelVisibility() {
        boolean showFps = repository.isCameraShowFpsEnabled();
        boolean showResolution = repository.isCameraShowResolutionEnabled();
        boolean showAny = showFps || showResolution;
        binding.debugInfoPanel.setVisibility(showAny ? View.VISIBLE : View.GONE);
    }

    /**
     * Check if barcode meets minimum size requirement
     */
    private boolean checkBarcodeSize(Barcode barcode) {
        if (barcode.getBoundingBox() == null) return true;

        int minSize = repository.getCameraMinBarcodeSize();
        int width = barcode.getBoundingBox().width();
        int height = barcode.getBoundingBox().height();

        return width >= minSize && height >= minSize;
    }

    /**
     * Check if barcode is near center (center-weighted scanning)
     */
    private boolean checkBarcodePosition(Barcode barcode, PreviewView preview) {
        if (!repository.isCameraCenterWeightedEnabled()) return true;
        if (barcode.getBoundingBox() == null) return true;

        int centerX = preview.getWidth() / 2;
        int centerY = preview.getHeight() / 2;
        int qrCenterX = barcode.getBoundingBox().centerX();
        int qrCenterY = barcode.getBoundingBox().centerY();

        double distance = Math.sqrt(
            Math.pow(centerX - qrCenterX, 2) +
            Math.pow(centerY - qrCenterY, 2)
        );

        int threshold = repository.getCameraCenterThreshold();
        return distance <= threshold;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Cleanup tone generator
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }

        // Restore auto-brightness if it was enabled
        if (repository.isCameraAutoBrightnessEnabled()) {
            Window window = requireActivity().getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            window.setAttributes(layoutParams);
        }

        // Remove keep screen on flag
        if (repository.isCameraKeepScreenOnEnabled()) {
            requireActivity().getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }
    }
}
