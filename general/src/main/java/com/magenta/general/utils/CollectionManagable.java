package com.magenta.general.utils;

import com.magenta.general.data.Dragon;

import java.util.Date;

public interface CollectionManagable {

    void add(Dragon dragon);

    void updateById(long id, Dragon dragon);

    String show();

    String getDescendingOrder();

    void clear(String login);

    String info();

    void remove(long key);

    void removeByAge(long age, String username);

    void removeGreaterElement(Dragon dragon, String username);

    void removeGreaterKey(long key, String username);

    void replaceIfGreater(long key, Dragon dragon, String username);

    double getAverageAge();

    Date getCreationDate();

    boolean isOwner(String username, long dragonId);

    void initializeData();
}
