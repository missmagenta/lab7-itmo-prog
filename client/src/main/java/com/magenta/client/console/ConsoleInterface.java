package com.magenta.client.console;

import java.io.IOException;

public interface ConsoleInterface {
    String readNext() throws IOException, ClassNotFoundException;

    String[] parseArgs(String input);
}
