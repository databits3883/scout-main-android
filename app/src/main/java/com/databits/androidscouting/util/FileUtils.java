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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    byte[] buf = new byte[1024];
    int len;
    try {
      while ((len = inputStream.read(buf)) != -1) {
        outputStream.write(buf, 0, len);
      }
      outputStream.close();
      inputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return outputStream.toString();
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

  public static String copyFileToInternal(Context context, final Uri fileUri, String fileName) {
    File file = new File(context.getFilesDir() + "/" + fileName);
      try {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
        int bufferSize = 1024;
        final byte[] buffers = new byte[bufferSize];
        int read;
        while ((read = inputStream.read(buffers)) != -1) {
          fileOutputStream.write(buffers, 0, read);
        }
        inputStream.close();
        fileOutputStream.close();
        return file.getPath();
      } catch (IOException e) {
        e.printStackTrace();
      }
    return null;
  }
}