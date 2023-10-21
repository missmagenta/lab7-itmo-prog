package com.magenta.general.commands;

import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.HistoryManagable;

public class Clear extends CollectionAbstractCommand {
    public Clear() {
        super("clear", "очистить коллекцию");
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote((this.getName()));
        collectionManager.clear(username);
        return new CommandResultDto("Collection you owned was cleared.", true);
    }
}
