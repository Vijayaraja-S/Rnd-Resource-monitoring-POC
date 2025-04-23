package com.p3.export.formatter;

import static com.p3.export.utility.CommonMessageConstants.ERROR_LOG_TEMPLATE;
import static com.p3.export.utility.others.Utility.getTextFormatted;

import com.p3.export.exceptions.ExportException;
import com.p3.export.options.Options;
import com.p3.export.specifics.BlobKeySplitBean;
import com.p3.export.specifics.ColumnEntity;
import com.p3.export.specifics.DataType;
import com.p3.export.specifics.ExcelSpecificDataType;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.impl.common.XMLChar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Slf4j
public class XmlExportHelper implements TextExportHelper {

  private final ExportFormat exportFormat;
  private final boolean xmlCaseSensitive;
  protected PrintWriter out;
  Options options;
  XMLStreamWriter writer;
  XMLOutputFactory factory;
  List<String> columnList;
  String currentFilePath;
  String title;
  String outputFolderPath;
  long currentRecordCount;
  boolean headerAdded = false;
  private String RECORD = "RECORD";
  private String RESULT = "RESULT";

  public XmlExportHelper(String title,
                         boolean xmlCaseSensitive, ExportFormat exportFormat,
                         String outputFolderPath,
                         Options options) throws ExportException {
    this.title = title;
    this.outputFolderPath = outputFolderPath;
    String[] tableSchemaName = title.split("-");
    this.RESULT = tableSchemaName[0];
    this.RECORD = tableSchemaName[1];
    this.exportFormat = exportFormat;
    System.setProperty("javax.xml.stream.XMLOutputFactory", "com.sun.xml.internal.stream.XMLOutputFactoryImpl");
    factory = XMLOutputFactory.newInstance();
    this.options = options;
    this.out = createWriter(title, exportFormat, outputFolderPath);
    this.xmlCaseSensitive = xmlCaseSensitive;
  }

  private PrintWriter createWriter(String title, ExportFormat exportFormat, String outputFolderPath) throws ExportException {
    PrintWriter printWriter = null;
    try {
      this.headerAdded = false;
      String outputFileTitle = title + "-" + options.getFileCountForTitle();
      this.options.setOutputFileTitle(outputFileTitle);
      options.incrementFileCount();
      String filePath = outputFolderPath + File.separator + outputFileTitle + exportFormat.getExtension();
      this.currentFilePath = filePath;
      printWriter= new PrintWriter(filePath);
      this.currentRecordCount=0;
      this.writer = factory.createXMLStreamWriter(printWriter);
    } catch (final IOException e) {
      throw new ExportException("Cannot open output writer", e);
    } catch (XMLStreamException e){
      log.error(ERROR_LOG_TEMPLATE, e);
    }
    return printWriter;
  }

  @Override
  public TextExportHelper append(String text) {
    out.write(text);
    out.flush();
    return this;
  }

  @Override
  public void writeDocumentEnd() throws Exception {
    if(currentRecordCount>0) {
      writeRootElementEnd();
      writer.writeEndDocument();
    }
    writer.flush();
    writer.close();
    cleanUp();
  }

  @Override
  public void writeDocumentStart() throws Exception {
    writer.writeStartDocument("UTF-8", "1.0");
    writeRootElementStart();
  }

  @Override
  public void writeEmptyRow() throws Exception {
    writer.writeCharacters("\n");
  }

  @Override
  public void writeRow(
      List<Object> data,
      List<ExcelSpecificDataType> excelSpecificDataTypes,
      List<DataType> dataTypes)
      throws Exception {
    if (!headerAdded) {
      writeDocumentStart();
      headerAdded = true;
    }
    long fileSize = new File(currentFilePath).length();
    if((options.getRecordsProcessed() > 0 &&
            (options.getRecordsProcessed() % options.getRecordPerFile() == 0))
            || (fileSize >= ((options.getSizePerFile()* 1024 * 1024) - (options.getThresholdSize() * 1024)))) {
      log.info("File Size : {}",fileSize);
      writeRootElementEnd();
      writer.writeEndDocument();
      writer.flush();
      writer.close();
      this.out = createWriter(title, exportFormat, outputFolderPath);
    }
    writeRecordStart();
    for (int column = 0; column < data.size(); column++) {
      String columnName = columnList.get(column);
      final DataType dataType = dataTypes.get(column);
      final Object dataValue = data.get(column);
      writeRecordElement(columnName, dataType, dataValue);
    }
    currentRecordCount += data.size();
    writeRecordEnd();
    out.flush();
  }

