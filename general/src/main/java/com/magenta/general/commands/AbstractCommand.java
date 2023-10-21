package com.magenta.general.commands;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
@ToString
public abstract class AbstractCommand implements Serializable {
    private final String name;
    private final String description;

    protected AbstractCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
