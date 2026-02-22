package com.databits.androidscouting.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.databits.androidscouting.databinding.FragmentScannerBinding;
import com.databits.androidscouting.data.repository.AppRepositories;
import com.databits.androidscouting.data.repository.CameraSettingsStore;
import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import com.databits.androidscouting.data.repository.ScheduleStore;
import com.databits.androidscouting.data.repository.SyncStore;
import com.databits.androidscouting.core.domain.scanner.FindMatchedTeamSlotUseCase;
import com.databits.androidscouting.core.domain.scanner.ProcessScanPayloadUseCase;
import com.databits.androidscouting.core.domain.provision.ApplyRoleProvisionUseCase;
import com.databits.androidscouting.core.domain.schedule.ImportMatchDataChunkUseCase;
import com.databits.androidscouting.core.domain.schedule.ImportResult;
import com.databits.androidscouting.core.domain.upload.QueueScanDataUseCase;
import com.databits.androidscouting.core.model.ScanPayload;
import com.databits.androidscouting.core.model.UploadMode;
import com.databits.androidscouting.model.QrCodeDrawable;
import com.databits.androidscouting.model.QrCodeViewModel;
import com.databits.androidscouting.util.GoogleAuthActivity;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.ScoutUtils;
import com.databits.androidscouting.util.SheetsUpdateTask;
import com.databits.androidscouting.util.TeamInfo;
import com.databits.androidscouting.viewmodel.CameraSettingsViewModel;
import com.databits.androidscouting.viewmodel.CameraSettingsViewModelFactory;
import com.databits.androidscouting.viewmodel.AppRepositoriesViewModel;
import com.databits.androidscouting.viewmodel.ProvisionViewModel;
import com.databits.androidscouting.viewmodel.ProvisionViewModelFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.preference.PowerPreference;
import com.travijuu.numberpicker.library.NumberPicker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED;

public class Scanner extends Fragment implements SheetsUpdateTask.UiCallback {

    // Scanner boundary contract:
    // - Own lifecycle/view wiring and delegate feature behavior to collaborators.
    // - Avoid direct data-source access patterns beyond dedicated stores + use-case orchestration.
    // - Keep QR payload decisions, upload side effects, and camera sub-behaviors in dedicated classes.

    protected BarcodeScanner qrScanner;
    private FragmentScannerBinding binding;
    private ProvisionViewModel viewModel;
    private CameraSettingsViewModel cameraSettingsViewModel;
    private ScannerCameraController scannerCameraController;
    private ScannerCameraUiController scannerCameraUiController;
    private ScannerUiFeedbackController scannerUiFeedbackController;
    private ScannerTeamScheduleController scannerTeamScheduleController;
    private ScannerUploadCoordinator scannerUploadCoordinator;
    private UploadAuditLogger uploadAuditLogger;
    private ScannerPayloadCoordinator payloadCoordinator;
    private FindMatchedTeamSlotUseCase findMatchedTeamSlotUseCase;
    private ProcessScanPayloadUseCase processScanPayloadUseCase;
    private ApplyRoleProvisionUseCase applyRoleProvisionUseCase;
    private ImportMatchDataChunkUseCase importMatchDataChunkUseCase;
    private QueueScanDataUseCase queueScanDataUseCase;
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    private ProvisionSettingsStore provisionStore;
    private CameraSettingsStore cameraSettingsStore;
    private ScheduleStore scheduleStore;
    private SyncStore syncStore;

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

    // Cached team data to avoid database access in analyzer callback
    private final String[] cachedTeamNumbers = new String[7]; // Index 0 unused, 1-6 for team positions

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppRepositories appRepositories = new ViewModelProvider(
            requireActivity(),
            new AppRepositoriesViewModel.Factory(requireContext())
        ).get(AppRepositoriesViewModel.class).getRepositories();
        provisionStore = appRepositories.provisionSettingsStore;
        cameraSettingsStore = appRepositories.cameraSettingsStore;
        scheduleStore = appRepositories.scheduleStore;
        syncStore = appRepositories.syncStore;
        ScannerDependencies deps = ScannerDependencies.create(requireContext());
        scannerCameraController = deps.scannerCameraController;
        scannerUiFeedbackController = deps.scannerUiFeedbackController;
        scannerTeamScheduleController = deps.scannerTeamScheduleController;
        scannerUploadCoordinator = deps.scannerUploadCoordinator;
        uploadAuditLogger = deps.uploadAuditLogger;
        findMatchedTeamSlotUseCase = deps.findMatchedTeamSlotUseCase;
        processScanPayloadUseCase = deps.processScanPayloadUseCase;
        applyRoleProvisionUseCase = deps.applyRoleProvisionUseCase;
        importMatchDataChunkUseCase = deps.importMatchDataChunkUseCase;
        queueScanDataUseCase = deps.queueScanDataUseCase;

