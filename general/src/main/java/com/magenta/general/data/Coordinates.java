package com.magenta.general.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
@ToString
public class Coordinates implements Serializable {
    private final Float x; // > -648, not null
    private final Integer y; // not null

    public Coordinates(Float x, Integer y) {
        this.x = x;
        this.y = y;
    }
}
