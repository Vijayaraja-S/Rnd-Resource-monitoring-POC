package com.p3.export.formatter;

import com.p3.export.options.Options;
import com.p3.export.specifics.ColumnEntity;
import com.p3.export.specifics.ExcelSpecificDataType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
public class PlainTextExportHelper extends BaseTextExportHelper {

  public PlainTextExportHelper(ExportFormat exportFormat, String title, String outputFolderPath, Options options) throws IOException {
    super(exportFormat,title,outputFolderPath,options);
  }
  public PlainTextExportHelper(PrintWriter out,ExportFormat exportFormat) throws FileNotFoundException {
    super(out,exportFormat);
  }

  @Override
  public void writeDocumentEnd() {}

  @Override
  public void writeDocumentStart() {}

  @Override
  public void flush() {
   out.flush();
  }

  @Override
  public void close() {
    if (out != null) {
      out.flush();
      out.close();
      cleanUp();
    }
  }

  @Override
  public void writeRow(List<Object> currentRow, List<ExcelSpecificDataType> excelSpecificDataTypes, List<ColumnEntity> columnEntities, List<String> attachementList) throws Exception {

  }

  @Override
  public void cleanUp() {
    if(Objects.nonNull(options) && options.getRecordsProcessed() ==0){
      FileUtils.deleteQuietly(new File(outputFilePath));
    }

  }
}
