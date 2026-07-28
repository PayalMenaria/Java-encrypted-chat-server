import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class Server {
    private static final int PORT = 5000;
    
    // Thread-safe Set jo saare connected clients ke Output Streams ko track rakhega
    private static Set<PrintWriter> clientWriters = new CopyOnWriteArraySet<>();

    public static void main(String[] args) {
        System.out.println("[SERVER] Broadcast Chat Server start ho gaya hai port " + PORT + " par...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Naya User Connect Hua: " + clientSocket.getInetAddress());

                // Har naye user ke liye dedicated thread
                ClientHandler clientThread = new ClientHandler(clientSocket);
                new Thread(clientThread).start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER ERROR]: " + e.getMessage());
        }
    }

    // Yeh method saare clients ko message bhejta hai
    public static void broadcastMessage(String message, PrintWriter senderWriter) {
        for (PrintWriter writer : clientWriters) {
            // Hum sender ko wapas wahi message nahi bhejenge (optional)
            if (writer != senderWriter) {
                writer.println(message);
            }
        }
    }

    // Client disconnect hone par List se hatana
    public static void removeClient(PrintWriter writer) {
        clientWriters.remove(writer);
    }

    // Client connect hone par List me add karna
    public static void addClient(PrintWriter writer) {
        clientWriters.add(writer);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter writer;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

  @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.writer = out;
            
            // 1. Name Ask Karna
            out.println("ENTER_NAME: Apna naam enter karein:");
            String encryptedName = reader.readLine();
            this.clientName = CryptoUtils.decrypt(encryptedName);

            Server.addClient(this.writer);

            System.out.println("[SERVER LOG]: " + clientName + " ne chat join ki.");
            
            // Notification Message
            String joinMsg = "🔔 [NOTIFICATION]: " + clientName + " chat me aa gaye hain!";
            Server.broadcastMessage(CryptoUtils.encrypt(joinMsg), writer);

            // 2. Continuous Listening Loop (Messages + Files)
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                // FILE TRANSFER HANDLER
                if (message.startsWith("FILE_TRANSFER:")) {
                    String[] parts = message.split(":");
                    String fileName = parts[1];
                    
                    System.out.println("[SERVER LOG]: File Transfer Request: " + fileName + " from " + clientName);
                    
                    // Baaki clients ko inform karna ki file aa rahi hai
                    Server.broadcastMessage("INCOMING_FILE:" + fileName, writer);
                    
                    // Bytes Stream handle karna
                    InputStream is = socket.getInputStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead = is.read(buffer, 0, buffer.length);

                } else {
                    // NORMAL ENCRYPTED CHAT HANDLER
                    String rawDecrypted = CryptoUtils.decrypt(message);
                    String formattedMessage = "[" + clientName + "]: " + rawDecrypted;
                    
                    System.out.println("[SERVER LOG]: " + formattedMessage);

                    // Re-encrypt formatted message & broadcast
                    Server.broadcastMessage(CryptoUtils.encrypt(formattedMessage), writer);
                }
            }

        } catch (IOException e) {
            System.err.println("[ERROR]: " + clientName + " disconnect ho gaye.");
        } finally {
            if (writer != null) {
                Server.removeClient(writer);
            }
            String leaveMsg = "❌ [NOTIFICATION]: " + clientName + " ne chat chhod di hai.";
            Server.broadcastMessage(CryptoUtils.encrypt(leaveMsg), writer);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}