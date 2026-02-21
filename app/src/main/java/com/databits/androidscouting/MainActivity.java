package com.databits.androidscouting;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepositoryProvider;
import com.databits.androidscouting.databinding.ActivityMainBinding;
import com.databits.androidscouting.util.ConnectionReceiver;
import com.databits.androidscouting.util.FileUtils;
import com.google.android.material.snackbar.Snackbar;
import com.preference.PowerPreference;

public class MainActivity extends AppCompatActivity implements ConnectionReceiver.ReceiverListener {

    private ActivityMainBinding binding;

    FileUtils fileUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
            .findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();

        PowerPreference.init(this);

        PreferenceRepositoryProvider.init(this);

        // Go Full screen
        View decorView = this.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        // Apply saved theme preference
        PreferenceRepository repository = PreferenceRepositoryProvider.get(this);
        String themeMode = repository.getThemeMode();
        int nightMode;

        switch (themeMode) {
            case "light":
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case "dark":
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:  // "system"
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }

        AppCompatDelegate.setDefaultNightMode(nightMode);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        fileUtils = new FileUtils(this);


        // Handle single zip file being sent to the app via share sheet
        // TODO: Add support for multiple different files being sent to the app via share sheet
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("application/zip".equals(type)) {
                fileUtils.handleZip(intent.getParcelableExtra(Intent.EXTRA_STREAM));
                //Bundle bundle = navController.saveState();
                //Objects.requireNonNull(bundle).putParcelable("fileUri",
                //    intent.getParcelableExtra(Intent.EXTRA_STREAM));
                //navController.navigate(R.id.action_StartFragment_to_SettingsFileHandlerFragment,
                //    bundle);
            }
        }
    }

    public boolean checkConnection() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.new.conn.CONNECTIVITY_CHANGE");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(new ConnectionReceiver(), intentFilter, RECEIVER_EXPORTED);
        }else {
            registerReceiver(new ConnectionReceiver(), intentFilter);
        }

        ConnectionReceiver.Listener = this;

        ConnectivityManager manager = (ConnectivityManager) getApplicationContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void showSnackBar(boolean isConnected) {
        String message;
        int color;

        if (isConnected) {
            message = "Connected to Internet";
            color = Color.WHITE;
        } else {
            message = "Not Connected to Internet";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar.make(findViewById(R.id.nav_host_fragment_content_main),
            message, Snackbar.LENGTH_LONG);

        // Find the snackbar view
        TextView snackText = snackbar.getView().findViewById(
            com.google.android.material.R.id.snackbar_text);

        snackText.setTextColor(color);
        snackbar.show();
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        showSnackBar(isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSnackBar(checkConnection());
    }

    @Override
    protected void onPause() {
        super.onPause();
        showSnackBar(checkConnection());
    }
}
