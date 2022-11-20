package dev.redio;

import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A simple server that provides an echo service.<p>
 * There is 1 command available in the server console:
 * <ul>
 * <li> exit : disconnects all clients and stops the server gracefully.
 * </ul>
 */
public class Server {

    private final ServerSocket serverSocket;
    private final List<Connection> connections;

    /**
     * Creates a new Server that accepts connections at the given port.
     * @param port the port where the server listens for clients.
     * @throws IOException when the server was unable to start.
     */
    public Server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.connections = new ArrayList<>();
    }

    /**
     * Starts the server and waits for clients to connect to it.<p>
     * This method will never return without throwing an IOException.
     * @throws IOException when the IOStreams could not be created successfully.
     */
    public void launch() throws IOException {
        new ServerConsole().start();
        while (true) {
            var socket = this.serverSocket.accept();
            System.out.println("Client connected at " + socket.getInetAddress().getHostName());
            var connection = new Connection(socket);
            connection.connect();
            synchronized (connections) {
                this.connections.add(connection);
            }
        }
    }

    /**
     * A private inner class to handle the connection with individual clients.
     * @apiNote For each connected client 1 Connection object needs to be created.
     * <li>The underlying thread will also be controlled by this class.
     */
    private class Connection {
        private static final Thread.Builder THREAD_BUILDER = Thread.ofVirtual().name("Connection-thread", 0);
        private final Socket socket;
        private Thread thread;

        /**
         * Creates a new Connection with a socket associated with it.
         * @param socket the socket associated with this connection.
         */
        public Connection(Socket socket) {
            this.socket = socket;
        }

        /**
         * Connects to the client and provides the echo server.<p>
         * This method will create an new thread and return immediately. 
         * The connection will be handles concurrently.
         */
        public void connect() {
            this.thread = THREAD_BUILDER.start(this::connectionMain);
        }

        /**
         * Interrupts the connection with the client and stops the thread associated with this Connection.
         */
        public void interrupt() {
            this.thread.interrupt();
        }

        /**
         * The "main" method of a connection thread.
         * It handles the communication with the Client.
         * This method will be executed concurrently.
         */
        private void connectionMain() {
            try (var out = new PrintStream(socket.getOutputStream());
                var in = new Scanner(socket.getInputStream())) {
                while(!Thread.interrupted()) 
                    out.println(in.next());
            } catch(IOException e) {
                e.printStackTrace();
                System.out.println("Unable to connect to client: IOException [" + e.getMessage() + "]");
            } catch(NoSuchElementException e) {
                e.printStackTrace();
                System.out.println("Client lost connection: Disconnected");
            } finally {
                System.out.println("Client " + socket.getInetAddress().getHostAddress() + " disconnected.");
                synchronized (connections) {
                    connections.remove(this);
                }
            }
        }
    }

    /**
     * A private inner class that handles the console of this server.
     */
    private class ServerConsole {
        private static final Console CONSOLE = System.console();

        /**
         * Starts a new thread that handles the input from the server console.
         * @apiNote if there is no associated console to this JVM this method does nothing.
         */
        public void start() {
            if (CONSOLE == null)
                return;
            Thread.ofVirtual().name("Console-thread").start(this::consoleMain);
        }

        /**
         * The "main" method of the console thread.
         * It handles the commands send to the console.
         * @see Server Valid commands.
         */
        private void consoleMain() {
            while(!Thread.interrupted()) {
                var input = CONSOLE.readLine();
                switch (input.toLowerCase().trim()) {
                    case "exit" -> {
                        synchronized (connections) {
                            connections.forEach(Connection::interrupt);
                        }
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("Failed to close server.");
                        }
                        return;
                    }
                    default -> System.out.println("Invalid command.");
                }
            }
        }
    }
}