package com.magenta.server;

import com.magenta.general.utils.CollectionManagable;
import com.magenta.general.utils.State;
import com.magenta.general.utils.UserDataManagable;
import com.magenta.server.postgresql.CollectionManager;
import com.magenta.server.postgresql.Database;
import com.magenta.server.postgresql.UserDataManager;
import com.magenta.server.utils.Console;
import com.magenta.server.utils.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.UnresolvedAddressException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public final class Server {
    private Server() {
        throw new UnsupportedOperationException("Utility class. Cannot be instantiated.");
    }

    private static final String serverIp = "localhost";
    private static final int serverPort = 4444;
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final String URL = "jdbc:postgresql://";
    private static final String dbHost = "localhost";
    private static final String dbname = "postgres";
    private static final String username = "postgres";
    private static final String password = "aboba";
    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool();
    private static final ExecutorService CACHED_THREAD_POOL = Executors.newCachedThreadPool();

    public static Connection connect() throws IOException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL + dbHost + "/" + dbname, username, password);
            LOGGER.info("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            LOGGER.error("Failed to connect to connect to the PostgreSQL. Check your login and password. {}", e.getMessage());
        }
        return connection;
    }


    public static void main(String[] args) {
        try {
            State<Boolean> isWorking = new State<>(true);
            Database database = new Database(connect(), LOGGER);
            CollectionManagable collectionManager = new CollectionManager(database, LOGGER);
            UserDataManagable userDataManager = new UserDataManager(database, LOGGER);
            collectionManager.initializeData();
            userDataManager.initializeData();
            Console console = new Console(isWorking, LOGGER);
            ServerManager serverManager = new ServerManager(
                    serverIp,
                    serverPort,
                    FORK_JOIN_POOL,
                    CACHED_THREAD_POOL,
                    collectionManager,
                    userDataManager,
                    LOGGER);
            CACHED_THREAD_POOL.submit(console::start);
            serverManager.start(isWorking);
        } catch (IOException e) {
            LOGGER.error("I/O problem. {}", e.getMessage());
        } catch (UnresolvedAddressException e) {
            LOGGER.error("Restart server with another address. {}", e.getMessage());
        } finally {
            FORK_JOIN_POOL.shutdown();
            CACHED_THREAD_POOL.shutdown();
        }
    }
}