import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            System.out.println("[CLIENT] Connected to Secure File & Chat Server!");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            // Thread 1: Server se messages aur files read karne ke liye
            Thread listenThread = new Thread(() -> {
                try {
                    String incomingMessage;
                    while ((incomingMessage = reader.readLine()) != null) {
                        if (incomingMessage.startsWith("ENTER_NAME:")) {
                            System.out.println(incomingMessage.replace("ENTER_NAME:", ""));
                        } else if (incomingMessage.startsWith("INCOMING_FILE:")) {
                            // File Receive Logic
                            String fileName = incomingMessage.split(":")[1];
                            receiveFile(socket, fileName);
                        } else {
                            // Encrypted Chat Decrypt Logic
                            String decryptedMsg = CryptoUtils.decrypt(incomingMessage);
                            System.out.println(decryptedMsg);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("[CLIENT]: Connection closed.");
                }
            });
            listenThread.start();

            // Main Thread: Send Logic
            while (true) {
                String userMsg = scanner.nextLine();

                if (userMsg.equalsIgnoreCase("exit")) {
                    writer.println("exit");
                    break;
                } else if (userMsg.startsWith("FILE:")) {
                    // Usage: FILE: C:/Users/HP/Desktop/test.pdf
                    String filePath = userMsg.replace("FILE:", "").trim();
                    sendFile(socket, filePath, writer);
                } else {
                    // Normal Chat Message
                    String encryptedMsg = CryptoUtils.encrypt(userMsg);
                    writer.println(encryptedMsg);
                }
            }

            socket.close();

        } catch (IOException e) {
            System.err.println("[CLIENT ERROR]: " + e.getMessage());
        }
    }

    // 📤 File Bhejne Ka Method (Byte Streams)
    private static void sendFile(Socket socket, String filePath, PrintWriter writer) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("❌ [SYSTEM ERROR]: File nahi mili! Path check karein.");
            return;
        }

        try {
            // Server ko inform karo ki File aa rahi hai
            writer.println("FILE_TRANSFER:" + file.getName() + ":" + file.length());

            OutputStream os = socket.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096]; // 4KB Chunks me data bhejenge
            int bytesRead;

            System.out.println("📤 [FILE]: File sending started: " + file.getName());
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            fis.close();
            System.out.println("✅ [FILE]: File Sent Successfully!");

        } catch (IOException e) {
            System.err.println("❌ [FILE TRANSFER ERROR]: " + e.getMessage());
        }
    }

    // 📥 File Receive Karne Ka Method
    private static void receiveFile(Socket socket, String fileName) {
        try {
            InputStream is = socket.getInputStream();
            // Received Files ko ek alag folder me save karenge
            String savePath = "received_" + fileName;
            FileOutputStream fos = new FileOutputStream(savePath);

            byte[] buffer = new byte[4096];
            int bytesRead;
            System.out.println("📥 [FILE]: Receiving file: " + fileName);

            // Limited buffer read for file bytes
            long fileSize = 0; // Handled dynamically
            // Simple byte stream reading for received packet
            BufferedInputStream bis = new BufferedInputStream(is);
            
            // Reading file chunk
            bytesRead = is.read(buffer, 0, buffer.length);
            fos.write(buffer, 0, bytesRead);
            fos.flush();
            fos.close();

            System.out.println("✅ [FILE]: File Saved as: " + savePath);

        } catch (IOException e) {
            System.err.println("❌ [FILE RECEIVE ERROR]: " + e.getMessage());
        }
    }
}