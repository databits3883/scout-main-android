package com.databits.androidscouting.fragment;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class UploadAuditLoggerTest {

    @Test
    public void appendWritesEntryToUploadCsv() throws Exception {
        Path tempDir = Files.createTempDirectory("upload-audit-test");
        UploadAuditLogger logger = new UploadAuditLogger(tempDir.toFile());

        logger.append("1,254,data");

        File uploadFile = tempDir.resolve("upload.csv").toFile();
        Assert.assertTrue(uploadFile.exists());

        List<String> lines = Files.readAllLines(uploadFile.toPath(), StandardCharsets.UTF_8);
        Assert.assertFalse(lines.isEmpty());
        Assert.assertTrue(lines.get(0).contains("1,254,data"));
    }
}
