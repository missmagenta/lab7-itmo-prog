package com.magenta.general.commands;

import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.data.Dragon;
import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.utils.HistoryManagable;

import java.io.IOException;

public class UpdateID extends CollectionAbstractCommand implements OwnerAccessable {
    private String idArg;
    private Dragon dragon;

    public UpdateID(Dragon dragonArg, String id) {
        this();
        this.idArg = id;
        this.dragon = dragonArg;
    }

    public UpdateID() {
        super("update_id", "обновить значение элемента коллекции, id которого равен заданному");
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        long longArg;
        try {
            longArg = Long.parseLong(idArg);
        } catch (NumberFormatException e) {
            return new CommandResultDto("Your argument was incorrect. The command was not executed", true);
        }
        try {
            collectionManager.updateById(longArg, dragon);
        } catch (RuntimeException e) {
            return new CommandResultDto("Element was not updated.", false);
        }
        return new CommandResultDto("Element was updated.", true);
    }

    @Override
    public long getDragonId() {
        try {
            return Long.parseLong(idArg);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }
}
