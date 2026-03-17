package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    public static void saveMessage(String message) {
        if (message == null || message.contains("Gần đây nhất")) return;

        String sql = "INSERT INTO ChatHistory (message) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, message);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            ServerFrame.updateLog("ERROR", "Lỗi lưu lịch sử: " + e.getMessage());
        }
    }

    public static synchronized List<String> getHistory(String username) {
        List<String> history = new ArrayList<>();
        history.add("--- Gần đây nhất ---");

        // CÂU LỆNH ĐÃ ĐƯỢC FIX: Lấy 50 tin nhắn MỚI NHẤT, sau đó sắp xếp lại theo thời gian
        String sql = "SELECT * FROM (SELECT TOP 50 message, created_at FROM ChatHistory ORDER BY created_at DESC) AS sub ORDER BY created_at ASC";

        int countTotal = 0;
        int countPrivate = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

            while (rs.next()) {
                countTotal++;
                String msg = rs.getString("message");
                Timestamp timeSql = rs.getTimestamp("created_at");
                String timeStr = (timeSql != null) ? sdf.format(timeSql) : "";

                if (msg == null) continue;

                // LỌC VÀ GIẢI MÃ TIN NHẮN RIÊNG
                if (msg.startsWith("[PRIVATE]|")) {
                    String[] parts = msg.split("\\|", 4);
                    if (parts.length >= 4) {
                        String sender = parts[1].trim();
                        String receiver = parts[2].trim();
                        String content = parts[3].trim();

                        if (username.trim().equalsIgnoreCase(sender)) {
                            history.add("[Bạn -> " + receiver + "]: [" + timeStr + "] " + content);
                            countPrivate++;
                        } else if (username.trim().equalsIgnoreCase(receiver)) {
                            history.add("[Tin riêng từ " + sender + "]: [" + timeStr + "] " + content);
                            countPrivate++;
                        }
                    }
                    continue; // Bỏ qua xử lý tin chung
                }

                // XỬ LÝ TIN NHẮN CHUNG
                if (msg.contains(": ") && !msg.startsWith("[")) {
                    String[] parts = msg.split(": ", 2);
                    history.add(parts[0] + ": [" + timeStr + "] " + parts[1]);
                } else {
                    history.add("[" + timeStr + "] " + msg);
                }
            }
            
            // In thông báo ra màn hình Server để bạn dễ theo dõi
            ServerFrame.updateLog("INFO", "Nạp lịch sử cho [" + username + "]: Tổng " + countTotal + " tin (Bao gồm " + countPrivate + " tin riêng).");

        } catch (SQLException e) {
            ServerFrame.updateLog("ERROR", "Lỗi nạp lịch sử SQL: " + e.getMessage());
        }
        return history;
    }
}