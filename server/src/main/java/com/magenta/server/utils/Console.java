package com.magenta.server.utils;

import com.magenta.general.utils.State;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Console {
    private final State<Boolean> serverIsWorking;
    private final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    private final Logger logger;

    public Console(State<Boolean> serverIsWorking, Logger logger) {
        this.serverIsWorking = serverIsWorking;
        this.logger = logger;
    }

    public void start() {
        while (serverIsWorking.getState()) {
            String input;
            try {
                input = bufferedReader.readLine();
            } catch (IOException e) {
                logger.error("I/O exception. {}", e.getMessage());
                serverIsWorking.setState(false);
                break;
            }
            if ("exit".equals(input)) {
                serverIsWorking.setState(false);
                logger.info("Closing a server. Goodbye.");
            }
        }
    }
}
