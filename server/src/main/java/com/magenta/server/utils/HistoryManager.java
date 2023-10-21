package com.magenta.server.utils;

import com.magenta.general.utils.HistoryManagable;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HistoryManager implements HistoryManagable {
    private static final int HISTORY_CAPACITY = 10;
    private final Queue<String> history = new ArrayBlockingQueue<>(HISTORY_CAPACITY);
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);

    @Override
    public void addNote(String note) {
        Lock writeLock = rwLock.writeLock();
        try {
            writeLock.lock();
            if (history.size() == HISTORY_CAPACITY) {
                history.remove();
            }
            history.add(note);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public String niceToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Last 10 commands:\n");
        for (String command : history) {
            sb.append(command).append("\n");
        }
        return sb.toString();
    }
}
