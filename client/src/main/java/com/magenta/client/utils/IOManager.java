package com.magenta.client.utils;

import com.magenta.client.exceptions.FilePermissionException;
import com.magenta.client.exceptions.FileRecursionError;

import java.io.*;
import java.util.Scanner;
import java.util.Stack;

public class IOManager {
    private Scanner scanner;
    private PrintStream printStream;
    private final Stack<BufferedReader> currentFilesReaders = new Stack<>();
    private final Stack<File> currentFiles = new Stack<>();

    public IOManager(InputStream inputStream, PrintStream printStream) {
        this.scanner = new Scanner(inputStream);
        this.printStream = printStream;
    }

    public IOManager(PrintStream printStream) {
        this.printStream = printStream;
    }
    public void println(String string) {
        printStream.println(string);
    }

    public void print(String string) {
        printStream.print(string);
    }

    public String readLine() throws IOException {
        if (!currentFiles.isEmpty()) {
            String input = currentFilesReaders.peek().readLine();
            if (input == null) {
                currentFiles.pop();
                currentFilesReaders.pop().close();
                return readLine();
            } else {
                return input;
            }
        } else {
            return scanner.nextLine();
        }
    }

    public void connectToFile(File file) throws FileNotFoundException, FilePermissionException, FileRecursionError {
        if (!file.exists())
            throw new FileNotFoundException("File not found");
        if (!file.canRead())
            throw new FilePermissionException("No read permission for file");
        if (currentFiles.contains(file)) {
            throw new FileRecursionError(file);
        } else {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            currentFiles.push(file);
            currentFilesReaders.push(reader);
        }
    }
}
