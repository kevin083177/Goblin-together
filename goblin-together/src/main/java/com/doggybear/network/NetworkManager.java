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
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("网络管理器初始化失败: " + e.getMessage());
        }
    }

    public void start() {
        receiveThread = new Thread(this::receiveMessages);
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    private void receiveMessages() {
        try {
            while (running.get()) {
                String message = in.readLine();
                if (message == null) {
                    break; // 连接已关闭
                }
                messageQueue.offer(message);
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("网络接收错误: " + e.getMessage());
            }
        }
    }

    public String pollMessage() {
        return messageQueue.poll();
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void stop() {
        running.set(false);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (receiveThread != null && receiveThread.isAlive()) {
                receiveThread.interrupt();
            }
        } catch (IOException e) {
            System.err.println("关闭网络连接时出错: " + e.getMessage());
        }
    }
}