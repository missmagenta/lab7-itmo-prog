package com.magenta.server.postgresql;

import com.magenta.general.userdata.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class UsersTable implements Table<User> {
    private final Connection connection;

    public UsersTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id serial PRIMARY KEY, "
                + "login varchar(50) NOT NULL, "
                + "password varchar(50) NOT NULL)";
        try (
                Statement statement = connection.createStatement()
        ) {
            statement.execute(sql);
        }
    }

    @Override
    public HashMap<Long, User> getCollection() throws SQLException {
        HashMap<Long, User> userCollection = new HashMap<>();

        String sql = "SELECT * FROM users";
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                User user = mapToObject(resultSet, id);
                userCollection.put(id, user);
            }
        }
        return userCollection;
    }

    @Override
    public User mapToObject(ResultSet resultSet, long id) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("login"),
                resultSet.getString("password"));
    }

    @Override
    public long add(User element) throws SQLException {
        String sql = "INSERT INTO users VALUES(default, ?, ?) RETURNING id";
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            makePreparedStatement(preparedStatement, element);
            try (
                    ResultSet resultSet = preparedStatement.executeQuery()
            ) {
                resultSet.next();
                return resultSet.getLong("id");
            }
        }
    }

    private static void makePreparedStatement(PreparedStatement preparedStatement, User user) throws SQLException {
        preparedStatement.setString(1, user.getUsername());
        preparedStatement.setString(2, user.getPassword());
    }
}
