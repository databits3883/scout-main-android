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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        PowerPreference.init(this);

        // Go Full screen
        View decorView = this.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        //// Set Night Mode based on installed Android version
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        //    UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        //    uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
        //} else {
        //    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        //}

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