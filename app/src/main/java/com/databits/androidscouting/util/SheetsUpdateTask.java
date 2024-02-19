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
import com.opencsv.CSVReader;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.databits.androidscouting.util.GoogleAuthActivity.REQUEST_ACCOUNT_PICKER;

public class SheetsUpdateTask extends AsyncTask<Void, Void, AppendValuesResponse> {

  public static UserRecoverableAuthIOException mLastError;

  @SuppressLint("StaticFieldLeak")
  private final Context context;

  private final com.google.api.services.sheets.v4.Sheets sheetsService;

  private static final String[] ACCOUNT_SCOPES = {SheetsScopes.SPREADSHEETS};

  private String spreadsheetId;

  @SuppressLint("StaticFieldLeak")
  private final Activity activity;

  Preference configPreference = PowerPreference.getFileByName("Config");
  Preference debugPreference = PowerPreference.getFileByName("Debug");
  Preference listPreference = PowerPreference.getFileByName("List");

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
    try {
      // Sheet name and range to upload data to
      String range = "StatsRaw!A1:BL700";

      // Configure a new value range to store the data
      ValueRange content = new ValueRange();
      content.setMajorDimension("ROWS");
      content.setRange(range);

      // Create a new CSV reader to read the data from the file
      // /data/data/com.databits.androidscouting/files
      CSVReader csvReader = new CSVReader(new FileReader(
          context.getFilesDir() + "/" + "upload.csv"));

      // Reading CSV into a list
      List<String[]> columnData = csvReader.readAll();

      // Create a new list to store the unique lines
      List<String[]> uniqueList = new ArrayList<>();

      for (String[] line : columnData) {
        String lineStringT = Arrays.toString(line);

        // Remove last comma and data after it
        String removeTimeStamp = lineStringT.substring(0, lineStringT.lastIndexOf(','));

        // Create a HashSet to keep track of the lines we've already seen
        Set<String> seenLines = listPreference.getObject("seen_lines", Set.class,
            new HashSet<>());

        // Check for duplicate and don't upload role qr data
        if (!seenLines.contains(removeTimeStamp) & !removeTimeStamp.contains("Role")) {
          // If we haven't seen this line before, add it to the unique list
          uniqueList.add(line);

          // Add the line minus the timestamp to the HashSet so we can check for duplicates later
          seenLines.add(removeTimeStamp);
          listPreference.setObject("seen_lines", seenLines);
        }
      }

      configPreference.setBoolean("upload_unique_toggle", true);
      // Replace the original list with the unique list if the preference is set (Debugging)
      if (configPreference.getBoolean("upload_unique_toggle")) {
        columnData = uniqueList;
      }

      // Get the number of lines to upload
      int uploadCount = columnData.size();

      // Add the number of lines to upload to preference
      debugPreference.setString("upload_line_count", String.valueOf(uploadCount));

      // Print the number of lines to upload
      Log.d("SheetsUpdateTask", "uploadCount" + " lines to upload");

      // Make sure the list has values
      if (uploadCount > 0) {

        // Create a new list to store the sheet values
        List upload = new ArrayList<String>();

        // Loop through the list and add each line to the upload list
        for (int i = 0; i < columnData.size(); i++) {
          upload.add(Arrays.asList(columnData.get(i)));
        }

        // Set the value range to our data
        content.setValues(upload);

        // Command to upload the data to google sheets
        return sheetsService.spreadsheets().values().append(spreadsheetId, range, content)
            .setValueInputOption("USER_ENTERED")
            .setInsertDataOption("INSERT_ROWS")
            .execute();
      }
      // Google Auth Flow handling (Makes a popup asking for permission to access google account)
    } catch (UserRecoverableAuthIOException g) {
      mLastError = g;
      cancel(true);
      g.printStackTrace();
      return null;
      // General Error reporting
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    return null;
  }

  @Override
  protected void onPreExecute() {
    String hardcode = "1ZKXLsKNM05-5BhIzbWjLOdvka-9NipSjVqFf_iG2eak";
    spreadsheetId = configPreference.getString("linked_spreadsheet_id", hardcode);
  }

  @Override
  protected void onPostExecute(AppendValuesResponse response) {
    // Print the number of rows to upload for debugging
    //Toast.makeText(context,"Number of rows to upload: " +
    //    debugPreference.getString("upload_count"), Toast.LENGTH_SHORT).show();

    // Reset the upload count
    debugPreference.setString("upload_count", "None");

    // Anything that is not null is a successful upload
    if (response == null) {
      Toast.makeText(context, "Null response while uploading data, or no data to upload",
          Toast.LENGTH_LONG).show();
    } else {
      // If the response is not null and rows were updated, send a message and delete the file
      Toast.makeText(context, "Data uploaded successfully!", Toast.LENGTH_SHORT).show();
      File file = new File(context.getFilesDir() + "/" + "upload.csv");

      // If delete() fails, make sure it's because the file didn't exist!
      if (!file.delete() && file.exists()) {
        try {
          throw new IOException("failed to delete " + file);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

  }

  @Override
  protected void onCancelled() {
    // Bring up the google auth app auth flow if play services cancels our first request
    activity.startActivityForResult ( mLastError.getIntent(),REQUEST_ACCOUNT_PICKER);
  }
}