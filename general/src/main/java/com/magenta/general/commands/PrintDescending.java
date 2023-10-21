package com.magenta.general.commands;

import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.HistoryManagable;

public class PrintDescending extends CollectionAbstractCommand {

    public PrintDescending() {
        super("print_descending", "вывести элементы коллекции в порядке убывания");
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        return new CommandResultDto(collectionManager.getDescendingOrder(), true);
    }
}
