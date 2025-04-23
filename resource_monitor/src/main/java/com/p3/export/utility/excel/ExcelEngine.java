package com.p3.export.utility.excel;

import com.p3.export.specifics.BlobKeySplitBean;
import com.p3.export.specifics.ExcelSpecificDataType;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

@Slf4j
@Data
public class ExcelEngine implements ExcelEngineHandler {
  private final File out;
  CreationHelper creationHelper;
  CellStyle titleCellStyle;
  CellStyle dataCellStyleString;
  CellStyle dataCellStyleBlob;
  CellStyle dataCellStyleNumber;
  CellStyle dataCellStyleDouble;
  CellStyle dataCellStyleCurrency;
  CellStyle dataCellStyleDate;
  CellStyle dataCellStyleDateTime;
  CellStyle aggregationCellStyleString;
  CellStyle aggregationCellStyleBlob;
  CellStyle aggregationCellStyleNumber;
  CellStyle aggregationCellStyleDouble;
  CellStyle aggregationCellStyleCurrency;
  CellStyle aggregationCellStyleDate;
  CellStyle aggregationCellStyleDateTime;
  CreationHelper createHelper;
  private Workbook workBook;
  private Map<Integer, Sheet> sheetMapper;
  private Sheet activeSheet;

  public ExcelEngine(final File out) {
    this.out = out;
  }

  @Override
  public void createWorkBook() {
    Workbook book = new SXSSFWorkbook();
    creationHelper = book.getCreationHelper();
    createHelper = book.getCreationHelper();
    setDefaultStyles(book);
    this.setWorkBook(book);
  }

  @Override
  public void createSheet() throws Exception {
    worksheetInitializationCheck();
    SXSSFSheet sheet = (SXSSFSheet) this.getWorkBook().createSheet();
    sheet.trackAllColumnsForAutoSizing();
    this.getSheetMapper().put(this.getSheetMapper().size(), sheet);
    this.setActiveSheet(sheet);
  }

  private void autoSizeColumn(SXSSFWorkbook wb, int columnCount) {
    for (int wbIndex = 0; wbIndex < wb.getNumberOfSheets(); wbIndex++) {
      for (int i = 0; i < columnCount; i++) {
        wb.getSheetAt(wbIndex).autoSizeColumn(i);
      }
    }
  }

  @Override
  public void createSheet(String name) throws Exception {
    worksheetInitializationCheck();
    SXSSFSheet sheet = (SXSSFSheet) this.getWorkBook().createSheet(name);
    sheet.trackAllColumnsForAutoSizing();
    this.getSheetMapper().put(this.getSheetMapper().size(), sheet);
    this.setActiveSheet(sheet);
  }

  @Override
  public void openSheet(int index) throws Exception {
    isWorkSheetAvailable();
    Sheet sheet = this.getSheetMapper().get(index);
    this.setActiveSheet(sheet);
  }

  @Override
  public void addTitlesRow(List<String> columns) throws Exception {
    Row row = createNewRow();
    for (int column = 0; column < columns.size(); column++) {
      Cell cell = row.createCell(column);
      cell.setCellStyle(createTitleCellStyle(this.getWorkBook()));
      cell.setCellValue(columns.get(column));
    }
  }

  @Override
  public void addRecordRow(List<Object> data, List<ExcelSpecificDataType> excelSpecificDataTypes)
      throws Exception {
    Row row = createNewRow();
    for (int column = 0; column < data.size(); column++) {
      Cell cell = row.createCell(column);
      putValueToCell(data.get(column), excelSpecificDataTypes.get(column), cell, true);
    }
  }

  @Override
  public void createAggregationRow(
      List<Object> data, List<ExcelSpecificDataType> excelSpecificDataTypes) throws Exception {
    Row row = createNewRow();
    for (int column = 0; column < data.size(); column++) {
      Cell cell = row.createCell(column);
      putValueToCell(data.get(column), excelSpecificDataTypes.get(column), cell, false);
    }
  }

  @Override
  public void writeExcel() throws Exception {
    isWorkBookAvailable();
    autoSizeColumn((SXSSFWorkbook) this.getWorkBook(), 100);
    this.getWorkBook().write(new FileOutputStream(out));
    ((SXSSFWorkbook) this.getWorkBook()).dispose();
    this.getWorkBook().close();
  }

  private void isWorkBookAvailable() throws Exception {
    if (this.getWorkBook() == null) {
      log.error("Workbook unavailable");
      throw new Exception("Workbook unavailable");
    }
  }

