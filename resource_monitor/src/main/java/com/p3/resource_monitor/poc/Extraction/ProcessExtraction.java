package com.p3.resource_monitor.poc.Extraction;

import com.p3.resource_monitor.poc.beans.ColumnInfo;
import com.p3.resource_monitor.poc.beans.DataType;
import com.p3.resource_monitor.poc.beans.JobInputBean;
import com.p3.resource_monitor.poc.util.FileUtil;
import com.p3.resource_monitor.poc.util.JDBCConnection;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProcessExtraction {
  private final CsvWriterStrategy csvWriter = new SimpleCsvWriter();

  public void extraction(JobInputBean inputBean) throws Exception {
    JDBCConnection jdbcConnection =
            new JDBCConnection(inputBean.getConnection(), inputBean.getConnectionType());
    String outputDir = inputBean.getOutputDir();
    String currentDir = outputDir + File.separator + System.currentTimeMillis();
    FileUtil.checkCreateDirectory(currentDir);

     exportDataStarting(jdbcConnection, currentDir);

     end(jdbcConnection);
  }

  public void exportDataStarting(JDBCConnection jdbcConnection, String currentDir) throws Exception {
    Connection connection = jdbcConnection.getConnection();
    DatabaseMetaData metaData = connection.getMetaData();
    exportSchemasToCsv(metaData, currentDir, jdbcConnection);
  }

  private void exportSchemasToCsv(
          DatabaseMetaData metaData, String outputFilePath, JDBCConnection jdbcConnection) {

    log.info("Starting export of schemas...");
    try {
      ResultSet schemas = metaData.getSchemas();
      int count = 0;

      // Write headers to data.csv
      writeHeader(outputFilePath);

      while (schemas.next()) {
        String schemaName = schemas.getString("TABLE_SCHEM");

        // Export schema name as the first row
        List<Object> data = new LinkedList<>();
        data.add(schemaName);
        data.add("N/A"); // Placeholder for table name
        data.add("N/A"); // Placeholder for column name
        data.add("N/A"); // Placeholder for data type
        data.add("N/A"); // Placeholder for value
        csvWriter.writeRecord(data, outputFilePath + File.separator + "data.csv");

        exportTablesToCsv(metaData, schemaName, outputFilePath, jdbcConnection);
        count++;
      }
      schemas.close();
      log.info("Exported {} schemas to {}", count, outputFilePath);
    } catch (Exception e) {
      log.error("Error exporting schemas: {}", e.getMessage(), e);
    }
  }

  // Export tables to CSV file
  private void exportTablesToCsv(
          DatabaseMetaData metaData,
          String schema,
          String outputFilePath,
          JDBCConnection jdbcConnection)
          throws Exception {

    log.info("Starting export of tables...");
    try {
      ResultSet tables = metaData.getTables(null, schema, "%", null);
      int count = 0;

      while (tables.next()) {
        String tableName = tables.getString("TABLE_NAME");

        List<Object> data = new LinkedList<>();
        data.add(schema);
        data.add(tableName);
        data.add(tables.getString("TABLE_TYPE"));
        data.add("N/A"); // Placeholder for column name
        data.add("N/A"); // Placeholder for data type
        data.add("N/A"); // Placeholder for value
        csvWriter.writeRecord(data, outputFilePath + File.separator + "data.csv");

        exportColumnsToCsv(metaData, schema, tableName, outputFilePath, jdbcConnection);
        count++;
      }
      tables.close();
      log.info("Exported {} tables to {}", count, outputFilePath);
    } catch (SQLException | IOException e) {
      log.error("Error exporting tables: {}", e.getMessage(), e);
    }
  }

  // Export column metadata to CSV
  private void exportColumnsToCsv(
          DatabaseMetaData metaData,
          String schema,
          String tableName,
          String outputFilePath,
          JDBCConnection jdbcConnection)
          throws Exception {

    log.info("Starting export of columns for table: {}.{}", schema, tableName);

    try {
      ResultSet columns = metaData.getColumns(null, schema, tableName, null);
      int count = 0;
      List<ColumnInfo> columnInfoList = new LinkedList<>();

      while (columns.next()) {
        String columnName = columns.getString("COLUMN_NAME");
        columnInfoList.add(
                ColumnInfo.builder().column(columnName).dataType(DataType.STRING).build());

        // Export column metadata to CSV
        List<Object> data = new LinkedList<>();
        data.add(schema);
        data.add(tableName);
        data.add(columnName);
        data.add(columns.getInt("DATA_TYPE"));
        data.add(columns.getString("TYPE_NAME"));
        data.add(columns.getInt("COLUMN_SIZE"));
        data.add(columns.getString("IS_NULLABLE"));
        csvWriter.writeRecord(data, outputFilePath + File.separator + "data.csv");
        count++;
      }
      columns.close();
      log.info("Exported {} columns from {}.{} to {}", count, schema, tableName, outputFilePath);

      // Export data for the table
      exportData(schema, tableName, columnInfoList, jdbcConnection, outputFilePath);

    } catch (SQLException | IOException e) {
      log.error("Error exporting columns from {}.{}: {}", schema, tableName, e.getMessage(), e);
    }
  }

  // Export data from the table to CSV
  private void exportData(
          String schema,
          String tableName,
          List<ColumnInfo> columnInfoList,
          JDBCConnection jdbcConnection,
          String outputFilePath)
          throws Exception {

    try {
      String sampleSelectQuery = jdbcConnection.getSampleSelectQuery(schema, tableName, columnInfoList);
      Connection connection = jdbcConnection.getConnection();
      connection.setAutoCommit(false);
      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery(sampleSelectQuery);

      while (resultSet.next()) {
        for (ColumnInfo columnInfo : columnInfoList) {
          List<Object> row = new LinkedList<>();
          row.add(schema);
          row.add(tableName);
          row.add(columnInfo.getColumn());
          row.add(columnInfo.getDataType().toString()); // assuming DataType is an enum
          row.add(resultSet.getString(columnInfo.getColumn())); // actual value
          csvWriter.writeRecord(row, outputFilePath + File.separator + "data.csv");
        }
      }
    } catch (Exception e) {
      log.error("Error exporting data from {}.{}: {}", schema, tableName, e.getMessage(), e);
    }
  }

  // Write header to the CSV file (single file)
  private void writeHeader(String outputFilePath) throws IOException {
    String dataFilePath = outputFilePath + File.separator + "data.csv";
    List<Object> header = List.of("Schema", "Table", "Column", "Data Type", "Value");
    csvWriter.writeRecord(header, dataFilePath);
  }

  // Close the JDBC connection
  public void end(JDBCConnection jdbcConnection) throws SQLException {
    jdbcConnection.closeConnection();
  }
}
