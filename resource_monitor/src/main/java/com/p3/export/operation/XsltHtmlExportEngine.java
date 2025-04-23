package com.p3.export.operation;

import static com.p3.export.utility.CommonMessageConstants.ERROR_LOG_TEMPLATE;

import com.p3.export.exceptions.TransformationXsltException;
import com.p3.export.file_utils.P3FileCommonUtils;
import com.p3.export.formatter.BaseExporter;
import com.p3.export.formatter.TextExportHelper;
import com.p3.export.options.ColumnInfo;
import com.p3.export.options.Options;
import com.p3.export.specifics.ColumnEntity;
import com.p3.export.specifics.DataType;
import com.p3.export.specifics.ExcelSpecificDataType;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

@Slf4j
final class XsltHtmlExportEngine extends BaseExporter implements ExportEngineHandler {

  List<ExcelSpecificDataType> excelSpecificDataTypes;
  List<DataType> dataTypes;

  XsltHtmlExportEngine(Options options, String title, List<ColumnInfo> columnsInfo, String dataformat)
      throws Exception {
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
    exportHelper.writeRow(currentRow, null, this.dataTypes);
    exportHelper.flush();
    options.incrementRecordProcessed();
    generateProgressReport(title);
  }

  @Override
  public void handleDataStart() throws Exception {
    exportHelper.writeDocumentStart();
    exportHelper.writeRowHeader(
        columnsInfo.stream().map(ColumnInfo::getColumn).collect(Collectors.toList()));
  }

  @Override
  public void handleDataEnd() throws Exception {
    exportHelper.flush();
    exportHelper.writeDocumentEnd();
    transformWithXslt();
    performCleanup();
  }

  @Override
  public void handleDataEnd(Object reportDetails) throws Exception {
  }

  @Override
  public void iterateRows(List<Object> values, List<String> attachementList) throws Exception {

  }

  private void performCleanup() {
    try {
      FileUtils.deleteDirectory(options.getOutputFile().toFile().getParentFile());
      FileUtils.deleteDirectory(new File(options.getTemplatePath()).getParentFile());
    } catch (Exception e) {
      log.error(ERROR_LOG_TEMPLATE, e);
    }
  }

  private void transformWithXslt() throws TransformationXsltException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    } catch (Exception attributeException) {
      log.error(ERROR_LOG_TEMPLATE, attributeException);
    }
    try (InputStream inputStream = new FileInputStream(options.getOutputFile().toFile())) {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(inputStream);
      try (FileOutputStream output =
          P3FileCommonUtils.createFileOut(
              options.getOutputFolderPath()
                  + File.separator
                  + options.getOutputFileTitle()
                  + ".html")) {
        transform(document, output);
      }

    } catch (Exception e) {
      log.error(ERROR_LOG_TEMPLATE, e);
      throw new TransformationXsltException("Invalid Transformation");
    }
  }

  private void transform(Document doc, OutputStream output) throws TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    Transformer transformer =
        transformerFactory.newTransformer(new StreamSource(new File(options.getTemplatePath())));
    transformer.transform(new DOMSource(doc), new StreamResult(output));
  }
  @Override
  public void iterateRows(List<Object> values, List<String> attachementList, List<ColumnEntity> columnMetadata) throws Exception {

  }
}
