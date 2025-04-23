package com.p3.export.options;

import static com.p3.export.utility.CommonMessageConstants.ERROR_LOG_TEMPLATE;
import static java.util.Objects.requireNonNull;

import com.p3.export.file_utils.P3FileCommonUtils;
import com.p3.export.formatter.ExportFormat;
import com.p3.export.iosource.CompressedFileOutputResource;
import com.p3.export.iosource.ConsoleOutputResource;
import com.p3.export.iosource.FileOutputResource;
import com.p3.export.iosource.OutputResource;
import com.p3.export.logutils.P3LoggerUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.zip.ZipUtil;

@Slf4j
@Data
@Builder
@AllArgsConstructor
public class Options {
  public static final String RESOURCE_FILE_SEPARATOR = "/";
  private final P3LoggerUtils logger = new P3LoggerUtils(Options.class);
  public static final String SEARCH_RESULT_EXPORT_ZIP = "dataExport.zip";

  // supporting
  private static final SecureRandom random = new SecureRandom();
  String OutputFolderPathPrefix;
  // Charset
  @Builder.Default private Charset inputEncodingCharset = StandardCharsets.UTF_8;
  @Builder.Default private Charset outputEncodingCharset = StandardCharsets.UTF_8;
  // Others
  private OutputResource outputResource;
  private ExportFormat exportFormat;
  private String outputFolderPath;
  @Builder.Default private String outputFileTitle = "export";
  private String templatePath;
  @Builder.Default private long recordsProcessed = 0;
  @Builder.Default private long fileCount = 0;
  @Builder.Default private boolean isXmlCaseSensitive = false;
  @Builder.Default private String fileStartAppender = "";
  @Builder.Default private String fileEndAppender = "";
  @Builder.Default private Long sizePerFile = 50L;
  @Builder.Default private Long thresholdSize = 500L;
  @Builder.Default private Long recordPerFile = 10000L;

  /** Character encoding for input files, such as scripts and templates. */
  public Charset getInputCharset() {
    if (inputEncodingCharset == null) {
      return StandardCharsets.UTF_8;
    } else {
      return inputEncodingCharset;
    }
  }

  /**
   * Gets the output reader. If the output resource is null, first set it to console output.
   *
   * @throws IOException
   */
  public Writer openNewOutputWriter(final boolean appendOutput) throws IOException {
    return obtainOutputResource().openNewOutputWriter(getOutputCharset(), appendOutput);
  }

  /** Gets the output resource. If the output resource is null, first set it to console output. */
  private OutputResource obtainOutputResource() {
    if (outputResource == null) {
      outputResource = new ConsoleOutputResource();
    }
    return outputResource;
  }

  /** Character encoding for output files. */
  public Charset getOutputCharset() {
    if (outputEncodingCharset == null) {
      return StandardCharsets.UTF_8;
    } else {
      return outputEncodingCharset;
    }
  }

  public Path getOutputFile() {
    final Path outputFile;
    if (outputResource instanceof FileOutputResource) {
      outputFile = ((FileOutputResource) outputResource).getOutputFile();
    } else if (outputResource instanceof CompressedFileOutputResource) {
      outputFile = ((CompressedFileOutputResource) outputResource).getOutputFile();
    } else {
      // Create output file path
      outputFile =
          Paths.get(
                  ".",
                  String.format(
                      "sc.%s.%s", nextRandomString(), exportFormat.getExtension().replace(".", "")))
              .normalize()
              .toAbsolutePath();
    }
    return outputFile;
  }

  /**
   * Sets the name of the output file. It is important to note that the output encoding should be
   * available at this point.
   *
   * @param outputFile Output file name.
   */
  public void setOutputFile(final Path outputFile) {
    requireNonNull(outputFile, "No output file provided");
    outputResource = new FileOutputResource(outputFile);
  }

  private String nextRandomString() {
    final int length = 8;
    return new BigInteger(length * 5, random).toString(32);
  }

  public void incrementRecordProcessed() {
    recordsProcessed++;
  }

  protected void copyZipResource(String resource, File dest) {
    try (FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
      IOUtils.copy(Options.class.getResourceAsStream(resource), fileOutputStream);
      fileOutputStream.flush();
    } catch (IOException e) {
      log.error(ERROR_LOG_TEMPLATE, e);
    }
  }

  public void setOutputFilePath(String basePath, String title) {
    OutputFolderPathPrefix = basePath;
    outputFolderPath = OutputFolderPathPrefix;
    new File(outputFolderPath).mkdirs();
    outputFileTitle = title + "-" + getFileCountForTitle();
    String outputFile =
        outputFolderPath + File.separator + outputFileTitle + exportFormat.getExtension();
    P3FileCommonUtils.createFile(outputFile).getParentFile().mkdirs();
    if (exportFormat == ExportFormat.dynamic_export_html
        || exportFormat == ExportFormat.dynamic_export_html_blob_only) {

      String searchResultExportZip = SEARCH_RESULT_EXPORT_ZIP;
      final File resourceFile = new File(outputFolderPath + File.separator + searchResultExportZip);
      copyZipResource(RESOURCE_FILE_SEPARATOR + searchResultExportZip, resourceFile);
      ZipUtil.unpack(resourceFile, new File(outputFolderPath));
      resourceFile.delete();
      outputFile =
          outputFolderPath
              + File.separator
              + "assets"
              + File.separator
              + "js"
              + File.separator
              + "migrationReportData.js";
    } else if (exportFormat == ExportFormat.xslt_html || exportFormat == ExportFormat.xsl_pdf) {
      final String xsltBase = OutputFolderPathPrefix + "temp";
      new File(xsltBase).mkdirs();
      outputFile = xsltBase + File.separator + "temp.xml";
    }

    setOutputFile(Paths.get(outputFile));
    logger.debug("output path = {} ", outputFile);
  }

  public String getFileCountForTitle() {
    return String.format("%08d", (fileCount + 1));
  }

  public void incrementFileCount() {
    fileCount += 1;
  }
}
