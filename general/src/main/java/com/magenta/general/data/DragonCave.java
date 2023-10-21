package com.magenta.general.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Comparator;

@Getter
@ToString
@EqualsAndHashCode
public class DragonCave implements Comparable<DragonCave>, Serializable {
    private final double depth;

    public DragonCave(double depth) {
        this.depth = depth;
    }

    @Override
    public int compareTo(DragonCave cave) {
        return Comparator.comparing(DragonCave::getDepth).compare(this, cave);
    }
}
