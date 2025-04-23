package com.p3.export.utility.excel;

import com.p3.export.specifics.ExcelSpecificDataType;
import java.util.List;

public interface ExcelEngineHandler {

  void createWorkBook();

  void createSheet() throws Exception;

  void createSheet(String name) throws Exception;

  void openSheet(int index) throws Exception;

  void addTitlesRow(final List<String> columns) throws Exception;

  void addRecordRow(final List<Object> data, List<ExcelSpecificDataType> excelSpecificDataTypes)
      throws Exception;

  void createAggregationRow(
      final List<Object> data, List<ExcelSpecificDataType> excelSpecificDataTypes) throws Exception;

  void writeExcel() throws Exception;
}
