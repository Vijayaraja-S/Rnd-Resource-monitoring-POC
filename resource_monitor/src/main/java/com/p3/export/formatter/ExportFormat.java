package com.p3.export.formatter;

public enum ExportFormat {
  txt("Text File (TXT) format", ".txt"),
  csv("Comma-separated values (CSV) format", ".csv"),
  tsv("Tab-separated values (TSV) format", ".tsv"),
  ssv("Semicolan-separated values (SSV) format", ".ssv"),
  json("JavaScript Object Notation (JSON) format", ".json"),
  js("JavaScript Format", ".js"),
  excel("Excel xlsx format", ".xlsx"),
  dynamic_export_html("Dynamic bootstrapped HTML report format", ""),
  dynamic_export_html_blob_only("Dynamic bootstrapped HTML report format", ""),
  html("HyperText Markup Language (HTML) format", ".html"),

  xml("eXtensive Markup Language (XML) format", ".xml"),
  xslt_html("HTML through XSLT format", ".html"),
  xsl_pdf("PDF through XSLT format", ".pdf"),
  parquet("Parquet Format", ".parquet");

  private final String description;
  private final String extension;

  ExportFormat(final String description, final String extension) {
    this.description = description;
    this.extension = extension;
  }

  public static ExportFormat getByValue(String exportFormatType) {
    for (ExportFormat exportFormat : ExportFormat.values()) {
      if (exportFormat.name().equalsIgnoreCase(exportFormatType)) return exportFormat;
    }
    return null;
  }

  public String getExtension() {
    return this.extension;
  }

  public String getDescription() {
    return this.description;
  }
}
