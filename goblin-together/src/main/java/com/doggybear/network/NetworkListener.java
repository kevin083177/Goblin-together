package com.doggybear.network;

public interface NetworkListener {
    // 成功連線
    void onConnected();
    
    // 斷開連線
    void onDisconnected();
    
    // 收到訊息
    void onMessageReceived(NetworkMessage message);
    
    // 連線錯誤
    default void onError(String error) {
        System.err.println("網路錯誤: " + error);
    }
}