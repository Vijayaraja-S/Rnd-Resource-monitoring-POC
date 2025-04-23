package com.p3.export.operation;

import com.p3.export.formatter.TextExportHelper;
import com.p3.export.specifics.ColumnEntity;
import java.util.List;

public interface ExportEngineHandler {
  TextExportHelper getExporter();
  void iterateRows(final List<Object> currentRow) throws Exception;
  void handleDataStart() throws Exception;
  void handleDataEnd() throws Exception;
  void handleDataEnd(Object reportDetails) throws Exception;
  void iterateRows(List<Object> values, List<String> attachementList)throws Exception;
  void iterateRows(List<Object> values, List<String> attachementList, List<ColumnEntity> columnMetadata) throws Exception;
}
