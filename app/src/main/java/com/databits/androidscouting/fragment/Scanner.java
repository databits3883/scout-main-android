package com.databits.androidscouting.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

    private final PreferenceRepository repository = PowerPreferenceRepository.getInstance();

    MatchInfo matchInfo;
    TeamInfo teamInfo;
    ScoutUtils scoutUtils;

    int match;

    PreviewView preview;

    LifecycleCameraController camController;

    private SheetsUpdateTask sheetsUpdateTask;
    private ActivityResultLauncher<Intent> googleAuthLauncher;
    private ActivityResultLauncher<Intent> authorizationLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewModel
        PreferenceRepository repo = PowerPreferenceRepository.getInstance();
        ConfigViewModelFactory factory = new ConfigViewModelFactory(repo);
        viewModel = new ViewModelProvider(this, factory).get(ConfigViewModel.class);

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

            requireActivity().runOnUiThread(() -> {
                if (teamsAreLoaded) {
                    setupTeamDisplay(match);
                }
            });
        }).start();

        //Override the default listener to configure the ui
        matchCounter.setValueChangedListener((value, action) -> {
            matchInfo.setMatch(value);
            setupTeamDisplay(value);
        });

        if (viewModel.getRoleLockedSync() && (!role.equals("master"))) {
            viewModel.updateIsMaster(false);
        } else if (role.equals("master")) {
            viewModel.updateIsMaster(true);
            binding.buttonBack.setVisibility(View.INVISIBLE);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        preview = binding.previewView;
        refreshUI();
        openCamera();
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

        preview.setController(camController);

        refreshActionBar();
    }

    protected MlKitAnalyzer getQrCodeAnalyzer(PreviewView preview) {
        return new MlKitAnalyzer(
            Collections.singletonList(qrScanner), COORDINATE_SYSTEM_VIEW_REFERENCED,
            ContextCompat.getMainExecutor(requireContext()),
            result -> {
                List<Barcode> qrResList = result.getValue(qrScanner);
                if (qrResList == null || qrResList.isEmpty() || qrResList.get(0) == null) {
                    preview.getOverlay().clear();
                    //if there is no result
                    return;
                }

                Barcode qr = qrResList.get(0);
                String bar_string = qr.getRawValue();
                preview.getOverlay().clear();

                QrCodeViewModel qrCodeViewModel = new QrCodeViewModel(qr);
                QrCodeDrawable qrCodeDrawable = new QrCodeDrawable(qrCodeViewModel);

                preview.getOverlay().add(qrCodeDrawable);

                assert bar_string != null;

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
                } else if (bar_string.contains(teamInfo.getMasterTeam(match, 1))) {
                    set_team(R.id.blue1);
                    saveData(bar_string);
                } else if (bar_string.contains(teamInfo.getMasterTeam(match, 2))) {
                    set_team(R.id.blue2);
                    saveData(bar_string);
                } else if (bar_string.contains(teamInfo.getMasterTeam(match, 3))) {
                    set_team(R.id.blue3);
                    saveData(bar_string);
                } else if (bar_string.contains(teamInfo.getMasterTeam(match, 4))) {
                    set_team(R.id.red1);
                    saveData(bar_string);
                } else if (bar_string.contains(teamInfo.getMasterTeam(match, 5))) {
                    set_team(R.id.red2);
                    saveData(bar_string);
                } else if (bar_string.contains(teamInfo.getMasterTeam(match, 6))) {
                    set_team(R.id.red3);
                    saveData(bar_string);
                } else if (!bar_string.startsWith("role")) {
                    saveData(bar_string);
                } else if (!bar_string.split(",")[0].equals(String.valueOf(
                    match))) {
                    // Disables the camera
                    camController.unbind();
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Wrong Match!");
                    builder.setCancelable(false);
                    builder.setMessage("Scanned Match: " + bar_string.split(",")[0]
                        + "\nCurrent Match: " + match);
                    builder.setPositiveButton("Ignore bad data", (dialog, which) -> {
                        dialog.dismiss();
                        camController.bindToLifecycle(this);
                        camController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(
                                requireContext()),
                            getQrCodeAnalyzer(preview));
                        //openCamera();
                    });
                    builder.setNeutralButton("Upload Anyways", (dialog, i) -> {
                        dialog.dismiss();
                        saveData(bar_string);
                        viewModel.updateForceUpload(true);
                        camController.bindToLifecycle(this);
                        camController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(
                                requireContext()),
                            getQrCodeAnalyzer(preview));
                        //openCamera();
                    });
                    builder.show();

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
                CSVWriter.DEFAULT_ESCAPE_CHARACTER);
            List<String[]> upload_data = new ArrayList<>();
            String timeStamp = new SimpleDateFormat("MM-dd-yy hh:mmaaa",
                Locale.getDefault()).format(new Date());

            upload_data.add(new String[] {bar_string + "," + timeStamp});

            uploader.writeAll(upload_data);
            uploader.flush();
            uploader.close();

            binding.buttonBack.setVisibility(View.INVISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(() -> binding.buttonBack.setVisibility(View.VISIBLE),
                3000);
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

        // Check for duplicate using Room seen_lines and don't upload role qr data
        if (!repository.hasSeenLine(bar_string, dataType) && !bar_string.contains("Role")) {
            // Mark as seen
            repository.markLineSeen(bar_string, dataType);

            // Add to upload queue
            com.databits.androidscouting.data.entity.UploadQueueItem item =
                new com.databits.androidscouting.data.entity.UploadQueueItem();
            item.uploadType = dataType;
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
    }

    private void set_team(int id) {
        TextView text = requireView().findViewById(id);
        text.setBackgroundTintList(getResources().getColorStateList(R.color.green_900,null));
    }

    private void setupTeamDisplay(int match) {
        int[] teamIds = new int[] {R.id.blue1, R.id.blue2, R.id.blue3, R.id.red1, R.id.red2,
            R.id.red3};

        // Load team data on background thread
        new Thread(() -> {
            String[] teams = new String[6];
            for (int i = 0; i < 6; i++) {
                teams[i] = teamInfo.getMasterTeam(match, i+1);
            }

            requireActivity().runOnUiThread(() -> {
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
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        assert activity != null;
        ActionBar actionBar = activity.getSupportActionBar();
        Objects.requireNonNull(actionBar).setTitle("Scanner");
        Objects.requireNonNull(actionBar).setSubtitle("");
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
        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Upload successful: " + updatedRange, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onUploadFailed() {
        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Upload failed. Please try again.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onNoDataToUpload() {
        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "No data to upload.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDuplicateData() {
        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Data is a duplicate and was not uploaded.", Toast.LENGTH_SHORT).show());
    }
}
