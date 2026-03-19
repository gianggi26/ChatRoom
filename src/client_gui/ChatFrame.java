package client_gui;

import client.ChatClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatFrame extends JFrame {
    // THÊM BIẾN NÀY ĐỂ CÁC HÀM CON CÓ THỂ GỌI ĐƯỢC CLIENT
    private ChatClient client;

    private JPanel messagePanel = new JPanel();
    private JScrollPane scrollChat;
    private ModernTextField inputField = new ModernTextField("Nhập tin nhắn...");
    private DefaultListModel<String> userListModel = new DefaultListModel<>();
    private JList<String> userList = new JList<>(userListModel);

    // --- BẢNG MÀU CHUẨN MATERIAL DESIGN ---
    private Color primaryColor = new Color(10, 102, 194);       // Xanh dương đậm
    private Color primaryHover = new Color(8, 82, 156);         // Xanh dương khi Hover
    private Color privateColor = new Color(142, 68, 173);       // Tím cho tin riêng
    private Color bgSidebar = new Color(30, 41, 59);            // Xanh Navy đậm
    private Color bgMain = new Color(248, 249, 250);            // Xám trắng rất nhẹ
    private Color textSystem = new Color(156, 163, 175);        // Xám mờ

    private String currentUser;
    private Map<String, ImageIcon> userAvatars = new HashMap<>();

    public ChatFrame(ChatClient client, String username) {
        this.client = client; // GÁN BIẾN CLIENT Ở ĐÂY
        this.currentUser = username;
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        setTitle("ĐỒ ÁN CHAT ROOM - " + username);
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ==========================================
        // 1. SIDEBAR (CỘT BÊN TRÁI)
        // ==========================================
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(bgSidebar);

        JPanel sidebarHeader = new JPanel(new BorderLayout());
        sidebarHeader.setBackground(bgSidebar);
        sidebarHeader.setBorder(new EmptyBorder(25, 20, 20, 20));

        JLabel lblHeader = new JLabel("TRỰC TUYẾN");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(Color.WHITE);
        sidebarHeader.add(lblHeader, BorderLayout.WEST);

        userList.setCellRenderer(new ModernUserListCellRenderer());
        userList.setFixedCellHeight(65);
        userList.setBackground(bgSidebar);
        userList.setSelectionBackground(new Color(255, 255, 255, 30));
        userList.setSelectionForeground(Color.WHITE);

        JScrollPane scrollSidebar = new JScrollPane(userList);
        scrollSidebar.setBorder(null);
        scrollSidebar.getVerticalScrollBar().setUI(new ModernScrollBarUI(bgSidebar, new Color(255, 255, 255, 50)));

        sidebar.add(sidebarHeader, BorderLayout.NORTH);
        sidebar.add(scrollSidebar, BorderLayout.CENTER);

        // ==========================================
        // 2. MAIN CHAT AREA (KHU VỰC CHÍNH)
        // ==========================================
        JPanel mainChatPanel = new JPanel(new BorderLayout());
        mainChatPanel.setBackground(bgMain);

        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(Color.WHITE);
        chatHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(15, 25, 15, 25)
        ));

        JLabel chatTitle = new JLabel("Phòng Chat Chung");
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        chatTitle.setForeground(new Color(30, 41, 59));
        chatHeader.add(chatTitle, BorderLayout.WEST);

        // --- Nút Xóa màn hình Chat ---
        HoverIconButton btnClearChat = new HoverIconButton("Xóa màn hình");
        btnClearChat.setForeground(new Color(239, 68, 68));
        btnClearChat.addActionListener(e -> {
            messagePanel.removeAll();
            messagePanel.revalidate();
            messagePanel.repaint();
            // Gửi tín hiệu xóa UI lên Server để cập nhật DB
            this.client.sendMessage("/clear_history");
        });
        chatHeader.add(btnClearChat, BorderLayout.EAST);

        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(bgMain);
        messagePanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        scrollChat = new JScrollPane(messagePanel);
        scrollChat.setBorder(null);
        scrollChat.getVerticalScrollBar().setUI(new ModernScrollBarUI(bgMain, new Color(200, 200, 200)));
        scrollChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mainChatPanel.add(chatHeader, BorderLayout.NORTH);
        mainChatPanel.add(scrollChat, BorderLayout.CENTER);

        // ==========================================
        // 3. INPUT BAR (THANH NHẬP LIỆN HIỆN ĐẠI)
        // ==========================================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(new EmptyBorder(8, 15, 0, 15));

        HoverIconButton emojiBtn = new HoverIconButton("Biểu tượng");
        emojiBtn.addActionListener(e -> showEmojiPicker());

        HoverIconButton fileBtn = new HoverIconButton("Đính kèm");
        fileBtn.addActionListener(e -> selectAndSendFile());

        toolBar.add(emojiBtn);
        toolBar.add(fileBtn);

        JPanel inputWrapper = new JPanel(new BorderLayout(15, 0));
        inputWrapper.setBackground(Color.WHITE);
        inputWrapper.setBorder(new EmptyBorder(5, 20, 15, 20));

        inputField.setFont(new Font("SansSerif", Font.PLAIN, 15));

        ModernButton btnSend = new ModernButton("GỬI", primaryColor, primaryHover);
        btnSend.setPreferredSize(new Dimension(90, 45));

        inputWrapper.add(inputField, BorderLayout.CENTER);
        inputWrapper.add(btnSend, BorderLayout.EAST);

        bottomPanel.add(toolBar, BorderLayout.NORTH);
        bottomPanel.add(inputWrapper, BorderLayout.CENTER);

        mainChatPanel.add(bottomPanel, BorderLayout.SOUTH);

        // --- RÁP LAYOUT ---
        add(sidebar, BorderLayout.WEST);
        add(mainChatPanel, BorderLayout.CENTER);

        // --- SỰ KIỆN ---
        btnSend.addActionListener(e -> sendAction());
        inputField.addActionListener(e -> sendAction());

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userList.getSelectedValue() != null) {
                String target = userList.getSelectedValue().replace(" (Admin)", "");

                // Ngăn tự click vào tên mình để gửi riêng
                if (target.equalsIgnoreCase(currentUser)) {
                    userList.clearSelection();
                    return;
                }

                inputField.setText("@" + target + " ");
                inputField.requestFocus();
            }
        });

        startMessageListener();
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
        JDialog emojiDialog = new JDialog(this, "Biểu tượng", false);
        JPanel emojiPanel = new JPanel(new GridLayout(2, 5));
        String[] emojis = {"😀", "😂", "😍", "😢", "😡", "👍", "❤️", "🔥", "👏", "🤔"};
        for (String em : emojis) {
            JButton btn = new JButton(em);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            btn.setBorderPainted(false); btn.setContentAreaFilled(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> { inputField.setText(inputField.getText() + em); inputField.requestFocus(); });
            emojiPanel.add(btn);
        }
        emojiDialog.add(emojiPanel);
        emojiDialog.pack();
        emojiDialog.setLocationRelativeTo(inputField);
        emojiDialog.setVisible(true);
    }

    private void sendAction() {
        String text = inputField.getText().trim();
        if (!text.isEmpty()) {
            this.client.sendMessage(text);
            inputField.setText("");
        }
    }

    private void startMessageListener() {
        new Thread(() -> {
            try {
                String message;
                while ((message = this.client.getReader().readLine()) != null) {
                    if (message.startsWith("LIST_USERS|")) {
                        updateUserList(message.substring(11));
                    } else if (message.contains("|FILE_DATA|")) {
                        handleIncomingFile(message);
                    } else if (message.startsWith("KICKED|")) {
                        JOptionPane.showMessageDialog(this, message.substring(7), "Ngắt kết nối", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    } else if (message.startsWith("REVOKE_UI|")) {
                        // BẮT SỰ KIỆN XÓA GIAO DIỆN TỪ SERVER KHI CÓ NGƯỜI THU HỒI
                        String targetRawMsg = message.substring(10);
                        SwingUtilities.invokeLater(() -> {
                            Component[] comps = messagePanel.getComponents();
                            for (int i = comps.length - 1; i >= 0; i--) {
                                if (targetRawMsg.equals(comps[i].getName())) {
                                    JPanel row = (JPanel) comps[i];
                                    row.removeAll();

                                    JLabel lblRevoked = new JLabel("🚫 Tin nhắn đã bị thu hồi");
                                    lblRevoked.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                                    lblRevoked.setForeground(new Color(156, 163, 175));
                                    JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                                    centerPanel.setOpaque(false);
                                    centerPanel.add(lblRevoked);
                                    row.add(centerPanel, BorderLayout.CENTER);

                                    row.setName("REVOKED");
                                    row.revalidate();
                                    row.repaint();
                                    break;
                                }
                            }
                        });
                    } else {
                        parseAndDisplayMessage(message);
                    }
                }
            } catch (Exception e) {}
        }).start();
    }

    // ================================================================
    // HÀM XỬ LÝ HIỂN THỊ TIN NHẮN
    // ================================================================
    private void parseAndDisplayMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.contains("--- Gần đây nhất ---")) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
                row.setBackground(bgMain);
                JLabel lblSys = new JLabel("--- Gần đây nhất ---");
                lblSys.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                lblSys.setForeground(textSystem);
                row.add(lblSys);
                addBubbleToPanel(row);
                return;
            }

            boolean isSystemOld = msg.startsWith("🟢") || msg.startsWith("🔴") || msg.startsWith("⚠️") || msg.startsWith("❌") || msg.startsWith("📝") || msg.startsWith("🔒");
            if (isSystemOld) {
                String cleanMsg = msg.substring(1).trim();
                JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
                row.setBackground(bgMain);
                JLabel lblSys = new JLabel(cleanMsg);
                lblSys.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                lblSys.setForeground(textSystem);
                row.add(lblSys);
                addBubbleToPanel(row);
                return;
            }

            boolean isPrivate = false;
            boolean isOwnMessage = false;
            String senderName = "System";
            String content = msg;
            String headerText = "";
            String timeStr = "";

            if (msg.startsWith("[Tin riêng từ ")) {
                isPrivate = true;
                int colonIdx = content.indexOf("]: ");
                if (colonIdx != -1) {
                    senderName = content.substring(14, colonIdx);
                    content = content.substring(colonIdx + 3);
                    headerText = " Tin mật từ " + senderName;
                }
            } else if (msg.startsWith("[Bạn -> ")) {
                isPrivate = true;
                isOwnMessage = true;
                int colonIdx = content.indexOf("]: ");
                if (colonIdx != -1) {
                    senderName = currentUser;
                    String target = content.substring(8, colonIdx);
                    content = content.substring(colonIdx + 3);
                    headerText = " Gửi mật cho " + target;
                }
            } else if (content.startsWith(currentUser + ": ")) {
                isOwnMessage = true;
                senderName = currentUser;
                content = content.substring(currentUser.length() + 2).trim();
            } else if (content.contains(": ") && !content.startsWith("[")) {
                int colonIdx = content.indexOf(": ");
                senderName = content.substring(0, colonIdx);
                content = content.substring(colonIdx + 2).trim();
                headerText = senderName;
            }

            if (content.startsWith("[")) {
                int endBracket = content.indexOf("]");
                if (endBracket > 0 && endBracket <= 12) {
                    timeStr = content.substring(1, endBracket);
                    content = content.substring(endBracket + 1).trim();
                }
            }

            if (timeStr.isEmpty()) {
                timeStr = new SimpleDateFormat("hh:mm a").format(new Date());
            }

            if (senderName.equals("System")) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
                row.setBackground(bgMain);
                JLabel lblSys = new JLabel(content + " (" + timeStr + ")");
                lblSys.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                lblSys.setForeground(textSystem);
                row.add(lblSys);
                addBubbleToPanel(row);
                return;
            }

            int bubbleWidth = Math.min(content.length() * 9, 350);
            if (bubbleWidth < 40) bubbleWidth = 40;
            String htmlText = "<html><div style='width: %dpx; font-family: \"Segoe UI Emoji\", sans-serif; line-height: 1.4;'>%s</div></html>";
            JLabel lblText = new JLabel(String.format(htmlText, bubbleWidth, content.replace("\n", "<br>")));
            lblText.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
            lblText.setForeground(isOwnMessage ? Color.WHITE : new Color(30, 41, 59));

            ChatBubble bubble = new ChatBubble(lblText, isOwnMessage, isPrivate);

            // LƯU VẾT BẰNG ROW NAME ĐỂ TÌM VÀ XÓA KHI CÓ LỆNH THU HỒI
            JPanel row = new JPanel(new BorderLayout());
            row.setName(msg);
            row.setBackground(bgMain);
            row.setBorder(new EmptyBorder(8, 0, 8, 0));

            JLabel avatarLabel = new JLabel(generateAvatar(senderName.replace(" (Admin)", "")));
            avatarLabel.setBorder(new EmptyBorder(0, isOwnMessage ? 12 : 0, 0, isOwnMessage ? 0 : 12));
            avatarLabel.setVerticalAlignment(SwingConstants.TOP);

            JPanel bubbleContentWrapper = new JPanel();
            bubbleContentWrapper.setLayout(new BoxLayout(bubbleContentWrapper, BoxLayout.Y_AXIS));
            bubbleContentWrapper.setOpaque(false);

            if (headerText != null && !headerText.isEmpty()) {
                JLabel lblName = new JLabel(headerText);
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblName.setForeground(new Color(100, 116, 139));
                JPanel nameRow = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 2));
                nameRow.setOpaque(false);
                nameRow.add(lblName);
                bubbleContentWrapper.add(nameRow);
            }

            JPanel bubbleRow = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            bubbleRow.setOpaque(false);
            bubbleRow.add(bubble);
            bubbleContentWrapper.add(bubbleRow);

            JLabel lblTime = new JLabel(timeStr);
            lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblTime.setForeground(new Color(156, 163, 175));
            JPanel timeRow = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 3));
            timeRow.setOpaque(false);
            timeRow.add(lblTime);
            bubbleContentWrapper.add(timeRow);

            // --- MENU CHUỘT PHẢI THU HỒI ---
            JPopupMenu popup = new JPopupMenu();
            JMenuItem revokeItem = new JMenuItem("Thu hồi tin nhắn này");
            revokeItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
            revokeItem.addActionListener(e -> {
                this.client.sendMessage("REVOKE_MSG|" + msg);
            });
            popup.add(revokeItem);

            bubbleContentWrapper.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
            // ---------------------------------

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

    private void selectAndSendFile() {
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
                this.client.sendMessage(prefix + "|FILE_DATA|" + file.getName() + "|" + encodedString);
            } catch (Exception ex) {}
        }
    }

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
                    headerText = " File mật từ " + senderName;
                } else if (senderInfo.contains("[Bạn -> ")) {
                    senderName = currentUser;
                    String target = senderInfo.substring(senderInfo.indexOf("-> ") + 3, senderInfo.indexOf("]"));
                    headerText = "Gửi file mật cho " + target;
                } else if (senderInfo.equals(currentUser)) {
                    senderName = currentUser;
                } else {
                    senderName = senderInfo.replace(":", "").trim();
                    headerText = senderName;
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
                    btnDownload.setContentAreaFilled(false);
                    btnDownload.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    btnDownload.addActionListener(e -> saveFileLocally(fileName, decodedBytes));
                    filePanel.add(btnDownload);
                    attachmentComp = filePanel;
                }

                ChatBubble bubble = new ChatBubble(attachmentComp, isOwnMessage, isPrivate);

                JPanel row = new JPanel(new BorderLayout());
                row.setName(message); // LƯU VẾT CHO FILE
                row.setBackground(bgMain);
                row.setBorder(new EmptyBorder(8, 0, 8, 0));

                JLabel avatarLabel = new JLabel(generateAvatar(senderName.replace(" (Admin)", "")));
                avatarLabel.setBorder(new EmptyBorder(0, isOwnMessage ? 12 : 0, 0, isOwnMessage ? 0 : 12));
                avatarLabel.setVerticalAlignment(SwingConstants.TOP);

                JPanel bubbleContentWrapper = new JPanel();
                bubbleContentWrapper.setLayout(new BoxLayout(bubbleContentWrapper, BoxLayout.Y_AXIS));
                bubbleContentWrapper.setOpaque(false);

                if (headerText != null && !headerText.isEmpty()) {
                    JLabel lblName = new JLabel(headerText);
                    lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lblName.setForeground(new Color(100, 116, 139));
                    JPanel nameRow = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 2));
                    nameRow.setOpaque(false);
                    nameRow.add(lblName);
                    bubbleContentWrapper.add(nameRow);
                }

                JPanel bubbleRow = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
                bubbleRow.setOpaque(false);
                bubbleRow.add(bubble);
                bubbleContentWrapper.add(bubbleRow);

                String timeStr = new SimpleDateFormat("hh:mm a").format(new Date());
                JLabel lblTime = new JLabel(timeStr);
                lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                lblTime.setForeground(new Color(156, 163, 175));
                JPanel timeRow = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 3));
                timeRow.setOpaque(false);
                timeRow.add(lblTime);
                bubbleContentWrapper.add(timeRow);

                // --- MENU CHUỘT PHẢI THU HỒI CHO FILE ---
                JPopupMenu popup = new JPopupMenu();
                JMenuItem revokeItem = new JMenuItem("Thu hồi file này");
                revokeItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                revokeItem.addActionListener(e -> {
                    this.client.sendMessage("REVOKE_MSG|" + message);
                });
                popup.add(revokeItem);

                bubbleContentWrapper.addMouseListener(new MouseAdapter() {
                    public void mouseReleased(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                });
                // ---------------------------------

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
        saveChooser.setSelectedFile(new File("Download_" + fileName));
        if (saveChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try { Files.write(saveChooser.getSelectedFile().toPath(), data); JOptionPane.showMessageDialog(this, "✅ Lưu thành công!"); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "❌ Lỗi lưu file!"); }
        }
    }

    // =========================================================
    // CÁC CLASS ĐỒ HỌA (UI COMPONENTS) NÂNG CAO - CSS TO JAVA
    // =========================================================

    class ChatBubble extends JPanel {
        private boolean isOwn;
        private boolean isPrivate;

        public ChatBubble(JComponent contentComp, boolean isOwn, boolean isPrivate) {
            this.isOwn = isOwn;
            this.isPrivate = isPrivate;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(12, 18, 12, 18));
            contentComp.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(contentComp, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(new Color(0, 0, 0, 15));
            g2d.fill(new RoundRectangle2D.Double(2, 3, getWidth()-2, getHeight()-2, 22, 22));

            if (isPrivate) g2d.setColor(privateColor);
            else if (isOwn) g2d.setColor(primaryColor);
            else g2d.setColor(Color.WHITE);

            g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth()-2, getHeight()-2, 22, 22));
            super.paintComponent(g);
        }
    }

    class ModernTextField extends JTextField {
        private String placeholder;
        public ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(241, 245, 249));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, getHeight(), getHeight()));
            super.paintComponent(g);
            g2.dispose();
        }
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (getText().length() == 0 && !hasFocus()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                int padding = (getHeight() - getFontMetrics(getFont()).getHeight()) / 2;
                g2.drawString(placeholder, 20, getHeight() - padding - 3);
                g2.dispose();
            }
        }
    }

    class ModernButton extends JButton {
        private Color normalColor;
        private Color hoverColor;
        private boolean isHovered = false;

        public ModernButton(String text, Color normalColor, Color hoverColor) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHovered ? hoverColor : normalColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            super.paintComponent(g);
            g2.dispose();
        }
    }

    class HoverIconButton extends JButton {
        private boolean isHovered = false;
        public HoverIconButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(new Color(100, 116, 139));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 12, 8, 12));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            if (isHovered) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    class ModernUserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout(15, 0));
            panel.setBorder(new EmptyBorder(12, 20, 12, 20));
            panel.setBackground(isSelected ? new Color(255, 255, 255, 20) : bgSidebar);

            String userName = value.toString();
            boolean isAdmin = userName.contains("(Admin)");
            String cleanName = userName.replace(" (Admin)", "");

            JLabel avatarLabel = new JLabel(generateAvatar(cleanName));
            panel.add(avatarLabel, BorderLayout.WEST);

            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
            namePanel.setOpaque(false);

            JPanel statusDot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(isAdmin ? new Color(239, 68, 68) : new Color(34, 197, 94));
                    g2d.fillOval(0, 6, 12, 12);
                }
                @Override public Dimension getPreferredSize() { return new Dimension(14, 20); }
            };

            JLabel nameLabel = new JLabel(isAdmin ? cleanName + " (Admin)" : cleanName);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            nameLabel.setForeground(Color.WHITE);

            namePanel.add(statusDot);
            namePanel.add(nameLabel);
            panel.add(namePanel, BorderLayout.CENTER);

            return panel;
        }
    }

    static class ModernScrollBarUI extends BasicScrollBarUI {
        private Color bgColor;
        private Color thumbCol;
        public ModernScrollBarUI(Color bg, Color thumb) { this.bgColor = bg; this.thumbCol = thumb; }
        @Override protected void configureScrollBarColors() { this.thumbColor = thumbCol; this.trackColor = bgColor; }
        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() { JButton btn = new JButton(); btn.setPreferredSize(new Dimension(0, 0)); return btn; }
    }
}