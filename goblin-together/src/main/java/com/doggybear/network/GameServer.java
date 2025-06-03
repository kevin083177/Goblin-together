package com.doggybear.network;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

public class GameServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int port;
    private boolean running = false;
    private Thread serverThread;
    
    private ClientDisconnectedCallback clientDisconnectedCallback;
    private ClientConnectedCallback clientConnectedCallback;
    
    // 用於與客戶端通信
    private PrintWriter clientOut;
    private BufferedReader clientIn;

    public interface ClientDisconnectedCallback {
        void onClientDisconnected();
    }

    public interface ClientConnectedCallback {
        void onClientConnected();
    }
    
    public void setClientDisconnectedCallback(ClientDisconnectedCallback callback) {
        this.clientDisconnectedCallback = callback;
    }

    public GameServer(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
    }
    
    public void start(ClientConnectedCallback callback) throws IOException {
        this.clientConnectedCallback = callback;
        running = true;
        System.out.println("伺服器啟動，等待連接... IP: " + getServerIP() + ":" + port);
        
        serverThread = new Thread(() -> {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("客戶端已連接: " + clientSocket.getInetAddress());
                
                // 設置通信流
                clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
                if (clientConnectedCallback != null) {
                    clientConnectedCallback.onClientConnected();
                }
                
                // 發送連接確認消息，但不發送遊戲開始消息
                clientOut.println("CONNECTION_CONFIRMED");
                
                // 處理客戶端消息
                handleClient();
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("伺服器錯誤: " + e.getMessage());
                    // 通知客戶端斷線
                    if (clientDisconnectedCallback != null) {
                        clientDisconnectedCallback.onClientDisconnected();
                    }
                }
            }
        });
        
        serverThread.start();
    }
    
    /**
     * 發送遊戲開始消息給客戶端
     */
    public void sendGameStart() {
        if (clientOut != null && isConnected()) {
            System.out.println("發送GAME_START消息給客戶端");
            clientOut.println("GAME_START");
        } else {
            System.err.println("無法發送遊戲開始消息：客戶端未連接");
        }
    }
    
    public void restart() throws IOException {
        stop();
        serverSocket = new ServerSocket(port);
        start(clientConnectedCallback);
    }

    private void handleClient() {
        try {
            String inputLine;
            while (running && (inputLine = clientIn.readLine()) != null) {
                System.out.println("收到客戶端消息: " + inputLine);
                
                if ("CLIENT_READY".equals(inputLine)) {
                    System.out.println("客戶端已準備就緒");
                } else if ("PING".equals(inputLine)) {
                    // 心跳檢測
                    clientOut.println("PONG");
                }
                // 其他消息可以在這裡處理
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("處理客戶端消息時出錯: " + e.getMessage());
                if (clientDisconnectedCallback != null) {
                    clientDisconnectedCallback.onClientDisconnected();
                }
            }
        } finally {
            System.out.println("客戶端連接已斷開");
        }
    }
    
    public void stop() {
        stop(true);
    }

    /**
     * 停止服務器
     * @param closeClientSocket 是否關閉客戶端 Socket
     */
    public void stop(boolean closeClientSocket) {
        System.out.println("GameServer 停止中... closeClientSocket: " + closeClientSocket);
        running = false;
        
        try {
            // 關閉服務器 Socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // 根據參數決定是否關閉客戶端連接
            if (closeClientSocket) {
                if (clientOut != null) {
                    clientOut.close();
                }
                if (clientIn != null) {
                    clientIn.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } else {
                System.out.println("GameServer: 保持客戶端 Socket 開啟");
            }
            
            if (serverThread != null) {
                serverThread.interrupt();
            }
        } catch (IOException e) {
            System.err.println("關閉伺服器時發生錯誤: " + e.getMessage());
        }
    }
    
    public String getServerIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        return ip;
                    }
                }
            }
            
            return InetAddress.getLocalHost().getHostAddress();
            
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
    
    public boolean isConnected() {
        return clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed();
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public Socket transferClientSocket() {
        Socket transferred = clientSocket;
        clientSocket = null; // 移除引用，避免被意外關閉
        clientOut = null;
        clientIn = null;
        return transferred;
    }
}