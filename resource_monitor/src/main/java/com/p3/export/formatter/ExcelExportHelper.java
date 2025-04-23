package com.p3.export.formatter;

import com.p3.export.options.Options;
import com.p3.export.specifics.ColumnEntity;
import com.p3.export.specifics.DataType;
import com.p3.export.specifics.ExcelSpecificDataType;
import com.p3.export.utility.excel.ExcelEngine;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
public class ExcelExportHelper implements TextExportHelper {

  private final ExportFormat exportFormat;
  private final String title;
  protected List<String> columnNames;
  String outputFilePath;
  String outputFolderPath;
  Options options;
  long currentFileCount;
  boolean headerAdded = false;
  private ExcelEngine excelEngineHandler;

  public ExcelExportHelper(final ExportFormat exportFormat,String title,String outputFolderPath,
                           Options options) {
    this.options = options;
    this.outputFolderPath = outputFolderPath;
    this.exportFormat = exportFormat;
    this.title = title;
    excelEngineHandler = createWriter(exportFormat, title, outputFolderPath);
  }

  private ExcelEngine createWriter(ExportFormat exportFormat, String title, String outputFolderPath) {
    final ExcelEngine excelEngineHandler;
    this.headerAdded = false;
    String outputFileTitle = title + "-" + options.getFileCountForTitle();
    this.options.setOutputFileTitle(outputFileTitle);
    String outputFilePath = outputFolderPath + File.separator + outputFileTitle + exportFormat.getExtension();
    this.outputFilePath = outputFilePath;
    File out = new File(outputFilePath);
    excelEngineHandler = new ExcelEngine(out);
    options.incrementFileCount();
    this.currentFileCount = 0;
    return excelEngineHandler;
  }

  @Override
  public TextExportHelper append(String text) {
    return null;
  }

  @Override
  public void writeDocumentEnd() throws Exception {
    if(currentFileCount>0) {
      excelEngineHandler.writeExcel();
    }
    cleanUp();
  }

  @Override
  public void writeDocumentStart() throws Exception {
    excelEngineHandler.createWorkBook();
    excelEngineHandler.createSheet(title);
  }

  @Override
  public void writeEmptyRow() throws Exception {
    excelEngineHandler.createNewRow();
  }

  @Override
  public void writeRow(
      List<Object> columnData,
      List<ExcelSpecificDataType> excelSpecificDataTypes,
      List<DataType> dataTypes)
      throws Exception {

    if(!headerAdded){
      writeDocumentStart();
      excelEngineHandler.addTitlesRow(columnNames);
      headerAdded=true;
    }
    long size = new File(outputFilePath).length();
    if((options.getRecordsProcessed() > 0 && (options.getRecordsProcessed() % options.getRecordPerFile() == 0))
            || (size >= ((options.getSizePerFile()* 1024 * 1024) - (options.getThresholdSize() * 1024)))){
      log.debug("File Size : {}",size);
      flush();
      writeDocumentEnd();
      excelEngineHandler = createWriter(exportFormat, title, outputFolderPath);
      writeDocumentStart();
      writeRowHeader(this.columnNames);
      excelEngineHandler.addTitlesRow(columnNames);
    }
    currentFileCount+=columnData.size();
    excelEngineHandler.addRecordRow(columnData, excelSpecificDataTypes);
  }

  @Override
  public void writeRowHeader(List<String> columnNames) throws Exception {
    this.columnNames = columnNames;
  }

  @Override
  public void flush() {}

  @Override
  public void close() {
  }

  @Override
  public void writeRow(List<Object> currentRow, List<ExcelSpecificDataType> excelSpecificDataTypes, List<ColumnEntity> columnEntities, List<String> attachementList) throws Exception {

  }

  @Override
  public void cleanUp() {
    if(currentFileCount <= 0){
      FileUtils.deleteQuietly(new File(outputFilePath));
    }
  }
}
