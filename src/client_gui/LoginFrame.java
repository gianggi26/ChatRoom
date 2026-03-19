package client_gui;

import client.ChatClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.prefs.Preferences;

public class LoginFrame extends JFrame {
    private JTextField txtHost = new RoundedTextField("localhost");
    private JTextField txtPort = new RoundedTextField("8888");
    
    // SỬA ĐỔI 1: Thay ô nhập text thường thành ô thả xuống (Dropdown)
    private JComboBox<String> cbUsername = new JComboBox<>();
    private JPasswordField txtPassword = new RoundedPasswordField();
    private JCheckBox chkRemember = new JCheckBox("Nhớ mật khẩu?");
    private JLabel lblStatus; 
    
    private Preferences prefs;
    
    private Color bgMain = new Color(243, 244, 246);       
    private Color bgNavy = new Color(30, 41, 59);          
    private Color colorLogin = new Color(26, 188, 156);    
    private Color colorRegister = new Color(41, 128, 185); 

    public LoginFrame() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        
        setTitle("Hệ thống Chat - Đăng nhập");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bgMain);

        prefs = Preferences.userNodeForPackage(this.getClass());

        // ==========================================
        // 1. HEADER (Thanh Tiêu Đề)
        // ==========================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(bgNavy);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel lblHeaderTitle = new JLabel("CHÀO MỪNG ĐẾN VỚI CHAT ROOM");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeaderTitle.setForeground(Color.WHITE);
        headerPanel.add(lblHeaderTitle, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. KHU VỰC CÁC THẺ (CARDS)
        // ==========================================
        JPanel cardsContainer = new JPanel(new GridLayout(1, 2, 25, 0));
        cardsContainer.setBackground(bgMain);
        cardsContainer.setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- THẺ TRÁI: CẤU HÌNH ---
        CardPanel configCard = new CardPanel();
        configCard.setLayout(new BoxLayout(configCard, BoxLayout.Y_AXIS));
        
        JLabel lblConfigTitle = new JLabel("CẤU HÌNH SERVER");
        lblConfigTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblConfigTitle.setForeground(bgNavy);
        lblConfigTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        configCard.add(lblConfigTitle);
        configCard.add(Box.createVerticalStrut(25));
        configCard.add(createInputGroup("IP Server:", txtHost));
        configCard.add(Box.createVerticalStrut(15));
        configCard.add(createInputGroup("Cổng (Port):", txtPort));
        configCard.add(Box.createVerticalStrut(15));

        // --- THẺ PHẢI: ĐĂNG NHẬP ---
        CardPanel loginCard = new CardPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        
        JLabel lblLoginTitle = new JLabel("THÔNG TIN TÀI KHOẢN");
        lblLoginTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLoginTitle.setForeground(bgNavy);
        lblLoginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // CẤU HÌNH Ô THẢ XUỐNG CHO USERNAME
        cbUsername.setEditable(true); // Cho phép vừa gõ chữ mới, vừa chọn từ danh sách
        cbUsername.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbUsername.setBackground(Color.WHITE);
        // Bắt sự kiện: Mỗi khi chọn 1 nick trong danh sách, tự động điền mật khẩu của người đó
        cbUsername.addActionListener(e -> {
            String selected = (String) cbUsername.getSelectedItem();
            if (selected != null && !selected.trim().isEmpty()) {
                String pass = prefs.get("PASS_" + selected.trim(), "");
                if (!pass.isEmpty()) {
                    txtPassword.setText(pass);
                    chkRemember.setSelected(true);
                } else {
                    txtPassword.setText("");
                    chkRemember.setSelected(false);
                }
            }
        });

        loginCard.add(lblLoginTitle);
        loginCard.add(Box.createVerticalStrut(25));
        loginCard.add(createInputGroup("Tài khoản:", cbUsername)); // Đã đổi thành cbUsername
        loginCard.add(Box.createVerticalStrut(15));
        loginCard.add(createInputGroup("Mật khẩu:", txtPassword));
        
        // Hàng Checkbox & Quên mật khẩu
        JPanel optionPanel = new JPanel(new BorderLayout());
        optionPanel.setOpaque(false);
        chkRemember.setOpaque(false);
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JLabel lblForgot = new JLabel("<html><u>Quên mật khẩu?</u></html>");
        lblForgot.setForeground(colorRegister);
        lblForgot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        lblForgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this, 
                    "Tính năng cấp lại mật khẩu tự động đang được bảo trì.\n" +
                    "Vui lòng liên hệ Admin (Hotline: 0987.xxx.xxx) để được hỗ trợ!", 
                    "Hỗ trợ", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        optionPanel.add(chkRemember, BorderLayout.WEST);
        optionPanel.add(lblForgot, BorderLayout.EAST);
        optionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        loginCard.add(Box.createVerticalStrut(5));
        loginCard.add(optionPanel);
        
        loginCard.add(Box.createVerticalStrut(20));
        
        // Nút bấm
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnRegister = new JButton("ĐĂNG KÝ");
        styleButton(btnRegister, colorRegister, Color.WHITE);
        JButton btnLogin = new JButton("ĐĂNG NHẬP");
        styleButton(btnLogin, colorLogin, Color.WHITE);
        
        btnPanel.add(btnRegister);
        btnPanel.add(btnLogin);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginCard.add(btnPanel);

        cardsContainer.add(configCard);
        cardsContainer.add(loginCard);
        add(cardsContainer, BorderLayout.CENTER);

        // ==========================================
        // 3. FOOTER
        // ==========================================
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
            new EmptyBorder(10, 20, 10, 20)
        ));
        
        JPanel statusGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusGroup.setOpaque(false);
        JPanel statusDot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(34, 197, 94));
                g2d.fillOval(0, 3, 10, 10);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(12, 16); }
        };
        
        lblStatus = new JLabel("Tình trạng kết nối: Khả dụng | Online");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(new Color(34, 197, 94));
        
        statusGroup.add(statusDot);
        statusGroup.add(lblStatus);
        
        JLabel lblVersion = new JLabel("Phiên bản panel: 2.2.0 (Multi-Account) | Giúp đỡ");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVersion.setForeground(new Color(148, 163, 184));
        
        footerPanel.add(statusGroup, BorderLayout.WEST);
        footerPanel.add(lblVersion, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);

        // --- NẠP DANH SÁCH TÀI KHOẢN ---
        loadSavedAccounts();

        btnLogin.addActionListener(e -> performAuth("LOGIN"));
        btnRegister.addActionListener(e -> performAuth("REGISTER"));
        
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // --- SỬA ĐỔI 2: HÀM NẠP ĐA TÀI KHOẢN ---
    private void loadSavedAccounts() {
        // Lấy danh sách các tài khoản đã lưu (dạng chuỗi cắt nhau bởi dấu phẩy)
        String savedUsers = prefs.get("SAVED_USERS", "");
        if (!savedUsers.isEmpty()) {
            String[] users = savedUsers.split(",");
            for (String u : users) {
                if (!u.trim().isEmpty()) {
                    cbUsername.addItem(u.trim());
                }
            }
            // Mặc định hiển thị người đăng nhập gần đây nhất
            String lastUser = prefs.get("LAST_USER", "");
            if (!lastUser.isEmpty()) {
                cbUsername.setSelectedItem(lastUser);
                txtPassword.setText(prefs.get("PASS_" + lastUser, ""));
                chkRemember.setSelected(true);
            }
        }
    }

    // --- SỬA ĐỔI 3: HÀM LƯU ĐA TÀI KHOẢN ---
    private void saveAccountStatus() {
        Object selectedObj = cbUsername.getSelectedItem();
        if (selectedObj == null) return;
        String user = ((String) selectedObj).trim();
        if (user.isEmpty()) return;

        if (chkRemember.isSelected()) {
            // Lưu mật khẩu của người này
            prefs.put("PASS_" + user, new String(txtPassword.getPassword()));
            // Ghi nhớ đây là người đăng nhập cuối cùng
            prefs.put("LAST_USER", user);
            
            // Thêm người này vào danh sách đề xuất (nếu chưa có)
            String savedUsers = prefs.get("SAVED_USERS", "");
            if (!savedUsers.contains(user)) {
                savedUsers = savedUsers.isEmpty() ? user : savedUsers + "," + user;
                prefs.put("SAVED_USERS", savedUsers);
            }
        } else {
            // Nếu bỏ tích, chỉ xóa mật khẩu, giữ tên trong danh sách đề xuất
            prefs.remove("PASS_" + user);
        }
    }

    private void performAuth(String action) {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();
        
        // Lấy chữ từ ô ComboBox thay vì TextField
        Object userObj = cbUsername.getSelectedItem();
        String username = userObj == null ? "" : ((String) userObj).trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (host.isEmpty() || portStr.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        int port;
        try { port = Integer.parseInt(portStr); } 
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Cổng không hợp lệ."); return; }

        lblStatus.setText("Tình trạng kết nối: Đang xử lý...");
        
        new Thread(() -> {
            try {
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                out.println(action + "|" + username + "|" + password);
                String response = in.readLine();

                SwingUtilities.invokeLater(() -> {
                    if (response != null) {
                        if (response.equals("LOGIN_SUCCESS")) {
                            saveAccountStatus(); // LƯU VÀO DANH SÁCH TẠI ĐÂY
                            lblStatus.setText("Tình trạng kết nối: Đăng nhập thành công!");
                            
                            new ChatFrame(new ChatClient(socket, in, out), username);
                            this.dispose();
                            
                        } else if (response.equals("REGISTER_SUCCESS")) {
                            JOptionPane.showMessageDialog(this, "✅ Đăng ký thành công! Vui lòng bấm Đăng nhập.");
                            lblStatus.setText("Tình trạng kết nối: Khả dụng | Online");
                            try { socket.close(); } catch (Exception ex) {}
                            
                        } else {
                            String message = response.contains("|") ? response.substring(response.indexOf("|") + 1) : response;
                            JOptionPane.showMessageDialog(this, message, "Lỗi đăng nhập", JOptionPane.WARNING_MESSAGE);
                            lblStatus.setText("Tình trạng kết nối: Lỗi xác thực");
                            try { socket.close(); } catch (Exception ex) {}
                        }
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "❌ Không thể kết nối tới Server!\nVui lòng kiểm tra IP hoặc xem Server đã bật chưa.");
                    lblStatus.setText("Tình trạng kết nối: Mất kết nối");
                });
            }
        }).start();
    }

    // --- CÁC CLASS ĐỒ HỌA ---
    private JPanel createInputGroup(String labelStr, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(labelStr);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(71, 85, 105));
        input.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        return panel;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setUI(new BasicButtonUI());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    class CardPanel extends JPanel {
        public CardPanel() { setOpaque(false); setBorder(new EmptyBorder(25, 25, 25, 25)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            super.paintComponent(g);
        }
    }

    class RoundedTextField extends JTextField {
        public RoundedTextField(String text) {
            super(text); setOpaque(false); setBorder(new EmptyBorder(5, 15, 5, 15)); setFont(new Font("Segoe UI", Font.PLAIN, 15));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(248, 250, 252));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, 10, 10));
            g2.setColor(new Color(203, 213, 225));
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, 10, 10));
            super.paintComponent(g);
        }
    }

    class RoundedPasswordField extends JPasswordField {
        public RoundedPasswordField() {
            setOpaque(false); setBorder(new EmptyBorder(5, 15, 5, 15)); setFont(new Font("Segoe UI", Font.PLAIN, 15));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(248, 250, 252));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, 10, 10));
            g2.setColor(new Color(203, 213, 225));
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, 10, 10));
            super.paintComponent(g);
        }
    }
}