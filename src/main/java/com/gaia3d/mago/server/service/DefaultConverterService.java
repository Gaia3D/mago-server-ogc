package com.gaia3d.mago.server.service;

import com.gaia3d.mago.server.domain.Pagination;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public interface DefaultConverterService {

    String LOG_FILE_NAME = "log.txt";

    List<?> getListWithPagination(Pagination request, String userId) throws IOException;

    List<?> getAllList(Pagination request, String userId);

    default String createFullPath(String outputPath, String userId, String fileName) {
        return outputPath + File.separator + userId + File.separator + fileName;
    }

    default File createFullPathFile(String outputPath, String userId, String fileName) {
        return new File(createFullPath(outputPath, userId, fileName));
    }

    default String createUserPath(String outputPath, String userId) {
        return outputPath + File.separator + userId;
    }

    default File createUserPathFile(String outputPath, String userId) {
        return new File(createUserPath(outputPath, userId));
    }

    default File getJavaPath(String javaPath) {
        File javaBin = new File(javaPath);
        if (javaPath.isEmpty() || !javaBin.exists()) {
            String javaHome = System.getProperty("java.home");
            javaBin = new File(javaHome, "bin");
        }

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return new File(javaBin, "java.exe");
        } else {
            return new File(javaBin, "java");
        }
    }

    default File unzipFile(File zipFile, File outputDir) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File entryFile = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!entryFile.exists() && !entryFile.mkdirs()) {
                        throw new RuntimeException("Can't create directory");
                    }
                } else {
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryFile))) {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = zipInputStream.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputDir;
    }
}
