package com.p3.resource_monitor.poc.util;

import com.p3.export.options.ColumnInfo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.p3.resource_monitor.poc.beans.ConnectionBean;
import com.p3.resource_monitor.poc.beans.ConnectionType;
import lombok.Getter;

public class JDBCConnection {

  private final ConnectionBean connectionBean;
  private final ConnectionType connectionType;
  @Getter private Connection connection;

  public JDBCConnection(ConnectionBean connectionBean, ConnectionType connectionType)
      throws SQLException {
    this.connectionBean = connectionBean;
    this.connectionType = connectionType;
    this.initConnection();
  }

  private void initConnection() throws SQLException {
    if (Objects.requireNonNull(connectionType) == ConnectionType.POSTGRES) {
      String connectionUrl =
          "jdbc:postgresql://"
              + connectionBean.getHost()
              + ":"
              + connectionBean.getPort()
              + "/"
              + connectionBean.getDatabase()
              + "?user="
              + connectionBean.getUsername()
              + "&password="
              + connectionBean.getPassword();

      connection = DriverManager.getConnection(connectionUrl);
    }
  }

  public void closeConnection() throws SQLException {
    if (connection != null) {
      connection.close();
    }
  }

  public String getSampleSelectQuery(
      String schema, String tableName, List<ColumnInfo> columnInfoList) {
    return getSampleSelectQuery(schema, tableName, columnInfoList, 150000);
  }

  public String getSampleSelectQuery(
      String schema, String tableName, List<ColumnInfo> columnInfoList, int records) {
    if (Objects.requireNonNull(connectionType) == ConnectionType.POSTGRES) {
      String columnList =
          columnInfoList.stream()
              .map(item -> "\"" + item.getColumn() + "\"")
              .collect(Collectors.joining(", "));
      return String.format("SELECT %s FROM \"%s\" LIMIT %d", columnList, tableName, records);
    }
    return "";
  }
}
