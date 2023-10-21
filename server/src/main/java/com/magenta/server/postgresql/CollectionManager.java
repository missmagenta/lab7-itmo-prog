package com.magenta.server.postgresql;

import com.magenta.general.data.Dragon;
import com.magenta.general.utils.CollectionManagable;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class CollectionManager implements CollectionManagable {
    private final Database database;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    @Getter @Setter
    private Map<Long, Dragon> collection = new HashMap<>();
    @Getter
    private final Date creationDate = new Date();
    private final Logger logger;

    public CollectionManager(Database database, Logger logger) {

        this.database = database;
        this.logger = logger;
    }

    @Override
    public String show() {
        Lock readLock = rwLock.readLock();
        try {
            readLock.lock();
            return collection.values().stream()
                    .sorted(Comparator.comparing(Dragon::getName))
                    .collect(Collectors.toList())
                    .toString();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void add(Dragon dragon) {
        Lock writeLock = rwLock.writeLock();
        try {
            writeLock.lock();
            long generatedId = database.getDataTable().add(dragon);
            dragon.setId(generatedId);
            collection.put(generatedId, dragon);
            logger.info("Added a dragon successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void updateById(long id, Dragon dragon) {
        Lock writeLock = rwLock.writeLock();
        try {
            writeLock.lock();
            database.getDataTable().updateById(id, dragon);
            collection.values().removeIf(x -> x.getId() == id);
            dragon.setId(id);
            collection.put(id, dragon);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public String getDescendingOrder() {
        Lock readLock = rwLock.readLock();
        try {
            readLock.lock();
            return collection.values().stream().sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList()).toString();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear(String login) {
        Lock writeLock = rwLock.writeLock();
        try {
            writeLock.lock();
            database.getDataTable().clear(login);
            collection.values().removeIf(x -> x.getOwnerUserName().equals(login));
        } catch (SQLException e) {
            logger.error("Failed to clear collection. {}", e.getMessage());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public String info() {
        Lock readLock = rwLock.readLock();
        try {
            readLock.lock();
            return "Collection type: " + collection.getClass().getName() + "\n" +
                    "Initialization date: " + creationDate + "\n" +
                    "Collection size: " + collection.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void remove(long id) {
        Lock writeLock = rwLock.writeLock();
        try {
            writeLock.lock();
            database.getDataTable().removeById(id);
            collection.values().removeIf(x -> x.getId() == id);
            logger.info("Dragon was removed. Id = {}", id);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeByAge(long age, String username) {
        Lock writeLock = rwLock.writeLock();
        try {
            writeLock.lock();
            Optional<Map.Entry<Long, Dragon>> dragonToRemove = collection.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().getAge() == age && entry.getValue().getOwnerUserName().equals(username))
                    .findFirst();

            dragonToRemove.ifPresent(entry -> {
                long id = entry.getKey();
                collection.remove(id);
                try {
                    database.getDataTable().removeById(id);
                    logger.info("Dragon with was removed.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeGreaterElement(Dragon dragon, String username) {
        Lock writeLock = rwLock.writeLock();
        try {
            writeLock.lock();
            Iterator<Map.Entry<Long, Dragon>> iterator = collection.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Dragon> entry = iterator.next();
                if (dragon.compareTo(entry.getValue()) < 0 && entry.getValue().getOwnerUserName().equals(username)) {
                    long idToRemove = entry.getKey();
                    iterator.remove();
                    database.getDataTable().removeById(idToRemove);
                    logger.info("Greater elements were deleted.");
                }
            }
        } catch (SQLException e) {
            logger.error("", e.getMessage());
            //e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeGreaterKey(long key, String username) {
        Lock writeLock = rwLock.writeLock();
        try {
            writeLock.lock();
            Iterator<Map.Entry<Long, Dragon>> iterator = collection.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Dragon> entry = iterator.next();
                if (entry.getKey().compareTo(key) > 0 && entry.getValue().getOwnerUserName().equals(username)) {
                    iterator.remove();
                    database.getDataTable().removeById(entry.getKey());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void replaceIfGreater(long id, Dragon newDragon, String username) {
        Lock writeLock = rwLock.writeLock();
        try {
            writeLock.lock();
            if (collection.values().stream().anyMatch(x -> x.getOwnerUserName().equals(username))) {
                collection.computeIfPresent(id, (existingKey, dragon) -> {
                    if (dragon.compareTo(newDragon) < 0) {
                        try {
                            database.getDataTable().updateById(id, newDragon);
                            logger.info("Dragon was replaced.");
                            newDragon.setId(id);
                            return newDragon;
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        logger.info("Dragon was not greater than existing one. Dragon was not replaced.");
                    }
                    return dragon;
                });
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public double getAverageAge() {
        Lock readLock = rwLock.readLock();
        try {
            readLock.lock();
            return collection.values().stream().mapToDouble(Dragon::getAge).average().orElse(0.0);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isOwner(String username, long dragonId) {
        Lock readLock = rwLock.readLock();
        try {
            readLock.lock();
            return collection.values().stream().anyMatch(x -> x.getOwnerUserName().equals(username) && x.getId() == dragonId);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void initializeData() {
        try {
            this.collection = database.getDataTable().getCollection();
            logger.info("Dragon collection was initialized successfully.");
        } catch (SQLException e) {
            logger.error("Dragon collection was not initialized. {}", e.getMessage());
        }
    }
}
