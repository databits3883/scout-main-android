package com.databits.androidscouting.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.databits.androidscouting.util.GoogleAuthActivity.REQUEST_ACCOUNT_PICKER;

public class SheetsUpdateTask extends AsyncTask<Void, Void, AppendValuesResponse> {

  public static UserRecoverableAuthIOException mLastError;

  @SuppressLint("StaticFieldLeak")
  private final Context context;

  private final Sheets sheetsService;

  private static final String[] ACCOUNT_SCOPES = {SheetsScopes.SPREADSHEETS};

  private String spreadsheetId;

  @SuppressLint("StaticFieldLeak")
  private final Activity activity;

  Preference configPreference = PowerPreference.getFileByName("Config");
  Preference debugPreference = PowerPreference.getFileByName("Debug");
  Preference matchPreference = PowerPreference.getFileByName("Match");

  MatchInfo matchInfo;

  public SheetsUpdateTask(Activity activity) {
    this.context = activity.getBaseContext();
    this.activity = activity;

    // Initialize the match info object
    matchInfo = new MatchInfo();

    // Setup the Google Account Credential object
    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
            context, Arrays.asList(ACCOUNT_SCOPES))
        .setBackOff(new ExponentialBackOff());

    // Check for a saved google account name to speed up the auth process
    credential.setSelectedAccountName(configPreference.getString("google_account_name",
        null));

    // Build the sheets service to be used in the background
    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    sheetsService = new Sheets.Builder(
        transport, jsonFactory, credential)
        .setApplicationName("Android Scouter")
        .build();
  }

  @Override protected AppendValuesResponse doInBackground(Void... voids) {
    List<List<String>> columnData = null;
    switch (configPreference.getString("uploadMode")) {
      case "Crowd":
        columnData = new ArrayList<>(matchPreference.getObject("upload_data",
            ArrayList.class, new ArrayList<>()));
        break;
      case "Pit":
        columnData = new ArrayList<>(matchPreference.getObject("pit_upload_data",
            ArrayList.class, new ArrayList<>()));
        break;
      case "Speciality":
        columnData = new ArrayList<>(matchPreference.getObject("special_upload_data",
            ArrayList.class, new ArrayList<>()));
        break;
    }


    try {
      // Sheet name and range to upload data to
      String range = null;

      switch (configPreference.getString("uploadMode")) {
        case "Crowd":
          range = configPreference.getString("Crowd_range","");
          break;
        case "Pit":
          range = configPreference.getString("Pit_range","");
          break;
        case "Speciality":
          range = configPreference.getString("Speciality_range","");
          break;
      }

      // Configure a new value range to store the data
      ValueRange content = new ValueRange();
      content.setMajorDimension("ROWS");
      content.setRange(range);

      // Make sure the list has values
      assert columnData != null;
      if (!columnData.isEmpty()) {

        // Create a new list to store the sheet values
        List<List<Object>> upload = new ArrayList<>();

        for (int i = 0; i < columnData.size(); i++) {
          List<String> rowData = columnData.get(i);
          List<Object> row = new ArrayList<>(rowData);
          upload.add(row);
        }

        // Set the value range to our data
        content.setValues(upload);

        // Command to upload the data to google sheets
        return sheetsService.spreadsheets().values().append(spreadsheetId, range, content)
            .setValueInputOption("USER_ENTERED")
            .setInsertDataOption("OVERWRITE")
            .execute();
      }
      // Google Auth Flow handling (Makes a popup asking for permission to access google account)
    } catch (UserRecoverableAuthIOException g) {
      mLastError = g;
      cancel(true);
      Log.d("SheetsUpdateTask", g.getMessage());
      debugPreference.putBoolean("upload_error", true);
      debugPreference.setString("upload_error_message", g.getMessage());
      g.printStackTrace();
      return null;
      // General Error reporting
    } catch (IOException e) {
      Log.d("SheetsUpdateTask", e.getMessage());
      e.printStackTrace();
      debugPreference.putBoolean("upload_error", true);
      debugPreference.setString("upload_error_message", e.getMessage());
      return null;
    }
    return null;
  }

  @Override
  protected void onPreExecute() {
    spreadsheetId = configPreference.getString("workbook_id","");
  }

  @Override
  protected void onPostExecute(AppendValuesResponse response) {
    // Anything that is not null is a successful upload
    if (response == null) {
      if (debugPreference.getBoolean("upload_error", false)) {
        debugPreference.putBoolean("upload_error", false);
        Toast.makeText(context, debugPreference.getString("upload_error_message"),
            Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(context, "No data to upload", Toast.LENGTH_LONG).show();
      }
    } else {
      // If the response is not null and rows were updated, send a message and clear the data cache
      String test = response.getUpdates().getUpdatedRange();
      Toast.makeText(context,"Updated Data range: " + test, Toast.LENGTH_LONG).show();
      Log.d("Sheets", "onPostExecute: " + response.getUpdates().toString());
      matchPreference.clear();
    }

  }

  @Override
  protected void onCancelled() {
    // Bring up the google auth app auth flow if play services cancels our first request
    activity.startActivityForResult ( mLastError.getIntent(),REQUEST_ACCOUNT_PICKER);
  }
}