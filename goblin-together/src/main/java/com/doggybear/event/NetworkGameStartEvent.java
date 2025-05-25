package com.doggybear.event;

import javafx.event.Event;
import javafx.event.EventType;

public class NetworkGameStartEvent extends Event {
    public static final EventType<NetworkGameStartEvent> NETWORK_GAME_START = 
        new EventType<>(Event.ANY, "NETWORK_GAME_START");
    
    public NetworkGameStartEvent() {
        super(NETWORK_GAME_START);
    }
}