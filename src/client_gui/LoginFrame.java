package client_gui;

import client.ChatClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Login window for the chat application.
 */
public class LoginFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());

    private JTextField txtHost = new JTextField("localhost");
    private JTextField txtPort = new JTextField("8888");
    private JTextField txtUsername = new JTextField();
    private JPasswordField txtPassword = new JPasswordField();
    private JButton btnLogin = new JButton("Đăng nhập");
    private JButton btnRegister = new JButton("Đăng ký");

    public LoginFrame() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to set look and feel", e);
        }
        setTitle("Hệ thống Chat - Đăng nhập");
        setSize(400, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        JLabel lblTitle = new JLabel("CHÀO MỪNG ĐẾN VỚI CHAT ROOM");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 15));
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        formPanel.setBackground(Color.WHITE);
        formPanel.add(new JLabel("IP Server:"));
        formPanel.add(txtHost);
        formPanel.add(new JLabel("Cổng (Port):"));
        formPanel.add(txtPort);
        formPanel.add(new JLabel("Tài khoản:"));
        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Mật khẩu:"));
        formPanel.add(txtPassword);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(120, 35));
        btnLogin.setBackground(new Color(46, 204, 113));
        btnLogin.setForeground(Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(120, 35));
        btnRegister.setBackground(new Color(52, 152, 219));
        btnRegister.setForeground(Color.WHITE);
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnLogin);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> performAuth("LOGIN"));
        btnRegister.addActionListener(e -> performAuth("REGISTER"));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void performAuth(String action) {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (host.isEmpty() || portStr.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port number.");
            return;
        }

        try {
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println(action + "|" + username + "|" + password);
            String response = in.readLine();

            if (response != null) {
                if (response.equals("LOGIN_SUCCESS")) {
                    new ChatFrame(new ChatClient(socket, in, out), username);
                    this.dispose();
                } else if (response.equals("REGISTER_SUCCESS")) {
                    JOptionPane.showMessageDialog(this, "✅ Đăng ký thành công!");
                    socket.close();
                } else {
                    String message = response.contains("|") ? response.substring(response.indexOf("|") + 1) : response;
                    JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
                    socket.close();
                }
            } else {
                JOptionPane.showMessageDialog(this, "No response from server.");
                socket.close();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage());
            LOGGER.log(Level.SEVERE, "Authentication error", ex);
        }
    }
}