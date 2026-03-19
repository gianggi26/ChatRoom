package server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return this.username;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            boolean isLoggedIn = false;

            while (!isLoggedIn) {
                String authMessage = in.readLine();
                if (authMessage == null) return;

                String[] parts = authMessage.split("\\|");
                String command = parts[0];

                if (command.equals("LOGIN") && parts.length == 3) {
                    String user = parts[1], pass = parts[2];

                    if (UserManager.checkLogin(user, pass)) {
                        if (UserManager.isBanned(user)) {
                            sendMessage("LOGIN_FAIL|🚫 Tài khoản của bạn đã bị KHÓA VĨNH VIỄN!");
                        } else if (ClientManager.isNameExist(user)) {
                            sendMessage("LOGIN_FAIL|Tài khoản này đang online ở máy khác!");
                        } else {
                            this.username = user;
                            isLoggedIn = true;
                            sendMessage("LOGIN_SUCCESS");

                            List<String> history = HistoryManager.getHistory(this.username);
                            if (!history.isEmpty()) {
                                for (String msg : history) {
                                    sendMessage(msg);
                                }
                            }
                        }
                    } else {
                        sendMessage("LOGIN_FAIL|Sai tài khoản hoặc mật khẩu!");
                    }
                }
                else if (command.equals("REGISTER") && parts.length == 3) {
                    String result = UserManager.register(parts[1], parts[2]);
                    if (result.equals("SUCCESS")) {
                        sendMessage("REGISTER_SUCCESS");
                    } else {
                        sendMessage("REGISTER_FAIL|" + result);
                    }
                }
            }

            ClientManager.addClient(this);
            String roleStr = username.equalsIgnoreCase("admin") ? " (Admin)" : "";
            ClientManager.broadcast(username + roleStr + " đã tham gia phòng chat");
            ServerFrame.updateLog("INFO", "Đăng nhập thành công: " + username + roleStr);
            ClientManager.broadcast(ClientManager.getOnlineUsers());

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/kick ")) {
                    if (this.username.equalsIgnoreCase("admin")) {
                        String targetUser = message.substring(6).trim();
                        ClientManager.kickUser(this.username, targetUser);
                    } else {
                        sendMessage("Hệ thống: Bạn không có quyền (Chỉ Admin mới được dùng lệnh này)!");
                    }
                }
                else if (message.startsWith("/ban ")) {
                    if (this.username.equalsIgnoreCase("admin")) {
                        String targetUser = message.substring(5).trim();
                        ClientManager.banUser(this.username, targetUser);
                    } else {
                        sendMessage("Hệ thống: Bạn không có quyền (Chỉ Admin mới được dùng lệnh này)!");
                    }
                }
                else if (message.startsWith("REVOKE_MSG|")) {
                    String targetMsg = message.substring(11);
                    boolean canRevoke = false;

                    if (this.username.equalsIgnoreCase("admin")) {
                        canRevoke = true;
                    } else if (targetMsg.contains(this.username + ": ") || targetMsg.contains(this.username + "|FILE_DATA|") || targetMsg.contains("[Bạn -> ")) {
                        canRevoke = true;
                    }

                    if (canRevoke) {
                        HistoryManager.revokeMessage(targetMsg, this.username);

                        // SỬA LỖI ĐỒNG BỘ TIN MẬT: Báo cho người nhận xóa cái bong bóng của họ
                        String otherSideMsg = "";
                        if (targetMsg.startsWith("[Bạn -> ")) {
                            String restOfMsg = targetMsg.substring(targetMsg.indexOf("]:") + 2);
                            otherSideMsg = "[Tin riêng từ " + this.username + "]:" + restOfMsg;
                        } else if (targetMsg.startsWith("[Tin riêng từ ")) {
                            String restOfMsg = targetMsg.substring(targetMsg.indexOf("]:") + 2);
                            otherSideMsg = "[Bạn -> " + this.username + "]:" + restOfMsg;
                        }

                        ClientManager.broadcast("REVOKE_UI|" + this.username + "|" + targetMsg);

                        if (!otherSideMsg.isEmpty()) {
                            ClientManager.broadcast("REVOKE_UI|" + this.username + "|" + otherSideMsg);
                        }

                        ServerFrame.updateLog("WARN", this.username + " đã thu hồi một tin nhắn.");
                    } else {
                        sendMessage("🔴 Hệ thống: Bạn không có quyền thu hồi tin nhắn của người khác!");
                    }
                }
                else if (message.equals("/clear_history")) {
                    UserManager.updateLastCleared(this.username);
                }
                else if (message.startsWith("@")) {
                    int firstSpace = message.indexOf(" ");
                    if (firstSpace != -1) {
                        String targetUser = message.substring(1, firstSpace);
                        String privateMsg = message.substring(firstSpace + 1);

                        if (targetUser.equalsIgnoreCase(this.username)) {
                            sendMessage("🔴 Hệ thống: Bạn không thể gửi tin nhắn riêng cho chính mình!");
                        } else {
                            ClientManager.sendPrivateMessage(username, targetUser, privateMsg);
                            HistoryManager.saveMessage("[PRIVATE]|" + this.username + "|" + targetUser + "|" + privateMsg);
                        }

                    } else {
                        sendMessage("Sai cú pháp tin riêng (@tên nội_dung)");
                    }
                }
                else if (message.contains("|FILE_DATA|")) {
                    ClientManager.broadcast(username + message);
                }
                else {
                    ClientManager.broadcast(username + ": " + message);
                }
            }

        } catch (Exception e) {
        } finally {
            if (username != null) {
                ClientManager.removeClient(this);
                ClientManager.broadcast(username + " đã rời phòng");
                ClientManager.broadcast(ClientManager.getOnlineUsers());
                ServerFrame.updateLog("WARN", "Client thoát: " + username);
            }
            disconnect();
        }
    }
}