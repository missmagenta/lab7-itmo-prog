package com.magenta.general.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode
@ToString
public class CommandResultDto implements Serializable {
    private final Serializable output;
    private final boolean executedCorrectly;

    public CommandResultDto(Serializable output, boolean executedCorrectly) {
        this.output = output;
        this.executedCorrectly = executedCorrectly;
    }

    public boolean isExecutedCorrectly() {
        return executedCorrectly;
    }

    public Serializable getOutput() {
        return output;
    }
}
