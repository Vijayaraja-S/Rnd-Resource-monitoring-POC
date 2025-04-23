package com.p3.resource_monitor.poc.Extraction;

import com.p3.export.formatter.ExportFormat;
import com.p3.export.operation.ExportEngine;
import com.p3.export.options.ColumnInfo;
import com.p3.export.specifics.DataType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import com.p3.export.utility.others.FileUtil;
import com.p3.resource_monitor.poc.beans.JobInputBean;
import com.p3.resource_monitor.poc.util.JDBCConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProcessExtraction {
  private ExportEngine schemaExportEngine;
  private ExportEngine tableExportEngine;
  private ExportEngine columnExportEngine;

  public void extraction(JobInputBean inputBean) throws Exception {
    JDBCConnection jdbcConnection =
        new JDBCConnection(inputBean.getConnection(), inputBean.getConnectionType());
    String outputDir = inputBean.getOutputDir();
    String currentDir = outputDir + File.separator + System.currentTimeMillis();
    FileUtil.checkCreateDirectory(currentDir);
    exportMetadata(jdbcConnection, currentDir);
    end(jdbcConnection);
  }

  public void exportMetadata(JDBCConnection jdbcConnection, String currentDir) throws Exception {
    Connection connection = jdbcConnection.getConnection();
    DatabaseMetaData metaData = connection.getMetaData();
    exportSchemasToCsv(metaData, currentDir, jdbcConnection);
    if (schemaExportEngine != null) {
      schemaExportEngine.handleDataEnd();
      schemaExportEngine.generateReport();
    }
    if (tableExportEngine != null) {
      tableExportEngine.handleDataEnd();
      tableExportEngine.generateReport();
    }
    if (columnExportEngine != null) {
      columnExportEngine.handleDataEnd();
      columnExportEngine.generateReport();
    }
  }

  private void exportSchemasToCsv(
      DatabaseMetaData metaData, String outputFilePath, JDBCConnection jdbcConnection)
      throws Exception {
    log.info("Starting export of schemas...");
    try {
      schemaExportEngine = getSchemaExportEngine(outputFilePath);
      ResultSet schemas = metaData.getSchemas();
      int count = 0;
      while (schemas.next()) {
        String schemaName = schemas.getString("TABLE_SCHEM");
        List<Object> data = new LinkedList<>();
        data.add(schemaName);
        writeRecords(data, schemaExportEngine);
        exportTablesToCsv(metaData, schemaName, outputFilePath, jdbcConnection);
        count++;
      }
      schemas.close();
      log.info("Exported {} schemas to {}", count, outputFilePath);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (schemaExportEngine != null) {
        schemaExportEngine.handleDataEnd();
        schemaExportEngine.generateReport();
        schemaExportEngine = null;
      }
    }
  }

  private void writeRecords(List<Object> data, ExportEngine exportEngine) throws Exception {
    exportEngine.iterateRows(data);
  }

  private ExportEngine getSchemaExportEngine(String currentDir) throws Exception {
    ExportEngine ee;
    ee =
        ExportEngine.builder()
            .basePath(currentDir)
            .exportFormat(ExportFormat.csv)
            .title("SCHEMA")
            .columnsInfo(
                List.of(
                    ColumnInfo.builder().column("SCHEMA_NAME").dataType(DataType.STRING).build()))
            .recordPerFile(10000L)
            .sizePerFile(50L)
            .fileEncoding(StandardCharsets.UTF_8.name())
            .build();
    ee.initialize();
    ee.handleDataStart();
    return ee;
  }

  private void exportTablesToCsv(
      DatabaseMetaData metaData,
      String schema,
      String outputFilePath,
      JDBCConnection jdbcConnection)
      throws Exception {
    log.info("Starting export of tables...");
    if (tableExportEngine == null) {
      tableExportEngine = getTableExportEngine(outputFilePath);
    }
    try {
      ResultSet tables = metaData.getTables(null, schema, "%", null);
      int count = 0;
      while (tables.next()) {
        String tableName = tables.getString("TABLE_NAME");
        List<Object> data = new LinkedList<>();
        data.add(tables.getString("TABLE_SCHEM"));
        data.add(tableName);
        data.add(tables.getString("TABLE_TYPE"));
        writeRecords(data, tableExportEngine);
        exportColumnsToCsv(metaData, schema, tableName, outputFilePath, jdbcConnection);
        count++;
      }
      tables.close();
      log.info("Exported {} tables to {}", count, outputFilePath);
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    }
  }

  private ExportEngine getTableExportEngine(String outputFilePath) throws Exception {
    ExportEngine ee;
    ee =
        ExportEngine.builder()
            .basePath(outputFilePath)
            .exportFormat(ExportFormat.csv)
            .title("TABLE")
            .columnsInfo(
                List.of(
                    ColumnInfo.builder().column("TABLE_SCHEMA").dataType(DataType.STRING).build(),
                    ColumnInfo.builder().column("TABLE_NAME").dataType(DataType.STRING).build(),
                    ColumnInfo.builder().column("TABLE_TYPE").dataType(DataType.STRING).build()))
            .recordPerFile(10000L)
            .sizePerFile(50L)
            .fileEncoding(StandardCharsets.UTF_8.name())
            .build();
    ee.initialize();
    ee.handleDataStart();
    return ee;
  }

  private void exportColumnsToCsv(
      DatabaseMetaData metaData,
      String schema,
      String tableName,
      String outputFilePath,
      JDBCConnection jdbcConnection)
      throws Exception {
    log.info("Starting export of columns for table: {}.{}", schema, tableName);
    if (columnExportEngine == null) {
      columnExportEngine = getColumnExportEngine(outputFilePath);
    }
    try {
      ResultSet columns = metaData.getColumns(null, schema, tableName, null);
      int count = 0;

      List<ColumnInfo> columnInfoList = new LinkedList<>();

      while (columns.next()) {
        String columnName = columns.getString("COLUMN_NAME");
        columnInfoList.add(
            ColumnInfo.builder().column(columnName).dataType(DataType.STRING).build());

        List<Object> data = new LinkedList<>();
        data.add(columns.getString("TABLE_SCHEM"));
        data.add(columns.getString("TABLE_NAME"));
        data.add(columnName);
        data.add(columns.getInt("DATA_TYPE"));
        data.add(columns.getString("TYPE_NAME"));
        data.add(columns.getInt("COLUMN_SIZE"));
        data.add(columns.getString("IS_NULLABLE"));
        writeRecords(data, columnExportEngine);
        count++;
      }
      columns.close();
      log.info("Exported {} columns from {}.{} to {}", count, schema, tableName, outputFilePath);

      exportData(schema, tableName, columnInfoList, jdbcConnection, outputFilePath);

    } catch (SQLException | IOException e) {
      log.error("Error exporting columns from {}.{}: {}", schema, tableName, e.getMessage(), e);
    }
  }

  private void exportData(
      String schema,
      String tableName,
      List<ColumnInfo> columnInfoList,
      JDBCConnection jdbcConnection,
      String outputFilePath)
      throws Exception {
    ExportEngine dataExportEngine = getDataExportEngine(tableName, columnInfoList, outputFilePath);
    try {
      String sampleSelectQuery =
          jdbcConnection.getSampleSelectQuery(schema, tableName, columnInfoList);
      Connection connection = jdbcConnection.getConnection();
      connection.setAutoCommit(false);
      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery(sampleSelectQuery);
      while (resultSet.next()) {
        List<Object> data = new LinkedList<>();
        for (ColumnInfo columnInfo : columnInfoList) {
          data.add(resultSet.getString(columnInfo.getColumn()));
        }
        writeRecords(data, dataExportEngine);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      dataExportEngine.handleDataEnd();
      dataExportEngine.generateReport();
      deleteFolderIfEmpty(tableName, outputFilePath);
    }
  }

  private void deleteFolderIfEmpty(String tableName, String outputFilePath) throws Exception {
    String tableDir = outputFilePath + File.separator + "DATA" + File.separator + tableName;
    if (Files.isDirectory(Path.of(tableDir)) && isDirectoryEmpty(Path.of(tableDir))) {
      Files.delete(Path.of(tableDir));
      log.info("Deleted table data directory {}", tableName);
    }
  }

  private boolean isDirectoryEmpty(Path directory) throws IOException {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
      return !stream.iterator().hasNext(); // If no files are found, the directory is empty
    }
  }

  private ExportEngine getColumnExportEngine(String outputFilePath) throws Exception {
    ExportEngine ee;
    ee =
        ExportEngine.builder()
            .basePath(outputFilePath)
            .exportFormat(ExportFormat.csv)
            .title("COLUMN")
            .columnsInfo(
                List.of(
                    ColumnInfo.builder().column("TABLE_SCHEMA").dataType(DataType.STRING).build(),
                    ColumnInfo.builder().column("TABLE_NAME").dataType(DataType.STRING).build(),
                    ColumnInfo.builder().column("COLUMN_NAME").dataType(DataType.STRING).build(),
                    ColumnInfo.builder().column("DATA_TYPE").dataType(DataType.STRING).build(),
                    ColumnInfo.builder().column("TYPE_NAME").dataType(DataType.STRING).build(),
                    ColumnInfo.builder().column("COLUMN_SIZE").dataType(DataType.STRING).build(),
                    ColumnInfo.builder().column("IS_NULLABLE").dataType(DataType.STRING).build()))
            .recordPerFile(10000L)
            .sizePerFile(50L)
            .fileEncoding(StandardCharsets.UTF_8.name())
            .build();
    ee.initialize();
    ee.handleDataStart();
    return ee;
  }

  private ExportEngine getDataExportEngine(
      String tableName, List<ColumnInfo> columnInfoList, String outputFilePath) throws Exception {
    ExportEngine ee;
    String dataDir = outputFilePath + File.separator + "DATA";
    FileUtil.checkCreateDirectory(dataDir);
    String tableDataDir = dataDir + File.separator + tableName;
    FileUtil.checkCreateDirectory(tableDataDir);
    ee =
        ExportEngine.builder()
            .basePath(tableDataDir)
            .exportFormat(ExportFormat.csv)
            .title(tableName)
            .columnsInfo(columnInfoList)
            .recordPerFile(10000L)
            .sizePerFile(50L)
            .fileEncoding(StandardCharsets.UTF_8.name())
            .build();
    ee.initialize();
    ee.handleDataStart();
    return ee;
  }

  public void end(JDBCConnection jdbcConnection) throws SQLException {
    jdbcConnection.closeConnection();
  }
}
