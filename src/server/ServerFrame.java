package server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerFrame extends JFrame {
    private static JTextArea logArea = new JTextArea();
    private JTextField portField = new JTextField("8888");
    private JComboBox<String> protocolBox = new JComboBox<>(new String[]{"TCP/UDP", "TCP Only"});
    private JButton startButton = new JButton("> Khởi động");
    private JButton stopButton = new JButton("x Dừng");
    private JButton restartButton = new JButton("Khởi động lại");
    private JLabel statusLabel = new JLabel("TRẠNG THÁI: ĐANG DỪNG");
    private JPanel statusDot = new JPanel();

    private ChatServer chatServer;
    private Thread serverThread;

    private Color bgMain = new Color(240, 244, 248);
    private Color bgSidebar = new Color(24, 43, 73);
    private Color colorSuccess = new Color(46, 204, 113);
    private Color colorDanger = new Color(231, 76, 60);
    private Color colorWarning = new Color(243, 156, 18);

    public ServerFrame() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        setTitle("QUẢN TRỊ SERVER - BẢNG ĐIỀU KHIỂN");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- SIDEBAR ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBackground(bgSidebar);
        
        JLabel logoLabel = new JLabel("<html><div style='text-align: center;'><h2 style='color: white; margin-bottom: 0;'>CHAT ROOM</h2><p style='color: #8da2c0; margin-top: 0;'>Admin Panel</p></div></html>");
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setBorder(new EmptyBorder(20, 10, 30, 10));
        sidebar.add(logoLabel);

        // Bỏ emoji, thay bằng text chuyên nghiệp
        sidebar.add(createMenuButton("Dashboard", true));
        sidebar.add(createMenuButton("Logs", false));
        sidebar.add(createMenuButton("Cấu hình", false));
        sidebar.add(createMenuButton("Hệ thống", false));
        add(sidebar, BorderLayout.WEST);

        // --- MAIN CONTENT ---
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBackground(bgMain);
        mainContent.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("SERVER CONTROL PANEL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(40, 40, 40));

        JPanel statusWrapper = new CardPanel(); 
        statusWrapper.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        statusDot.setPreferredSize(new Dimension(15, 15));
        statusDot.setBackground(colorDanger);
        statusDot.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(Color.DARK_GRAY);
        statusWrapper.add(statusDot);
        statusWrapper.add(statusLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusWrapper, BorderLayout.EAST);
        mainContent.add(headerPanel, BorderLayout.NORTH);

        // --- CARDS ---
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        cardsPanel.setOpaque(false);

        CardPanel configCard = new CardPanel();
        configCard.setLayout(new BoxLayout(configCard, BoxLayout.Y_AXIS));
        configCard.add(createLabel("Cổng (Port):"));
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        portField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        configCard.add(portField);
        configCard.add(Box.createVerticalStrut(10));
        configCard.add(createLabel("Giao thức:"));
        protocolBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        configCard.add(protocolBox);
        configCard.add(Box.createVerticalStrut(15));
        
        JButton advBtn = new JButton("Tùy chọn khác");
        styleButton(advBtn, new Color(52, 73, 94));
        advBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        configCard.add(advBtn);

        CardPanel controlCard = new CardPanel();
        controlCard.setLayout(new GridLayout(3, 1, 0, 10));
        styleButton(startButton, colorSuccess);
        styleButton(stopButton, new Color(200, 100, 100)); 
        styleButton(restartButton, colorWarning);
        stopButton.setEnabled(false);
        restartButton.setEnabled(false);
        controlCard.add(startButton);
        controlCard.add(stopButton);
        controlCard.add(restartButton);

        CardPanel statsCard = new CardPanel();
        statsCard.setLayout(new GridLayout(3, 1, 0, 5));
        // Bỏ emoji
        statsCard.add(createStatRow("CPU Sử dụng:", "5%", colorSuccess));
        statsCard.add(createStatRow("RAM Sử dụng:", "2.1GB / 16GB", colorSuccess));
        statsCard.add(createStatRow("Băng thông:", "0 B/s", Color.GRAY));

        cardsPanel.add(configCard);
        cardsPanel.add(controlCard);
        cardsPanel.add(statsCard);

        // --- LOGS ---
        CardPanel logsCard = new CardPanel();
        logsCard.setLayout(new BorderLayout());
        logsCard.setBackground(new Color(40, 44, 52)); 
        
        JPanel logToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logToolbar.setOpaque(false);
        JButton btnClear = new JButton("Xóa Log");
        styleButton(btnClear, new Color(70, 75, 85));
        btnClear.setPreferredSize(new Dimension(100, 30));
        btnClear.addActionListener(e -> logArea.setText(""));
        logToolbar.add(btnClear);
        
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14)); 
        logArea.setBackground(new Color(40, 44, 52));  
        logArea.setForeground(new Color(170, 185, 200)); 
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setBorder(null);
        scrollLog.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        logsCard.add(logToolbar, BorderLayout.NORTH);
        logsCard.add(scrollLog, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(logsCard, BorderLayout.CENTER);
        
        mainContent.add(centerPanel, BorderLayout.CENTER);

        JLabel footerLabel = new JLabel("Hệ điều hành: Windows/Linux | Phiên bản panel: 2.1.0 | Người dùng: Admin");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainContent.add(footerLabel, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);

        // Bắt sự kiện
        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        
        setLocationRelativeTo(null);
        setVisible(true);
        
        updateLog("INFO", "Starting Control Panel interface...");
        updateLog("INFO", "Configuration loaded: Port 8888, Protocol TCP/UDP");
        updateLog("WARN", "Waiting for user action to start...");
    }

    private JButton createMenuButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setUI(new BasicButtonUI()); // Ép giao diện hiển thị đúng màu
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(isActive ? Color.WHITE : new Color(150, 170, 200));
        btn.setBackground(isActive ? new Color(41, 128, 185) : bgSidebar);
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setOpaque(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.DARK_GRAY);
        lbl.setBorder(new EmptyBorder(5, 0, 5, 0));
        return lbl;
    }

    private JPanel createStatRow(String title, String value, Color iconColor) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel lblTitle = new JLabel(title); lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel lblValue = new JLabel(value); lblValue.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblValue.setForeground(iconColor);
        p.add(lblTitle, BorderLayout.WEST); p.add(lblValue, BorderLayout.EAST);
        return p;
    }

    // Fix lỗi màu chữ trắng trên Windows
    private void styleButton(JButton btn, Color bg) {
        btn.setUI(new BasicButtonUI()); // Ép giao diện hiển thị đúng màu
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg); 
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); 
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // --- LOGIC GIAO DIỆN KẾT NỐI VỚI CHAT SERVER ---
    private void startServer() {
        int port;
        try { port = Integer.parseInt(portField.getText().trim()); } 
        catch (NumberFormatException ex) { updateLog("ERROR", "Cổng (Port) không hợp lệ!"); return; }
        
        chatServer = new ChatServer(port);
        serverThread = new Thread(() -> chatServer.start());
        serverThread.start();
        
        startButton.setEnabled(false); 
        stopButton.setEnabled(true); 
        stopButton.setBackground(colorDanger); 
        restartButton.setEnabled(true);
        portField.setEditable(false);
        statusLabel.setText("TRẠNG THÁI: ĐANG CHẠY");
        statusLabel.setForeground(colorSuccess);
        statusDot.setBackground(colorSuccess);
    }

    private void stopServer() {
        ClientManager.kickAll(); 
        if (chatServer != null) chatServer.stop();
        
        startButton.setEnabled(true); 
        stopButton.setEnabled(false); 
        stopButton.setBackground(new Color(200, 100, 100));
        restartButton.setEnabled(false);
        portField.setEditable(true);
        statusLabel.setText("TRẠNG THÁI: ĐANG DỪNG");
        statusLabel.setForeground(colorDanger);
        statusDot.setBackground(colorDanger);
        updateLog("WARN", "SERVER ĐÃ DỪNG HOẠT ĐỘNG.");
    }

    public static void updateLog(String level, String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + " " + level + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); 
        });
    }

    public static void updateLog(String message) {
        updateLog("INFO", message);
    }

    class CardPanel extends JPanel {
        public CardPanel() { setOpaque(false); setBorder(new EmptyBorder(15, 15, 15, 15)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            super.paintComponent(g);
        }
    }

    static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { this.thumbColor = new Color(100, 100, 100); this.trackColor = new Color(40, 44, 52); }
        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() { JButton btn = new JButton(); btn.setPreferredSize(new Dimension(0, 0)); return btn; }
    }
}