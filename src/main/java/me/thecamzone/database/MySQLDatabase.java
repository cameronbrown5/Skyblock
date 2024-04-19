package me.thecamzone.database;

import me.thecamzone.Skyblock;

import java.sql.*;

public class MySQLDatabase {

    private final String url;
    private final String username;
    private final String password;
    private Connection connection;

    public MySQLDatabase(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        connection = DriverManager.getConnection(url, username, password);
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean executeStatement(String... sql) throws SQLException {
        Statement statement = getConnection().createStatement();

        StringBuilder sqlString = new StringBuilder();
        boolean success = statement.execute(String.join(" ", sql));

        statement.close();
        return success;
    }

    public ResultSet executeQueryStatement(String... sql) throws SQLException {
        Statement statement = getConnection().createStatement();

        StringBuilder sqlString = new StringBuilder();
        ResultSet set = statement.executeQuery(String.join(" ", sql));

        statement.close();
        return set;
    }

    public int executeUpdateStatement(String... sql) throws SQLException {
        Statement statement = getConnection().createStatement();

        StringBuilder sqlString = new StringBuilder();
        int status = statement.executeUpdate(String.join(" ", sql));

        statement.close();
        return status;
    }
}