package com.p3.export.iosource;

import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompressedFileOutputResource implements OutputResource {

  private final Path outputFile;
  private final String internalPath;

  public CompressedFileOutputResource(final Path filePath, final String internalPath)
      throws IOException {
    outputFile = requireNonNull(filePath, "No file path provided").normalize().toAbsolutePath();
    final Path parentPath = requireNonNull(filePath.getParent(), "Invalid output directory");
    if (!exists(parentPath) || !isWritable(parentPath) || !isDirectory(parentPath)) {
      throw new IOException("Cannot write file, " + filePath);
    }

    this.internalPath = requireNonNull(internalPath, "No internal file path provided");
  }

  public Path getOutputFile() {
    return outputFile;
  }

  @Override
  public Writer openNewOutputWriter(final Charset charset, final boolean appendOutput)
      throws IOException {
    if (appendOutput) {
      throw new IOException("Cannot append to compressed file");
    }
    final OpenOption[] openOptions = new OpenOption[] {WRITE, CREATE, TRUNCATE_EXISTING};
    final OutputStream fileStream = newOutputStream(outputFile, openOptions);

    try (final ZipOutputStream zipOutputStream = new ZipOutputStream(fileStream)) {
      zipOutputStream.putNextEntry(new ZipEntry(internalPath));

      final Writer writer = new OutputStreamWriter(zipOutputStream, charset);
      log.info("Opened output writer to compressed file, " + outputFile);
      return new OutputWriter(getDescription(), writer, true);
    }
  }

  private String getDescription() {
    return outputFile.toString();
  }

  @Override
  public String toString() {
    return getDescription();
  }
}
