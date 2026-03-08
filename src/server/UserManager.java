package server;

import java.io.*;

public class UserManager {
    private static final String FILE_NAME = "users.txt";

    public static boolean checkLogin(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(","); 
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {}
        return false;
    }

    public static String register(String username, String password) {
        if (username.contains(",") || username.contains("|") || password.contains(",") || password.contains("|")) {
            return "❌ Lỗi: Tài khoản/Mật khẩu chứa ký tự cấm!";
        }
        try {
            File file = new File(FILE_NAME);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.split(",")[0].equalsIgnoreCase(username)) {
                        br.close();
                        return "❌ Lỗi: Tên đăng nhập này đã tồn tại!";
                    }
                }
                br.close();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true));
            bw.write(username + "," + password);
            bw.newLine();
            bw.close();
            ServerFrame.updateLog("📝 Người dùng mới đăng ký: " + username);
            return "SUCCESS";
        } catch (IOException e) {
            return "❌ Lỗi: Không thể lưu dữ liệu!";
        }
    }
}