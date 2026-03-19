package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        String sql = "SELECT * FROM ( " +
                "   SELECT TOP 50 c.message, c.created_at " +
                "   FROM ChatHistory c " +
                "   WHERE c.created_at > ISNULL((SELECT last_cleared_at FROM Users WHERE username = ?), '1900-01-01') " +
                "   ORDER BY c.created_at DESC " +
                ") AS sub ORDER BY created_at ASC";

        int countTotal = 0;
        int countPrivate = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
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
                        continue;
                    }

                    // XỬ LÝ TIN NHẮN CHUNG
                    if (msg.contains(": ") && !msg.startsWith("[")) {
                        String[] parts = msg.split(": ", 2);
                        history.add(parts[0] + ": [" + timeStr + "] " + parts[1]);
                    } else {
                        history.add("[" + timeStr + "] " + msg);
                    }
                }
            }
            ServerFrame.updateLog("INFO", "Nạp lịch sử cho [" + username + "]: Tổng " + countTotal + " tin (Bao gồm " + countPrivate + " tin riêng).");

        } catch (SQLException e) {
            ServerFrame.updateLog("ERROR", "Lỗi nạp lịch sử SQL: " + e.getMessage());
        }
        return history;
    }

    public static void revokeMessage(String targetMsg, String revoker) {
        // 1. Loại bỏ thẻ thời gian
        String originalMsg = targetMsg.replaceAll("\\[\\d{2}:\\d{2} [a-zA-Z]{2}\\] ", "");

        // 2. Tái tạo lại chuỗi gốc trong DB
        String dbSearchMsg = originalMsg;
        if (originalMsg.startsWith("[Bạn -> ")) {
            String receiver = originalMsg.substring(8, originalMsg.indexOf("]:"));
            String content = originalMsg.substring(originalMsg.indexOf("]: ") + 3);
            dbSearchMsg = "[PRIVATE]|" + revoker + "|" + receiver + "|" + content;
        } else if (originalMsg.startsWith("[Tin riêng từ ")) {
            String sender = originalMsg.substring(14, originalMsg.indexOf("]:"));
            String content = originalMsg.substring(originalMsg.indexOf("]: ") + 3);
            dbSearchMsg = "[PRIVATE]|" + sender + "|" + revoker + "|" + content;
        }

        // 3. Lấy Prefix để giữ lại TÊN NGƯỜI GỬI
        String prefix = "";
        if (dbSearchMsg.contains("|FILE_DATA|")) {
            prefix = dbSearchMsg.substring(0, dbSearchMsg.indexOf("|FILE_DATA|")) + ": ";
        } else if (dbSearchMsg.contains(": ") && !dbSearchMsg.startsWith("[")) {
            prefix = dbSearchMsg.substring(0, dbSearchMsg.indexOf(": ") + 2);
        } else if (dbSearchMsg.startsWith("[PRIVATE]|")) {
            String[] parts = dbSearchMsg.split("\\|", 4);
            if (parts.length >= 4) {
                prefix = "[PRIVATE]|" + parts[1] + "|" + parts[2] + "|";
            }
        }

        // 4. Mã hóa thu hồi
        String newMsg = prefix + (revoker.equalsIgnoreCase("admin") ? "[REVOKED_BY_ADMIN]" : "[REVOKED]");

        String sql = "UPDATE ChatHistory SET message = ? WHERE id = (SELECT TOP 1 id FROM ChatHistory WHERE message = ? ORDER BY id DESC)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newMsg);
            pstmt.setString(2, dbSearchMsg);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            ServerFrame.updateLog("ERROR", "Lỗi thu hồi tin nhắn DB: " + e.getMessage());
        }
    }
}