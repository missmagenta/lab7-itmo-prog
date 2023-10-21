package com.magenta.general.commands;

import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.utils.HistoryManagable;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Help extends CollectionAbstractCommand {
    private final Map<String, String> commandsCollection = new HashMap<>();

    public Help() {
        super("help", "вывести справку по доступным командам");
    }

    @Override
    public CommandResultDto execute(CollectionManagable collectionManager, String username, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        commandsCollection.clear();
        commandsCollection.put("exit", "завершить программу");
        commandsCollection.put("execute_script file_name", "считать и исполнить скрипт из указанного файла. " +
                "В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.");

        try {
            Set<Class<?>> commandClasses1 = getClasses("com.magenta.general.commands", UserAbstractCommand.class);
            Set<Class<?>> commandClasses2 = getClasses("com.magenta.general.commands", CollectionAbstractCommand.class);

            collect(commandClasses1);
            collect(commandClasses2);

            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append("Список команд\n");
            for (Map.Entry<String, String> entry : commandsCollection.entrySet()) {
                resultBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append(("\n"));
            }

            String result = resultBuilder.toString();
            if (!result.isEmpty()) {
                return new CommandResultDto(result, true);
            }
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            return new CommandResultDto("Problem with searching and collecting commands." + e.getMessage(), false);
        }

        return new CommandResultDto("No commands found", true);
    }

    private void collect(Set<Class<?>> packageCommand) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException {
        for (Class<?> commandClass : packageCommand) {
            Object commandInstance = commandClass.getDeclaredConstructor().newInstance();

            Method getNameMethod = commandClass.getMethod("getName");
            String name = (String) getNameMethod.invoke(commandInstance);

            Method getDescriptionMethod = commandClass.getMethod("getDescription");
            String description = (String) getDescriptionMethod.invoke(commandInstance);

            commandsCollection.put(name, description);
        }
    }

    public static Set<Class<?>> getClasses(String packageName, Class<?> clazz1) {
        Reflections reflections = new Reflections(packageName);

        return new HashSet<>(reflections.getSubTypesOf(clazz1));
    }
}