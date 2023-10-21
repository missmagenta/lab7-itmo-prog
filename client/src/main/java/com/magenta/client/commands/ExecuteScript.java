package com.magenta.client.commands;

import com.magenta.client.exceptions.FilePermissionException;
import com.magenta.client.exceptions.FileRecursionError;
import com.magenta.client.utils.IOManager;
import com.magenta.general.commands.AbstractCommand;
import com.magenta.general.dto.CommandResultDto;

import java.io.File;
import java.io.FileNotFoundException;

public class ExecuteScript extends AbstractCommand {
    private String arg;

    public ExecuteScript(String arg) {
        this();
        this.arg = arg;
    }

    public ExecuteScript() {
        super("execute_script file_name", "считать и исполнить скрипт из указанного файла. " +
                "В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.");
    }

    public CommandResultDto execute(IOManager ioManager) {
        try {
            ioManager.connectToFile(new File(arg));
            return new CommandResultDto("Starting to execute script", true);
        } catch (FileRecursionError | FilePermissionException | FileNotFoundException e) {
            return new CommandResultDto("Problem with file. " +
                    "Check if it exists, is available for writing to it and does not contatain recursion! " + e.getMessage(),
                    false);
        }
    }
}
