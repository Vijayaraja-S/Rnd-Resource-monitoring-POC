package com.p3.export.iosource;

import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Objects.requireNonNull;

import com.p3.export.logutils.P3LoggerUtils;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class FileOutputResource implements OutputResource {

  private final Path outputFile;
  private final P3LoggerUtils logger;

  public FileOutputResource(final Path filePath) {
    outputFile = requireNonNull(filePath, "No file path provided").normalize().toAbsolutePath();
    this.logger = new P3LoggerUtils(FileOutputResource.class);
  }

  public Path getOutputFile() {
    return outputFile;
  }

  @Override
  public Writer openNewOutputWriter(final Charset charset, final boolean appendOutput)
      throws IOException {
    requireNonNull(charset, "No output charset provided");
    final OpenOption[] openOptions;
    if (appendOutput) {
      openOptions = new OpenOption[] {WRITE, CREATE, APPEND};
    } else {
      openOptions = new OpenOption[] {WRITE, CREATE, TRUNCATE_EXISTING};
    }
    final Writer writer = newBufferedWriter(outputFile, charset, openOptions);
    logger.info("Opened output writer to file ", outputFile);
    return new OutputWriter(getDescription(), writer, true);
  }

  private String getDescription() {
    return outputFile.toString();
  }

  @Override
  public String toString() {
    return getDescription();
  }
}
