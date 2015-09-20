package com.tdc.addon.swagger.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by pestano on 20/09/15.
 */
public class FileUtils {

    public static final Logger log = LoggerFactory.getLogger(FileUtils.class.getName());

    public static void unzip(File zipFile, String targetDir) throws IOException {
        log.info("Unzip {}.", zipFile.getAbsolutePath());
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
        try {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                File destPath = new File(targetDir, zipEntry.getName());
                log.info("Unpacking {}.", destPath.getAbsoluteFile());
                if (!zipEntry.isDirectory()) {
                    FileOutputStream fout = new FileOutputStream(destPath);
                    final byte[] buffer = new byte[8192];
                    int n = 0;
                    while (-1 != (n = zipInputStream.read(buffer))) {
                        fout.write(buffer, 0, n);
                    }
                    fout.close();
                } else {
                    destPath.mkdir();
                }
                zipEntry = zipInputStream.getNextEntry();
            }
        } finally {
            zipInputStream.close();
        }
    }
}
