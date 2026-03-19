package server;

import java.sql.*;

public class UserManager {
    public static boolean checkLogin(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Trả về true nếu tìm thấy tài khoản khớp
        } catch (SQLException e) {
            ServerFrame.updateLog("ERROR", "Lỗi truy vấn Đăng nhập: " + e.getMessage());
            return false;
        }
    }

    public static String register(String username, String password) {
        String checkSql = "SELECT username FROM Users WHERE username = ?";
        String insertSql = "INSERT INTO Users (username, password) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Kiểm tra xem tên này đã có ai dùng chưa
            PreparedStatement checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setString(1, username);
            if (checkPstmt.executeQuery().next()) return "❌ Tên đăng nhập đã tồn tại!";

            // Nếu chưa có thì thêm mới vào bảng Users
            PreparedStatement insertPstmt = conn.prepareStatement(insertSql);
            insertPstmt.setString(1, username);
            insertPstmt.setString(2, password);
            insertPstmt.executeUpdate();
            
            ServerFrame.updateLog("INFO", "📝 Đã đăng ký User mới vào SQL Server: " + username);
            return "SUCCESS";
        } catch (SQLException e) {
            return "❌ Lỗi SQL Register: " + e.getMessage();
        }
    }
    // Thêm vào dưới cùng của class UserManager
    public static boolean banUser(String username) {
        String sql = "UPDATE Users SET is_banned = 1 WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int rows = pstmt.executeUpdate();
            return rows > 0; // Trả về true nếu update thành công
        } catch (SQLException e) {
            ServerFrame.updateLog("ERROR", "Lỗi Ban User: " + e.getMessage());
            return false;
        }
    }

    public static boolean isBanned(String username) {
        String sql = "SELECT is_banned FROM Users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_banned");
            }
        } catch (SQLException e) {
            ServerFrame.updateLog("ERROR", "Lỗi kiểm tra Ban: " + e.getMessage());
        }
        return false;
    }
}