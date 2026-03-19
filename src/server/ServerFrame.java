package server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerFrame extends JFrame {
    private static JTextPane logArea = new JTextPane();
    private ModernTextField portField = new ModernTextField("8888");
    
    private JButton startButton = new ModernRoundedButton("KHỞI ĐỘNG");
    private JButton stopButton = new ModernRoundedButton("DỪNG");
    private JButton restartButton = new ModernRoundedButton("KHỞI ĐỘNG LẠI");
    private JLabel statusLabel = new JLabel("TRẠNG THÁI: ĐANG DỪNG");
    private JPanel statusDot = new JPanel();

    // --- CÁC NHÃN THỐNG KÊ REAL-TIME MỚI ---
    private JLabel lblUptime = new JLabel("00:00:00");
    private JLabel lblRam = new JLabel("0 MB");
    private JLabel lblThreads = new JLabel("0");
    private Timer statTimer;
    private int secondsUp = 0;

    private ChatServer chatServer;
    private Thread serverThread;

    private Color bgMain = new Color(210, 220, 233); 
    private Color colorSuccess = new Color(16, 185, 129); 
    private Color colorDanger = new Color(239, 68, 68);   
    private Color colorWarning = new Color(245, 158, 11); 
    private Color textDark = new Color(30, 41, 59);

    public ServerFrame() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        setTitle("QUẢN TRỊ SERVER - BẢNG ĐIỀU KHIỂN CHUYÊN NGHIỆP");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bgMain);

        // --- MAIN CONTENT ---
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(bgMain);
        mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("SERVER CONTROL PANEL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(textDark);

        JPanel statusWrapper = new CardPanel(); 
        statusWrapper.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        statusDot.setPreferredSize(new Dimension(16, 16));
        statusDot.setBackground(colorDanger);
        statusDot.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statusLabel.setForeground(new Color(100, 116, 139));
        statusWrapper.add(statusDot);
        statusWrapper.add(statusLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusWrapper, BorderLayout.EAST);
        mainContent.add(headerPanel, BorderLayout.NORTH);

        // --- CARDS (3 CỘT) ---
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);

        // Card 1: Cấu hình
        CardPanel configCard = new CardPanel();
        configCard.setLayout(new BoxLayout(configCard, BoxLayout.Y_AXIS));
        configCard.add(createLabel("Cổng (Port) hoạt động:"));
        configCard.add(Box.createVerticalStrut(15));
        
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        portField.setFont(new Font("Segoe UI", Font.BOLD, 22));
        portField.setHorizontalAlignment(JTextField.CENTER);
        configCard.add(portField);

        // Card 2: Nút điều khiển
        CardPanel controlCard = new CardPanel();
        controlCard.setLayout(new GridLayout(3, 1, 0, 12));
        
        startButton.setBackground(colorSuccess);
        stopButton.setBackground(colorDanger); 
        restartButton.setBackground(colorWarning);
        
        stopButton.setEnabled(false);
        restartButton.setEnabled(false);
        
        controlCard.add(startButton);
        controlCard.add(stopButton);
        controlCard.add(restartButton);

        // Card 3: THỐNG KÊ REAL-TIME (Đã thay thế code ảo)
        CardPanel statsCard = new CardPanel();
        statsCard.setLayout(new GridLayout(3, 1, 0, 15));
        
        lblUptime.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblUptime.setForeground(colorSuccess);
        
        lblRam.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRam.setForeground(new Color(59, 130, 246));
        
        lblThreads.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblThreads.setForeground(colorWarning);

        statsCard.add(createRealStatRow("THỜI GIAN CHẠY:", lblUptime));
        statsCard.add(createRealStatRow("RAM SỬ DỤNG:", lblRam));
        statsCard.add(createRealStatRow("LUỒNG XỬ LÝ:", lblThreads));

        cardsPanel.add(configCard);
        cardsPanel.add(controlCard);
        cardsPanel.add(statsCard);

        // --- LOGS ---
        CardPanel logsCard = new CardPanel();
        logsCard.setLayout(new BorderLayout());
        logsCard.setBackground(new Color(15, 23, 42)); 
        
        JPanel logToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logToolbar.setOpaque(false);
        
        JButton btnClear = new ModernRoundedButton("Xóa Log");
        btnClear.setBackground(new Color(51, 65, 85));
        btnClear.setPreferredSize(new Dimension(110, 32));
        btnClear.addActionListener(e -> logArea.setText(""));
        logToolbar.add(btnClear);
        
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14)); 
        logArea.setBackground(new Color(15, 23, 42));  
        logArea.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setBorder(null);
        scrollLog.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        logsCard.add(logToolbar, BorderLayout.NORTH);
        logsCard.add(scrollLog, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(logsCard, BorderLayout.CENTER);
        
        mainContent.add(centerPanel, BorderLayout.CENTER);

        JLabel footerLabel = new JLabel("Hệ điều hành: Windows/Linux | Phiên bản panel: 3.3 (Real-time Stats) | Người dùng: Admin");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(120, 135, 155));
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainContent.add(footerLabel, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);

        // --- BẮT SỰ KIỆN NÚT BẤM ---
        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        
        restartButton.addActionListener(e -> {
            updateLog("WARN", "Đang tiến hành khởi động lại Server...");
            stopServer();
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ex) {}
                SwingUtilities.invokeLater(() -> startServer());
            }).start();
        });
        
        setLocationRelativeTo(null);
        setVisible(true);
        
        updateLog("INFO", "Hệ thống UI đã được tải thành công.");
        updateLog("INFO", "Sẵn sàng lắng nghe tại Cổng: " + portField.getText());
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(textDark);
        lbl.setBorder(new EmptyBorder(5, 0, 5, 0));
        return lbl;
    }

    // Hàm tạo giao diện cho Thông số thật
    private JPanel createRealStatRow(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        
        JLabel lblTitle = new JLabel(title); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(71, 85, 105));
        
        panel.add(lblTitle, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.EAST);
        
        JPanel line = new JPanel();
        line.setBackground(new Color(226, 232, 240)); 
        line.setPreferredSize(new Dimension(100, 2));
        panel.add(line, BorderLayout.SOUTH);
        
        return panel;
    }

    private void startServer() {
        int port;
        try { port = Integer.parseInt(portField.getText().trim()); } 
        catch (NumberFormatException ex) { updateLog("ERROR", "Cổng (Port) không hợp lệ!"); return; }
        
        chatServer = new ChatServer(port);
        serverThread = new Thread(() -> chatServer.start());
        serverThread.start();
        
        startButton.setEnabled(false); 
        startButton.setBackground(new Color(16, 185, 129, 100)); 
        stopButton.setEnabled(true); 
        stopButton.setBackground(colorDanger); 
        restartButton.setEnabled(true);
        restartButton.setBackground(colorWarning);
        portField.setEditable(false);
        
        statusLabel.setText("TRẠNG THÁI: ĐANG CHẠY");
        statusLabel.setForeground(colorSuccess);
        statusDot.setBackground(colorSuccess);
        
        // --- KHỞI ĐỘNG BỘ ĐẾM THÔNG SỐ (Cập nhật mỗi 1 giây) ---
        secondsUp = 0;
        if (statTimer != null) statTimer.stop();
        statTimer = new Timer(1000, e -> {
            // 1. Cập nhật thời gian chạy
            secondsUp++;
            int h = secondsUp / 3600;
            int m = (secondsUp % 3600) / 60;
            int s = secondsUp % 60;
            lblUptime.setText(String.format("%02d:%02d:%02d", h, m, s));
            
            // 2. Cập nhật RAM thực tế đang dùng của Java
            Runtime rt = Runtime.getRuntime();
            long usedMB = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
            lblRam.setText(usedMB + " MB");
            
            // 3. Cập nhật Số luồng đang chạy
            lblThreads.setText(String.valueOf(Thread.activeCount()));
        });
        statTimer.start();
    }

    private void stopServer() {
        ClientManager.kickAll(); 
        if (chatServer != null) chatServer.stop();
        
        startButton.setEnabled(true); 
        startButton.setBackground(colorSuccess);
        stopButton.setEnabled(false); 
        stopButton.setBackground(new Color(239, 68, 68, 100)); 
        restartButton.setEnabled(false);
        restartButton.setBackground(new Color(245, 158, 11, 100)); 
        portField.setEditable(true);
        
        statusLabel.setText("TRẠNG THÁI: ĐANG DỪNG");
        statusLabel.setForeground(colorDanger);
        statusDot.setBackground(colorDanger);
        updateLog("ERROR", "SERVER ĐÃ BỊ DỪNG HOẠT ĐỘNG.");
        
        // --- DỪNG BỘ ĐẾM THÔNG SỐ ---
        if (statTimer != null) statTimer.stop();
        lblUptime.setText("00:00:00");
        lblRam.setText("0 MB");
        lblThreads.setText("0");
    }

    public static void updateLog(String level, String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            StyledDocument doc = logArea.getStyledDocument();
            
            Color levelColor = Color.WHITE;
            if (level.equals("INFO")) levelColor = new Color(56, 189, 248);     
            else if (level.equals("WARN")) levelColor = new Color(250, 204, 21); 
            else if (level.equals("ERROR")) levelColor = new Color(248, 113, 113);

            try {
                Style timeStyle = logArea.addStyle("Time", null);
                StyleConstants.setForeground(timeStyle, new Color(148, 163, 184));
                doc.insertString(doc.getLength(), "[" + timestamp + "] ", timeStyle);
                
                Style levelStyle = logArea.addStyle("Level", null);
                StyleConstants.setForeground(levelStyle, levelColor);
                StyleConstants.setBold(levelStyle, true);
                doc.insertString(doc.getLength(), "[" + level + "] ", levelStyle);
                
                Style msgStyle = logArea.addStyle("Msg", null);
                StyleConstants.setForeground(msgStyle, new Color(241, 245, 249));
                doc.insertString(doc.getLength(), message + "\n", msgStyle);
                
                logArea.setCaretPosition(doc.getLength()); 
            } catch (Exception e) {}
        });
    }

    public static void updateLog(String message) {
        updateLog("INFO", message);
    }

    class CardPanel extends JPanel {
        public CardPanel() { setOpaque(false); setBorder(new EmptyBorder(18, 18, 18, 18)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(0, 0, 0, 10)); 
            g2d.fill(new RoundRectangle2D.Double(3, 4, getWidth()-6, getHeight()-6, 20, 20));
            g2d.setColor(Color.WHITE); 
            g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth()-4, getHeight()-4, 20, 20));
            super.paintComponent(g);
        }
    }

    static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { this.thumbColor = new Color(71, 85, 105); this.trackColor = new Color(15, 23, 42); }
        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() { JButton btn = new JButton(); btn.setPreferredSize(new Dimension(0, 0)); return btn; }
    }

    class ModernTextField extends JTextField {
        public ModernTextField(String text) {
            super(text);
            setOpaque(false);
            setBorder(new EmptyBorder(5, 15, 5, 15));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(245, 247, 250)); 
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25); 
            g2.setColor(new Color(200, 210, 220)); 
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    class ModernRoundedButton extends JButton {
        public ModernRoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 15)); 
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); 
            super.paintComponent(g);
            g2.dispose();
        }
    }
}