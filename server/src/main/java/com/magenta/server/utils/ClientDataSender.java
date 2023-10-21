package com.magenta.server.utils;

import com.magenta.general.dto.CommandResultDto;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeoutException;

public class ClientDataSender extends RecursiveTask<Void> {
    private static final int TIMEOUT_TO_SEND = 5;
    private static final int HEADER_LENGTH = 4;
    private final CommandResultDto commandResultDto;
    private final transient DatagramChannel datagramChannel;
    private final SocketAddress clientAddress;
    private final transient Logger logger;

    public ClientDataSender(CommandResultDto commandResultDto,
                            DatagramChannel datagramChannel,
                            SocketAddress socketAddress,
                            Logger logger) {
        this.commandResultDto = commandResultDto;
        this.datagramChannel = datagramChannel;
        this.clientAddress = socketAddress;
        this.logger = logger;
    }

    public void send(SocketAddress clientAddress) throws IOException, TimeoutException {
        byte[][] dataAndMetadata = serialize(commandResultDto);
        byte[] dataBytes = dataAndMetadata[0];
        byte[] dataSizeBytes = dataAndMetadata[1];

        try {
            sendBytes(datagramChannel, clientAddress, dataSizeBytes);
            logger.info("Size of data sent.");
        } catch (IOException e) {
            logger.error("Failed to send information about message size.");
        }

        try {
            sendBytes(datagramChannel, clientAddress, dataBytes);
            logger.info("Data sent.");
        } catch (IOException e) {
            logger.error("Failed to send message.");
        }
    }

    private void sendBytes(DatagramChannel datagramChannel, SocketAddress clientAddress, byte[] bytes) throws IOException, TimeoutException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int limit = TIMEOUT_TO_SEND;
        while (datagramChannel.send(buffer, clientAddress) <= 0) {
            limit -= 1;
            logger.info("Failed to send a package. Retrying.");
            if (limit == 0) {
                throw new TimeoutException("All attempts used. Failed to send data.");
            }
        }
        logger.info("Bytes to int gives you {}", bytesToInt(buffer.array()));
    }

    public static byte[][] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        byte[] dataBytes = baos.toByteArray();
        byte[] metadataSizeBytes = ByteBuffer.allocate(HEADER_LENGTH).putInt(baos.size()).array();

        return new byte[][]{dataBytes, metadataSizeBytes};
    }

    @Override
    protected Void compute() {
        logger.info("Started sending message to the client.");
        try {
            send(clientAddress);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            logger.error("Problem with sending response to client. {}", e.getMessage());
        }
        return null;
    }

    public static int bytesToInt(byte[] bytes) {
        int value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 0xFF);
        }
        return value;
    }
}
