package com.magenta.general.commands;

import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.HistoryManagable;

public class Info extends CollectionAbstractCommand {

    public Info() {
        super("info", "вывести в стандартный поток вывода информацию о коллекции " +
                "(тип, дата инициализации, количество элементов и т.д.");
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        return new CommandResultDto(collectionManager.info(), true);
    }
}
