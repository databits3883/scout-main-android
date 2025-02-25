package com.databits.androidscouting.fragment;

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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentScannerBinding;
import com.databits.androidscouting.model.QrCodeDrawable;
import com.databits.androidscouting.model.QrCodeViewModel;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.ScoutUtils;
import com.databits.androidscouting.util.SheetsUpdateTask;
import com.databits.androidscouting.util.TeamInfo;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.opencsv.CSVWriter;
import com.preference.PowerPreference;
import com.preference.Preference;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED;

public class Scanner extends Fragment {

    protected BarcodeScanner qrScanner;
    protected ExecutorService cameraExecutor;

    private FragmentScannerBinding binding;

    Preference configPreference = PowerPreference.getFileByName("Config");
    Preference debugPreference = PowerPreference.getFileByName("Debug");
    Preference listPreference = PowerPreference.getFileByName("List");
    Preference matchPreference = PowerPreference.getFileByName("Match");
    Preference defaultPreference = PowerPreference.getDefaultFile();

    MatchInfo matchInfo;
    TeamInfo teamInfo;
    ScoutUtils scoutUtils;

    int match;

    PreviewView preview;

    LifecycleCameraController camController;

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
                    debugPreference.setBoolean("isMaster", !debugPreference.getBoolean(
                        "isMaster", false));
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

        match = matchInfo.getMatch();

        String role = configPreference.getString("device_role");

        NavController controller = NavHostFragment.findNavController(Scanner.this);

        binding.buttonBack.setOnClickListener(view1 -> controller
            .navigateUp());

        binding.buttonUpload.setOnClickListener(view1 -> {
            //if (!new File(requireContext().getFilesDir() + "/" + "upload.csv").exists()) {
            //    Toast.makeText(getContext(), "No Team Data to upload!",
            //        Toast.LENGTH_SHORT).show();
            //} else {
            //call_sheets();
            call_sheets();
            //}
        });

        binding.buttonGroupUploadMode.setOnPositionChangedListener(position -> {
            switch (position) {
                case 0:
                    configPreference.setString("uploadMode", "Crowd");
                    break;
                case 1:
                    configPreference.setString("uploadMode","Speciality");
                    break;
                case 2:
                    configPreference.setString("uploadMode","Pit");
                    break;

            }
        });

        binding.testButton.setOnClickListener(view1 ->
            PowerPreference.showDebugScreen(true));

        NumberPicker matchCounter = binding.uiInsideNumberPicker;

        matchCounter = matchInfo.configurePicker(matchCounter);

        if (listPreference.getObject("team_match",String[][].class) == null) {
            teamInfo.read_teams();
        }

        if (teamInfo.teamsLoaded()) {
            setupTeamDisplay(match);
        }

        //Override the default listener to configure the ui
        matchCounter.setValueChangedListener((value, action) -> {
            matchInfo.setMatch(value);
            setupTeamDisplay(value);
        });

