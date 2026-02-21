package server;

import java.util.Vector;

public class ClientManager {
    private static Vector<ClientHandler> clients = new Vector<>();

    public static void addClient(ClientHandler client) {
        clients.add(client);
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
        System.out.println(message);
    }
}
