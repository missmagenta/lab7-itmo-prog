package com.magenta.client.utils;

import com.magenta.general.dto.ClientCommandDto;
import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.utils.DataCantBeSentException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.TimeoutException;


public class ConnectiontManager {
    private final String clientIp;
    private final int clientPort;
    private final String serverIp;
    private final int serverPort;
    private final IOManager ioManager;
    private static final int TIMEOUT_TO_SEND = 50;
    private static final int WAITING_TIME = 30000000;
    private static final int HEADER_LENGTH = 4;

    public ConnectiontManager(String clientIp,
                              int clientPort,
                              String serverIp,
                              int serverPort,
                              IOManager ioManager) {
        this.clientIp = clientIp;
        this.clientPort = clientPort;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.ioManager = ioManager;
    }

    public CommandResultDto sendMessage(ClientCommandDto clientCommandDto) throws UnresolvedAddressException, DataCantBeSentException {
        try (DatagramChannel datagramChannel = DatagramChannel.open()
        ) {
            datagramChannel.configureBlocking(false);
            sendBytes(datagramChannel, clientCommandDto);
            return receiveFromBuffer(datagramChannel);
        } catch (BindException e) {
            return new CommandResultDto("Unable to connect. Bind exception. " + e.getMessage(), false);
        } catch (IOException e) {
            e.printStackTrace();
            return new CommandResultDto("I/O problem." + e.getMessage(), false);
        }
    }

    private void sendBytes(DatagramChannel datagramChannel, ClientCommandDto clientCommandDto) throws IOException, DataCantBeSentException {
        datagramChannel.bind(new InetSocketAddress(clientIp, clientPort));
        SocketAddress serverAddress = new InetSocketAddress(serverIp, serverPort);

        byte[][] dataAndMetadata = serialize(clientCommandDto);
        byte[] dataBytes = dataAndMetadata[0];
        byte[] dataSizeBytes = dataAndMetadata[1];

        try {
            ByteBuffer sizeBuffer = ByteBuffer.wrap(dataSizeBytes);
            int attemptsCounter = TIMEOUT_TO_SEND;
            while (datagramChannel.send(sizeBuffer, serverAddress) < dataSizeBytes.length) {
                attemptsCounter -= 1;
                ioManager.println("Retrying to send package.");
                if (attemptsCounter == 0) {
                    ioManager.println("Time is over");
                    throw new DataCantBeSentException();
                }
            }
            ByteBuffer dataBuffer = ByteBuffer.wrap(dataBytes);
            while(datagramChannel.send(dataBuffer, serverAddress) < dataBytes.length) {
                attemptsCounter -= 1;
                ioManager.println("Retrying to send package.");
                if (attemptsCounter == 0) {
                    ioManager.println("Time is over");
                    throw new DataCantBeSentException();
                }
            }
        } catch (IOException e) {
            ioManager.println("Unable to resolve Inet address (IP and port) you entered. Check it. Restart client app.");
        }
    }

    public void receiveToBuffer(DatagramChannel datagramChannel, ByteBuffer buffer, int waitingTime) throws IOException, TimeoutException {
        int timeout = waitingTime;
        SocketAddress socketAddress = null;
        while (socketAddress == null) {
            socketAddress = datagramChannel.receive(buffer);
            if (timeout == 0) {
                throw new TimeoutException("Time is over. Failed to receive data.");
            }
            timeout--;
        }
    }

    public CommandResultDto receiveFromBuffer(DatagramChannel datagramChannel) throws IOException {
        byte[] sizeBytes = new byte[HEADER_LENGTH];
        ByteBuffer sizeBuffer = ByteBuffer.wrap(sizeBytes);
        try {
            receiveToBuffer(datagramChannel, sizeBuffer, WAITING_TIME);
            byte[] dataBytes = new byte[bytesToInt(sizeBytes)];
            ByteBuffer dataBuffer = ByteBuffer.wrap(dataBytes);
            receiveToBuffer(datagramChannel, dataBuffer, WAITING_TIME);
            return (CommandResultDto) deserialize(dataBytes);
        } catch (ClassNotFoundException e) {
            return new CommandResultDto("Received invalid data from serve." + e.getMessage(), false);
        } catch (TimeoutException e) {
            return new CommandResultDto("Time is over. Failed to receive data from server.", false);
        }
    }

    public static byte[][] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);

        byte[] dataBytes = baos.toByteArray();
        byte[] metadataSizeBytes = ByteBuffer.allocate(HEADER_LENGTH).putInt(baos.size()).array();

        return new byte[][]{dataBytes, metadataSizeBytes};
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }

    public static int bytesToInt(byte[] bytes) {
        int value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 0xFF);
        }
        return value;
    }
}
