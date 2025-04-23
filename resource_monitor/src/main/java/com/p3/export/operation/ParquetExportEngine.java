package com.p3.export.operation;

import com.p3.export.formatter.BaseExporter;
import com.p3.export.formatter.TextExportHelper;
import com.p3.export.options.ColumnInfo;
import com.p3.export.options.Options;
import com.p3.export.specifics.ColumnEntity;
import com.p3.export.specifics.DataType;
import com.p3.export.specifics.ExcelSpecificDataType;
import java.util.List;
import java.util.stream.Collectors;

final class ParquetExportEngine extends BaseExporter implements ExportEngineHandler {

  List<ExcelSpecificDataType> excelSpecificDataTypes;
  List<DataType> dataTypes;

  ParquetExportEngine(
      Options options, String title, List<ColumnInfo> columnsInfo, String dataformat)
      throws Exception {
    super(options, columnsInfo, title, dataformat);
    this.dataTypes = columnsInfo.stream().map(ColumnInfo::getDataType).collect(Collectors.toList());
  }

  @Override
  public TextExportHelper getExporter() {
    return exportHelper;
  }

  public void iterateRows(final List<Object> currentRow) throws Exception {
    exportHelper.writeRow(currentRow, null, this.dataTypes);
    exportHelper.flush();
    options.incrementRecordProcessed();
    generateProgressReport(title);
  }

  @Override
  public void handleDataStart() throws Exception {}

  @Override
  public void handleDataEnd() throws Exception {
    exportHelper.writeDocumentEnd();
  }

  @Override
  public void handleDataEnd(Object reportDetails) throws Exception {}

  @Override
  public void iterateRows(List<Object> currentRow, List<String> attachementList) throws Exception {}

  @Override
  public void iterateRows(
      List<Object> currentRow, List<String> attachementList, List<ColumnEntity> columnMetadata)
      throws Exception {
    exportHelper.writeRow(currentRow, null, columnMetadata, attachementList);
    exportHelper.flush();
    options.incrementRecordProcessed();
    generateProgressReport(title);
  }
}
