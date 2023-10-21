package com.magenta.general.commands;

import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.utils.HistoryManagable;


public class AverageAge extends CollectionAbstractCommand {

    public AverageAge() {
        super("average_of_age", "вывести среднее значение поля age для всех элементов коллекции");
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        return new CommandResultDto("Average age: " + collectionManager.getAverageAge(), true);
    }
}
