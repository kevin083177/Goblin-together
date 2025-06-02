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
    private ClientConnectedCallback clientConnectedCallback; // 保存ClientConnectedCallback

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
                
                if (clientConnectedCallback != null) {
                    clientConnectedCallback.onClientConnected();
                }
                
                // 發送訊息至客户端 - 開始遊戲
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("GAME_START");
                
                // 處理訊息
                handleClient();
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("伺服器錯誤: " + e.getMessage());
                }
            }
        });
        
        serverThread.start();
    }
    
    public void restart() throws IOException {
        stop();
        serverSocket = new ServerSocket(port);
        start(clientConnectedCallback);
    }

    private void handleClient() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            String inputLine;
            while (running && (inputLine = in.readLine()) != null) {
                if ("CLIENT_READY".equals(inputLine)) {
                    // 客户机准备就绪，不需要特殊处理
                    System.out.println("客戶端已準備就緒");
                }
            }
        } catch (IOException e) {
            
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
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
}