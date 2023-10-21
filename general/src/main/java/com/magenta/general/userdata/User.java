package com.magenta.general.userdata;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Comparator;

@Getter
@EqualsAndHashCode
@ToString
public class User implements Comparable<User> {
    @Setter
    private long id;
    private final String username;
    private final String password;

    public User(long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    @Override
    public int compareTo(User user) {
        return Comparator.comparing(User::getId).compare(this, user);
    }
}
