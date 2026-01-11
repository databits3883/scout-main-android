package com.databits.androidscouting.util;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import java.util.Arrays;

/**
 * An activity for handling Google account selection and authorization.
 * This activity is designed to be started with `registerForActivityResult`.
 */
public class GoogleAuthActivity extends AppCompatActivity {

    public static final String EXTRA_ACCOUNT_NAME = "extra_account_name";
    private static final String[] ACCOUNT_SCOPES = {SheetsScopes.SPREADSHEETS};

    private GoogleAccountCredential credential;

    private final ActivityResultLauncher<Intent> accountPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handleAccountPickerResult(result.getData());
                } else {
                    // User cancelled, finish with a cancelled result
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            });

    /**
     * Creates a new Intent to start this activity.
     * @param context The context to create the intent from.
     * @return An Intent to start GoogleAuthActivity.
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, GoogleAuthActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(ACCOUNT_SCOPES))
                .setBackOff(new ExponentialBackOff());

        // Check for Google Play Services availability.
        int googlePlayServicesAvailability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (GoogleApiAvailability.getInstance().isUserResolvableError(googlePlayServicesAvailability)) {
            GoogleApiAvailability.getInstance().showErrorDialogFragment(this, googlePlayServicesAvailability, 0, dialog -> {
                setResult(Activity.RESULT_CANCELED);
                finish();
            });
        } else {
            launchAccountPicker();
        }
    }

    private void launchAccountPicker() {
        Intent intent = credential.newChooseAccountIntent();
        accountPickerLauncher.launch(intent);
    }

    private void handleAccountPickerResult(Intent data) {
        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        if (accountName != null) {
            // Return the selected account name to the calling activity.
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_ACCOUNT_NAME, accountName);
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            // If account name is somehow null, treat as cancellation.
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }
}
