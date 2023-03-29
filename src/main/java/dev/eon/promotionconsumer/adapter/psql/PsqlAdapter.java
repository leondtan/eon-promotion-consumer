package dev.eon.promotionconsumer.adapter.psql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PsqlAdapter {
    private Connection connection;

    public PsqlAdapter(String host, String username, String password) {
        try {
            connection = DriverManager.getConnection(
                    host, username, password
            );
            if (connection != null) {
                System.out.println("Connected to the database!");
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.out.printf("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return this.connection;
    }
}
