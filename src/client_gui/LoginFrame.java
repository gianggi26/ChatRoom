package client_gui;

import client.ChatClient;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JButton loginButton;

    // 👉 ĐỔI IP NÀY THÀNH IP MÁY CHẠY SERVER
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;

    public LoginFrame() {

        setTitle("Chat Login");
        setSize(300, 180);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        // ===== PANEL CENTER =====
        JPanel panel = new JPanel(new GridLayout(2,1,5,5));
        panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        panel.add(new JLabel("Username:"));

        usernameField = new JTextField();
        panel.add(usernameField);

        add(panel, BorderLayout.CENTER);

        // ===== LOGIN BUTTON =====
        loginButton = new JButton("Login");
        add(loginButton, BorderLayout.SOUTH);

        // ===== EVENT LOGIN =====
        loginButton.addActionListener(e -> connectToServer());

        setVisible(true);
    }

    /**
     * Kết nối server bằng thread riêng
     * -> tránh freeze UI
     */
    private void connectToServer() {

        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập username!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Connecting...");

        // ✅ NETWORK THREAD (QUAN TRỌNG)
        new Thread(() -> {
            try {

                ChatClient client =
                        new ChatClient(SERVER_IP, SERVER_PORT, username);

                // quay lại UI thread
                SwingUtilities.invokeLater(() -> {
                    new ChatFrame(client);
                    dispose();
                });

            } catch (Exception ex) {

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Không thể kết nối tới Server!\n" +
                                    "Kiểm tra:\n" +
                                    "- Server đã chạy chưa\n" +
                                    "- IP server đúng chưa\n" +
                                    "- Firewall",
                            "Connection Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                });
            }

        }).start();
    }
}