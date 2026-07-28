# 🔒 Real-Time Encrypted Chat & File Transfer System

A high-performance, multi-threaded console application built in **Java** that enables real-time group chat with **AES-128 End-to-End Encryption** and **File Sharing capabilities** using low-level TCP Sockets.

---

## ✨ Features

- 🔐 **End-to-End Encryption (AES-128):** All messages are encrypted on the client side before hitting the network, ensuring complete privacy.
- 👥 **Multi-Threaded Group Chat:** Supports multiple concurrent users using custom thread handlers and `CopyOnWriteArraySet` to handle race conditions safely.
- 📁 **Real-Time File Sharing:** Send and receive binary files (PDFs, Images, Text files) over TCP Byte Streams (`BufferedInputStream` & `FileOutputStream`).
- ⚡ **Low-Latency TCP Sockets:** Direct network communication using Java Sockets (`ServerSocket` & `Socket`).
- 🔔 **System Notifications:** Dynamic notifications when users join or leave the chat session.

---

## 🛠️ Tech Stack & Concepts Used

- **Language:** Java (JDK 17+)
- **Networking:** Java Sockets (TCP/IP), Input/Output Streams
- **Multi-Threading:** `Runnable`, Custom Worker Threads
- **Security & Cryptography:** `javax.crypto` (AES - Advanced Encryption Standard, Base64 Encoding)
- **Data Structures:** Concurrent Collections (`CopyOnWriteArraySet`), Dynamic Buffers

---

## 🚀 How to Run Locally

### Prerequisites
- Java Development Kit (JDK 11 or higher) installed.

### Step 1: Compile the Code
```bash
javac Server.java Client.java CryptoUtils.java