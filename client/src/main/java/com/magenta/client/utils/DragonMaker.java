package com.magenta.client.utils;

import com.magenta.general.data.Color;
import com.magenta.general.data.Coordinates;
import com.magenta.general.data.Dragon;
import com.magenta.general.data.DragonCave;
import com.magenta.general.data.DragonCharacter;
import com.magenta.general.data.DragonType;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class DragonMaker {
    private static final String ERROR_MESSAGE = "Your input was incorrect. Try again.";
    private final Asker asker;
    private final IOManager ioManager;
    private final String ownerName;

    public DragonMaker(IOManager ioManager, String ownerName) {
        this.ioManager = ioManager;
        this.asker = new Asker(ioManager);
        this.ownerName = ownerName;
    }

    public static class Asker {
        private final IOManager ioManager;

        public Asker(IOManager ioManager) {
            this.ioManager = ioManager;
        }

        public <T> T ask(String question,
                         Predicate<? super T> validator,
                         Function<String, ? extends T> parser,
                         String errorMessage,
                         String invalidValueMessage,
                         boolean nullable) throws IOException {
            ioManager.println(question);
            String input;
            T value;
            do {
                try {
                    input = ioManager.readLine();
                    if ("".equals(input) && nullable) {
                        return null;
                    }
                    value = parser.apply(input);
                } catch (IllegalArgumentException e) {
                    ioManager.println(errorMessage);
                    continue;
                }
                if (validator.test(value)) {
                    return value;
                } else {
                    ioManager.println(invalidValueMessage);
                }
            } while (true); // никогда не использовать while true
        }
    }

    public Dragon makeDragon() throws IOException {
        ioManager.println("Enter information about Dragon.");
        String name = asker.ask(
                "Enter name.",
                arg -> !arg.isEmpty(),
                String::valueOf,
                ERROR_MESSAGE,
                "Name cannot be empty. Try again.",
                false);

        Coordinates coordinates = askForCoordinates();

        long age = asker.ask(
                "Enter age.",
                arg -> arg > 0L,
                Long::parseLong,
                ERROR_MESSAGE,
                "Age must be greater than 0. Try again.",
                false);

        Color color = asker.ask(
                "Enter dragon color. Available options: RED, BLACK, ORANGE, BROWN. " +
                        "Or you can leave an empty input (the value will be set to null).",
                arg -> true,
                arg -> Color.valueOf(arg.toUpperCase()),
                ERROR_MESSAGE,
                "Invalid color. Try again. Available options: RED, BLACK, ORANGE, BROWN. Or null.",
                true);

        DragonType type = asker.ask(
                "Enter dragon type. Available options: WATER, UNDERGROUND, AIR. " +
                        "Or you can leave an empty input (the value will be set to null).",
                arg -> true,
                arg -> DragonType.valueOf(arg.toUpperCase()),
                ERROR_MESSAGE,
                "Invalid type. Try again. Available options: WATER, UNDERGROUND, AIR. Or null.",
                true);

        DragonCharacter character = asker.ask(
                "Enter dragon character. Available options: CUNNING, WISE, EVIL, GOOD, FICKLE. " +
                        "Or you can leave an empty input (the value will be set to null).",
                arg -> true,
                arg -> DragonCharacter.valueOf(arg.toUpperCase()),
                ERROR_MESSAGE,
                "Invalid character. Try again. Available options: CUNNING, WISE, EVIL, GOOD, FICKLE. Or null.",
                true);

        DragonCave cave = askForDragonCave();

        return   Dragon.builder()
                .name(name)
                .coordinates(coordinates)
                .creationDate(new Date())
                .age(age)
                .color(color)
                .type(type)
                .character(character)
                .cave(cave)
                .ownerUserName(ownerName)
                .build();
    }

    public Coordinates askForCoordinates() throws IOException {
        ioManager.println("Enter coordinates.");
        final Float LIMIT_X = -648.0f;

        Float x = asker.ask(
                "Enter X coordinate. It must be greater than -648.",
                arg -> arg > LIMIT_X,
                Float::parseFloat,
                ERROR_MESSAGE,
                "X coordinate must be greater than -648. Try again.",
                false);

        Integer y = asker.ask(
                "Enter Y coordinate. Cannot be empty.",
                Objects::nonNull,
                Integer::valueOf,
                ERROR_MESSAGE,
                "Y coordinate cannot be null. Try again.",
                false);
        return new Coordinates(x, y);
    }

    public DragonCave askForDragonCave() throws IOException {
        Double depth = asker.ask(
                "Enter cave depth. " +
                        "Or you can leave an empty input (the value will be set to null).",
                arg -> true,
                Double::parseDouble,
                ERROR_MESSAGE,
                ERROR_MESSAGE,
                true);

        if (depth == null) {
            depth = 0.0;
        }
        return new DragonCave(depth);
    }
}

