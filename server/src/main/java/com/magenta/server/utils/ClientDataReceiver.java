package com.magenta.server.utils;

import com.magenta.general.dto.ClientCommandDto;
import com.magenta.general.utils.State;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.AbstractMap;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

public class ClientDataReceiver {
    private final Queue<AbstractMap.SimpleImmutableEntry<SocketAddress, ClientCommandDto>> queueToBeExecuted;
    private static final int TIMEOUT_MILLS = 5;
    private static final int HEADER_LENGTH = 4;
    private final Logger logger;

    public ClientDataReceiver(
            Queue<AbstractMap.SimpleImmutableEntry<SocketAddress, ClientCommandDto>> queueToBeExecuted,
            Logger logger) {
        this.queueToBeExecuted = queueToBeExecuted;
        this.logger = logger;
    }

    public void receiveMessage(
            DatagramChannel datagramChannel,
            State<Boolean> isWorking) throws IOException, InterruptedException {
        while (isWorking.getState()) {
            ByteBuffer dataSizeBuffer = ByteBuffer.wrap(new byte[HEADER_LENGTH]);
            receieveWhileWaiting(datagramChannel, dataSizeBuffer, isWorking);
            logger.info("Received information about size. The size is {}", bytesToInt(dataSizeBuffer.array()));
            if (isWorking.getState()) {
                ByteBuffer dataBuffer = ByteBuffer.wrap(new byte[bytesToInt(dataSizeBuffer.array())]);
                SocketAddress receivedAddress = null;
                try {
                    receivedAddress = receiveWithTimeOut(datagramChannel, dataBuffer, TIMEOUT_MILLS);
                } catch (TimeoutException e) {
                    logger.error(e.getMessage());
                }
                ClientCommandDto receivedCommand;
                try {
                    receivedCommand = (ClientCommandDto) deserialize(dataBuffer.array());
                    AbstractMap.SimpleImmutableEntry<SocketAddress, ClientCommandDto> pairOfClientAndCommand =
                            new AbstractMap.SimpleImmutableEntry<>(receivedAddress, receivedCommand);
                    queueToBeExecuted.add(pairOfClientAndCommand);
                } catch (ClassNotFoundException e) {
                    logger.error("Invalid data. Cannot convert to objets. {}", e.getMessage());
                }
            }
        }
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }

    private SocketAddress receieveWhileWaiting(
            DatagramChannel datagramChannel,
            ByteBuffer byteBuffer,
            State<Boolean> isWorking) throws IOException {
        while (isWorking.getState()) {
            SocketAddress receivedAddress = datagramChannel.receive(byteBuffer);
            if (Objects.nonNull(receivedAddress)) {
                logger.info("Received client message 1/2.");
                return receivedAddress;
            }
        }
        return null;
    }

    private SocketAddress receiveWithTimeOut(
            DatagramChannel datagramChannel,
            ByteBuffer byteBuffer,
            int timeout) throws IOException, TimeoutException, InterruptedException {
        int timeToWait = timeout;
        SocketAddress receivedAddress;
        while (timeToWait > 0) {
            receivedAddress = datagramChannel.receive(byteBuffer);
            if (Objects.nonNull(receivedAddress)) {
                logger.info("Received client message 2/2.");
                return receivedAddress;
            } else {
                Thread.sleep(1);
                timeToWait--;
            }
        }
        throw new TimeoutException("Waiting time is over. Failed to recieve data.");
    }

    public static int bytesToInt(byte[] bytes) {
        int value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 0xFF);
        }
        return value;
    }
}
