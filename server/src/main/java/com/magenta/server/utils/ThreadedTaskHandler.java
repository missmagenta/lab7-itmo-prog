package com.magenta.server.utils;

import com.magenta.general.commands.CollectionAbstractCommand;
import com.magenta.general.commands.OwnerAccessable;
import com.magenta.general.commands.SignUp;
import com.magenta.general.commands.UserAbstractCommand;
import com.magenta.general.dto.ClientCommandDto;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.utils.HistoryManagable;
import com.magenta.general.utils.State;
import com.magenta.general.utils.UserDataManagable;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.util.AbstractMap;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

public class ThreadedTaskHandler {
    private final Logger logger;
    private final HistoryManagable historyManager;
    private final CollectionManagable collectionManager;
    private final UserDataManagable userDataManager;
    private final Queue<AbstractMap.SimpleImmutableEntry<SocketAddress, CommandResultDto>> queueToBeSent;
    private final Queue<AbstractMap.SimpleImmutableEntry<SocketAddress, ClientCommandDto>> queueToBeExecuted;

    public ThreadedTaskHandler(
            Queue<AbstractMap.SimpleImmutableEntry<SocketAddress, ClientCommandDto>> queueToBeExecuted,
            Queue<AbstractMap.SimpleImmutableEntry<SocketAddress, CommandResultDto>> queueToBeSent,
            Logger logger,
            HistoryManagable historyManager,
            CollectionManagable collectionManager,
            UserDataManagable userDataManager) {
        this.queueToBeExecuted = queueToBeExecuted;
        this.queueToBeSent = queueToBeSent;
        this.logger = logger;
        this.historyManager = historyManager;
        this.collectionManager = collectionManager;
        this.userDataManager = userDataManager;
    }

    public void start(ExecutorService executorService,
                      State<Boolean> isWorking) {
        Runnable checkCommandsToRun = () -> {
            while (isWorking.getState()) {
                if (!queueToBeExecuted.isEmpty()) {
                    AbstractMap.SimpleImmutableEntry<SocketAddress, ClientCommandDto> pairClientAndCommand = queueToBeExecuted.poll();
                    Runnable executeTask = () -> {
                        logger.info("Started executing new command");
                        assert pairClientAndCommand != null;
                        SocketAddress clientAddress = pairClientAndCommand.getKey();
                        ClientCommandDto clientCommandDto = pairClientAndCommand.getValue();
                        try {
                            executeAndValidate(clientCommandDto, clientAddress);
                            logger.info("Command was executed.");
                        } catch (RuntimeException e) {
                            logger.error(e.getMessage());
                        }
                    };
                    executorService.submit(executeTask);
                }
            }
        };
        executorService.submit(checkCommandsToRun);
    }

    private void executeAndValidate(ClientCommandDto clientCommandDto, SocketAddress clientAddress) {
        String username = clientCommandDto.getLogin();
        String password = clientCommandDto.getPassword();
        if (userDataManager.isValidatedUser(username, password) || clientCommandDto.getCommand() instanceof SignUp) {
            if (clientCommandDto.getCommand() instanceof OwnerAccessable) {
                CollectionAbstractCommand collectionCommand = (CollectionAbstractCommand) clientCommandDto.getCommand();
                long dragonId = ((OwnerAccessable) clientCommandDto.getCommand()).getDragonId();
                if (collectionManager.isOwner(username, dragonId)) {
                    queueToBeSent.add(new AbstractMap.SimpleImmutableEntry<>(
                            clientAddress, collectionCommand.execute(collectionManager, username, historyManager)));
                } else {
                    queueToBeSent.add(new AbstractMap.SimpleImmutableEntry<>(
                            clientAddress, new CommandResultDto("You are not owner of this object. " +
                                    "You are not allowed to change it.", true)));
                }
            } else {
                if (clientCommandDto.getCommand() instanceof UserAbstractCommand) {
                    UserAbstractCommand userCommand = (UserAbstractCommand) clientCommandDto.getCommand();
                    queueToBeSent.add(new AbstractMap.SimpleImmutableEntry<>(
                            clientAddress, userCommand.execute(userDataManager, historyManager)));
                } else if (clientCommandDto.getCommand() instanceof CollectionAbstractCommand) {
                    CollectionAbstractCommand collectionCommand = (CollectionAbstractCommand) clientCommandDto.getCommand();
                    queueToBeSent.add(new AbstractMap.SimpleImmutableEntry<>(
                            clientAddress, collectionCommand.execute(collectionManager, username, historyManager)));
                }
            }
        } else {
            queueToBeSent.add(new AbstractMap.SimpleImmutableEntry<>(
                    clientAddress, new CommandResultDto("Invalid login and password. " +
                            "You are not not allowed to work with data.", false)));
        }

    }

}
