package com.magenta.server.postgresql;

import lombok.Getter;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

@Getter
public class Database {
    private final DataTable dataTable;
    private final UsersTable usersTable;
    private final Logger logger;

    public Database(Connection connection, Logger logger) {
        this.dataTable = new DataTable(connection);
        this.usersTable = new UsersTable(connection);
        this.logger = logger;

        try {
            initTables();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    private void initTables() throws SQLException {
        dataTable.create();
        usersTable.create();
    }
}
