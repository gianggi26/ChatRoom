package client_gui;

import client.ChatClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LoginFrame extends JFrame {
    private JTextField txtHost = new RoundedTextField("localhost");
    private JTextField txtPort = new RoundedTextField("8888");
    private JComboBox<String> cbProtocol = new JComboBox<>(new String[]{"TCP/UDP", "TCP Only"});
    
    private JTextField txtUsername = new RoundedTextField("");
    private JPasswordField txtPassword = new RoundedPasswordField();
    private JCheckBox chkRemember = new JCheckBox("Nhớ mật khẩu?");
    
    // Bảng màu chuẩn theo thiết kế
    private Color bgMain = new Color(243, 244, 246);       // Xám rất nhẹ
    private Color bgNavy = new Color(30, 41, 59);          // Xanh navy đậm
    private Color colorLogin = new Color(26, 188, 156);    // Xanh ngọc (Nút Đăng nhập)
    private Color colorRegister = new Color(41, 128, 185); // Xanh dương (Nút Đăng ký)

    public LoginFrame() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        
        setTitle("Hệ thống Chat - Đăng nhập");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bgMain);

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
        // 2. KHU VỰC CÁC THẺ (CARDS) - BỐ CỤC NGANG
        // ==========================================
        JPanel cardsContainer = new JPanel(new GridLayout(1, 2, 25, 0));
        cardsContainer.setBackground(bgMain);
        cardsContainer.setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- THẺ TRÁI: CẤU HÌNH SERVER ---
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
        
        loginCard.add(lblLoginTitle);
        loginCard.add(Box.createVerticalStrut(25));
        loginCard.add(createInputGroup("Tài khoản:", txtUsername));
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
        
        optionPanel.add(chkRemember, BorderLayout.WEST);
        optionPanel.add(lblForgot, BorderLayout.EAST);
        optionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        loginCard.add(Box.createVerticalStrut(5));
        loginCard.add(optionPanel);
        
        loginCard.add(Box.createVerticalStrut(20));
        
        // Nút bấm Đăng nhập & Đăng ký
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

        // Thêm 2 thẻ vào vùng chứa
        cardsContainer.add(configCard);
        cardsContainer.add(loginCard);
        add(cardsContainer, BorderLayout.CENTER);

        // ==========================================
        // 3. FOOTER (Thanh Chân Trang)
        // ==========================================
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
            new EmptyBorder(10, 20, 10, 20)
        ));
        
        // Group chứa Đèn LED và chữ Trạng thái
        JPanel statusGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusGroup.setOpaque(false);
        
        // Tự vẽ đèn LED xanh lá
        JPanel statusDot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(34, 197, 94)); // Màu xanh lá chuẩn
                g2d.fillOval(0, 3, 10, 10);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(12, 16); }
        };
        
        JLabel lblStatus = new JLabel("Tình trạng kết nối: Khả dụng | Online");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(new Color(34, 197, 94));
        
        statusGroup.add(statusDot);
        statusGroup.add(lblStatus);
        
        JLabel lblVersion = new JLabel("Phiên bản panel: 2.1.0 | Giúp đỡ (Help)");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVersion.setForeground(new Color(148, 163, 184));
        
        footerPanel.add(statusGroup, BorderLayout.WEST);
        footerPanel.add(lblVersion, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);

        // --- SỰ KIỆN NÚT BẤM ---
        btnLogin.addActionListener(e -> performAuth("LOGIN"));
        btnRegister.addActionListener(e -> performAuth("REGISTER"));
        
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // --- CÁC HÀM TIỆN ÍCH TẠO UI ---
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

    // --- LOGIC MẠNG ---
    private void performAuth(String action) {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (host.isEmpty() || portStr.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        int port;
        try { port = Integer.parseInt(portStr); } 
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Cổng không hợp lệ."); return; }

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
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Không thể kết nối tới Server!");
        }
    }

    // --- CÁC CLASS ĐỒ HỌA (UI COMPONENTS) ---
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