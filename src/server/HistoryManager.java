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

    // 1. Lưu tin nhắn vào Database (Thời gian do SQL tự động lấy)
    public static void saveMessage(String message) {
        // Không lưu dòng phân cách vào Database
        if (message.contains("Gần đây nhất")) return;

        String sql = "INSERT INTO ChatHistory (message) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, message);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            ServerFrame.updateLog("ERROR", "Lỗi lưu lịch sử: " + e.getMessage());
        }
    }

    // 2. Tải lịch sử và gắn thời gian chuẩn từ Database
    public static synchronized List<String> getHistory() {
        List<String> history = new ArrayList<>();
        
        // Dòng phân cách sạch sẽ, không có Emoji
        history.add("--- Gần đây nhất ---");

        String sql = "SELECT TOP 50 message, created_at FROM ChatHistory ORDER BY created_at ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            // Định dạng thời gian giống với UI của bạn (Ví dụ: 06:14 PM)
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

            while (rs.next()) {
                String msg = rs.getString("message");
                Timestamp timeSql = rs.getTimestamp("created_at");
                String timeStr = (timeSql != null) ? sdf.format(timeSql) : "";

                // Bộ lọc làm sạch các emoji cũ lỡ bị lưu trong Database
                msg = msg.replace("🟢", "").replace("🚪", "").replace("🔔", "").replace("📝", "").replace("", "").trim();

                // Ghép thời gian chuẩn vào nội dung tin nhắn
                if (msg.contains(": ")) {
                    // Nếu là tin nhắn bình thường (admin: hello)
                    String[] parts = msg.split(": ", 2);
                    history.add(parts[0] + ": [" + timeStr + "] " + parts[1]);
                } else {
                    // Nếu là thông báo hệ thống (admin đã tham gia)
                    history.add("[" + timeStr + "] " + msg);
                }
            }
        } catch (SQLException e) {
            ServerFrame.updateLog("ERROR", "Lỗi nạp lịch sử từ SQL: " + e.getMessage());
        }
        return history;
    }
}