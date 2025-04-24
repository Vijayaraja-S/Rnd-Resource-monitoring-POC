package com.p3.resource_monitor.poc.util;

import com.p3.resource_monitor.poc.beans.ConnectionBean;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import lombok.Getter;

public class JDBCConnection {

  private final ConnectionBean connectionBean;
  private final String connectionType;
  @Getter private Connection connection;

  public JDBCConnection(ConnectionBean connectionBean, String connectionType) throws SQLException {
    this.connectionBean = connectionBean;
    this.connectionType = connectionType;
    this.initConnection();
  }

  private void initConnection() throws SQLException {
    if (Objects.requireNonNull(connectionType).equals("POSTGRES")) {
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
}
