package client_gui;

import client.ChatClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ChatFrame extends JFrame {
    private JPanel messagePanel = new JPanel(); 
    private JScrollPane scrollChat;
    private JTextField inputField = new JTextField();
    private DefaultListModel<String> userListModel = new DefaultListModel<>();
    private JList<String> userList = new JList<>(userListModel);
    
    private Color primaryColor = new Color(0, 132, 255); 
    private Color privateColor = new Color(142, 68, 173); // Tím cho tin riêng
    private Color backgroundColor = new Color(240, 242, 245);
    private String currentUser;
    private Map<String, ImageIcon> userAvatars = new HashMap<>();

    public ChatFrame(ChatClient client, String username) {
        this.currentUser = username;
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        
        // Đã xóa bỏ thương hiệu Zalo
        setTitle("Đồ án Chat Room - " + username);
        setSize(1050, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- SIDEBAR ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JPanel sidebarHeader = new JPanel(new BorderLayout());
        sidebarHeader.setBackground(Color.WHITE);
        sidebarHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));
        JLabel lblHeader = new JLabel("  Trực tuyến");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setBorder(new EmptyBorder(15, 10, 15, 15));
        sidebarHeader.add(lblHeader, BorderLayout.NORTH);

        userList.setCellRenderer(new ProUserListCellRenderer());
        userList.setFixedCellHeight(65);
        userList.setSelectionBackground(new Color(231, 243, 255));
        JScrollPane scrollSidebar = new JScrollPane(userList);
        scrollSidebar.setBorder(null);

        sidebar.add(sidebarHeader, BorderLayout.NORTH);
        sidebar.add(scrollSidebar, BorderLayout.CENTER);

        // --- MAIN CHAT AREA ---
        JPanel mainChatPanel = new JPanel(new BorderLayout());
        mainChatPanel.setBackground(backgroundColor);

        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(Color.WHITE);
        chatHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        JLabel chatTitle = new JLabel("Phòng Chat Chung");
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chatTitle.setBorder(new EmptyBorder(15, 20, 15, 15));
        chatHeader.add(chatTitle, BorderLayout.WEST);

        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(backgroundColor);
        messagePanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        scrollChat = new JScrollPane(messagePanel);
        scrollChat.setBorder(null);
        scrollChat.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mainChatPanel.add(chatHeader, BorderLayout.NORTH);
        mainChatPanel.add(scrollChat, BorderLayout.CENTER);

        // --- BOTTOM PANEL ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBackground(Color.WHITE);

        JButton emojiBtn = new JButton("Biểu tượng");
        emojiBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        emojiBtn.setForeground(new Color(80, 80, 80));
        emojiBtn.setBorderPainted(false); emojiBtn.setContentAreaFilled(false); emojiBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        emojiBtn.addActionListener(e -> showEmojiPicker());
        
        JButton fileBtn = new JButton("Đính kèm");
        fileBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fileBtn.setForeground(new Color(80, 80, 80));
        fileBtn.setBorderPainted(false); fileBtn.setContentAreaFilled(false); fileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fileBtn.addActionListener(e -> selectAndSendFile(client));
        
        toolBar.add(emojiBtn);
        toolBar.add(fileBtn);

        JPanel inputWrapper = new JPanel(new BorderLayout(10, 0));
        inputWrapper.setBackground(Color.WHITE);
        inputWrapper.setBorder(new EmptyBorder(10, 20, 15, 20));

     // Dùng font đa năng "SansSerif" để Java tự động mix cả Tiếng Việt và Icon
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(12, 15, 12, 15)
        ));

        JButton btnSend = new JButton("GỬI");
        btnSend.setUI(new BasicButtonUI()); 
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setBackground(primaryColor);
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setBorderPainted(false); 
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.setPreferredSize(new Dimension(90, 45));

        inputWrapper.add(inputField, BorderLayout.CENTER);
        inputWrapper.add(btnSend, BorderLayout.EAST);

        bottomPanel.add(toolBar, BorderLayout.NORTH);
        bottomPanel.add(inputWrapper, BorderLayout.CENTER);

        mainChatPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
        add(mainChatPanel, BorderLayout.CENTER);

        btnSend.addActionListener(e -> sendAction(client));
        inputField.addActionListener(e -> sendAction(client));

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userList.getSelectedValue() != null) {
                String target = userList.getSelectedValue().replace(" (Admin)", "");
                inputField.setText("@" + target + " ");
                inputField.requestFocus();
            }
        });

        startMessageListener(client);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private ImageIcon generateAvatar(String name) {
        if (userAvatars.containsKey(name)) return userAvatars.get(name);
        
        int size = 42;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int hash = Math.abs(name.hashCode());
        Color bgColor = new Color((hash & 0xFF0000) >> 16, (hash & 0x00FF00) >> 8, hash & 0x0000FF).brighter();
        
        g2d.setColor(bgColor);
        g2d.fillOval(0, 0, size, size);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
        String letter = name.length() > 0 ? name.substring(0, 1).toUpperCase() : "?";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (size - fm.stringWidth(letter)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(letter, x, y);
        g2d.dispose();
        
        ImageIcon icon = new ImageIcon(img);
        userAvatars.put(name, icon);
        return icon;
    }

    private void showEmojiPicker() {
        JDialog emojiDialog = new JDialog(this, "Chọn Emoji", false);
        JPanel emojiPanel = new JPanel(new GridLayout(2, 5));
        String[] emojis = {"😀", "😂", "😍", "😢", "😡", "👍", "❤️", "🔥", "👏", "🤔"};
        for (String em : emojis) {
            JButton btn = new JButton(em);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            btn.setBorderPainted(false); btn.setContentAreaFilled(false);
            btn.addActionListener(e -> { inputField.setText(inputField.getText() + em); inputField.requestFocus(); });
            emojiPanel.add(btn);
        }
        emojiDialog.add(emojiPanel);
        emojiDialog.pack();
        emojiDialog.setLocationRelativeTo(inputField);
        emojiDialog.setVisible(true);
    }

    private void sendAction(ChatClient client) {
        String text = inputField.getText().trim();
        if (!text.isEmpty()) {
            client.sendMessage(text);
            inputField.setText("");
        }
    }

    private void startMessageListener(ChatClient client) {
        new Thread(() -> {
            try {
                String message;
                while ((message = client.getReader().readLine()) != null) {
                    if (message.startsWith("LIST_USERS|")) {
                        updateUserList(message.substring(11));
                    } else if (message.contains("|FILE_DATA|")) {
                        handleIncomingFile(message);
                    } else if (message.startsWith("KICKED|")) {
                        JOptionPane.showMessageDialog(this, message.substring(7), "Ngắt kết nối", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    } else {
                        parseAndDisplayMessage(message);
                    }
                }
            } catch (Exception e) {}
        }).start();
    }

    // --- FIX HIỂN THỊ TÊN NGƯỜI NHẮN RÕ RÀNG Ở TRÊN BONG BÓNG ---
    private void parseAndDisplayMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            boolean isSystem = msg.startsWith("🟢") || msg.startsWith("🔴") || msg.startsWith("⚠️") || msg.startsWith("❌") || msg.startsWith("📝") || msg.startsWith("🔒");
            if (isSystem) {
                String cleanMsg = msg.substring(1).trim(); 
                JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
                row.setBackground(backgroundColor);
                JLabel lblSys = new JLabel(cleanMsg);
                lblSys.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                lblSys.setForeground(new Color(130, 130, 130));
                row.add(lblSys);
                addBubbleToPanel(row);
                return;
            }

            boolean isPrivate = false;
            boolean isOwnMessage = false;
            String senderName = "System";
            String content = msg;
            String headerText = ""; // Biến lưu tên hiển thị trên bong bóng

            if (msg.startsWith("[Tin riêng từ ")) {
                isPrivate = true;
                int colonIdx = msg.indexOf("]: ");
                if (colonIdx != -1) {
                    senderName = msg.substring(14, colonIdx); 
                    content = msg.substring(colonIdx + 3);
                    headerText = "🔒 Tin mật từ " + senderName;
                }
            } else if (msg.startsWith("[Bạn -> ")) {
                isPrivate = true;
                isOwnMessage = true;
                int colonIdx = msg.indexOf("]: ");
                if (colonIdx != -1) {
                    senderName = currentUser;
                    String target = msg.substring(8, colonIdx);
                    content = msg.substring(colonIdx + 3);
                    headerText = "🔒 Gửi mật cho " + target;
                }
            } else if (msg.startsWith(currentUser + ": ")) {
                isOwnMessage = true;
                senderName = currentUser;
                content = msg.substring(currentUser.length() + 2);
                headerText = "Bạn";
            } else if (msg.contains(": ")) {
                int colonIdx = msg.indexOf(": ");
                senderName = msg.substring(0, colonIdx);
                content = msg.substring(colonIdx + 2);
                headerText = senderName; // Hiện tên của người khác trong nhóm chat
            }

            int bubbleWidth = Math.min(content.length() * 8, 300);
            if (bubbleWidth < 30) bubbleWidth = 30;
            String htmlText = "<html><div style='width: %dpx; font-family: \"Segoe UI Emoji\", sans-serif;'>%s</div></html>";
            JLabel lblText = new JLabel(String.format(htmlText, bubbleWidth, content.replace("\n", "<br>")));
            lblText.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
            lblText.setForeground(isOwnMessage ? Color.WHITE : Color.BLACK);

            ChatBubble bubble = new ChatBubble(lblText, isOwnMessage, isPrivate);
            
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(backgroundColor);
            row.setBorder(new EmptyBorder(5, 0, 5, 0));

            JLabel avatarLabel = new JLabel(generateAvatar(senderName.replace(" (Admin)", "")));
            avatarLabel.setBorder(new EmptyBorder(0, isOwnMessage ? 10 : 0, 0, isOwnMessage ? 0 : 10));

            // Đóng gói Bong bóng và Tên người gửi vào chung 1 khối
            JPanel bubbleContentWrapper = new JPanel();
            bubbleContentWrapper.setLayout(new BoxLayout(bubbleContentWrapper, BoxLayout.Y_AXIS));
            bubbleContentWrapper.setOpaque(false);
            
            if (headerText != null && !headerText.isEmpty()) {
                JLabel lblName = new JLabel(headerText);
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblName.setForeground(new Color(130, 130, 130)); // Chữ tên xám nhẹ
                
                JPanel nameRow = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
                nameRow.setOpaque(false);
                nameRow.add(lblName);
                bubbleContentWrapper.add(nameRow);
            }
            
            JPanel bubbleRow = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            bubbleRow.setOpaque(false);
            bubbleRow.add(bubble);
            bubbleContentWrapper.add(bubbleRow);

            if (isOwnMessage) {
                row.add(bubbleContentWrapper, BorderLayout.CENTER);
                row.add(avatarLabel, BorderLayout.EAST);
            } else {
                row.add(avatarLabel, BorderLayout.WEST);
                row.add(bubbleContentWrapper, BorderLayout.CENTER);
            }

            addBubbleToPanel(row);
        });
    }

    private void addBubbleToPanel(Component comp) {
        messagePanel.add(comp);
        messagePanel.revalidate();
        messagePanel.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollChat.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void updateUserList(String data) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String u : data.split(",")) if (!u.isEmpty()) userListModel.addElement(u);
        });
    }

    private void selectAndSendFile(ChatClient client) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file.length() > 5 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this, "Chỉ hỗ trợ file dưới 5MB.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                String encodedString = Base64.getEncoder().encodeToString(fileContent);
                String target = inputField.getText().trim();
                String prefix = target.startsWith("@") ? target.split(" ")[0] + " " : "";
                client.sendMessage(prefix + "|FILE_DATA|" + file.getName() + "|" + encodedString);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi đọc file!"); }
        }
    }

    // --- XỬ LÝ NHẬN FILE/ẢNH CÓ KÈM TÊN ---
    private void handleIncomingFile(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                String[] parts = message.split("\\|FILE_DATA\\|");
                String senderInfo = parts[0];
                String fileName = parts[1].split("\\|")[0];
                String base64Data = parts[1].split("\\|")[1];

                boolean isPrivate = senderInfo.contains("[Tin riêng từ ") || senderInfo.contains("[Bạn -> ");
                boolean isOwnMessage = senderInfo.equals(currentUser) || senderInfo.contains("[Bạn -> ");
                String senderName = "System";
                String headerText = "";

                if (senderInfo.contains("[Tin riêng từ ")) {
                    senderName = senderInfo.substring(senderInfo.indexOf("từ ") + 3, senderInfo.indexOf("]"));
                    headerText = "🔒 File mật từ " + senderName;
                } else if (senderInfo.contains("[Bạn -> ")) {
                    senderName = currentUser;
                    String target = senderInfo.substring(senderInfo.indexOf("-> ") + 3, senderInfo.indexOf("]"));
                    headerText = "🔒 Gửi file mật cho " + target;
                } else if (senderInfo.equals(currentUser)) {
                    senderName = currentUser;
                    headerText = "Bạn";
                } else {
                    senderName = senderInfo.replace(":", "").trim();
                    headerText = senderName; // Hiện tên nếu người khác gửi file
                }

                boolean isImage = fileName.toLowerCase().matches(".*\\.(png|jpg|jpeg|gif)$");
                byte[] decodedBytes = Base64.getDecoder().decode(base64Data);

                JComponent attachmentComp;
                if (isImage) {
                    ImageIcon imgIcon = new ImageIcon(decodedBytes);
                    int w = imgIcon.getIconWidth(), h = imgIcon.getIconHeight();
                    if (w > 250) { h = (h * 250) / w; w = 250; imgIcon = new ImageIcon(imgIcon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH)); }
                    JLabel imgLabel = new JLabel(imgIcon);
                    imgLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    imgLabel.setToolTipText("Nhấn đúp để lưu");
                    imgLabel.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) saveFileLocally(fileName, decodedBytes); }
                    });
                    attachmentComp = imgLabel;
                } else {
                    JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    filePanel.setOpaque(false);
                    JButton btnDownload = new JButton("💾 Tải file: " + fileName);
                    btnDownload.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    btnDownload.setFocusPainted(false);
                    btnDownload.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    btnDownload.addActionListener(e -> saveFileLocally(fileName, decodedBytes));
                    filePanel.add(btnDownload);
                    attachmentComp = filePanel;
                }

                ChatBubble bubble = new ChatBubble(attachmentComp, isOwnMessage, isPrivate);

                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(backgroundColor);
                row.setBorder(new EmptyBorder(5, 0, 5, 0));

                JLabel avatarLabel = new JLabel(generateAvatar(senderName.replace(" (Admin)", "")));
                avatarLabel.setBorder(new EmptyBorder(0, isOwnMessage ? 10 : 0, 0, isOwnMessage ? 0 : 10));

                // Đóng gói Bong bóng file và Tên
                JPanel bubbleContentWrapper = new JPanel();
                bubbleContentWrapper.setLayout(new BoxLayout(bubbleContentWrapper, BoxLayout.Y_AXIS));
                bubbleContentWrapper.setOpaque(false);
                
                if (headerText != null && !headerText.isEmpty()) {
                    JLabel lblName = new JLabel(headerText);
                    lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lblName.setForeground(new Color(130, 130, 130));
                    JPanel nameRow = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
                    nameRow.setOpaque(false);
                    nameRow.add(lblName);
                    bubbleContentWrapper.add(nameRow);
                }
                
                JPanel bubbleRow = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
                bubbleRow.setOpaque(false);
                bubbleRow.add(bubble);
                bubbleContentWrapper.add(bubbleRow);

                if (isOwnMessage) {
                    row.add(bubbleContentWrapper, BorderLayout.CENTER);
                    row.add(avatarLabel, BorderLayout.EAST);
                } else {
                    row.add(avatarLabel, BorderLayout.WEST);
                    row.add(bubbleContentWrapper, BorderLayout.CENTER);
                }
                addBubbleToPanel(row);

            } catch (Exception e) {}
        });
    }

    private void saveFileLocally(String fileName, byte[] data) {
        JFileChooser saveChooser = new JFileChooser();
        // Đã đổi tên khi lưu file thành Download_...
        saveChooser.setSelectedFile(new File("Download_" + fileName));
        if (saveChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try { Files.write(saveChooser.getSelectedFile().toPath(), data); JOptionPane.showMessageDialog(this, "✅ Lưu thành công!"); } 
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "❌ Lỗi lưu file!"); }
        }
    }

    class ChatBubble extends JPanel {
        private boolean isOwn;
        private boolean isPrivate;

        public ChatBubble(JComponent contentComp, boolean isOwn, boolean isPrivate) {
            this.isOwn = isOwn;
            this.isPrivate = isPrivate;
            setOpaque(false); 
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 15, 10, 15)); 
            
            contentComp.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(contentComp, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (isPrivate) g2d.setColor(privateColor);
            else if (isOwn) g2d.setColor(primaryColor);
            else g2d.setColor(Color.WHITE);

            g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
            super.paintComponent(g);
        }
    }

    class ProUserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout(15, 0));
            panel.setBorder(new EmptyBorder(10, 15, 10, 15));
            panel.setBackground(isSelected ? new Color(231, 243, 255) : Color.WHITE);

            String userName = value.toString();
            boolean isAdmin = userName.contains("(Admin)");
            String cleanName = userName.replace(" (Admin)", "");

            JLabel avatarLabel = new JLabel(generateAvatar(cleanName));
            panel.add(avatarLabel, BorderLayout.WEST);

            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
            namePanel.setOpaque(false);
            
            JPanel statusDot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(isAdmin ? new Color(231, 76, 60) : new Color(46, 204, 113)); 
                    g2d.fillOval(0, 5, 10, 10);
                }
                @Override public Dimension getPreferredSize() { return new Dimension(12, 20); }
            };

            JLabel nameLabel = new JLabel(isAdmin ? cleanName + " (Admin)" : cleanName);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            nameLabel.setForeground(new Color(40, 40, 40));

            namePanel.add(statusDot);
            namePanel.add(nameLabel);
            panel.add(namePanel, BorderLayout.CENTER);

            return panel;
        }
    }

    static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { this.thumbColor = new Color(200, 200, 200); this.trackColor = new Color(240, 242, 245); }
        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() { JButton btn = new JButton(); btn.setPreferredSize(new Dimension(0, 0)); return btn; }
    }
}