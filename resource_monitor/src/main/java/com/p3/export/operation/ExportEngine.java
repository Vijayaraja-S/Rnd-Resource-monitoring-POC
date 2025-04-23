package com.p3.export.operation;

import com.p3.export.formatter.ExportFormat;
import com.p3.export.options.ColumnInfo;
import com.p3.export.options.Options;
import com.p3.export.specifics.ColumnEntity;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Builder
public class ExportEngine {

  private final String basePath;
  private final String title;
  private String xsltFilePath;
  private ExportFormat exportFormat;
  private List<ColumnInfo> columnsInfo;
  private ExportEngineExecutable oe;
  private ExportEngineHandler eh;
  private String dateFormat;
  private String fileEncoding;
  @Builder.Default private Long blobPerFolder = 100L;
  @Builder.Default private Long sizePerFile = 50L;
  @Builder.Default private Long thresholdSize = 500L;
  @Builder.Default private Long recordPerFile = 10000L;

  public ExportEngineHandler getExportEngineHandler() throws Exception {
    Options options = initiateOptions();
    oe = new ExportEngineExecutable(options, title, columnsInfo, dateFormat);
    return oe.getExportEngineHandler();
  }

  private Options initiateOptions() throws IOException {
    if (exportFormat == ExportFormat.xslt_html) {
      if (xsltFilePath == null) {
        exportFormat = ExportFormat.dynamic_export_html;
      }
    }
    Options options = Options.builder()
            .exportFormat(exportFormat)
            .build();
    options.setOutputFilePath(basePath, title);
    options.setXmlCaseSensitive(xsltFilePath != null);
    options.setTemplatePath(xsltFilePath);
    options.setSizePerFile(sizePerFile);
    options.setRecordPerFile(recordPerFile);
    options.setOutputEncodingCharset(Charset.forName(fileEncoding));
    return options;
  }

  private String writeXsltFile(Options options) throws IOException {
    String path = options.getOutputFolderPathPrefix() + "xslt";
    new File(path).mkdirs();
    String xsltPath = path + File.separator + "xsltFile.xslt";
    try (FileWriter fileWriter = new FileWriter(xsltPath)) {
      fileWriter.write(xsltFilePath);
      fileWriter.flush();
    }
    return xsltPath;
  }

  public void initialize() throws Exception {
    eh = getExportEngineHandler();
  }

  public void handleDataStart() throws Exception {
    eh.handleDataStart();
  }

  public void iterateRows(List<Object> values) throws Exception {
    eh.iterateRows(values);
  }

  public void handleDataEnd() throws Exception {
    eh.handleDataEnd();
  }
  public void handleDataEnd(Object reportDetails) throws Exception {
    eh.handleDataEnd(reportDetails);
  }

  public void generateReport() {
    oe.generateReport();
  }

  public String getResultPath(boolean zip) {
    return zip ? oe.zipOutput() : oe.baseResultPath();
  }

  public void iterateRows(List<Object> values, List<String> attachementList, Map<String, ColumnEntity> columnMetadata) throws Exception {
    eh.iterateRows(values,attachementList,columnMetadata.values().stream().sorted(Comparator.comparing(ColumnEntity::getOrdinal)).collect(Collectors.toList()));
  }
}
