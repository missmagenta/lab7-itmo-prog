package com.magenta.general.commands;

import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.HistoryManagable;

public class RemoveByAge extends CollectionAbstractCommand {
    private String arg;

    public RemoveByAge(String arg) {
        this();
        this.arg = arg;
    }

    public RemoveByAge() {
        super("remove_any_by_age age", "удалить из коллекции один элемент, значение поля age которого эквивалентно заданному");
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        long longArg;
        try {
            longArg = Long.parseLong(arg);
        } catch (NumberFormatException e) {
            return new CommandResultDto("Your argument was incorrect and cannot be converted. Try again.", false);
        }
        collectionManager.removeByAge(longArg, username);
        return new CommandResultDto("The element with age value = " + arg + " was removed.", true);
    }
}
