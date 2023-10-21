package com.magenta.general.dto;

import com.magenta.general.commands.AbstractCommand;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class ClientCommandDto<T extends AbstractCommand> implements Serializable {
    private T command;
    private final String login;
    private final String password;

    public ClientCommandDto(T command, String login, String password) {
        this.command = command;
        this.login = login;
        this.password = password;
    }

    public ClientCommandDto(T command) {
        this.command = command;
        this.login = "";
        this.password = "";
    }

    public T getCommand() {
        return this.command;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }
}
