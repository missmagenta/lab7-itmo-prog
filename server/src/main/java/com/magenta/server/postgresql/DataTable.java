package com.magenta.server.postgresql;

import com.magenta.general.data.Color;
import com.magenta.general.data.Coordinates;
import com.magenta.general.data.Dragon;
import com.magenta.general.data.DragonCave;
import com.magenta.general.data.DragonCharacter;
import com.magenta.general.data.DragonType;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;

public class DataTable implements Table<Dragon> {
    private static final int NAME_PARAMETER_INDEX = 1;
    private static final int COORDINATES_X_PARAMETER_INDEX = 2;
    private static final int COORDINATES_Y_PARAMETER_INDEX = 3;
    private static final int TIMESTAMP_PARAMETER_INDEX = 4;
    private static final int AGE_PARAMETER_INDEX = 5;
    private static final int COLOR_PARAMETER_INDEX = 6;
    private static final int TYPE_PARAMETER_INDEX = 7;
    private static final int CHARACTER_PARAMETER_INDEX = 8;
    private static final int CAVE_DEPTH_PARAMETER_INDEX = 9;
    private static final int OWNER_NAME_PARAMETER_INDEX = 10;

    private final Connection connection;

    public DataTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create() throws SQLException {
        final String sql = "CREATE TABLE IF NOT EXISTS dragons ("
                + "id serial PRIMARY KEY, "
                + "name varchar(50) NOT NULL, "
                + "coordinates_x real NOT NULL, "
                + "coordinates_y integer NOT NULL, "
                + "creation_date TIMESTAMP NOT NULL, "
                + "age bigint NOT NULL, "
                + "color varchar(50), "
                + "type varchar(50), "
                + "character varchar(50), "
                + "cave_depth double precision, "
                + "owner_username varchar(50) NOT NULL)";
        try (
                Statement statement = connection.createStatement()
        ) {
            statement.execute(sql);
        }
    }

    @Override
    public HashMap<Long, Dragon> getCollection() throws SQLException {
        HashMap<Long, Dragon> dragonCollection = new HashMap<>();

        String sql = "SELECT * FROM dragons";
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                Dragon dragon = mapToObject(resultSet, id);
                dragonCollection.put(id, dragon);
            }
        }
        return dragonCollection;
    }

    @Override
    public Dragon mapToObject(ResultSet resultSet, long id) throws SQLException {
        return Dragon.builder()
                .id(id)
                .name(resultSet.getString("name"))
                .coordinates(new Coordinates(resultSet.getFloat("coordinates_x"), resultSet.getInt("coordinates_y")))
                .creationDate(new Date(resultSet.getTimestamp("creation_date").getTime()))
                .age(resultSet.getLong("age"))
                .color(resultSet.getString("color") != null ? Color.valueOf(resultSet.getString("color")) : null)
                .type(resultSet.getString("type") != null ? DragonType.valueOf(resultSet.getString("type")) : null)
                .character(resultSet.getString("character") != null ? DragonCharacter.valueOf(resultSet.getString("character")) : null)
                .cave(resultSet.wasNull() ? null : new DragonCave(resultSet.getDouble("cave_depth")))
                .ownerUserName(resultSet.getString("owner_username"))
                .build();
    }

    @Override
    public long add(Dragon element) throws SQLException {
        String sql = "INSERT INTO dragons VALUES(default, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
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

    private static void makePreparedStatement(PreparedStatement preparedStatement, Dragon dragon) throws SQLException {
        preparedStatement.setString(NAME_PARAMETER_INDEX, dragon.getName());
        preparedStatement.setFloat(COORDINATES_X_PARAMETER_INDEX, dragon.getCoordinates().getX());
        preparedStatement.setInt(COORDINATES_Y_PARAMETER_INDEX, dragon.getCoordinates().getY());
        preparedStatement.setTimestamp(TIMESTAMP_PARAMETER_INDEX, new Timestamp(dragon.getCreationDate().getTime()));
        preparedStatement.setLong(AGE_PARAMETER_INDEX, dragon.getAge());
        if (dragon.getColor() != null) {
            preparedStatement.setString(COLOR_PARAMETER_INDEX, dragon.getColor().toString());
        } else {
            preparedStatement.setNull(COLOR_PARAMETER_INDEX, Types.VARCHAR);
        }
        if (dragon.getType() != null) {
            preparedStatement.setString(TYPE_PARAMETER_INDEX, dragon.getType().toString());
        } else {
            preparedStatement.setNull(TYPE_PARAMETER_INDEX, Types.VARCHAR);
        }
        if (dragon.getCharacter() != null) {
            preparedStatement.setString(CHARACTER_PARAMETER_INDEX, dragon.getCharacter().toString());
        } else {
            preparedStatement.setNull(CHARACTER_PARAMETER_INDEX, Types.VARCHAR);
        }
        if (dragon.getCave() != null) {
            preparedStatement.setDouble(CAVE_DEPTH_PARAMETER_INDEX, dragon.getCave().getDepth());
        } else {
            preparedStatement.setNull(CAVE_DEPTH_PARAMETER_INDEX, Types.DOUBLE);
        }
        preparedStatement.setString(OWNER_NAME_PARAMETER_INDEX, dragon.getOwnerUserName());
    }

    public void clear(String login) throws SQLException {
        String sql = "DELETE FROM dragons WHERE owner_username = ?";
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setString(1, login);
            preparedStatement.executeUpdate();
        }
    }

    public void updateById(long id, Dragon dragon) throws SQLException {
        String sql = "UPDATE dragons SET "
                + "name=?"
                + ",coordinates_x=?"
                + ",coordinates_y=?"
                + ",creation_date=?"
                + ",age=?"
                + ",color=?"
                + ",type=?"
                + ",character=?"
                + ",cave_depth=?"
                + ",owner_username=? "
                + "WHERE id =" + id;
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            makePreparedStatement(preparedStatement, dragon);
            preparedStatement.executeUpdate();
        }
    }

    public void removeById(long id) throws SQLException {
        String sql = "DELETE FROM dragons WHERE id = " + id;
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.executeUpdate();
        }
    }
}
