package com.magenta.client.exceptions;

import java.io.File;

public class FileRecursionError extends Throwable {
    public FileRecursionError(File file) {
        super("File recursion: " + file + " is called recursively");
    }
}
