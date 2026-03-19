package server;

import java.util.Vector;

public class ClientManager {
    private static final Vector<ClientHandler> clients = new Vector<>();

    public static synchronized void addClient(ClientHandler client) {
        clients.add(client);
    }

    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
        ServerFrame.updateLog("INFO", "[BROADCAST]: " + message);
        
        // --- TÍNH NĂNG MỚI: LƯU LỊCH SỬ CHAT ---
        // Bỏ qua danh sách user online, bỏ qua file đính kèm và lệnh KICKED
        if (!message.startsWith("LIST_USERS|") && !message.contains("|FILE_DATA|") && !message.startsWith("KICKED|")) {
            HistoryManager.saveMessage(message);
        }
    }

    public static synchronized void sendPrivateMessage(String sender, String receiver, String msg) {
        boolean found = false;
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(receiver)) {
                client.sendMessage("[Tin riêng từ " + sender + "]: " + msg);
                found = true;
                break;
            }
        }
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(sender)) {
                if (found) {
                    client.sendMessage("[Bạn -> " + receiver + "]: " + msg);
                } else {
                    client.sendMessage("❌ Hệ thống: Không tìm thấy '" + receiver + "'");
                }
                break;
            }
        }
        ServerFrame.updateLog("WARN", "🔒 [TIN RIÊNG] " + sender + " -> " + receiver + ": " + msg);
    }

    public static synchronized void kickUser(String adminName, String targetName) {
        ClientHandler targetClient = null;
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(targetName)) {
                targetClient = client;
                break;
            }
        }

        if (targetClient != null) {
            targetClient.sendMessage("KICKED|❌ Bạn đã bị Admin đuổi khỏi phòng chat!");
            targetClient.disconnect();
            broadcast("⚠️ Hệ thống: [" + targetName + "] đã bị Admin mời ra khỏi phòng!");
            ServerFrame.updateLog("WARN", "⚠️ [ADMIN] " + adminName + " đã kick " + targetName);
        } else {
            for (ClientHandler client : clients) {
                if (client.getUsername().equalsIgnoreCase(adminName)) {
                    client.sendMessage("❌ Hệ thống: Không tìm thấy người dùng '" + targetName + "'");
                    break;
                }
            }
        }
    }
    // Thêm vào dưới hàm kickUser trong ClientManager
    public static synchronized void banUser(String adminName, String targetName) {
        // 1. Cập nhật Database
        boolean dbSuccess = UserManager.banUser(targetName);
        if (!dbSuccess) {
            ServerFrame.updateLog("ERROR", "Không tìm thấy user '" + targetName + "' trong DB để BAN.");
            return;
        }

        // 2. Tìm xem người đó có đang online không để Kick ra ngay lập tức
        ClientHandler targetClient = null;
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(targetName)) {
                targetClient = client;
                break;
            }
        }

        if (targetClient != null) {
            targetClient.sendMessage("KICKED|🚫 Tài khoản của bạn đã bị KHÓA VĨNH VIỄN bởi Admin!");
            targetClient.disconnect();
        }

        broadcast("🚫 Hệ thống: Tài khoản [" + targetName + "] đã bị Admin KHÓA vĩnh viễn!");
        ServerFrame.updateLog("WARN", "🚫 [ADMIN] " + adminName + " đã BAN " + targetName);
    }

    public static synchronized void kickAll() {
        for (ClientHandler client : clients) {
            client.sendMessage("KICKED|Server đã đóng cửa. Vui lòng thoát!");
            client.disconnect();
        }
        clients.clear();
    }

    public static synchronized String getOnlineUsers() {
        StringBuilder sb = new StringBuilder("LIST_USERS|");
        for (ClientHandler client : clients) {
            sb.append(client.getUsername()).append(",");
        }
        return sb.toString();
    }

    public static synchronized boolean isNameExist(String name) {
        for (ClientHandler client : clients) {
            if (client.getUsername() != null && client.getUsername().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}