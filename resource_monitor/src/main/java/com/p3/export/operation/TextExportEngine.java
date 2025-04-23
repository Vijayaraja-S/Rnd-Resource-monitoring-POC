package com.p3.export.operation;

import com.p3.export.formatter.BaseExporter;
import com.p3.export.formatter.TextExportHelper;
import com.p3.export.options.ColumnInfo;
import com.p3.export.options.Options;
import com.p3.export.specifics.ColumnEntity;
import java.util.List;
import java.util.stream.Collectors;

final class TextExportEngine extends BaseExporter implements ExportEngineHandler {
  TextExportEngine(Options options, String title, List<ColumnInfo> columnsInfo, String dataformat) throws Exception {
    super(options, columnsInfo, title, dataformat);
  }

  @Override
  public TextExportHelper getExporter() {
    return exportHelper;
  }

  public void iterateRows(final List<Object> currentRow) throws Exception {
    exportHelper.writeRow(currentRow, null, null);
    exportHelper.flush();
    options.incrementRecordProcessed();
    generateProgressReport(title);
  }

  @Override
  public void handleDataStart() throws Exception {
    exportHelper.writeDocumentStart();
    exportHelper.append(options.getFileStartAppender());
    exportHelper.writeRowHeader(
        columnsInfo.stream().map(ColumnInfo::getColumn).collect(Collectors.toList()));
    exportHelper.flush();
  }

  @Override
  public void handleDataEnd() throws Exception {
    exportHelper.writeDocumentEnd();
    exportHelper.flush();
    exportHelper.append(options.getFileEndAppender());
    exportHelper.flush();
    exportHelper.close();
    // TODO exportHelper.cleanUp();
  }

  @Override
  public void handleDataEnd(Object reportDetails) throws Exception {
  }

  @Override
  public void iterateRows(List<Object> values, List<String> attachementList) throws Exception {

  }
  @Override
  public void iterateRows(List<Object> values, List<String> attachementList, List<ColumnEntity> columnMetadata) throws Exception {

  }
}
