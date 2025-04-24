package com.p3.resource_monitor.poc.Extraction;

import com.p3.resource_monitor.poc.beans.JobInputBean;
import com.p3.resource_monitor.poc.util.FileUtil;
import com.p3.resource_monitor.poc.util.JDBCConnection;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
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

    extractAllTables(jdbcConnection, currentDir);
    jdbcConnection.closeConnection();
  }

  private void extractAllTables(JDBCConnection jdbcConnection, String outputFilePath) {
    try {
      DatabaseMetaData metaData = jdbcConnection.getConnection().getMetaData();
      ResultSet schemas = metaData.getSchemas();

      while (schemas.next()) {
        String schemaName = schemas.getString("TABLE_SCHEM");
        ResultSet tables = metaData.getTables(null, schemaName, "%", new String[] {"TABLE"});

        while (tables.next()) {
          String tableName = tables.getString("TABLE_NAME");
          extractTable(schemaName, tableName, jdbcConnection, outputFilePath);
        }
        tables.close();
      }
      schemas.close();
    } catch (Exception e) {
      log.error("Error extracting tables: {}", e.getMessage(), e);
    }
  }

  private void extractTable(
      String schema, String tableName, JDBCConnection jdbcConnection, String outputFilePath) {

    final int BATCH_SIZE = 10000;
    int offset = 0;
    int fileIndex = 1;

    try {
      DatabaseMetaData metaData = jdbcConnection.getConnection().getMetaData();
      ResultSet columns = metaData.getColumns(null, schema, tableName, null);
      List<String> columnNames = new LinkedList<>();

      while (columns.next()) {
        columnNames.add(columns.getString("COLUMN_NAME"));
      }
      columns.close();

      if (columnNames.isEmpty()) return;

      Connection connection = jdbcConnection.getConnection();

      while (true) {
        String query =
            "SELECT "
                + columnNames.stream()
                    .map(col -> "\"" + col + "\"")
                    .collect(Collectors.joining(", "))
                + " FROM \""
                + schema
                + "\".\""
                + tableName
                + "\""
                + " LIMIT "
                + BATCH_SIZE
                + " OFFSET "
                + offset;

        try (Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query)) {

          if (!resultSet.isBeforeFirst()) break;

          String paddedIndex = String.format("%03d", fileIndex);
          String batchFilePath =
              outputFilePath + File.separator + tableName + "_" + paddedIndex + ".csv";

          log.info("batchFilePath: {}", batchFilePath);

          csvWriter.writeRecord(new ArrayList<>(columnNames), batchFilePath);

          while (resultSet.next()) {
            List<Object> row = new LinkedList<>();
            for (String col : columnNames) {
              row.add(resultSet.getString(col));
            }
             csvWriter.writeRecord(row, batchFilePath);
          }

          offset += BATCH_SIZE;
          fileIndex++;

        } catch (SQLException e) {
          log.error("Error reading data from {}.{}: {}", schema, tableName, e.getMessage(), e);
          break;
        }
      }

    } catch (Exception e) {
      log.error("Error extracting table {}.{}: {}", schema, tableName, e.getMessage(), e);
    }
  }
}
