package com.magenta.general.commands;

import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.HistoryManagable;

public class RemoveGreaterKey extends CollectionAbstractCommand {
    private String keyArg;

    public RemoveGreaterKey() {
        super("remove_greater_key", "удалить из коллекции все элементы, ключ которых превышает заданный");
    }

    public RemoveGreaterKey(String arg) {
        this();
        this.keyArg = arg;
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        Long key;
        try {
            key = Long.parseLong(keyArg);
        } catch (NumberFormatException e) {
            return new CommandResultDto("Invalid argument. Command was not executed", false);
        }
        collectionManager.removeGreaterKey(key, username);
        return new CommandResultDto("Removed element(s) with keys greater than " + key, true);
    }
}
