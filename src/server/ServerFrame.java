package server;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerFrame extends JFrame {
    private static JTextArea logArea = new JTextArea();
    private JButton startButton = new JButton("▶ Khởi động Server");
    private JButton stopButton = new JButton("⏹ Dừng Server");
    private JTextField portField = new JTextField("8888", 5);

    private ServerSocket serverSocket;
    private Thread serverThread;
    private boolean isRunning = false;

    public ServerFrame() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        setTitle("Quản trị Server - Đồ án Lập trình mạng");
        setSize(600, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14)); 
        logArea.setBackground(new Color(40, 42, 54)); 
        logArea.setForeground(new Color(248, 248, 242)); 

        startButton.setBackground(new Color(46, 204, 113));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        stopButton.setBackground(new Color(231, 76, 60));
        stopButton.setForeground(Color.WHITE);
        stopButton.setFocusPainted(false);
        stopButton.setEnabled(false); 

        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        topPanel.add(new JLabel("Cổng (Port):"));
        topPanel.add(portField);
        topPanel.add(startButton);
        topPanel.add(stopButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        setLocationRelativeTo(null); 
        setVisible(true);
    }

    private void startServer() {
        int port = Integer.parseInt(portField.getText().trim());
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                isRunning = true;
                updateLog("🟢 SERVER ĐÃ KHỞI ĐỘNG (Port: " + port + ")");
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(false); stopButton.setEnabled(true); portField.setEditable(false);
                });

                while (isRunning) {
                    Socket socket = serverSocket.accept();
                    updateLog("⚡ Có kết nối mới từ IP: " + socket.getInetAddress().getHostAddress());
                    ClientHandler clientHandler = new ClientHandler(socket);
                    clientHandler.start(); // <-- Đã xóa AddClient ở đây để hết lỗi Null
                }
            } catch (IOException e) { if (isRunning) updateLog("❌ Lỗi Server: " + e.getMessage()); }
        });
        serverThread.start();
    }

    private void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close(); 
            updateLog("🔴 SERVER ĐÃ DỪNG.");
            startButton.setEnabled(true); stopButton.setEnabled(false); portField.setEditable(true);
        } catch (IOException e) {}
    }

    public static void updateLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); 
        });
    }

    public static void main(String[] args) { new ServerFrame(); }
}