        ProvisionViewModelFactory factory = new ProvisionViewModelFactory(appRepositories.provisionSettingsStore);
        viewModel = new ViewModelProvider(requireActivity(), factory).get(ProvisionViewModel.class);
        CameraSettingsViewModelFactory cameraFactory = new CameraSettingsViewModelFactory(appRepositories.cameraSettingsStore);
        cameraSettingsViewModel = new ViewModelProvider(requireActivity(), cameraFactory).get(CameraSettingsViewModel.class);
        payloadCoordinator = deps.createPayloadCoordinator(new ScannerPayloadCoordinator.Actions() {
            @Override
            public void onScouterList(List<String> scouters) {
                viewModel.updateScouterList(scouters);
            }

            @Override
            public void onGoogleConfig(ScanPayload.GoogleConfig config) {
                viewModel.updateWorkbookId(config.getWorkbookId());
                viewModel.updateCrowdRange(config.getCrowdRange());
                viewModel.updatePitRange(config.getPitRange());
                viewModel.updateSpecialtyRange(config.getSpecialtyRange());
            }

            @Override
            public void onMatchDataChunk(ScanPayload.MatchDataChunk payload) {
                processMatchDataChunk(payload);
            }

            @Override
            public void onRoleProvision(ScanPayload.RoleProvision payload) {
                processRoleProvision(payload);
            }

            @Override
            public void onTeamData(String rawCsv) {
                processTeamData(rawCsv);
            }
        });
        googleAuthLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                String accountName = result.getData().getStringExtra(GoogleAuthActivity.EXTRA_ACCOUNT_NAME);
                viewModel.updateGoogleAccountName(accountName);
                // Retry the upload after getting the account
                callSheets();
            } else {
                Toast.makeText(getContext(), "Google Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });

        authorizationLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // Retry the upload after authorization
                callSheets();
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
                        scannerCameraUiController.applyCameraSettings();
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

        matchInfo = new MatchInfo(provisionStore);
        teamInfo = new TeamInfo(requireContext(), provisionStore, scheduleStore);
        scoutUtils = new ScoutUtils(requireContext(), provisionStore, scheduleStore);
        sheetsUpdateTask = new SheetsUpdateTask(requireContext(), this, provisionStore, syncStore);
        scannerCameraUiController = new ScannerCameraUiController(
            this,
            binding,
            cameraSettingsViewModel,
            cameraSettingsStore,
            scannerUiFeedbackController
        );

        match = matchInfo.getMatch();

        String role = viewModel.getDeviceRoleSync();

        NavController controller = NavHostFragment.findNavController(Scanner.this);

        binding.buttonBack.setOnClickListener(view1 -> controller
            .navigateUp());

        binding.buttonUpload.setOnClickListener(view1 -> {
            scannerUploadCoordinator.callSheets(this, provisionStore, googleAuthLauncher, sheetsUpdateTask);
        });

        binding.buttonGroupUploadMode.setOnPositionChangedListener(position -> {
            switch (position) {
                case 0:
                    viewModel.updateUploadMode(UploadMode.CROWD.getRawValue());
                    break;
                case 1:
                    viewModel.updateUploadMode(UploadMode.SPECIALTY.getRawValue());
                    break;
                case 2:
                    viewModel.updateUploadMode(UploadMode.PIT.getRawValue());
                    break;

            }
        });

        binding.testButton.setOnClickListener(view1 ->
            PowerPreference.showDebugScreen(true));

        NumberPicker matchCounter = binding.uiInsideNumberPicker;

        matchCounter = matchInfo.configurePicker(matchCounter);

        scannerTeamScheduleController.ensureTeamsLoaded(
            this,
            backgroundExecutor,
            scheduleStore,
            teamInfo,
            () -> setupTeamDisplay(match)
        );

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

        preview = binding.previewView;
        refreshUI();
        openCamera();

        // Setup camera controls and auto-settings
        scannerCameraUiController.setupControls();
        scannerCameraUiController.setupAutoSettings();
    }

    protected void openCamera() {
        qrScanner = scannerCameraController.createQrScanner();
        camController = scannerCameraController.bindCamera(this, preview, getQrCodeAnalyzer(preview));
        scannerCameraUiController.attachCameraController(camController, preview);

        // Apply camera settings from preferences
        scannerCameraUiController.applyCameraSettings();

        // Update resolution display once preview is ready
        preview.post(() -> {
            if (cameraSettingsStore.isCameraShowResolutionEnabled()) {
                scannerUiFeedbackController.updateResolutionDisplay(this, cameraSettingsStore, binding, preview);
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
                scannerUiFeedbackController.onFrame(this, cameraSettingsStore, binding);

                List<Barcode> qrResList = result.getValue(qrScanner);
                if (qrResList == null || qrResList.isEmpty() || qrResList.get(0) == null) {
                    preview.getOverlay().clear();
                    //if there is no result
                    return;
                }

                Barcode qr = qrResList.get(0);

                // Apply ML Kit enhancements - size and position filtering
                if (!scannerCameraController.checkBarcodeSize(qr, cameraSettingsStore)) {
                    preview.getOverlay().clear();
                    return; // Too small, ignore
                }
                if (!scannerCameraController.checkBarcodePosition(qr, preview, cameraSettingsStore)) {
                    preview.getOverlay().clear();
                    return; // Not centered, ignore
                }

                // Check if scanning is paused
                if (scannerCameraUiController.isScanningPaused()) {
                    preview.getOverlay().clear();
                    return; // Scanning paused
                }

                String bar_string = qr.getRawValue();
                preview.getOverlay().clear();

                QrCodeViewModel qrCodeViewModel = new QrCodeViewModel(qr);
                QrCodeDrawable qrCodeDrawable = new QrCodeDrawable(qrCodeViewModel);

                preview.getOverlay().add(qrCodeDrawable);

                assert bar_string != null;

                scannerUiFeedbackController.onSuccessfulScan(this, cameraSettingsStore, binding);

                ScanPayload payload = processScanPayloadUseCase.invoke(bar_string);
                payloadCoordinator.route(payload, bar_string);
            }
        );
    }

    private void processMatchDataChunk(ScanPayload.MatchDataChunk payload) {
        ImportResult result = importMatchDataChunkUseCase.execute(payload);
        if (result instanceof ImportResult.Duplicate) {
            android.util.Log.d("Scanner", "Duplicate chunk detected: " + payload.getChunkIndex());
            return;
        }

        if (result instanceof ImportResult.Imported) {
            setupTeamDisplay(match);
        }
    }

    private void processTeamData(String barString) {
        android.util.Log.d("Scanner", "Checking QR: " + barString);
        android.util.Log.d("Scanner", "Cached teams: " + java.util.Arrays.toString(cachedTeamNumbers));

        Integer matchedSlot = findMatchedTeamSlotUseCase.execute(barString, getScoutingTeamSlots());
        if (matchedSlot != null) {
            android.util.Log.d("Scanner", "Matched team slot " + matchedSlot + ": " + cachedTeamNumbers[matchedSlot]);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                ScannerTeamUiHelper.highlightMatchedSlot(requireView(), getResources(), matchedSlot);
            });
        } else {
            android.util.Log.d("Scanner", "No team match found, saving data anyway");
        }

        saveData(barString);
    }

    private List<String> getScoutingTeamSlots() {
        return scannerTeamScheduleController.getScoutingTeamSlots(cachedTeamNumbers);
    }

    private void saveData(String bar_string) {

        String uploadMode = provisionStore.getUploadMode();
        UploadMode typedMode = UploadMode.fromRaw(uploadMode);

        // Make upload.csv for debugging
        uploadAuditLogger.append(bar_string);

        // Queue new upload entries using domain use case
        final UploadMode finalMode = typedMode;
        backgroundExecutor.execute(() -> queueScanDataUseCase.queueIfNew(bar_string, finalMode));
    }

    private void setupTeamDisplay(int match) {
        scannerTeamScheduleController.setupTeamDisplay(
            this,
            backgroundExecutor,
            teamInfo,
            match,
            cachedTeamNumbers
        );
    }

    private void processRoleProvision(ScanPayload.RoleProvision payload) {
        applyRoleProvisionUseCase.invoke(payload);
    }

    public static void restartApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    protected void callSheets() {
        scannerUploadCoordinator.callSheets(this, provisionStore, googleAuthLauncher, sheetsUpdateTask);
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
        scannerUploadCoordinator.onAuthorizationRequired(authorizationLauncher, e);
    }

    @Override
    public void onUploadSuccess(String updatedRange) {
        scannerUploadCoordinator.showUploadSuccess(this, updatedRange);
    }

    @Override
    public void onUploadFailed() {
        scannerUploadCoordinator.showUploadFailed(this);
    }

    @Override
    public void onNoDataToUpload() {
        scannerUploadCoordinator.showNoDataToUpload(this);
    }

    @Override
    public void onDuplicateData() {
        scannerUploadCoordinator.showDuplicateData(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        backgroundExecutor.shutdown();
        if (sheetsUpdateTask != null) {
            sheetsUpdateTask.shutdown();
        }
        scannerUiFeedbackController.release();
        scannerCameraUiController.restoreWindowSettings();
    }
}
