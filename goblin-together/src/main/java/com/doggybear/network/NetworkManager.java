package com.doggybear.network;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager {
    private static NetworkManager instance;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ExecutorService executor;
    private boolean isHost;
    private boolean isConnected;
    private NetworkListener listener;
    
    private String roomName;
    private String hostIP;
    
    public static final int DEFAULT_PORT = 12345;
    
    private NetworkManager() {
        executor = Executors.newFixedThreadPool(2);
        isConnected = false;
    }
    
    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }
    
    public void setNetworkListener(NetworkListener listener) {
        this.listener = listener;
    }
    
    public boolean createRoom(String roomName, int port) {
        try {
            this.roomName = roomName;
            this.isHost = true;
            
            serverSocket = new ServerSocket(port);
            hostIP = getLocalIP();
            
            System.out.println("房間已創建: " + roomName);
            System.out.println("等待連接，IP: " + hostIP + ":" + port);
            
            executor.submit(this::waitForConnection);
            
            return true;
        } catch (IOException e) {
            System.err.println("創建房間失敗: " + e.getMessage());
            return false;
        }
    }
    
    public boolean joinRoom(String ipAddress, int port) {
        try {
            this.isHost = false;
            this.hostIP = ipAddress;
            
            clientSocket = new Socket(ipAddress, port);
            
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            
            isConnected = true;
            System.out.println("成功連接到房間: " + ipAddress + ":" + port);
            
            executor.submit(this::listenForMessages);
            
            if (listener != null) {
                listener.onConnected();
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("加入房間失敗: " + e.getMessage());
            return false;
        }
    }
    
    private void waitForConnection() {
        try {
            System.out.println("等待玩家連接...");
            Socket clientConnection = serverSocket.accept();
            
            outputStream = new ObjectOutputStream(clientConnection.getOutputStream());
            inputStream = new ObjectInputStream(clientConnection.getInputStream());
            
            isConnected = true;
            System.out.println("玩家已連接!");
            
            executor.submit(this::listenForMessages);
            
            if (listener != null) {
                listener.onConnected();
            }
            
        } catch (IOException e) {
            System.err.println("連線錯誤: " + e.getMessage());
        }
    }
    
    private void listenForMessages() {
        try {
            while (isConnected && inputStream != null) {
                NetworkMessage message = (NetworkMessage) inputStream.readObject();
                
                if (listener != null) {
                    listener.onMessageReceived(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("監聽錯誤: " + e.getMessage());
            disconnect();
        }
    }
    
    public void sendMessage(NetworkMessage message) {
        if (isConnected && outputStream != null) {
            try {
                outputStream.writeObject(message);
                outputStream.flush();
            } catch (IOException e) {
                System.err.println("傳送訊息失敗: " + e.getMessage());
            }
        }
    }
    
    // 中斷連線
    public void disconnect() {
        isConnected = false;
        
        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("連線中斷錯誤: " + e.getMessage());
        }
        
        if (listener != null) {
            listener.onDisconnected();
        }
    }
    
    // 獲取本地IP地址
    private String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
    
    // Getter
    public boolean isHost() { return isHost; }
    public boolean isConnected() { return isConnected; }
    public String getRoomName() { return roomName; }
    public String getHostIP() { return hostIP; }
    
    public void shutdown() {
        disconnect();
        executor.shutdown();
    }
}