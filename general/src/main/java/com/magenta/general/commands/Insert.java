package com.magenta.general.commands;

import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.data.Dragon;
import com.magenta.general.utils.HistoryManagable;

public class Insert extends CollectionAbstractCommand {
    private Dragon arg;

    public Insert() {
        super("insert", "добавить новый элемент");
    }

    public Insert(Dragon arg) {
        this();
        this.arg = arg;
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        Dragon dragon = arg;
        dragon.setId(-1L);
        collectionManager.add(dragon);
        return new CommandResultDto("The element was added", true);
    }
}
