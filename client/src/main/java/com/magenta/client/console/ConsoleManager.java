package com.magenta.client.console;

import com.magenta.client.commands.ExecuteScript;
import com.magenta.client.utils.ConnectiontManager;
import com.magenta.client.utils.DragonMaker;
import com.magenta.client.utils.IOManager;
import com.magenta.general.commands.*;
import com.magenta.general.data.Dragon;
import com.magenta.general.dto.ClientCommandDto;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.DataCantBeSentException;


import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.UnresolvedAddressException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class ConsoleManager implements ConsoleInterface {
    private static final Pattern COMPILE = Pattern.compile("\\s+");
    private final IOManager ioManager;
    private final List<String> commandList;
    private final ConnectiontManager connectiontManager;
    private String username;
    private String password;

    public ConsoleManager(IOManager ioManager,
                          List<String> commandList,
                          ConnectiontManager connectiontManager) {
        this.ioManager = ioManager;
        this.commandList = commandList;
        this.connectiontManager = connectiontManager;
    }

    public void start() throws IOException, TimeoutException, ClassNotFoundException, DataCantBeSentException {
        ioManager.println("You are in interactive mode now\n");
        getLoginAndPassword();
        DragonMaker dragonMaker = new DragonMaker(ioManager, username);
        String input;
        do {
            input = readNext();
            if ("exit".equals(input)) {
                break;
            }
            String[] parsedInput = parseArgs(input);
            String commandName = parsedInput[0];
            Serializable commandArg = parsedInput[1];
            String commandArg2 = "";

            if (commandList.contains(commandName)) {
                if ("insert".equals(commandName) || "remove_greater_element".equals(commandName)) {
                    commandArg = dragonMaker.makeDragon();
                }
                if ("update_id".equals(commandName) || "replace_if_greater".equals(commandName)) {
                    commandArg2 = (String) commandArg;
                    commandArg = dragonMaker.makeDragon();
                }
                if ("execute_script".equals(commandName)) {
                    new ExecuteScript((String) commandArg).execute(ioManager);
                } else {
                    try {
                        ioManager.println(connectiontManager.sendMessage(
                                new ClientCommandDto<>(getCommandByName(commandName, commandArg, commandArg2), username, password))
                                .getOutput().toString());
                    } catch (DataCantBeSentException e) {
                        ioManager.println("Failed to send a message to server.");
                    }
                }
            } else {
                ioManager.println("Command not found. Type 'help' to see available commands.");
            }
        } while (true);
    }

    @Override
    public String readNext() throws IOException {
        ioManager.println(">>>");
        try {
            return ioManager.readLine();
        } catch (NoSuchElementException e) {
            return "exit";
        }
    }

    private void getLoginAndPassword() throws IOException, UnresolvedAddressException, DataCantBeSentException {
        ioManager.println("If you are new, type 'yes' to sign up. " +
                "If you have signed up before, type anything else to enter your username and password.");
        String answer = ioManager.readLine().trim();
        if ("yes".equals(answer)) {
            ioManager.println("Enter your new username.");
            String loginToSignUp = ioManager.readLine();
            ioManager.println("Enter your new password");
            String passwordToSignUp = ioManager.readLine();

            CommandResultDto registerCommandResult = connectiontManager.sendMessage(
                    new ClientCommandDto<UserAbstractCommand>(new SignUp(new String[]{loginToSignUp, passwordToSignUp})));
            if (registerCommandResult.isExecutedCorrectly()) {
                if (!((SignUp.RegisterCommandResult) registerCommandResult).isWasRegistered()) {
                    ioManager.println("User was not registered.");
                    getLoginAndPassword();
                } else {
                    password = passwordToSignUp;
                    username = loginToSignUp;
                }
            } else {
                ioManager.println("Registration was not executed correctly. User was not registered.");
                throw new DataCantBeSentException();
            }
        } else {
            ioManager.println("Enter username.");
            username = ioManager.readLine();
            ioManager.println("Enter password");
            password = ioManager.readLine();
        }
    }

    @Override
    public String[] parseArgs(String input) {
        String[] inputParts = COMPILE.split(input);
        String commandName = inputParts[0].toLowerCase();
        String commandArg = "";
        if (inputParts.length >= 2) {
            commandArg = inputParts[1];
        }

        return new String[]{commandName, commandArg};
    }

    private AbstractCommand getCommandByName(String commandName, Serializable arg1, String arg2) throws DataCantBeSentException, IOException {
        AbstractCommand command;
        switch (commandName) {
            case "info":
                command = new Info();
                break;
            case "show":
                command = new Show();
                break;
            case "insert":
                command = new Insert((Dragon) arg1);
                break;
            case "update_id":
                command = new UpdateID((Dragon) arg1, arg2);
                break;
            case "remove_greater_key":
                command = new RemoveGreaterKey((String) arg1);
                break;
            case "clear":
                command = new Clear();
                break;
            case "remove_key":
                command = new Remove((String) arg1);
                break;
            case "remove_greater_element":
                command = new RemoveGreaterElement((Dragon) arg1);
                break;
            case "replace_if_greater":
                command = new ReplaceIfGreater((Dragon) arg1, arg2);
                break;
            case "remove_any_by_age":
                command = new RemoveByAge((String) arg1);
                break;
            case "average_of_age":
                command = new AverageAge();
                break;
            case "print_descending":
                command = new PrintDescending();
                break;
            case "history":
                command = new History();
                break;
            default:
                command = new Help();
        }
        return command;
    }
}
