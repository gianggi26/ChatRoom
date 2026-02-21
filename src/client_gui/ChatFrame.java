package client_gui;

import client.ChatClient;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;

public class ChatFrame extends JFrame {
    private JTextArea chatArea = new JTextArea();
    private JTextField inputField = new JTextField();

    public ChatFrame(ChatClient client) {
        setTitle("Chat Room");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        chatArea.setEditable(false);

        inputField.addActionListener(e -> {
            client.sendMessage(inputField.getText());
            inputField.setText("");
        });

        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        new Thread(() -> {
            try {
                BufferedReader in = client.getReader();
                String message;
                while ((message = in.readLine()) != null) {
                    chatArea.append(message + "\n");
                }
            } catch (Exception ignored) {}
        }).start();

        setVisible(true);
    }
}
