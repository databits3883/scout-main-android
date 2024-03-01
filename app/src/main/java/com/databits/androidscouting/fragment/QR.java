package com.databits.androidscouting.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentQRBinding;
import com.databits.androidscouting.util.FileUtils;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.TeamInfo;
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer;
import com.github.sumimakito.awesomeqr.RenderResult;
import com.github.sumimakito.awesomeqr.option.RenderOption;
import com.github.sumimakito.awesomeqr.option.color.ColorQR;
import com.github.sumimakito.awesomeqr.option.logo.Logo;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.travijuu.numberpicker.library.NumberPicker;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class QR extends Fragment {

    private FragmentQRBinding binding;

    MatchInfo matchInfo;
    TeamInfo teamInfo;
    FileUtils fileUtils;

    boolean mode;
    int team;

    Preference debugPreference = PowerPreference.getFileByName("Debug");
    Preference matchPreference = PowerPreference.getFileByName("Match");
    Preference listPreference = PowerPreference.getFileByName("List");
    Preference pitDataPreference = PowerPreference.getFileByName("PitData");

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        binding = FragmentQRBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchInfo = new MatchInfo();
        teamInfo = new TeamInfo(requireContext());
        fileUtils = new FileUtils(requireContext());

        // Go Full screen
        View decorView = requireActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        NavController controller = NavHostFragment.findNavController(QR.this);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String data = bundle.getString("qrData");
            mode = bundle.getBoolean("mode");
            team = 0;
            if (mode) {
               team = Integer.parseInt(data.split(",")[0]);
            }
            matchInfo.setTempMatch(matchInfo.getMatch());
            setTeamText(mode,team);
            newQrCode(data);
            saveData(data, mode);
            checkMode(mode);
            ArrayList<String> specialData = new ArrayList<>();
            specialData.add(data);
            listPreference.setObject("special_scout",specialData);
        } else {
            setTeamText(mode,0);
        }

        binding.buttonBack.setOnClickListener(view1 -> {
                if (mode) {
                    controller.navigate(R.id.action_QRFragment_to_pitScoutFragment);
                } else {
                    controller.navigate(R.id.action_QRFragment_to_crowdScoutFragment);
                }
        });

        binding.buttonNext.setOnClickListener(view1 -> {
            // Turn off team/match override after each match
            debugPreference.setBoolean("manual_team_override_toggle", false);
            debugPreference.setBoolean("manual_match_override_toggle", false);
            debugPreference.remove("manual_team_override_value");
            matchInfo.incrementMatch();
            matchInfo.setTempMatch(matchInfo.getMatch());
            controller.navigateUp();
        });

        // Updates the match, team, selector when the page loads
        matchSelector();
        setMatchText();
        //setTeamText(mode,0);
        checkBrightBar();

        if (mode) {
            binding.buttonNext.setText(R.string.next_team);
        } else {
            binding.buttonNext.setText(R.string.next_match);
        }

        binding.cycleButton.setOnClickListener(view1 -> {
            NumberPicker match = requireView().findViewById(R.id.number_counter_inside);
            debugPreference.setInt("match_backup", matchInfo.getMatch());
            match.setValue(1);
            matchInfo.setMatch(1);
            matchInfo.setTempMatch(1);
            setTeamText(mode, team);
            //ImageView qr_img = requireView().findViewById(R.id.qr_img);
            //qr_img.setImageBitmap(null);

            // Count the entries in pit and match data and cycle through that number based on mode
            final Handler handler = new Handler();
            Map<String, ?> pitData = pitDataPreference.getData();
            Map<String, ?> matchData = matchPreference.getData();
            handler.postDelayed(new Runnable() {
                int i = 0;
                public void run() {
                    handler.postDelayed(this, 250);
                    match.increment();
                    if (mode) {
                        if (i == pitData.size()) {
                            //matchInfo.setTempMatch(matchInfo.getTempMatch()+1);
                            handler.removeCallbacks(this);
                        }
                    } else {
                        //match.increment();
                        if (i == matchData.size()) {
                            handler.removeCallbacks(this);
                            matchInfo.setMatch(debugPreference.getInt("match_backup"));
                        }
                    }
                    i++;
                }
            }, 250);
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void checkMode(boolean mode) {
        // Hide/Show buttons and text based on Pit or Crowd mode
        if (mode) {
            // Pit Mode
            //binding.matchCounter.numberCounterInside.setVisibility(View.GONE);
            //binding.qrMatchCounterText.setText(R.string.cycle_qr_title);
            //binding.qrMatchText.setVisibility(View.GONE);
            //binding.cycleButton.setVisibility(View.VISIBLE);
            binding.matchCounter.numberCounterInside.setVisibility(View.VISIBLE);
            binding.qrMatchCounterText.setText(R.string.match_selector);
            binding.qrMatchText.setVisibility(View.VISIBLE);
            binding.cycleButton.setVisibility(View.GONE);
        } else {
            // Crowd Mode
            binding.matchCounter.numberCounterInside.setVisibility(View.VISIBLE);
            binding.qrMatchCounterText.setText(R.string.match_selector);
            binding.qrMatchText.setVisibility(View.VISIBLE);
            binding.cycleButton.setVisibility(View.GONE);
        }
    }

    private void checkBrightBar() {
        if (Settings.System.canWrite(requireActivity())) {
            binding.qrBrightBarText.setText(R.string.brightbar_activated_title);
            binding.brightBar.setEnabled(true);
            brightBar();
        } else {
            binding.qrBrightBarText.setText(R.string.brightbar_deactivated_title);
            binding.qrBrightBarText.setOnClickListener(view1 -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            binding.brightBar.setEnabled(false);
        }
    }

    private void brightBar() {
        final ContentResolver[] conResolver = new ContentResolver[1];
        Window window = requireActivity().getWindow();
        conResolver[0] = requireActivity().getContentResolver();

        final int[] brightness = { 0 };
        SeekBar bright_bar = requireView().findViewById(R.id.bright_bar);

        try {
            brightness[0] = Settings.System.getInt(conResolver[0],
                Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }

        bright_bar.setProgress(brightness[0]);

        bright_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                Settings.System.putInt(conResolver[0], Settings.System.SCREEN_BRIGHTNESS,
                    brightness[0]);

                WindowManager.LayoutParams layoutParams = window.getAttributes();

                layoutParams.screenBrightness = brightness[0] / (float) 255;

                window.setAttributes(layoutParams);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                brightness[0] = Math.max(progress, 20);
            }
        });
    }

    private void setTeamText(boolean mode, int team) {
            int tempMatch = matchInfo.getTempMatch();
            int teamCount = teamInfo.getPitTeamCount();

            binding.qrTeamText.setText(String.format(Locale.US,"Team: %d",
                teamInfo.getTeam(tempMatch)));
            if (mode && tempMatch <= teamCount) {
                // Pit Mode
                if (team != 0) {
                    binding.qrTeamText.setText(String.format(Locale.US, "Team: %d",
                        team));
                }
            }
    }

    private void setMatchText() {
        binding.qrMatchText.setText(String.format(Locale.US,"Match: %d",
            matchInfo.getTempMatch()));
    }

    private void matchSelector() {
        NumberPicker match = requireView().findViewById(R.id.number_counter_inside);
        match = matchInfo.configurePicker(match);
        //matchInfo.setTempMatch(matchInfo.getMatch());

        //Override the default listener to configure the ui and generate the qr code
        match.setValueChangedListener((value, action) -> {
            matchInfo.setTempMatch(value);

            binding.qrMatchText.setText(String.format(Locale.US,"Match: %d",
                value));
            String matchData;

            setTeamText(false, 0);

            // get the match scouting data from the shared preferences

            if (listPreference.getBoolean("pit_remove_enabled")) {
                matchData = pitDataPreference.getString(String.format(Locale.US, "Match%d",
                    value), "No Data");
            } else {
                matchData = matchPreference.getString(String.format(Locale.US, "Match%d",
                    value), "No Data");
            }
            // Only generate a qr code if there is data
            if (matchData.equals("No Data")) {
                binding.qrImg.setImageBitmap(textAsBitmap("No Data", 100,
                    R.color.green_900));
            } else {
                newQrCode(matchData);
            }
        });
    }

    public Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    private void saveData(String data, boolean mode) {
        if (mode) {
            // Pit Mode
            pitDataPreference.setString(String.format(Locale.US,"Match%d",
                matchInfo.getTempMatch()), data);
            teamInfo.setTeam(Integer.parseInt(data.split(",")[0]));
        } else {
            // Match Mode
            matchPreference.setString(String.format(Locale.US,"Match%d",
                matchInfo.getTempMatch()), data);
        }
    }

    private void newQrCode(String data) {
        Bitmap logoBitmap = BitmapFactory
            .decodeResource(requireContext().getResources(), R.drawable.logo);

        Logo logo = new Logo();

        logo.setBitmap(logoBitmap);
        // scale for the logo in the QR code
        logo.setScale(0.25f);
        // crop the logo image before applying it to the QR code
        logo.setClippingRect(new RectF(0, 0, 200, 200));

        ColorQR color = new ColorQR();
        // for blank spaces
        color.setLight(getResources().getColor(R.color.white, null));
        // for non-blank spaces
        color.setDark(getResources().getColor(R.color.black, null));
        // for the background (will be overridden by background images, if set)
        color.setBackground(getResources().getColor(R.color.white, null));

        RenderOption renderOption = new RenderOption();
        // content to encode
        renderOption.setContent(data);
        // size of the final QR code image
        renderOption.setSize(1000);
        // if set to true, the patterns will be rounded
        renderOption.setRoundedPatterns(false);
        // width of the empty space around the QR code
        renderOption.setBorderWidth(35);
        // (optional) specify QR code version
        renderOption.setQrCodeVersion(12);
        // (optional) specify a scale for patterns
        renderOption.setPatternScale(1f);
        // if set to true, the background will NOT be drawn on the border area
        renderOption.setClearBorder(false);
        // set a colorQR palette for the QR code
        renderOption.setColorQR(color);
        // set a logo for the QR code
        renderOption.setLogo(logo);
        try {
            RenderResult render = AwesomeQrRenderer.render(renderOption);
            ImageView qr_img = requireView().findViewById(R.id.qr_img);
            qr_img.setImageBitmap(render.getBitmap());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void onResume() {
        super.onResume();
        checkBrightBar();
    }
}