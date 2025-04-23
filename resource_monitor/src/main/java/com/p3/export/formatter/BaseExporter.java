package com.p3.export.formatter;

import static java.util.Objects.requireNonNull;

import com.p3.export.exceptions.ExportException;
import com.p3.export.logutils.P3LoggerUtils;
import com.p3.export.options.ColumnInfo;
import com.p3.export.options.Options;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseExporter {

  private static final long RECORD_PROCESS_LOG_CHECKER_VALUE = 100000;
  private final P3LoggerUtils logger;
  private final String dataformat;
  protected TextExportHelper exportHelper;
  protected Options options;
  protected PrintWriter out;
  protected List<ColumnInfo> columnsInfo;
  protected String title;

  public BaseExporter(Options outputOptions, List<ColumnInfo> columnsInfo, String title, String dataformat)
      throws Exception {
    this.options = requireNonNull(outputOptions, "Options not provided");
    this.title = title;
    this.columnsInfo = columnsInfo;
      this.dataformat = dataformat;
      this.logger = new P3LoggerUtils(BaseExporter.class);
    if (this.options.getExportFormat().equals(ExportFormat.parquet) ||
            this.options.getExportFormat().equals(ExportFormat.xml)
    ) {
    } else {
      try {
        Writer newOutputWriter = options.openNewOutputWriter(true);
        out = new PrintWriter(newOutputWriter, true);
      } catch (final IOException e) {
        throw new ExportException("Cannot open output writer", e);
      }
    }
    setFormattingHelper();
  }

  protected void setFormattingHelper() throws ExportException, IOException {
    final ExportFormat exportFormat = options.getExportFormat();
    switch (exportFormat) {
      case csv:
      case tsv:
      case txt:
      case ssv:
        exportHelper = new PlainTextExportHelper(exportFormat,title,options.getOutputFolderPath(),options);
        break;
      case dynamic_export_html:
      case dynamic_export_html_blob_only:
      case js:
        exportHelper = new JsExportHelper(out, exportFormat);
        break;
      case json:
        exportHelper = new JsonExportHelper(out, exportFormat);
        break;
      case html:
        exportHelper = new HtmlExportHelper(out, exportFormat);
        break;
      case excel:
        exportHelper = new ExcelExportHelper(exportFormat,title,options.getOutputFolderPath(),options);
        break;
      case xml:
      case xslt_html:
      case xsl_pdf:
        exportHelper = new XmlExportHelper(title,options.isXmlCaseSensitive(), exportFormat,
                options.getOutputFolderPath(),
                options);
        break;
      case parquet:
        exportHelper =
            new ParquetExportHelper(
                title, columnsInfo, options.getOutputFile().toFile().getAbsolutePath(),
                    options.getOutputFileTitle(),options.getOutputFolderPath(),options,dataformat);
        break;
    }
  }

  protected void generateProgressReport(String title) {
    if (options.getRecordsProcessed() % RECORD_PROCESS_LOG_CHECKER_VALUE == 0) {
      logger.info("Records Processed : {} : {} ", title, options.getRecordsProcessed());
    }
  }
}