        if (configPreference.getBoolean("role_locked_toggle") && (!role.equals("master"))) {
            debugPreference.setBoolean("isMaster", false);
        } else if (role.equals("master")) {
            debugPreference.setBoolean("isMaster", true);
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
                    debugPreference.setObject("scouter_list", scouterList);
                }  else if (bar_string.startsWith("GoogleConfig")) {
                    String[] parts = bar_string.split(",");
                    // parts[0] is "GoogleConfig"
                    configPreference.setString("workbook_id", parts[1]);
                    configPreference.setString("crowd_range", parts[2]);
                    configPreference.setString("pit_range", parts[3]);
                    configPreference.setString("specialty_range", parts[4]);
                } else if (bar_string.startsWith("MatchData")) {
                    List<String[]> matchData = splitMatchData(bar_string);
                    String[][] originalMatchData = listPreference.getObject("team_match",
                        String[][].class, new String[0][0]);
                    // Combine matchData with originalMatchData and delete duplicates
                    String[][] combinedMatchData = new String[originalMatchData.length +
                        matchData.size()][];
                    System.arraycopy(originalMatchData, 0, combinedMatchData, 0,
                        originalMatchData.length);
                    System.arraycopy(matchData.toArray(new String[0][0]), 0, combinedMatchData,
                        originalMatchData.length, matchData.size());
                    listPreference.setObject("team_match", combinedMatchData);

                    // Sort/Organize combinedMatchData entries in the array by match number
                    // Handle the case where the Int is not a number
                    // Deduplicate identical lines
                    for (String[] entry : combinedMatchData) {
                        try {
                            entry[0] = String.valueOf(Integer.parseInt(entry[0]));
                        } catch (NumberFormatException e) {
                            entry[0] = "0";
                        }
                    }
                    Arrays.sort(combinedMatchData,
                        Comparator.comparingInt(a -> Integer.parseInt(a[0])));

                  int newLength = combinedMatchData.length;

                    for (int i = 1; i < newLength; i++) {
                        if (Arrays.equals(combinedMatchData[i], combinedMatchData[i - 1])) {
                            // Shift elements to the left, overwriting the duplicate
                            System.arraycopy(combinedMatchData, i + 1, combinedMatchData, i, newLength - i - 1);
                            // Reduce the effective size of the array
                            newLength--;
                            // Decrement i to recheck the current position, as the next element has been shifted
                            i--;
                        }
                    }

                    // Create a new array with the correct size
                    String[][] uniqueMatchData = new String[newLength][];
                    System.arraycopy(combinedMatchData, 0, uniqueMatchData, 0, newLength);

                    // Save size and match info for use elsewhere
                    debugPreference.setInt("team_match_list_size", newLength);
                    listPreference.setObject("team_match", uniqueMatchData);
                    setupTeamDisplay(match);
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
                        configPreference.setBoolean("force_upload_toggle", true);
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
        Set<Integer> processedChunks = listPreference.getObject("processedChunks", Set.class,
            new HashSet<>());
        // Check if this chunk has already been processed
        if (processedChunks.contains(chunkIndex)) {
            System.out.println("Duplicate chunk detected: " + chunkIndex + ". Skipping.");
            return result; // Return empty list to indicate no new data
        }

        // Add the chunk index to the set of processed chunks
        processedChunks.add(chunkIndex);
        listPreference.setObject("processedChunks", processedChunks);

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

        String uploadData = null;
        String uploadLines = null;

        switch (configPreference.getString("uploadMode", "Crowd")) {
            case "Crowd":
                uploadData = "upload_data";
                uploadLines = "seen_lines";
                break;
            case "Speciality":
                uploadData = "special_upload_data";
                uploadLines = "special_seen_lines";
                break;
            case "Pit":
                uploadData = "pit_upload_data";
                uploadLines = "pit_seen_lines";
                break;
        }

        // Check to see if data is already in the list, if not initialize the list
        List<String[]> raw_data = matchPreference.getObject(uploadData, ArrayList.class,
            new ArrayList<>());

        // Split the string into an array
        String[] split = bar_string.split(",");
        raw_data.add(split);

        // Make upload.csv for debugging
        makeUploadFile(bar_string);

        // Add non-split data to the list
        //raw_data.add(new String[] {bar_string});

        // Create a HashSet to keep track of the lines we've already seen
        Set<String> seenLines = listPreference.getObject(uploadLines, Set.class,
            new HashSet<>());

        // Check for duplicate and don't upload role qr data
        if (!seenLines.contains(bar_string) & !bar_string.contains("Role")) {

            // Add the line to the HashSet so we can check for duplicates in the future
            seenLines.add(bar_string);

            // Save the HashSet to the shared preferences
            listPreference.setObject(uploadLines, seenLines);

            // Save the data to the shared preferences
            matchPreference.setObject(uploadData, raw_data);
        }
    }

    private void set_team(int id) {
        TextView text = requireView().findViewById(id);
        text.setBackgroundTintList(getResources().getColorStateList(R.color.green_900,null));
    }

    private void setupTeamDisplay(int match) {
        int[] teamIds = new int[] {R.id.blue1, R.id.blue2, R.id.blue3, R.id.red1, R.id.red2,
            R.id.red3};
        for (int i = 0; i < teamIds.length; i++) {
            TextView team = requireView().findViewById(teamIds[i]);
            team.setText(teamInfo.getMasterTeam(match, i+1));
            if (i == 0 || i == 1 || i == 2) {
                team.setBackgroundTintList(getResources().getColorStateList(
                    android.R.color.holo_blue_light,null));
            } else {
                team.setBackgroundTintList(getResources().getColorStateList(
                    android.R.color.holo_red_light, null));
            }

        }
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

        configPreference.setString("device_role", role);
        configPreference.setInt("crowd_position", crowd_num);
        configPreference.setString("current_scouter", name);
        configPreference.setBoolean("role_locked_toggle", locked);
        configPreference.setBoolean("specialSwitch", special_selector);
    }

    public static void restartApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    // Starts the AsyncTask to push the data to the Sheets API.
    protected void call_sheets(){
        SheetsUpdateTask task = new SheetsUpdateTask(requireActivity());
        task.execute();
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
        if (!debugPreference.getBoolean("isMaster", false)) {
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
}