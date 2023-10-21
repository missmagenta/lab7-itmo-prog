package com.magenta.general.commands;

import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.data.Dragon;
import com.magenta.general.utils.HistoryManagable;

public class RemoveGreaterElement extends CollectionAbstractCommand {
    private Dragon dragon;

    public RemoveGreaterElement() {
        super("remove_greater_element", "удалить из коллекции все элементы, превышающие заданный");
    }

    public RemoveGreaterElement(Dragon arg) {
        this();
        this.dragon = arg;
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        collectionManager.removeGreaterElement(dragon, username);
        return new CommandResultDto("Removed element(s)", true);

    }
}
