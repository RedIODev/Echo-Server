package dev.redio;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        System.setErr(new PrintStream("server_log" + UUID.randomUUID() + ".txt"));
        new Server(8000).launch();
    }
}
