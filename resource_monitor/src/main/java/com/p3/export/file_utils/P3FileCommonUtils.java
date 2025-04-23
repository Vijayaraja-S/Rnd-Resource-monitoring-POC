package com.p3.export.file_utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class P3FileCommonUtils {

  public static Path getPath(String path) {
    return Paths.get(path);
  }

  public static File createFile(String path) {
    return new File(path);
  }

  public static FileOutputStream createFileOut(String path) throws FileNotFoundException {
    return new FileOutputStream(path);
  }
}
