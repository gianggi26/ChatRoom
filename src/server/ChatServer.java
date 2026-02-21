package server;

import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private int port;

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat Server started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler handler = new ClientHandler(socket);
                ClientManager.addClient(handler);
                handler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
