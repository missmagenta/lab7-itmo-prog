package com.magenta.general.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Comparator;

@Getter
@ToString
@EqualsAndHashCode
@Builder
public class Dragon implements Comparable<Dragon>, Serializable {
    @NotNull
    private long id;
    @NotBlank
    private final String name;
    @NotNull
    private final Coordinates coordinates;
    @NotNull
    private final java.util.Date creationDate;
    @NotEmpty
    private final long age;
    private final Color color;
    private final DragonType type;
    private final DragonCharacter character;
    private final DragonCave cave;
    private String ownerUserName;

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int compareTo(Dragon dragon) {
        return Comparator.comparing(Dragon::getName).
                thenComparing(Dragon::getAge).
                thenComparing(Dragon::getColor, Comparator.nullsLast(Comparator.naturalOrder())).
                thenComparing(Dragon::getType, Comparator.nullsLast(Comparator.naturalOrder())).
                thenComparing(Dragon::getCharacter, Comparator.nullsLast(Comparator.naturalOrder())).
                thenComparing(Dragon::getCave, Comparator.nullsLast(Comparator.naturalOrder())).compare(this, dragon);
    }
}