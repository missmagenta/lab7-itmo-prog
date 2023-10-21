package com.magenta.server.utils;

import com.magenta.general.dto.ClientCommandDto;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.utils.State;
import com.magenta.general.utils.UserDataManagable;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.AbstractMap;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;


public class ServerManager {
    private final String serverIp;
    private final int serverPort;
    private final ForkJoinPool forkJoinPool;
    private final ExecutorService executor;
    private final CollectionManagable collectionManager;
    private final UserDataManagable userDataManager;
    private final Queue<AbstractMap.SimpleImmutableEntry<SocketAddress, CommandResultDto>> queueToBeSent;
    private final Queue<AbstractMap.SimpleImmutableEntry<SocketAddress, ClientCommandDto>> queueToBeExecuted;
    private final ThreadedTaskHandler taskHandler;
    private final ClientDataReceiver clientDataReceiver;
    private final Logger logger;

    public ServerManager(String serverIp,
                         int serverPort,
                         ForkJoinPool forkJoinPool,
                         ExecutorService executor,
                         CollectionManagable collectionManager,
                         UserDataManagable userDataManager,
                         Logger logger) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.forkJoinPool = forkJoinPool;
        this.executor = executor;
        this.collectionManager = collectionManager;
        this.userDataManager = userDataManager;
        this.queueToBeSent = new LinkedBlockingQueue<>();
        this.queueToBeExecuted = new LinkedBlockingQueue<>();
        this.logger = logger;
        this.clientDataReceiver = new ClientDataReceiver(queueToBeExecuted, logger);
        this.taskHandler = new ThreadedTaskHandler(queueToBeExecuted, queueToBeSent, logger, new HistoryManager(), collectionManager, userDataManager);
    }

    public void start(State<Boolean> isWorking) throws IOException {
        try (DatagramChannel datagramChannel = DatagramChannel.open()
        ) {
            InetSocketAddress address = new InetSocketAddress(serverIp, serverPort);
            datagramChannel.bind(address);
            datagramChannel.configureBlocking(false);
            logger.info("Server started at {}", address);
            executor.submit(() -> {
                try {
                    clientDataReceiver.receiveMessage(datagramChannel, isWorking);
                } catch (IOException | InterruptedException e) {
                    logger.error(e.getMessage());
                }
            });

            taskHandler.start(executor, isWorking);

            while (isWorking.getState()) {
                if (!queueToBeSent.isEmpty()) {
                    AbstractMap.SimpleImmutableEntry<SocketAddress, CommandResultDto> addressAndCommandResult = queueToBeSent.poll();
                    CommandResultDto commandResult = addressAndCommandResult.getValue();
                    SocketAddress clientAddress = addressAndCommandResult.getKey();
                    forkJoinPool.invoke(new ClientDataSender(commandResult, datagramChannel, clientAddress, logger));
                }
            }
        } catch (BindException e) {
            logger.error("Failed to run server. {}", e.getMessage());
        }
    }
}
