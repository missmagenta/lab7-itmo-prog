package com.magenta.general.commands;

import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.HistoryManagable;
import com.magenta.general.utils.UserDataManagable;

public class History extends UserAbstractCommand {
    public History() {
        super("history", "посмотреть последние 10 команд");
    }

    @Override
    public CommandResultDto execute(UserDataManagable userDataManager, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        return new CommandResultDto(historyManager.niceToString(), true);
    }
}
