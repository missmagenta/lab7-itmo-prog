package com.magenta.server.postgresql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public interface Table<T> {
    void create() throws SQLException;

    HashMap<Long, T> getCollection() throws SQLException;

    T mapToObject(ResultSet resultSet, long id) throws SQLException;

    long add(T element) throws SQLException;
}
