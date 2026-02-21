package com.databits.androidscouting.fragment;

import android.util.Log;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class UploadAuditLogger {
    private static final String TAG = "UploadAuditLogger";
    private final File uploadFile;

    UploadAuditLogger(File filesDir) {
        this.uploadFile = new File(filesDir, "upload.csv");
    }

    void append(String rawCsv) {
        try {
            FileWriter writer = new FileWriter(uploadFile, true);
            CSVWriter csvWriter = new CSVWriter(
                writer,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END
            );

            String timeStamp = new SimpleDateFormat("MM-dd-yy hh:mmaaa", Locale.getDefault())
                .format(new Date());
            csvWriter.writeNext(new String[] {rawCsv + "," + timeStamp});
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed appending upload audit entry", e);
        }
    }
}
