package com.p3.export.operation;

import com.p3.export.exceptions.TransformationXsltException;
import com.p3.export.file_utils.P3FileCommonUtils;
import com.p3.export.formatter.BaseExporter;
import com.p3.export.formatter.TextExportHelper;
import com.p3.export.options.ColumnInfo;
import com.p3.export.options.Options;
import com.p3.export.specifics.ColumnEntity;
import com.p3.export.specifics.DataType;
import com.p3.export.specifics.ExcelSpecificDataType;
import com.p3.export.utility.CommonMessageConstants;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.fop.apps.*;

@Slf4j
final class XslPdfExportEngine extends BaseExporter implements ExportEngineHandler {

  List<ExcelSpecificDataType> excelSpecificDataTypes;
  List<DataType> dataTypes;

  XslPdfExportEngine(Options options, String title, List<ColumnInfo> columnsInfo, String dataformat) throws Exception {
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
      log.error(CommonMessageConstants.ERROR_LOG_TEMPLATE, e);
    }
  }

  private void transformWithXslt() throws TransformationXsltException {
    StreamSource xmlSource =
        new StreamSource(
            P3FileCommonUtils.createFile(options.getOutputFile().toAbsolutePath().toString()));
    try (OutputStream output =
        P3FileCommonUtils.createFileOut(
            options.getOutputFolderPath()
                + File.separator
                + options.getOutputFileTitle()
                + ".pdf")) {
      convertToPDF();
    } catch (Exception e) {
      log.error(CommonMessageConstants.ERROR_LOG_TEMPLATE, e);
      throw new TransformationXsltException("Invalid Transformation");
    }
  }

  private void transformToPdf(StreamSource streamSource, OutputStream output)
      throws TransformerException, FOPException {
    FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
    FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, output);
    TransformerFactory factory = TransformerFactory.newInstance();
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    Transformer transformer = factory.newTransformer(new StreamSource(options.getTemplatePath()));

    Result res = new SAXResult(fop.getDefaultHandler());

    transformer.transform(streamSource, res);
  }

  public void convertToPDF() throws IOException, FOPException, TransformerException {
    File xsltFile = new File(options.getTemplatePath());
    StreamSource xmlSource =
        new StreamSource(
            P3FileCommonUtils.createFile(options.getOutputFile().toAbsolutePath().toString()));
    FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
    FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
    OutputStream out;
    out =
        P3FileCommonUtils.createFileOut(
            options.getOutputFolderPath() + File.separator + options.getOutputFileTitle() + ".pdf");

    try {
      Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

      TransformerFactory factory = TransformerFactory.newInstance();
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      Transformer transformer = factory.newTransformer(new StreamSource(xsltFile));
      Result res = new SAXResult(fop.getDefaultHandler());
      transformer.transform(xmlSource, res);
    } finally {
      out.close();
    }
  }
  @Override
  public void iterateRows(List<Object> values, List<String> attachementList, List<ColumnEntity> columnMetadata) throws Exception {

  }
}
