package com.doggybear;

import java.net.Socket;

public class GameData {
    private static Socket socket;
    private static boolean isHost;
    
    public static void setSocket(Socket socket, boolean isHost) {
        GameData.socket = socket;
        GameData.isHost = isHost;
    }
    
    public static Socket getSocket() {
        return socket;
    }
    
    public static boolean isHost() {
        return isHost;
    }
}