package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    public static synchronized void saveMessage(String message) {
        String sql = "INSERT INTO ChatHistory (message) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            ServerFrame.updateLog("ERROR", "Lỗi lưu lịch sử vào SQL: " + e.getMessage());
        }
    }

    public static synchronized List<String> getHistory() {
        List<String> history = new ArrayList<>();
        // Lấy 50 tin nhắn cũ nhất để hiển thị lại theo đúng trình tự thời gian
        String sql = "SELECT TOP 50 message FROM ChatHistory ORDER BY created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) { // Đã sửa lỗi chữ 'v' thành 'y'
            while (rs.next()) {
                history.add(rs.getString("message"));
            }
        } catch (SQLException e) {
            ServerFrame.updateLog("ERROR", "Lỗi nạp lịch sử từ SQL: " + e.getMessage());
        }
        return history;
    }
}