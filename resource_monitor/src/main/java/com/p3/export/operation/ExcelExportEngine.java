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

final class ExcelExportEngine extends BaseExporter implements ExportEngineHandler {

  List<ExcelSpecificDataType> excelSpecificDataTypes;
  List<DataType> dataTypes;

  ExcelExportEngine(Options options, String title, List<ColumnInfo> columnsInfo, String dataformat) throws Exception {
    super(options, columnsInfo, title, dataformat);
    this.excelSpecificDataTypes =
        columnsInfo.stream().map(ColumnInfo::getExcelDataType).collect(Collectors.toList());
    this.dataTypes = columnsInfo.stream().map(ColumnInfo::getDataType).collect(Collectors.toList());
  }

  @Override
  public TextExportHelper getExporter() {
    return exportHelper;
  }

  public void iterateRows(final List<Object> currentRow) throws Exception {
    exportHelper.writeRow(currentRow, this.excelSpecificDataTypes, null);
    exportHelper.flush();
    options.incrementRecordProcessed();
    generateProgressReport(title);
  }

  @Override
  public void handleDataStart() throws Exception {
   // exportHelper.writeDocumentStart();
    exportHelper.writeRowHeader(
        columnsInfo.stream().map(ColumnInfo::getColumn).collect(Collectors.toList()));
  }

  @Override
  public void handleDataEnd() throws Exception {
    exportHelper.flush();
    exportHelper.writeDocumentEnd();
  }

  @Override
  public void handleDataEnd(Object reportDetails) throws Exception {}

  @Override
  public void iterateRows(List<Object> values, List<String> attachementList) throws Exception {

  }
  @Override
  public void iterateRows(List<Object> values, List<String> attachementList, List<ColumnEntity> columnMetadata) throws Exception {

  }
}
