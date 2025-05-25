package com.doggybear.network;

import com.almasb.fxgl.entity.Entity;
import com.doggybear.component.Goblin;
import javafx.scene.input.KeyCode;

public class NetworkGameManager implements NetworkListener {
    private static NetworkGameManager instance;
    private NetworkManager networkManager;
    private boolean isNetworkGame = false;
    private Entity localPlayer;
    private Entity remotePlayer;
    
    private NetworkGameManager() {
        networkManager = NetworkManager.getInstance();
        networkManager.setNetworkListener(this);
    }
    
    public static NetworkGameManager getInstance() {
        if (instance == null) {
            instance = new NetworkGameManager();
        }
        return instance;
    }
    
    public void setNetworkGameMode(boolean isNetworkGame) {
        this.isNetworkGame = isNetworkGame;
    }
    
    public void startNetworkGame(Entity player1, Entity player2) {
        isNetworkGame = true;
        
        if (networkManager.isHost()) {
            localPlayer = player1;
            remotePlayer = player2;
        } else {
            localPlayer = player2;
            remotePlayer = player1;
        }
        
        System.out.println("网络游戏开始! 身份: " + (networkManager.isHost() ? "主机控制玩家1" : "客户端控制玩家2"));
    }
    
    // 處理本地玩家輸入
    public void handleLocalInput(KeyCode keyCode, boolean isPressed) {
        if (!isNetworkGame || localPlayer == null) return;
        
        NetworkMessage inputMessage = new NetworkMessage(NetworkMessage.MessageType.PLAYER_INPUT)
                .put("keyCode", keyCode.toString())
                .put("isPressed", isPressed)
                .put("playerId", networkManager.isHost() ? 1 : 2);
        
        networkManager.sendMessage(inputMessage);
        
        processPlayerInput(localPlayer, keyCode, isPressed);
    }
    
    // 同步玩家位置
    public void syncPlayerPosition(Entity player, int playerId) {
        if (!isNetworkGame || player == null) return;
        
        boolean shouldSync = false;
        if (networkManager.isHost() && playerId == 1) {
            shouldSync = true;
        } else if (!networkManager.isHost() && playerId == 2) {
            shouldSync = true;
        }
        
        if (shouldSync) {
            var physics = player.getComponent(com.almasb.fxgl.physics.PhysicsComponent.class);
            
            NetworkMessage positionMessage = new NetworkMessage(NetworkMessage.MessageType.PLAYER_POSITION)
                    .put("playerId", playerId)
                    .put("x", player.getX())
                    .put("y", player.getY())
                    .put("velocityX", physics != null ? physics.getVelocityX() : 0.0)
                    .put("velocityY", physics != null ? physics.getVelocityY() : 0.0);
            
            networkManager.sendMessage(positionMessage);
        }
    }
    
    private void processPlayerInput(Entity player, KeyCode keyCode, boolean isPressed) {
        if (player == null || player.getComponent(Goblin.class) == null) return;
        
        Goblin goblinComponent = player.getComponent(Goblin.class);
        
        if (isPressed) {
            switch (keyCode) {
                case A:
                case LEFT:
                    goblinComponent.moveLeft();
                    break;
                case D:
                case RIGHT:
                    goblinComponent.moveRight();
                    break;
                case SPACE:
                case ENTER:
                    goblinComponent.jump();
                    break;
            }
        } else {
            switch (keyCode) {
                case A:
                case LEFT:
                case D:
                case RIGHT:
                    goblinComponent.stop();
                    break;
            }
        }
    }
    
    @Override
    public void onConnected() {
        System.out.println("网络游戏连接成功");
    }
    
    @Override
    public void onDisconnected() {
        System.out.println("网络连接断开");
        isNetworkGame = false;
    }
    
    @Override
    public void onMessageReceived(NetworkMessage message) {
        switch (message.getType()) {
            case PLAYER_INPUT:
                handleRemoteInput(message);
                break;
            case PLAYER_POSITION:
                handleRemotePosition(message);
                break;
            case GAME_OVER:
                handleGameOver(message);
                break;
        }
    }
    
    private void handleRemoteInput(NetworkMessage message) {
        String keyCodeStr = message.get("keyCode");
        boolean isPressed = message.get("isPressed");
        int playerId = message.get("playerId");
        
        KeyCode keyCode = KeyCode.valueOf(keyCodeStr);
        
        Entity targetPlayer = null;
        if (networkManager.isHost()) {
            if (playerId == 2) {
                targetPlayer = remotePlayer;
            }
        } else {
            if (playerId == 1) {
                targetPlayer = remotePlayer;
            }
        }
        
        if (targetPlayer != null) {
            processPlayerInput(targetPlayer, keyCode, isPressed);
        }
    }
    
    private void handleRemotePosition(NetworkMessage message) {
        int playerId = message.get("playerId");
        double x = message.get("x");
        double y = message.get("y");
        double velocityX = message.get("velocityX");
        double velocityY = message.get("velocityY");
        
        Entity targetPlayer = null;
        
        if (networkManager.isHost()) {
            if (playerId == 2) {
                targetPlayer = remotePlayer;
            }
        } else {
            if (playerId == 1) {
                targetPlayer = remotePlayer;
            }
        }
        
        if (targetPlayer != null) {
            targetPlayer.setX(x);
            targetPlayer.setY(y);
            
            var physics = targetPlayer.getComponent(com.almasb.fxgl.physics.PhysicsComponent.class);
            if (physics != null) {
                physics.setVelocityX(velocityX);
                physics.setVelocityY(velocityY);
            }
        }
    }
    
    private void handleGameOver(NetworkMessage message) {
        System.out.println("网络游戏结束");
    }
    
    public void sendGameOver() {
        if (isNetworkGame) {
            NetworkMessage gameOverMessage = new NetworkMessage(NetworkMessage.MessageType.GAME_OVER);
            networkManager.sendMessage(gameOverMessage);
        }
    }
    
    public NetworkManager getNetworkManager() {
        return networkManager;
    }
    
    public boolean isNetworkGame() {
        return isNetworkGame;
    }
    
    public void stopNetworkGame() {
        isNetworkGame = false;
        localPlayer = null;
        remotePlayer = null;
    }
}