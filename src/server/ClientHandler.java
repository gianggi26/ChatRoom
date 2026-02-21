package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            out = new PrintWriter(socket.getOutputStream(), true);

            username = in.readLine();
            ClientManager.broadcast("🟢 " + username + " joined the chat");

            String message;
            while ((message = in.readLine()) != null) {
                ClientManager.broadcast(username + ": " + message);
            }
        } catch (Exception e) {
            System.out.println("Client disconnected");
        } finally {
            ClientManager.removeClient(this);
            ClientManager.broadcast("🔴 " + username + " left the chat");
        }
    }
}
