package server;

public class MainServer {
    public static void main(String[] args) {
        ChatServer server = new ChatServer(12345);
        server.start();
    }
}
