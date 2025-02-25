package com.databits.androidscouting.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;
import com.anggrayudi.storage.file.DocumentFileCompat;
import com.anggrayudi.storage.file.DocumentFileType;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class FileUtils {

  Context context;

  public FileUtils(Context context) {
    this.context = context;
  }

  private static final String TAG = "FileUtils";

  public String readFile(File file) {
    StringBuilder text = new StringBuilder();

    try {
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line;

      while ((line = br.readLine()) != null) {
        text.append(line);
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return text.toString();
  }

  private boolean unpackZip(String path, String zipname, String targetPath)
  {
    InputStream is;
    ZipInputStream zis;
    try
    {
      is = Files.newInputStream(Paths.get(path + zipname));
      zis = new ZipInputStream(new BufferedInputStream(is));
      ZipEntry ze;

      while((ze = zis.getNextEntry()) != null)
      {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;

        String filename = ze.getName();
        FileOutputStream fout = new FileOutputStream(targetPath + filename);

        // reading and writing
        while((count = zis.read(buffer)) != -1)
        {
          baos.write(buffer, 0, count);
          byte[] bytes = baos.toByteArray();
          fout.write(bytes);
          baos.reset();
        }

        fout.close();
        zis.closeEntry();
      }

      zis.close();
    }
    catch(IOException e)
    {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  public ArrayList<String> readList(File file) {
    ArrayList<String> listEntries = null;
    try {
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line;
      listEntries = new ArrayList<>();

      while ((line = br.readLine()) != null) {
        listEntries.add(line);
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return listEntries;
  }

  public String readTextFile(InputStream inputStream) {
    if (inputStream == null) {
      return null; // Handle null input stream
    }
    StringBuilder stringBuilder = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
        StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line).append('\n');
      }
    } catch (IOException e) {
      e.printStackTrace();
      return null; // Return null on error
    }
    return stringBuilder.toString();
  }

  public String readInternalFile(String fileName) {
    try (FileInputStream fis = context.openFileInput(fileName)) {
      return readTextFile(fis);
    } catch (FileNotFoundException e) {
      System.err.println("File not found: " + fileName);
      return null; // Return null if file not found
    } catch (IOException e) {
      e.printStackTrace();
      return null; // Return null on error
    }
  }

  public Intent intentFileDialog() {
    Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    data.setType("*/*");
    //String[] mimeTypes = {"text/plain", "application/json", "text/csv"};
    //data.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
    data.addCategory(Intent.CATEGORY_OPENABLE);
    return data;
  }

  public DocumentFile storageFolder() {
    return DocumentFileCompat.fromFullPath(context, Objects.requireNonNull(DocumentFileCompat.
            getAccessibleAbsolutePaths(context).get("primary")).iterator().next(),
        DocumentFileType.FOLDER,true, false);
  }

  public boolean fileExists(String filePath) {
    File file = new File(filePath);
    return file.exists();
  }

  public void testStorage() {
    DocumentFile storage = storageFolder();

    if (storage != null) {
      storage.createFile("text/csv", "pit");
    }
  }

  public void handleZip(Uri uri) {
    if ((uri != null) && !uri.toString().contains("ERROR")) {
      try {
        DocumentFile sentFile = DocumentFileCompat.fromUri(context, uri);
        File destinationDir = context.getFilesDir();

        // Copy the ZIP file to internal app storage
        copyFileToInternal(context,
            Objects.requireNonNull(sentFile).getUri(), sentFile.getName());

        // Extract the ZIP file
        ZipFile zipFile = new ZipFile(destinationDir + "/" + sentFile.getName());
        zipFile.extractAll(destinationDir.getPath());

        // Delete the ZIP file
        if (zipFile.getFile().exists()) {
          if (!zipFile.getFile().delete()) {
            Log.d(TAG, "Error: File Not Deleted");
          }
        } else {
          Log.d(TAG, "Error: File Does Not Exist");
        }

        Log.d(TAG, "File Imported and Extracted");
      } catch (ZipException e) {
        e.printStackTrace();
      }
    }
  }

  public static String copyFileToInternal(Context context, Uri fileUri, String fileName) {
    if (context == null || fileUri == null || fileName == null || fileName.trim().isEmpty()) {
      Log.e(TAG, "Invalid input parameters: context, fileUri, or fileName is null or empty.");
      return null;
    }

    File destinationFile = new File(context.getFilesDir(), fileName);
    InputStream inputStream = null;
    OutputStream outputStream = null;

    try {
      inputStream = context.getContentResolver().openInputStream(fileUri);
      if (inputStream == null) {
        Log.e(TAG, "Failed to open input stream for URI: " + fileUri);
        return null;
      }

      outputStream = Files.newOutputStream(destinationFile.toPath());
      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      Log.d(TAG, "File copied successfully to: " + destinationFile.getAbsolutePath());
      return destinationFile.getAbsolutePath();
    } catch (IOException e) {
      Log.e(TAG, "Error copying file to internal storage", e);
      return null;
    } finally {
      // Ensure input/output streams are closed in the finally block
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          Log.e(TAG, "Error closing input stream", e);
        }
      }
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          Log.e(TAG, "Error closing output stream", e);
        }
      }
    }
  }
}