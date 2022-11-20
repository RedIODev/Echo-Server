package dev.redio;

import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
/**
 * A simple client to connect to an echo server and talk to it.<p>
 * There are 3 reserved inputs:
 * <ul>
 * <li> clear   : clears the console
 * <li> ""      : does nothing, if no (non whitespace) text is supplied no request will be send to the server.
 * <li> exit    : disconnects from the server and closes the program.
 * </ul>
 * Otherwise all text will be send to the send to the specified (echo) server.
 */
public class Client {

    private static final Console CONSOLE = System.console();

    private final Socket socket;

    /**
     * Creates a new Client and connects to the specified server at the given port.
     * @param hostname the hostname to connect to.
     * @param port the port used to connect.
     * @throws IOException when the client was unable to connect to the server.
     */
    public Client(String hostname, int port) throws IOException {
        this.socket = new Socket(hostname, port);
    }

    /**
     * Starts the echo prompt.<p>
     * This method will never return without throwing an IOException.
     * @throws IOException when the IOStreams could not be created successfully.
     */
    public void launch() throws IOException {
        try (var out = new PrintStream(socket.getOutputStream());
                var in = new Scanner(socket.getInputStream())) {
            while (true) {
                var input = CONSOLE.readLine("[Client]$ ");
                switch (input.toLowerCase().trim()) {
                    case "clear"    -> System.out.print("\033c");
                    case ""         -> {}
                    case "exit"     -> { return; }
                    default         -> {
                        out.println(input);
                        String serverinput = in.next();
                        System.out.println("[Server] " + serverinput);
                    }
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            System.out.println("Server closed: Disconnected");
        }
    }
}