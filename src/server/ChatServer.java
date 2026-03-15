package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private int port;
    private ServerSocket serverSocket;
    private boolean isRunning = false;

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            ServerFrame.updateLog("INFO", "========================================");
            ServerFrame.updateLog("INFO", "SERVER ĐÃ KHỞI ĐỘNG THÀNH CÔNG (Port: " + port + ")");
            ServerFrame.updateLog("INFO", "========================================");

            while (isRunning) {
                Socket socket = serverSocket.accept();
                ServerFrame.updateLog("INFO", "Kết nối mới từ IP: " + socket.getInetAddress().getHostAddress());
                
                ClientHandler handler = new ClientHandler(socket);
                handler.start();
            }
        } catch (IOException e) {
            if (isRunning) {
                ServerFrame.updateLog("ERROR", "Lỗi Server: " + e.getMessage());
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            ServerFrame.updateLog("ERROR", "Lỗi khi đóng ServerSocket: " + e.getMessage());
        }
    }
}