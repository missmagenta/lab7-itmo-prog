package com.magenta.general.commands;

import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.HistoryManagable;
import com.magenta.general.utils.UserDataManagable;
import lombok.ToString;

@ToString
public abstract class UserAbstractCommand extends AbstractCommand {

    protected UserAbstractCommand(String name, String description) {
        super(name, description);
    }

    public abstract CommandResultDto execute(UserDataManagable userDataManager, HistoryManagable historyManager);
}
