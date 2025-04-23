package com.p3.export.formatter;

import static com.p3.export.utility.CommonMessageConstants.ERROR_LOG_TEMPLATE;

import com.p3.export.options.ColumnInfo;
import com.p3.export.options.Options;
import com.p3.export.specifics.ColumnEntity;
import com.p3.export.specifics.DataType;
import com.p3.export.specifics.ExcelSpecificDataType;
import com.p3.export.utility.others.FileUtil;
import com.p3.export.utility.parquet.ParquetWriterBuilder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.schema.*;

@Slf4j
public class ParquetExportHelper implements TextExportHelper {


  public static final String TEMP_FOLDER ="temp";
  private final String  dataformat;
  MessageType parquetSchema;
  ParquetWriter<List<Object>> parquetWriter;
  Configuration hadoopConfig;
  String title;
  String outputFolderPath;
  String currentAttachmentFilePath;
  PrintWriter attachmentFileWriter;
  String attachmentFileFolderName;
  String parentFolder;
  Options options;
  long filesRecordCount= 0L;

  public ParquetExportHelper(String title, List<ColumnInfo> columnsInfo, String outputPath, String outputTitle,
                             String outputFolderPath, Options options, String dataformat) {
      this.dataformat = dataformat;
      try {
      parquetSchema = createParquetSchema(title.substring(title.indexOf("-") + 1), columnsInfo);
      hadoopConfig =
          new Configuration() {
            public Class<?> getClassByName(String name) throws ClassNotFoundException {
              return Class.forName(name);
            }
          };
      hadoopConfig.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
      parquetWriter = createParquetWriter(outputTitle,outputPath);
      this.outputFolderPath = outputFolderPath;
      this.options = options;
      this.title = title;
    } catch (Exception exception) {
      log.error(ERROR_LOG_TEMPLATE, exception);
    }
  }

  private MessageType createParquetSchema(String title, List<ColumnInfo> columnInfoList) {
    MessageType schema;
    Types.MessageTypeBuilder schemaBuilder = Types.buildMessage();
    for (ColumnInfo columnInfo : columnInfoList) {
      switch (columnInfo.getDataType()) {
        case BOOLEAN:
          schemaBuilder.addField(
              new PrimitiveType(
                  Type.Repetition.OPTIONAL,
                  PrimitiveType.PrimitiveTypeName.BOOLEAN,
                  columnInfo.getColumn()));
          break;
        case DECIMAL:
          schemaBuilder.addField(
              new PrimitiveType(
                  Type.Repetition.OPTIONAL,
                  PrimitiveType.PrimitiveTypeName.DOUBLE,
                  columnInfo.getColumn()));
          break;
        case NUMBER:
          schemaBuilder.addField(
              new PrimitiveType(
                  Type.Repetition.OPTIONAL,
                  PrimitiveType.PrimitiveTypeName.INT64,
                  columnInfo.getColumn()));
          break;
        case DATE:
          schemaBuilder.addField(
              new PrimitiveType(
                  Type.Repetition.OPTIONAL,
                  PrimitiveType.PrimitiveTypeName.INT32,
                  columnInfo.getColumn(),
                  OriginalType.DATE));
          break;
        case DATETIME:
          schemaBuilder.addField(
              new PrimitiveType(
                  Type.Repetition.OPTIONAL,
                  PrimitiveType.PrimitiveTypeName.INT64,
                  columnInfo.getColumn(),
                  OriginalType.TIMESTAMP_MILLIS));
          break;
        case BLOB:
        case STRING:
        default:
          schemaBuilder.addField(
              new PrimitiveType(
                  Type.Repetition.OPTIONAL,
                  PrimitiveType.PrimitiveTypeName.BINARY,
                  columnInfo.getColumn(),
                  OriginalType.UTF8));
          break;
      }
    }
    schema = schemaBuilder.named(title);
    return schema;
  }

  @Override
  public TextExportHelper append(String text) {
    return this;
  }

  @Override
  public void writeDocumentEnd() throws Exception {
    if (parquetWriter != null) {
      parquetWriter.close();
    }
    if (attachmentFileWriter != null) {
      attachmentFileWriter.flush();
      attachmentFileWriter.close();
    }
    disposeHandler(currentAttachmentFilePath);
  }


