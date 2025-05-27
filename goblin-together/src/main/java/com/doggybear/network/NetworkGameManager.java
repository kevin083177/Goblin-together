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
    
    // 插值相關
    private double remotePlayerTargetX = 0;
    private double remotePlayerTargetY = 0;
    private double remotePlayerCurrentX = 0;
    private double remotePlayerCurrentY = 0;
    private boolean hasRemoteTarget = false;
    
    // 同步控制
    private double syncTimer = 0;
    private static final double SYNC_INTERVAL = 1.0 / 30.0;
    private static final double INTERPOLATION_SPEED = 8.0;
    
    // 遊戲狀態同步
    private double gameStateTimer = 0;
    private static final double GAME_STATE_INTERVAL = 1.0 / 10.0; // 10fps同步遊戲狀態
    
    // 顏色偏好設定
    private boolean player1IsBlue = false;
    
    private NetworkGameManager() {
        networkManager = NetworkManager.getInstance();
        networkManager.setNetworkListener(this);
        player1IsBlue = false; // 初始化顏色偏好
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
            System.out.println("主機端：控制遊戲邏輯和玩家1");
        } else {
            localPlayer = player2;
            remotePlayer = player1;
            System.out.println("客戶端：只控制玩家2輸入");
        }
        
        // 初始化插值位置
        if (remotePlayer != null) {
            remotePlayerCurrentX = remotePlayer.getX();
            remotePlayerCurrentY = remotePlayer.getY();
            remotePlayerTargetX = remotePlayerCurrentX;
            remotePlayerTargetY = remotePlayerCurrentY;
        }
    }
    
    // 更新插值和遊戲狀態同步
    public void updateNetworkGame(double tpf, Entity goblin1, Entity goblin2, double currentLavaHeight, int survivalTime) {
        if (!isNetworkGame) return;
        
        // 更新插值
        updateInterpolation(tpf);
        
        // 主機端：同步遊戲狀態給客戶端
        if (networkManager.isHost()) {
            gameStateTimer += tpf;
            if (gameStateTimer >= GAME_STATE_INTERVAL) {
                syncGameState(goblin1, goblin2, currentLavaHeight, survivalTime);
                gameStateTimer = 0;
            }
            
            // 同步本地玩家位置
            syncTimer += tpf;
            if (syncTimer >= SYNC_INTERVAL) {
                syncPlayerPosition(goblin1, 1);
                syncTimer = 0;
            }
        } else {
            // 客戶端：同步本地玩家位置
            syncTimer += tpf;
            if (syncTimer >= SYNC_INTERVAL) {
                syncPlayerPosition(goblin2, 2);
                syncTimer = 0;
            }
        }
    }
    
    // 主機端同步遊戲狀態
    private void syncGameState(Entity goblin1, Entity goblin2, double lavaHeight, int survivalTime) {
        if (!networkManager.isHost()) return;
        
        NetworkMessage gameStateMessage = new NetworkMessage(NetworkMessage.MessageType.GAME_STATE)
                .put("player1X", goblin1.getX())
                .put("player1Y", goblin1.getY())
                .put("player2X", goblin2.getX())
                .put("player2Y", goblin2.getY())
                .put("lavaHeight", lavaHeight)
                .put("survivalTime", survivalTime);
        
        // 檢查碰撞狀態（主機端權威）
        boolean isGameOver = checkGameOver(goblin1, goblin2, lavaHeight);
        gameStateMessage.put("isGameOver", isGameOver);
        
        networkManager.sendMessage(gameStateMessage);
        
        if (isGameOver) {
            // 主機端觸發遊戲結束
            sendGameOver();
        }
    }
    
    // 主機端檢查遊戲結束條件
    private boolean checkGameOver(Entity goblin1, Entity goblin2, double lavaHeight) {
        if (goblin1 == null || goblin2 == null) return false;
        
        double lavaY = 1000; // 這個值應該從主遊戲類傳入
        
        return (goblin1.getY() + goblin1.getHeight() > lavaY - lavaHeight) || 
               (goblin2.getY() + goblin2.getHeight() > lavaY - lavaHeight);
    }
    
    // 更新插值
    public void updateInterpolation(double tpf) {
        if (!isNetworkGame || remotePlayer == null || !hasRemoteTarget) return;
        
        double deltaX = remotePlayerTargetX - remotePlayerCurrentX;
        double deltaY = remotePlayerTargetY - remotePlayerCurrentY;
        
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (distance > 200) {
            remotePlayerCurrentX = remotePlayerTargetX;
            remotePlayerCurrentY = remotePlayerTargetY;
        } else {
            remotePlayerCurrentX += deltaX * INTERPOLATION_SPEED * tpf;
            remotePlayerCurrentY += deltaY * INTERPOLATION_SPEED * tpf;
        }
        
        remotePlayer.setX(remotePlayerCurrentX);
        remotePlayer.setY(remotePlayerCurrentY);
    }
    
    // 處理本地玩家輸入
    public void handleLocalInput(KeyCode keyCode, boolean isPressed) {
        if (!isNetworkGame || localPlayer == null) return;
        
        // 立即處理本地輸入
        processPlayerInput(localPlayer, keyCode, isPressed);
        
        // 發送輸入到遠端
        int playerId = networkManager.isHost() ? 1 : 2;
        NetworkMessage inputMessage = new NetworkMessage(NetworkMessage.MessageType.PLAYER_INPUT)
                .put("keyCode", keyCode.toString())
                .put("isPressed", isPressed)
                .put("playerId", playerId);
        
        networkManager.sendMessage(inputMessage);
    }
    
    // 同步玩家位置
    public void syncPlayerPosition(Entity player, int playerId) {
        if (!isNetworkGame || player == null) return;
        
        boolean shouldSync = false;
        if (networkManager.isHost() && playerId == 1 && player == localPlayer) {
            shouldSync = true;
        } else if (!networkManager.isHost() && playerId == 2 && player == localPlayer) {
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
            case GAME_STATE:
                handleGameState(message);
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
            if (playerId == 2 && remotePlayer != null) {
                targetPlayer = remotePlayer;
            }
        } else {
            if (playerId == 1 && remotePlayer != null) {
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
        
        boolean shouldUpdate = false;
        if (networkManager.isHost() && playerId == 2) {
            shouldUpdate = true;
        } else if (!networkManager.isHost() && playerId == 1) {
            shouldUpdate = true;
        }
        
        if (shouldUpdate && remotePlayer != null) {
            remotePlayerTargetX = x;
            remotePlayerTargetY = y;
            hasRemoteTarget = true;
            
            var physics = remotePlayer.getComponent(com.almasb.fxgl.physics.PhysicsComponent.class);
            if (physics != null) {
                physics.setVelocityX(velocityX);
                physics.setVelocityY(velocityY);
            }
        }
    }
    
    // 處理主機端的遊戲狀態同步
    private void handleGameState(NetworkMessage message) {
        if (networkManager.isHost()) return; // 主機端不處理此訊息
        
        // 客戶端接收遊戲狀態
        boolean isGameOver = message.get("isGameOver", false);
        int survivalTime = message.get("survivalTime", 0);
        double lavaHeight = message.get("lavaHeight", 0.0);
        
        // 通知主遊戲更新狀態
        if (gameStateListener != null) {
            gameStateListener.onGameStateUpdate(survivalTime, lavaHeight, isGameOver);
        }
    }
    
    private void handleGameOver(NetworkMessage message) {
        System.out.println("收到遊戲結束訊息");
        if (gameStateListener != null) {
            gameStateListener.onGameOver();
        }
    }
    
    public void sendGameOver() {
        if (isNetworkGame) {
            NetworkMessage gameOverMessage = new NetworkMessage(NetworkMessage.MessageType.GAME_OVER);
            networkManager.sendMessage(gameOverMessage);
        }
    }
    
    // 遊戲狀態監聽器接口
    public interface GameStateListener {
        void onGameStateUpdate(int survivalTime, double lavaHeight, boolean isGameOver);
        void onGameOver();
    }
    
    private GameStateListener gameStateListener;
    
    public void setGameStateListener(GameStateListener listener) {
        this.gameStateListener = listener;
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
        hasRemoteTarget = false;
        gameStateListener = null;
        player1IsBlue = false;
    }
    
    public void setPlayerColorPreference(boolean player1IsBlue) {
        this.player1IsBlue = player1IsBlue;
    }
    
    public boolean isPlayer1Blue() {
        return player1IsBlue;
    }
}