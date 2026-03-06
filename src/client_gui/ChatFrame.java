package client_gui;

import client.ChatClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class ChatFrame extends JFrame {
    private JTextPane chatPane = new JTextPane(); 
    private JTextField inputField = new JTextField();
    private DefaultListModel<String> userListModel = new DefaultListModel<>();
    private JList<String> userList = new JList<>(userListModel);
    private Color zaloBlue = new Color(0, 132, 255);

    public ChatFrame(ChatClient client) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        setTitle("Zalo Clone - Đồ án Lập trình mạng");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. SIDEBAR (Bảng danh sách Online) ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));
        JLabel lblHeader = new JLabel("  Danh sách Online");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setPreferredSize(new Dimension(0, 60));
        
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        userList.setFixedCellHeight(50);
        userList.setSelectionBackground(new Color(231, 243, 255));
        sidebar.add(lblHeader, BorderLayout.NORTH);
        sidebar.add(new JScrollPane(userList), BorderLayout.CENTER);

        // --- 2. CHAT AREA (Khu vực hiển thị tin nhắn) ---
        JPanel mainChatPanel = new JPanel(new BorderLayout());
        chatPane.setEditable(false);
        chatPane.setBackground(new Color(240, 242, 245));
        chatPane.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- 3. BOTTOM PANEL (Khung nhập liệu + Emoji + Gửi File) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Thanh công cụ Emoji & File
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolBar.setBackground(Color.WHITE);
        String[] emojis = {"😀", "😂", "😍", "😢", "😡", "👍"};
        for (String em : emojis) {
            JButton btn = new JButton(em);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> inputField.setText(inputField.getText() + em));
            toolBar.add(btn);
        }
        JButton fileBtn = new JButton("📁 Gửi File");
        fileBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fileBtn.setBackground(new Color(240, 240, 240));
        fileBtn.setFocusPainted(false);
        fileBtn.addActionListener(e -> selectAndSendFile(client));
        toolBar.add(fileBtn);

        // Thanh nhập tin nhắn
        JPanel inputWrapper = new JPanel(new BorderLayout(10, 0));
        inputWrapper.setBackground(Color.WHITE);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        JButton btnSend = new JButton("GỬI");
        btnSend.setBackground(zaloBlue); btnSend.setForeground(Color.WHITE); btnSend.setFont(new Font("Segoe UI", Font.BOLD, 13));

        inputWrapper.add(inputField, BorderLayout.CENTER);
        inputWrapper.add(btnSend, BorderLayout.EAST);
        
        bottomPanel.add(toolBar, BorderLayout.NORTH);
        bottomPanel.add(inputWrapper, BorderLayout.CENTER);

        mainChatPanel.add(new JScrollPane(chatPane), BorderLayout.CENTER);
        mainChatPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
        add(mainChatPanel, BorderLayout.CENTER);

        // Bắt sự kiện click
        btnSend.addActionListener(e -> sendAction(client));
        inputField.addActionListener(e -> sendAction(client));
        
        // Click vào tên ai thì tự động điền @tên người đó vào ô chat
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userList.getSelectedValue() != null) {
                // Tách bỏ chữ "(Admin)" nếu có khi click vào tên admin
                String target = userList.getSelectedValue().replace(" (Admin)", "");
                inputField.setText("@" + target + " ");
                inputField.requestFocus();
            }
        });

        startMessageListener(client);
        setLocationRelativeTo(null); setVisible(true);
    }

    private void sendAction(ChatClient client) {
        String text = inputField.getText().trim();
        if (!text.isEmpty()) { client.sendMessage(text); inputField.setText(""); }
    }

    // --- LUỒNG LẮNG NGHE TIN NHẮN TỪ SERVER ---
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
                        // TÍNH NĂNG ADMIN: Bị kick khỏi phòng
                        String reason = message.substring(7);
                        JOptionPane.showMessageDialog(this, reason, "Thông báo từ Admin", JOptionPane.ERROR_MESSAGE);
                        System.exit(0); // Tắt phần mềm
                    } else {
                        displayMessage(message);
                    }
                }
            } catch (Exception e) {}
        }).start();
    }

    private void displayMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            SimpleAttributeSet aset = new SimpleAttributeSet();
            StyleConstants.setFontFamily(aset, "Segoe UI");
            StyleConstants.setFontSize(aset, 14);
            
            // Format màu sắc dựa vào nội dung tin nhắn
            if (msg.startsWith("🟢")) StyleConstants.setForeground(aset, new Color(39, 174, 96));
            else if (msg.startsWith("🔴") || msg.startsWith("⚠️")) StyleConstants.setForeground(aset, Color.RED);
            else if (msg.contains("Tin riêng")) { StyleConstants.setForeground(aset, new Color(155, 89, 182)); StyleConstants.setBold(aset, true); }
            else if (msg.contains(": ")) { StyleConstants.setBold(aset, true); StyleConstants.setForeground(aset, zaloBlue); }
            
            try {
                Document doc = chatPane.getStyledDocument();
                doc.insertString(doc.getLength(), msg + "\n", aset);
                chatPane.setCaretPosition(doc.getLength()); // Cuộn xuống dòng mới nhất
            } catch (Exception e) {}
        });
    }

    private void updateUserList(String data) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String u : data.split(",")) if (!u.isEmpty()) userListModel.addElement(u);
        });
    }

    // --- LOGIC GỬI VÀ NHẬN FILE (MÃ HÓA BASE64) ---
    private void selectAndSendFile(ChatClient client) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                String encodedString = Base64.getEncoder().encodeToString(fileContent);
                String target = inputField.getText().trim();
                String prefix = target.startsWith("@") ? target.split(" ")[0] + " " : "";
                client.sendMessage(prefix + "|FILE_DATA|" + file.getName() + "|" + encodedString);
                displayMessage("✔ Đang gửi file: " + file.getName() + "...");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi đọc file: Quá lớn hoặc bị hỏng!"); }
        }
    }

    private void handleIncomingFile(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                String[] parts = message.split("\\|FILE_DATA\\|");
                String senderInfo = parts[0];
                String[] fileParts = parts[1].split("\\|");
                String fileName = fileParts[0], base64Data = fileParts[1];

                int choice = JOptionPane.showConfirmDialog(this, 
                    senderInfo + " gửi file: " + fileName + "\nBạn có muốn nhận không?", 
                    "Có File Mới", JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    JFileChooser saveChooser = new JFileChooser();
                    saveChooser.setSelectedFile(new File("nhan_" + fileName));
                    if (saveChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
                        Files.write(saveChooser.getSelectedFile().toPath(), decodedBytes);
                        displayMessage("📥 Đã lưu file: " + fileName);
                    }
                }
            } catch (Exception e) { displayMessage("❌ Lỗi tải file!"); }
        });
    }
}