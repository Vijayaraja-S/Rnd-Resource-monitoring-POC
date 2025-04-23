package com.p3.export.formatter;

import com.p3.export.specifics.ColumnEntity;
import com.p3.export.specifics.DataType;
import com.p3.export.specifics.ExcelSpecificDataType;
import java.util.List;

/** Methods to format entire rows of output. */
public interface TextExportHelper {

  TextExportHelper append(String text);

  void writeDocumentEnd() throws Exception;

  void writeDocumentStart() throws Exception;

  void writeEmptyRow() throws Exception;

  void writeRow(
      List<Object> columnData,
      List<ExcelSpecificDataType> excelSpecificDataTypes,
      List<DataType> dataTypes)
      throws Exception;

  void writeRowHeader(List<String> columnNames) throws Exception;

  void flush() throws Exception;

  void close() throws Exception;

  void writeRow(List<Object> currentRow, List<ExcelSpecificDataType> excelSpecificDataTypes,
                List<ColumnEntity> columnEntities, List<String> attachementList)throws Exception;
  void cleanUp();
}
