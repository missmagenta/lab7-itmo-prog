package com.magenta.client;


import com.magenta.client.utils.ConnectiontManager;
import com.magenta.client.console.ConsoleManager;
import com.magenta.client.utils.IOManager;
import com.magenta.general.utils.DataCantBeSentException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Client {
    private Client() {
    throw new UnsupportedOperationException("Utility class. Cannot be instantiated.");
}

    private static String clientIp = "localhost";
    private static int clientPort = 2222;
    private static String serverIp = "localhost";
    private static int serverPort = 4444;
    private static final int MAX_PORT = 65535;
    private static final int MIN_PORT = 1024;
    private static final List<String> COMMAND_LIST = new ArrayList<>();
    private static final BufferedReader BUFFERED_READER = new BufferedReader(new InputStreamReader(System.in));
    private static final IOManager IO_MANAGER = new IOManager(System.in, System.out);

    public static void main(String[] args) {
        createCommandList();
//        try {
//            getInfoForConnection();
//        } catch (IOException e) {
//            OUTPUT_MANAGER.println("Invalid arguments for connection. I/O exception. {}" + e.getMessage());
//            return;
//        }
        try {
            ConnectiontManager connectiontManager = new ConnectiontManager(clientIp, clientPort, serverIp, serverPort, IO_MANAGER);
            ConsoleManager console = new ConsoleManager(IO_MANAGER, COMMAND_LIST, connectiontManager);
            console.start();
        } catch (IOException e) {
            IO_MANAGER.println("Problem with I/O with server." + e.getMessage());
        } catch (ClassNotFoundException e) {
            IO_MANAGER.println("Problem with data on server.");
        } catch (TimeoutException e) {
            IO_MANAGER.println("Failed to send registration request to server. Try again later.");
        } catch (DataCantBeSentException e) {
            IO_MANAGER.println("Could not send data.");
            e.printStackTrace();
        }
    }

    private static void createCommandList() {
        COMMAND_LIST.add("help");
        COMMAND_LIST.add("info");
        COMMAND_LIST.add("insert");
        COMMAND_LIST.add("exit");
        COMMAND_LIST.add("show");
        COMMAND_LIST.add("update_id");
        COMMAND_LIST.add("remove_greater_key");
        COMMAND_LIST.add("clear");
        COMMAND_LIST.add("execute_script");
        COMMAND_LIST.add("remove_greater_element");
        COMMAND_LIST.add("remove_key");
        COMMAND_LIST.add("replace_if_greater");
        COMMAND_LIST.add("remove_any_by_age");
        COMMAND_LIST.add("average_of_age");
        COMMAND_LIST.add("print_descending");
        COMMAND_LIST.add("sign_up");
        COMMAND_LIST.add("history");
        COMMAND_LIST.add("sign_up");
    }

    private static void getInfoForConnection() throws IOException {
        clientIp = ask("Enter client IP.");
        clientPort = ask(
                "Enter client port.",
                x -> (x >= MIN_PORT && x <= MAX_PORT),
                Integer::parseInt,
                "Client port must be a number.",
                "Client port must be a number in range from 1024 to 65535.");
        serverIp = ask("Enter server IP.");
        serverPort = ask(
                "Enter server port.",
                x -> (x >= MIN_PORT && x <= MAX_PORT),
                Integer::parseInt,
                "Server port must be a number.",
                "Server port must be a number in range from 1024 to 65535.");
    }

    public static <T> T ask(String question,
                            Predicate<? super T> validator,
                            Function<String, ? extends T> parser,
                            String errorMessage,
                            String invalidValueMessage) throws IOException {
        IO_MANAGER.println(question);
        String input;
        T value;
        do {
            try {
                input = BUFFERED_READER.readLine();
                value = parser.apply(input);
            } catch (IllegalArgumentException e) {
                IO_MANAGER.println(errorMessage);
                continue;
            }
            if (validator.test(value)) {
                return value;
            } else {
                IO_MANAGER.println(invalidValueMessage);
            }
        } while (true);
    }

    public static String ask(String question) throws IOException {
        IO_MANAGER.println(question);
        String input;
        input = BUFFERED_READER.readLine();
        return input;
    }
}