  private void disposeHandler(String attachmentFilePath) {
    try {
      File tableFolderDirectory = new File(this.parentFolder);
      String blobTableFolderPath = tableFolderDirectory + File.separator + "blobs" + File.separator;
      File attachmentFile = new File(attachmentFilePath);
      if(attachmentFile.exists()) {
        String individualBlobFolderPath = blobTableFolderPath + File.separator + attachmentFile.getName().replace(".txt", "");
        FileUtil.createDir(individualBlobFolderPath);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(attachmentFile))) {
          String line;
          while ((line = bufferedReader.readLine()) != null) {
            if (StringUtils.isNotEmpty(line)) {
              File sourceFile = FileUtil.createFile(blobTableFolderPath + File.separator + line);
              if (sourceFile.exists()) {
                File targetFile = FileUtil.createFile(individualBlobFolderPath + File.separator + line);
                Files.move(Paths.get(sourceFile.getPath()), Paths.get(targetFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
              }
            }
          }
        }
        FileUtils.deleteQuietly(attachmentFile);
        File attachmentFolder = FileUtil.createFile(individualBlobFolderPath);
        if (Objects.requireNonNull(attachmentFolder.listFiles()).length == 0) {
          FileUtils.deleteQuietly(attachmentFolder);
        }
      }else{
        log.error("Attachment file not found :{}", attachmentFilePath);
      }

    }
    catch (IOException e) {
      log.error(ERROR_LOG_TEMPLATE, e);
    }
  }
  private void disposeHandler() {
    File tableFolderDirectory = new File(this.parentFolder);
    File[] tableFolderDirectoryFiles = new File(tableFolderDirectory.getAbsolutePath()+File.separator+TEMP_FOLDER)
            .listFiles((d, s) -> s.toLowerCase().endsWith(".txt"));
    try {
      if (tableFolderDirectoryFiles != null && tableFolderDirectoryFiles.length > 0) {
        for (File attachmentFile : tableFolderDirectoryFiles) {
          String blobTableFolderPath = tableFolderDirectory + File.separator + "blobs" + File.separator;
          String individualBlobFolderPath = blobTableFolderPath + File.separator + attachmentFile.getName().replace(".txt", "");
          FileUtil.createDir(individualBlobFolderPath);
          try (BufferedReader bufferedReader = new BufferedReader(new FileReader(attachmentFile.getAbsolutePath()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
              if (StringUtils.isNotEmpty(line)) {
                File sourceFile = FileUtil.createFile(blobTableFolderPath + File.separator + line);
                if (sourceFile.exists()){
                  File targetFile = FileUtil.createFile(individualBlobFolderPath + File.separator + line);
                  Files.move(Paths.get(sourceFile.getPath()), Paths.get(targetFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
                }
              }
            }
          }
          File attachmentFolder = FileUtil.createFile(individualBlobFolderPath);
          if (Objects.requireNonNull(attachmentFolder.listFiles()).length == 0) {
            FileUtils.deleteQuietly(attachmentFolder);
          }
        }
        File attachmentBlob = FileUtil.createFile(tableFolderDirectory+File.separator+"blobs");
        if (Objects.requireNonNull(attachmentBlob.listFiles()).length == 0) {
          FileUtils.deleteQuietly(attachmentBlob);
        }
      }
    } catch (IOException e) {
      log.error(ERROR_LOG_TEMPLATE, e);
    }
  }

  @Override
  public void writeDocumentStart() throws Exception {}

  @Override
  public void writeEmptyRow() {}

  @Override
  public void writeRow(List<Object> columnData, List<ExcelSpecificDataType> excelSpecificDataTypes,
                       List<DataType> dataTypes) throws Exception {

  }

  @Override
  public void writeRow(List<Object> currentRow, List<ExcelSpecificDataType> excelSpecificDataTypes,
                       List<ColumnEntity> columnEntities, List<String> attachementList) throws Exception {
    long size = parquetWriter.getDataSize();
    if((options.getRecordsProcessed() > 0 && (options.getRecordsProcessed() % options.getRecordPerFile() == 0))
            || (size >= ((options.getSizePerFile()* 1024 * 1024) - (options.getThresholdSize() * 1024)))){
      log.debug("File Size : {}",size);
      writeDocumentEnd();
      String outputFileTitle = title + "-" + options.getFileCountForTitle();
      options.setOutputFileTitle(outputFileTitle);
      options.incrementFileCount();
      String outputFile = outputFolderPath + File.separator + outputFileTitle + ".parquet";
      parquetWriter = createParquetWriter(outputFileTitle,outputFile);
    }
    filesRecordCount +=currentRow.size();
    if (attachmentFileWriter != null) {
      for (String attachmentFileName : attachementList) {
        attachmentFileWriter.write(attachmentFileName);
        attachmentFileWriter.write("\n");
        attachmentFileWriter.flush();
      }
    }
    parquetWriter.write(processRow(currentRow,columnEntities));
  }

  @Override
  public void cleanUp() {

  }

  private List<Object> processRow(List<Object> currentRow, List<ColumnEntity> columnEntities) {
    List<Object> objectList = new ArrayList<>();
    for (int i = 0; i < currentRow.size(); i++) {
      if (columnEntities.get(i).getType().equals(DataType.BLOB.toString())) {
        String objectValue = currentRow.get(i).toString();
        objectValue = attachmentFileFolderName + "::" + objectValue;
        objectList.add(objectValue);
      }else{
        objectList.add(currentRow.get(i));
      }

    }
    return objectList;
  }

  private ParquetWriter<List<Object>> createParquetWriter(String outputFileTitle,String outputPath) throws IOException {
    this.parentFolder = new File(outputPath).getParentFile().getAbsolutePath();
    this.attachmentFileFolderName = outputFileTitle;
    this.currentAttachmentFilePath = this.parentFolder + File.separator + TEMP_FOLDER + File.separator + outputFileTitle + ".txt";
    this.attachmentFileWriter = new PrintWriter(this.currentAttachmentFilePath);
    filesRecordCount = 0L;
    return new ParquetWriterBuilder(new org.apache.hadoop.fs.Path(outputPath),dataformat)
                    .withConf(hadoopConfig)
                    .withType(parquetSchema)
                    .withCompressionCodec(CompressionCodecName.UNCOMPRESSED)
                    .withDictionaryEncodingSetup()
                    .build();
  }

  @Override
  public void writeRowHeader(List<String> columnNames) throws Exception {}

  @Override
  public void flush() {}

  @Override
  public void close() throws Exception {}




}
