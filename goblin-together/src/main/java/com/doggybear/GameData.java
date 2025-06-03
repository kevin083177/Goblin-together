package com.doggybear;

import java.net.Socket;

/**
 * 遊戲數據管理類
 * 用於在不同場景之間共享數據
 */
public class GameData {
    private static Socket socket;
    private static boolean isHost;
    private static boolean socketManaged = false; // 標記 Socket 是否已被 NetworkManager 接管
    private static String hostIP; // 新增：存儲主機IP
    private static double lavaHeight;

    /**
     * 設置網絡連接資訊
     */
    public static void setSocket(Socket newSocket, boolean host) {
        // 如果有舊的 socket 且未被管理，需要關閉它
        if (socket != null && !socketManaged && socket != newSocket) {
            try {
                socket.close();
                System.out.println("GameData: 關閉舊的 Socket");
            } catch (Exception e) {
                System.err.println("GameData: 關閉舊 Socket 失敗: " + e.getMessage());
            }
        }
        
        socket = newSocket;
        isHost = host;
        socketManaged = false; // 新的 socket 尚未被管理
        
        System.out.println("GameData: 設置 Socket - isHost: " + host + ", Socket: " + (socket != null ? "已設置" : "null"));
    }
    
    /**
     * 標記 Socket 已被 NetworkManager 接管
     */
    public static void markSocketAsManaged() {
        socketManaged = true;
        System.out.println("GameData: Socket 已被 NetworkManager 接管");
    }
    
    /**
     * 標記 Socket 未被管理
     */
    public static void markSocketAsUnmanaged() {
        socketManaged = false;
        System.out.println("GameData: Socket 標記為未被管理");
    }
    
    /**
     * 獲取網絡連接
     */
    public static Socket getSocket() {
        return socket;
    }
    
    /**
     * 是否為主機
     */
    public static boolean isHost() {
        return isHost;
    }
    
    /**
     * 設置主機IP
     */
    public static void setHostIP(String ip) {
        hostIP = ip;
        System.out.println("GameData: 設置主機IP: " + ip);
    }
    
    /**
     * 獲取主機IP
     */
    public static String getHostIP() {
        return hostIP;
    }
    
    /**
     * 重置所有數據
     */
    public static void reset() {
        System.out.println("GameData: 重置遊戲數據");
        
        // 只有在 Socket 未被管理時才關閉它
        if (socket != null && !socketManaged) {
            try {
                socket.close();
                System.out.println("GameData: 關閉 Socket");
            } catch (Exception e) {
                System.err.println("GameData: 關閉 Socket 失敗: " + e.getMessage());
            }
        }
        
        socket = null;
        isHost = false;
        socketManaged = false;
        hostIP = null;
        lavaHeight = 0;
    }

    public static void setLavaHeight(double height) {
        lavaHeight = height;
    }

    public static double getLavaHeight() {
        return lavaHeight;
    }
}