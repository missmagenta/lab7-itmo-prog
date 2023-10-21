package com.magenta.general.commands;

import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.utils.HistoryManagable;
import lombok.ToString;

@ToString
public abstract class CollectionAbstractCommand extends AbstractCommand {

    protected CollectionAbstractCommand(String name, String description) {
        super(name, description);
    }

    public abstract CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager);
}
