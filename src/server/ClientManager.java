package server;

import java.util.Vector;

public class ClientManager {
    private static Vector<ClientHandler> clients = new Vector<>();

    public static void addClient(ClientHandler client) { clients.add(client); }
    public static void removeClient(ClientHandler client) { clients.remove(client); }

    public static void broadcast(String message) {
        for (ClientHandler client : clients) client.sendMessage(message);
        ServerFrame.updateLog("[BROADCAST]: " + message);
    }

    public static void sendPrivateMessage(String sender, String receiver, String msg) {
        boolean found = false;
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(receiver)) {
                client.sendMessage("[Tin riêng từ " + sender + "]: " + msg);
                found = true; break;
            }
        }
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(sender)) {
                if (found) client.sendMessage("[Bạn -> " + receiver + "]: " + msg);
                else client.sendMessage("❌ Hệ thống: Không tìm thấy '" + receiver + "'");
                break;
            }
        }
        ServerFrame.updateLog("🔒 [TIN RIÊNG] " + sender + " -> " + receiver + ": " + msg);
    }

    // --- TÍNH NĂNG ADMIN: KÍCH NGƯỜI DÙNG ---
    public static void kickUser(String adminName, String targetName) {
        ClientHandler targetClient = null;
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(targetName)) {
                targetClient = client;
                break;
            }
        }

        if (targetClient != null) {
            // Gửi tối hậu thư cho người bị kích
            targetClient.sendMessage("KICKED|❌ Bạn đã bị Admin đuổi khỏi phòng chat!");
            targetClient.disconnect(); // Ngắt kết nối mạng của họ
            
            // Thông báo cho toàn phòng
            broadcast("⚠️ Hệ thống: [" + targetName + "] đã bị Admin mời ra khỏi phòng!");
            ServerFrame.updateLog("⚠️ [ADMIN] " + adminName + " đã kick " + targetName);
        } else {
            // Báo lỗi cho Admin nếu gõ sai tên
            for (ClientHandler client : clients) {
                if (client.getUsername().equalsIgnoreCase(adminName)) {
                    client.sendMessage("❌ Hệ thống: Không tìm thấy người dùng '" + targetName + "'");
                    break;
                }
            }
        }
    }

    public static String getOnlineUsers() {
        StringBuilder sb = new StringBuilder("LIST_USERS|");
        for (ClientHandler client : clients) sb.append(client.getUsername()).append(",");
        return sb.toString();
    }

    public static boolean isNameExist(String name) {
        for (ClientHandler client : clients) {
            if (client.getUsername() != null && client.getUsername().equalsIgnoreCase(name)) return true;
        }
        return false;
    }
}