  private void isWorkSheetAvailable() throws Exception {
    if (this.getSheetMapper() == null) {
      log.error("Workbook unavailable");
      throw new Exception("Worksheet unavailable");
    }
  }

  private void worksheetInitializationCheck() throws Exception {
    isWorkBookAvailable();
    if (this.getSheetMapper() == null) {
      this.setSheetMapper(new TreeMap<>());
    }
  }

  public Row createNewRow() throws Exception {
    Sheet sheet = this.getActiveSheet();
    if (sheet == null) {
      log.error("No Sheet is set Active");
      throw new Exception("No Sheet is set Active");
    }
    int lastRowNum = sheet.getLastRowNum();
    if (lastRowNum == 0 && sheet.getRow(0) == null) return sheet.createRow(lastRowNum);
    return sheet.createRow(lastRowNum + 1);
  }

  private void putValueToCell(
      Object value, ExcelSpecificDataType dataType, Cell cell, boolean data) {
    if (Objects.isNull(value)) {
      cell.setCellValue("");
      cell.setCellStyle(data ? dataCellStyleString : aggregationCellStyleString);
      return;
    }
    try {
      cell.setCellStyle(data ? dataCellStyleString : aggregationCellStyleString);
      switch (dataType) {
        case BLOB:
          cell.setCellStyle(data ? dataCellStyleBlob : aggregationCellStyleBlob);
          final BlobKeySplitBean blobKeySplitBean = (BlobKeySplitBean) value;
          final String outputFileName = blobKeySplitBean.getOutputFileName();
          if (blobKeySplitBean.getError() == null) {
            cell.setCellValue(outputFileName);
            Hyperlink hp = createHelper.createHyperlink(HyperlinkType.FILE);
            String FileAddress = blobKeySplitBean.getRelativePath().replace("\\", "/");
            hp.setAddress(FileAddress);
            cell.setHyperlink(hp);
          } else {
            cell.setCellValue(outputFileName + "\n" + blobKeySplitBean.getError());
          }
          break;
        case FORMULA:
        case STRING:
          cell.setCellStyle(data ? dataCellStyleString : aggregationCellStyleString);
          cell.setCellValue(value.toString());
          break;
        case NUMBER:
          cell.setCellStyle(data ? dataCellStyleNumber : aggregationCellStyleNumber);
          cell.setCellValue(
              value instanceof Integer ? (Integer) value : Integer.parseInt(value.toString()));
          break;
        case DOUBLE:
          cell.setCellStyle(data ? dataCellStyleDouble : aggregationCellStyleDouble);
          cell.setCellValue(
              value instanceof Double ? (Double) value : Double.parseDouble(value.toString()));
          break;
        case CURRENCY:
          cell.setCellStyle(data ? dataCellStyleCurrency : aggregationCellStyleCurrency);
          cell.setCellValue(
              value instanceof Double ? (Double) value : Double.parseDouble(value.toString()));
          break;
        case DATE:
          cell.setCellStyle(data ? dataCellStyleDate : aggregationCellStyleDate);
          cell.setCellValue((Date) value);
          break;
        case DATETIME:
          cell.setCellStyle(data ? dataCellStyleDateTime : aggregationCellStyleDateTime);
          cell.setCellValue((Date) value);
          break;
      }
    } catch (Exception e) {
      cell.setCellValue(value.toString());
    }

    if (value.toString().contains("\n")) {
      cell.getCellStyle().setWrapText(true);
    }
  }

  private void getFormattingStyle(CellStyle cellStyle, ExcelSpecificDataType dataType) {
    switch (dataType) {
      case DOUBLE:
        cellStyle.setDataFormat(
            creationHelper.createDataFormat().getFormat("#,##0.00_);[Red](#,##0.00)"));
        break;
      case CURRENCY:
        cellStyle.setDataFormat(
            creationHelper.createDataFormat().getFormat("\"$ \"#,##0.00_);[Red](\"$ \"#,##0.00)"));
        break;
      case DATE:
        cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy"));
        break;
      case DATETIME:
        cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy HH:MM:SS"));
        break;
      case BLOB:
      case FORMULA:
      case STRING:
      case NUMBER:
      default:
        break;
    }
  }