  private void writeRecordElement(String columnName, DataType dataType, Object dataValue) {
    writeElementStart(columnName);
    writeAttribute("dataType", dataType.name());
    writeAttribute("columnName", columnName);
    if (dataValue == null) writeAttribute("nil", Boolean.TRUE.toString());
    else if (dataValue.toString().isEmpty()) writeAttribute("empty", Boolean.TRUE.toString());
    else processValue(dataValue, dataType);
    writeElementEnd();
  }

  private void processValue(Object data, DataType dataType) {
    switch (dataType) {
      case BLOB:
        String value = "";
        final BlobKeySplitBean blobKeySplitbean = (BlobKeySplitBean) data;
        writeAttribute("type", "blob");
        if (blobKeySplitbean.getError() == null) {
          writeAttribute("ref", blobKeySplitbean.getRelativePath());
        } else {
          writeAttribute("ref", "");
          writeAttribute("message", blobKeySplitbean.getError());
        }
        value = blobKeySplitbean.getOutputFileName();
        writeValue(value);
        break;
      case STRING:
      case NUMBER:
      case DECIMAL:
      case DATE:
      case DATETIME:
      case BOOLEAN:
      default:
        writeValue(String.valueOf(data));
    }
  }

  @Override
  public void writeRowHeader(List<String> columnNames) throws Exception {
    columnList = columnNames;
  }

  @Override
  public void flush() {
    try {
      writer.flush();
    } catch (Exception e) {
    }
  }

  @Override
  public void close() throws Exception {
    try {
      writer.flush();
    } catch (Exception e) {
      try {
        writer.close();
      } catch (Exception e1) {
      }
    }
  }

  @Override
  public void writeRow(List<Object> currentRow, List<ExcelSpecificDataType> excelSpecificDataTypes, List<ColumnEntity> columnEntities, List<String> attachementList) throws Exception {

  }

  @Override
  public void cleanUp() {
    if (currentRecordCount <= 0) {
      if (Objects.nonNull(currentFilePath)) {
        File file = new File(currentFilePath);
        FileUtils.deleteQuietly(file);
      }
    }
  }

  public void writeValue(String value) {
    try {
      writer.writeCharacters(value);
    } catch (XMLStreamException e) {
      log.error(ERROR_LOG_TEMPLATE, e);
    }
  }

  public void writeElementEnd() {
    try {
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      log.error(ERROR_LOG_TEMPLATE, e);
    }
  }

  public void writeElementStart(String columnName) {
    try {
      writer.writeCharacters("\n\t\t");
      String textFormatted =
          this.xmlCaseSensitive
              ? getTextFormatted(columnName)
              : getTextFormatted(columnName.toUpperCase());
      writer.writeStartElement(textFormatted);
    } catch (XMLStreamException e) {
      log.error(ERROR_LOG_TEMPLATE, e);
    }
  }

  public void writeRecordStart() {
    try {
      writer.writeCharacters("\n\t");
      writer.writeStartElement(RECORD);
    } catch (XMLStreamException e) {
      log.error(ERROR_LOG_TEMPLATE, e);
    }
  }

  public void writeRecordEnd() {
    try {
      writer.writeCharacters("\n\t");
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      log.error(ERROR_LOG_TEMPLATE, e);
    }
  }

  public void writeRootElementEnd() {
    try {
      writer.writeCharacters("\n");
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      log.error(ERROR_LOG_TEMPLATE, e);
    }
  }

  public void writeRootElementStart() {
    try {
      writer.writeCharacters("\n");
      writer.writeStartElement(RESULT);
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
  }

  public void writeAttribute(String name, String value) {
    try {
      writer.writeAttribute(getTextFormatted(name), value);
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
  }

  public void writeCData(String value) {
    try {
      if (value == null) {
        writeValue("");
      } else if (value.equals("")) {
        writer.writeCData("");
      } else {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElement = document.createElement("_");
        document.appendChild(rootElement);
        rootElement.appendChild(document.createCDATASection(stripNonValidXMLCharacters(value)));
        String xmlString = getXMLFromDocument(document);
        writer.writeCData(xmlString.substring(0, xmlString.length() - 7).substring(12));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getXMLFromDocument(Document document) throws TransformerException {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    StreamResult streamResult = new StreamResult(new StringWriter());
    DOMSource source = new DOMSource(document);
    transformer.transform(source, streamResult);
    return streamResult.getWriter().toString();
  }

  private String stripNonValidXMLCharacters(String in) {
    if (in == null || ("".equals(in))) return "";
    StringBuilder sb = new StringBuilder();
    for (char c : in.toCharArray()) sb.append(XMLChar.isValid(c) ? c : "");
    return (sb.toString().trim());
  }
}
