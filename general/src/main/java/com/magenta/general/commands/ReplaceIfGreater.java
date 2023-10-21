package com.magenta.general.commands;

import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.data.Dragon;
import com.magenta.general.utils.HistoryManagable;

public class ReplaceIfGreater extends CollectionAbstractCommand implements OwnerAccessable {
    private String keyArg;
    private Dragon dragon;

    public ReplaceIfGreater() {
        super("replace_if_greater", "заменить значение по ключу, если новое значение больше старого");
    }

    public ReplaceIfGreater(Dragon dragonArg, String keyArg) {
        this();
        this.keyArg = keyArg;
        this.dragon = dragonArg;
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        Long longKey;
        try {
            longKey = Long.parseLong(keyArg);
        } catch (NumberFormatException e) {
            return new CommandResultDto("Your argument was incorrect and cannot be converted.", false);
        }
        collectionManager.replaceIfGreater(longKey, dragon, username);
        return new CommandResultDto("The element was replaced successfully.", true);
    }

    @Override
    public long getDragonId() {
        try {
            return Long.parseLong(keyArg);
        } catch (NumberFormatException e) {
            return -1L;
        }

    }
}
