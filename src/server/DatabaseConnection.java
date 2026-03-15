package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Lưu ý: Thêm 127.0.0.1 và đảm bảo databaseName đúng tên bạn đã tạo trong SSMS
    private static final String URL = "jdbc:sqlserver://127.0.0.1:1433;databaseName=ChatRoomDB;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa"; 
    private static final String PASS = "123456";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            ServerFrame.updateLog("ERROR", "Không tìm thấy Driver SQL JDBC!");
            throw new SQLException(e);
        }
    }
}