  private void setDefaultStyles(Workbook book) {
    titleCellStyle = createTitleCellStyle(book);
    dataCellStyleString = createDateCellStyle(book, ExcelSpecificDataType.STRING);
    dataCellStyleBlob = createDateCellStyle(book, ExcelSpecificDataType.BLOB);
    dataCellStyleNumber = createDateCellStyle(book, ExcelSpecificDataType.NUMBER);
    dataCellStyleDouble = createDateCellStyle(book, ExcelSpecificDataType.DOUBLE);
    dataCellStyleCurrency = createDateCellStyle(book, ExcelSpecificDataType.CURRENCY);
    dataCellStyleDate = createDateCellStyle(book, ExcelSpecificDataType.DATE);
    dataCellStyleDateTime = createDateCellStyle(book, ExcelSpecificDataType.DATETIME);
    aggregationCellStyleString = createAggregationStyle(book, ExcelSpecificDataType.STRING);
    aggregationCellStyleBlob = createAggregationStyle(book, ExcelSpecificDataType.BLOB);
    aggregationCellStyleNumber = createAggregationStyle(book, ExcelSpecificDataType.NUMBER);
    aggregationCellStyleDouble = createAggregationStyle(book, ExcelSpecificDataType.DOUBLE);
    aggregationCellStyleCurrency = createAggregationStyle(book, ExcelSpecificDataType.CURRENCY);
    aggregationCellStyleDate = createAggregationStyle(book, ExcelSpecificDataType.DATE);
    aggregationCellStyleDateTime = createAggregationStyle(book, ExcelSpecificDataType.DATETIME);
  }

  private CellStyle createTitleCellStyle(Workbook book) {
    CellStyle titleCellStyle = book.createCellStyle();
    titleCellStyle.setAlignment(HorizontalAlignment.CENTER);
    titleCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    titleCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    titleCellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
    borderStyles(titleCellStyle, BorderStyle.THIN, IndexedColors.DARK_BLUE.getIndex());
    fontStyles(book, titleCellStyle, IndexedColors.WHITE.getIndex(), true, false);
    return titleCellStyle;
  }

  private CellStyle createDateCellStyle(Workbook book, ExcelSpecificDataType dataType) {
    CellStyle dataCellStyle = book.createCellStyle();
    borderStyles(dataCellStyle, BorderStyle.THIN, IndexedColors.BLACK.getIndex());
    if (dataType == ExcelSpecificDataType.BLOB) {
      fontStyles(book, dataCellStyle, IndexedColors.LIGHT_BLUE.getIndex(), false, true);
    } else {
      fontStyles(book, dataCellStyle, IndexedColors.BLACK.getIndex(), false, false);
    }
    getFormattingStyle(dataCellStyle, dataType);
    return dataCellStyle;
  }

  private CellStyle createAggregationStyle(Workbook book, ExcelSpecificDataType dataType) {
    CellStyle aggregationCellStyle = book.createCellStyle();
    aggregationCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    aggregationCellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
    borderStyles(aggregationCellStyle, BorderStyle.THIN, IndexedColors.LIGHT_BLUE.getIndex());
    fontStyles(
        book,
        aggregationCellStyle,
        IndexedColors.WHITE.getIndex(),
        true,
        dataType == ExcelSpecificDataType.BLOB);
    getFormattingStyle(aggregationCellStyle, dataType);
    return aggregationCellStyle;
  }

  private void fontStyles(
      Workbook book, CellStyle cellStyle, short colorIndex, boolean bold, boolean underline) {
    Font fontStyle = book.createFont();
    fontStyle.setColor(colorIndex);
    fontStyle.setBold(bold);
    fontStyle.setUnderline(underline ? Font.U_SINGLE : Font.U_NONE);
    cellStyle.setFont(fontStyle);
  }

  private void borderStyles(CellStyle cellStyle, BorderStyle borderStyle, short colorIndex) {
    cellStyle.setBorderBottom(borderStyle);
    cellStyle.setBorderLeft(borderStyle);
    cellStyle.setBorderRight(borderStyle);
    cellStyle.setBorderTop(borderStyle);
    cellStyle.setBottomBorderColor(colorIndex);
    cellStyle.setLeftBorderColor(colorIndex);
    cellStyle.setRightBorderColor(colorIndex);
    cellStyle.setTopBorderColor(colorIndex);
  }

  public void cleanup(String outputFilePath, String outputFolderPath,
                      long currentFileCount) {
    if(currentFileCount <= 0){
      FileUtils.deleteQuietly(new File(outputFilePath));
    }
    if(Objects.nonNull(outputFolderPath)) {
      for (File file : new File(outputFolderPath).listFiles()) {
        if (file.length() == 0) {
          FileUtils.deleteQuietly(file);
        }
      }
    }
  }
}
