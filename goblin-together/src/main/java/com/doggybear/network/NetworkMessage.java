package com.doggybear.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        PLAYER_INPUT,      // 玩家輸入
        GAME_START,        // 開始遊戲
        GAME_STATE,        // 狀態同步
        PLAYER_POSITION,   // 玩家位置
        GAME_OVER,         // 遊戲結束
        PING,              // ping
        ROOM_INFO,         // 房間訊息
        CHAT_MESSAGE       // 聊天訊息
    }
    
    private MessageType type;
    private Map<String, Object> data;
    private long timestamp;
    
    public NetworkMessage(MessageType type) {
        this.type = type;
        this.data = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    public NetworkMessage put(String key, Object value) {
        data.put(key, value);
        return this;
    }
    
    public <T> T get(String key) {
        return (T) data.get(key);
    }
    
    public <T> T get(String key, T defaultValue) {
        T value = (T) data.get(key);
        return value != null ? value : defaultValue;
    }
    
    // Getter & Setter
    public MessageType getType() { return type; }
    public Map<String, Object> getData() { return data; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "NetworkMessage{type=" + type + ", data=" + data + ", timestamp=" + timestamp + "}";
    }
}