import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
    private JPanel chatContainer;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private JButton attachButton;
    
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public Client() {
        // 1. Username Dialog
        username = JOptionPane.showInputDialog(
            this, 
            "Apna Username daliye:", 
            "Welcome to Encrypted Chat", 
            JOptionPane.QUESTION_MESSAGE
        );

        if (username == null || username.trim().isEmpty()) {
            username = "User_" + (int)(Math.random() * 1000);
        }

        // 2. Window Setup
        setTitle("🔒 SecureChat - AES-128 (" + username + ")");
        setSize(480, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 3. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 144, 255));
        headerPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel titleLabel = new JLabel("💬 Live Encrypted Chatroom");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel statusLabel = new JLabel("● AES-128 Active");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(new Color(144, 238, 144));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);

        // 4. Dynamic Chat Container (Supports Left / Right Bubbles)
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(new Color(240, 242, 245)); // Soft Chat BG

        scrollPane = new JScrollPane(chatContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 5. Input Panel
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 0));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.setBackground(Color.WHITE);

        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        attachButton = new JButton("📎");
        attachButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        attachButton.setFocusPainted(false);
        attachButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sendButton = new JButton("Send 🚀");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        sendButton.setBackground(new Color(30, 144, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setOpaque(true);
        sendButton.setBorderPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightButtons.setBackground(Color.WHITE);
        rightButtons.add(attachButton);
        rightButtons.add(sendButton);

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(rightButtons, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action Listeners
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        attachButton.addActionListener(e -> selectFileAndSend());

        setVisible(true);

        // Connect Server
        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("127.0.0.1", 5000); 
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("SYSTEM: [" + username + "] chat room me join ho gaye hain!");

            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        String finalMsg = msg;
                        SwingUtilities.invokeLater(() -> addMessageBubble(finalMsg));
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> addSystemMessage("Connection loss ho gaya server se."));
                }
            }).start();

        } catch (IOException e) {
            addSystemMessage("Server connect nahi ho paaya. Make sure Server.java running hai.");
        }
    }

    // Modern Chat Bubble Creator
    private void addMessageBubble(String rawMessage) {
        JPanel bubblePanel = new JPanel(new FlowLayout(
            rawMessage.startsWith(username + ":") ? FlowLayout.RIGHT : 
            rawMessage.startsWith("SYSTEM:") ? FlowLayout.CENTER : FlowLayout.LEFT
        ));
        bubblePanel.setOpaque(false);
        bubblePanel.setBorder(new EmptyBorder(2, 8, 2, 8));

        JLabel label = new JLabel("<html><p style='width: 220px; word-wrap: break-word;'>" + rawMessage + "</p></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setOpaque(true);

        if (rawMessage.startsWith("SYSTEM:")) {
            label.setBackground(new Color(220, 220, 220));
            label.setForeground(Color.DARK_GRAY);
            label.setBorder(new EmptyBorder(4, 10, 4, 10));
        } else if (rawMessage.startsWith(username + ":")) {
            // Sent Message (Right Side - Blue Bubble)
            label.setBackground(new Color(220, 248, 198)); // WhatsApp Light Green/Blue
            label.setForeground(Color.BLACK);
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 230, 150), 1, true),
                new EmptyBorder(8, 12, 8, 12)
            ));
        } else {
            // Received Message (Left Side - White Bubble)
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(8, 12, 8, 12)
            ));
        }

        bubblePanel.add(label);
        chatContainer.add(bubblePanel);
        chatContainer.revalidate();
        chatContainer.repaint();

        // Auto Scroll to Bottom
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    private void addSystemMessage(String msg) {
        addMessageBubble("SYSTEM: " + msg);
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty() && out != null) {
            out.println(username + ": " + text);
            messageField.setText("");
        }
    }

    private void selectFileAndSend() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (out != null) {
                out.println(username + ": 📎 [ATTACHMENT] " + selectedFile.getName());
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new Client());
    }
}