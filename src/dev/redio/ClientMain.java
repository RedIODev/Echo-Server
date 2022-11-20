package dev.redio;

import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        System.setErr(new PrintStream("client_log" + UUID.randomUUID() + ".txt"));
        new Client("localhost", 8000).launch();
    }   
}
