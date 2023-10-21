package com.magenta.general.utils;

import com.magenta.general.userdata.User;

public interface UserDataManagable {
    void add(User user);

    boolean isValidatedUser(String login, String password);

    boolean isUniqueUsername(String username);

    void initializeData();
}
