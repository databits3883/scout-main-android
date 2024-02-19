package com.databits.androidscouting.util;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.util.Arrays;

public class GoogleAuthActivity extends AppCompatActivity {
  Preference configPreference = PowerPreference.getFileByName("Config");

  private GoogleAccountCredential credential;

  public static final int REQUEST_ACCOUNT_PICKER = 1000;
  public static final int REQUEST_AUTHORIZATION = 1001;
  public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
  private static final String[] ACCOUNT_SCOPES = {SheetsScopes.SPREADSHEETS};

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    credential = GoogleAccountCredential.usingOAuth2(
            this, Arrays.asList(ACCOUNT_SCOPES))
        .setBackOff(new ExponentialBackOff());

    selectAccount();
  }

  protected void selectAccount() {
    // Start a dialog from which the user can choose an account
    startActivityForResult(
        credential.newChooseAccountIntent(),
        REQUEST_ACCOUNT_PICKER);

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_ACCOUNT_PICKER:
        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
          String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
          // Save the account name for use to see if the account is already selected
          configPreference.setString("google_account_name", accountName);
          credential.setSelectedAccountName(accountName);
        }
        finish();
      case REQUEST_GOOGLE_PLAY_SERVICES:
      case REQUEST_AUTHORIZATION:
        if (resultCode == Activity.RESULT_OK) {
          Toast.makeText(this, "Auth complete for " +
              configPreference.getString("google_account_name"), Toast.LENGTH_LONG).show();
        } else {
          startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }
        break;
    }


  }
}