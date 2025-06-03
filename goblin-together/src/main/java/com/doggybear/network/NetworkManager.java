package com.doggybear.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkManager {
    private final Socket socket;
    private final boolean isHost;
    private PrintWriter out;
    private BufferedReader in;
    private Thread receiveThread;
    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);

    public NetworkManager(Socket socket, boolean isHost) {
        this.socket = socket;
        this.isHost = isHost;
        
        try {
            if (socket != null && !socket.isClosed()) {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("NetworkManager 初始化成功 - isHost: " + isHost);
            } else {
                System.err.println("NetworkManager 初始化失敗: Socket 無效或已關閉");
            }
        } catch (IOException e) {
            System.err.println("NetworkManager 初始化失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() {
        if (in == null || out == null) {
            System.err.println("NetworkManager 無法啟動: I/O 流未初始化");
            return;
        }
        
        receiveThread = new Thread(this::receiveMessages);
        receiveThread.setDaemon(true);
        receiveThread.start();
        System.out.println("NetworkManager 接收線程已啟動");
    }

    private void receiveMessages() {
        try {
            // 檢查初始狀態
            if (in == null || socket == null || socket.isClosed()) {
                System.err.println("NetworkManager: 連接無效，無法接收消息");
                return;
            }
            
            System.out.println("NetworkManager 開始接收消息...");
            String message;
            
            while (running.get() && !socket.isClosed()) {
                try {
                    message = in.readLine();
                    
                    if (message == null) {
                        System.out.println("NetworkManager: 連接已斷開 (收到 null)");
                        break;
                    }
                    
                    // 將消息加入隊列
                    messageQueue.offer(message);
                    System.out.println("NetworkManager 收到消息: " + message);
                    
                } catch (IOException e) {
                    if (running.get()) {
                        System.err.println("NetworkManager 接收消息錯誤: " + e.getMessage());
                    }
                    break;
                }
            }
            
        } catch (Exception e) {
            System.err.println("NetworkManager 接收線程異常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("NetworkManager 接收線程結束");
        }
    }

    public String pollMessage() {
        return messageQueue.poll();
    }

    public void sendMessage(String message) {
        if (out != null && !socket.isClosed()) {
            out.println(message);
            System.out.println("NetworkManager 發送消息: " + message);
        } else {
            System.err.println("NetworkManager 無法發送消息: 連接無效");
        }
    }

    public void stop() {
        stop(true);
    }
    
    /**
     * 停止網絡管理器
     * @param closeSocket 是否關閉底層Socket
     */
    public void stop(boolean closeSocket) {
        System.out.println("NetworkManager 停止中... closeSocket: " + closeSocket);
        running.set(false);
        
        try {
            // 關閉 I/O 流
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            
            // 根據參數決定是否關閉 Socket
            if (closeSocket && socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("NetworkManager: 關閉Socket");
            }
            
            // 中斷接收線程
            if (receiveThread != null && receiveThread.isAlive()) {
                receiveThread.interrupt();
                try {
                    receiveThread.join(1000); // 等待最多1秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
        } catch (IOException e) {
            System.err.println("NetworkManager 關閉時出錯: " + e.getMessage());
        }
        
        System.out.println("NetworkManager 已停止");
    }
    
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() && running.get();
    }
}