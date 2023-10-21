package com.magenta.general.commands;

import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.HistoryManagable;

public class Remove extends CollectionAbstractCommand implements OwnerAccessable {
    private String arg;

    public Remove() {
        super("remove_key", "удалить элемент из коллекции по его ключу");
    }

    public Remove(String arg) {
        this();
        this.arg = arg;
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        Long longArg;
        try {
            longArg = Long.parseLong((arg));
        } catch (NumberFormatException e) {
            return new CommandResultDto("Your argument was incorrect and cannot be converted.", true);
        }
        collectionManager.remove(longArg);
        return new CommandResultDto("The element with key " + longArg + " was deleted.", true);
    }

    @Override
    public long getDragonId() {
        try {
            return Long.parseLong(arg);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }
}
