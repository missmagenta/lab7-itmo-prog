package com.magenta.server.postgresql;

import com.magenta.general.userdata.User;
import com.magenta.general.utils.UserDataManagable;
import com.magenta.server.utils.Encryptor;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserDataManager implements UserDataManagable {
    private final Database database;
    private Map<Long, User> users = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private final Logger logger;

    public UserDataManager(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    @Override
    public void add(User user) {
        Lock writeLock = rwLock.writeLock();
        try {
            writeLock.lock();
            User encryptedUser = new User(user.getId(), user.getUsername(), Encryptor.encryptThisString(user.getPassword()));
            long generatedId = database.getUsersTable().add(encryptedUser);
            encryptedUser.setId(generatedId);
            users.put(generatedId, encryptedUser);
            logger.info("User added.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean isValidatedUser(String login, String password) {
        Lock readLock = rwLock.readLock();
        try {
            readLock.lock();
            return users.values().stream()
                    .anyMatch(x -> x.getUsername().equals(login) && x.getPassword().equals(Encryptor.encryptThisString(password)));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isUniqueUsername(String username) {
        Lock readLock = rwLock.readLock();
        try {
            readLock.lock();
            return users.values().stream().noneMatch(x -> x.getUsername().equals(username));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void initializeData() {
        try {
            this.users = database.getUsersTable().getCollection();
            logger.info("Users collection was initialized successfully.");
        } catch (SQLException e) {
            logger.error("Collection was not initialized. {}", e.getMessage());
        }
    }
}
