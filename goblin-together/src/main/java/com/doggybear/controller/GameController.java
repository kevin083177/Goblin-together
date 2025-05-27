package com.doggybear.controller;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.doggybear.component.Timer;
import com.doggybear.event.NetworkGameStartEvent;
import com.doggybear.factory.FactoryManager;
import com.doggybear.levels.Level;
import com.doggybear.levels.LevelManager;
import com.doggybear.network.NetworkGameManager;
import javafx.scene.paint.Color;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameController {
    
    private Entity goblin;
    private Entity goblin2;
    private com.doggybear.component.Timer timer;
    private NetworkGameManager networkGameManager;
    private Entity lava;
    private Level level;
    
    // 遊戲狀態
    private double lavaHeight = 100;
    private double lavaRiseSpeed = 5;
    private double lavaY = 1000;
    private double timePassed = 0;
    private boolean isGameOver = false;
    private int WORLD_HEIGHT = 10000;
    
    // 客戶端狀態
    private boolean clientGameOver = false;
    private int clientSurvivalTime = 0;
    private double clientLavaHeight = 0;
    
    public void initGame() {
        isGameOver = false;
        clientGameOver = false; // 重置客戶端狀態
        
        networkGameManager = NetworkGameManager.getInstance();
        
        // 設置遊戲狀態監聽器
        networkGameManager.setGameStateListener(new NetworkGameManager.GameStateListener() {
            @Override
            public void onGameStateUpdate(int survivalTime, double lavaHeight, boolean isGameOver) {
                // 客戶端接收主機端的權威狀態
                clientSurvivalTime = survivalTime;
                clientLavaHeight = lavaHeight;
                
                // 更新計時器顯示
                if (timer != null) {
                    timer.setNetworkTime(survivalTime);
                }
                
                if (isGameOver && !clientGameOver) {
                    clientGameOver = true;
                    javafx.application.Platform.runLater(() -> showGameOver());
                }
            }
            
            @Override
            public void onGameOver() {
                if (!clientGameOver) {
                    clientGameOver = true;
                    javafx.application.Platform.runLater(() -> showGameOver());
                }
            }
        });
        
        getEventBus().addEventHandler(NetworkGameStartEvent.NETWORK_GAME_START, 
            e -> {
                // System.out.println("收到網路遊戲監聽");
            });
        
        FactoryManager.addAllFactories(getGameWorld());
        
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);
        
        level = LevelManager.createLevel();
        
        // 生成第一個哥布林
        goblin = spawn("goblin", level.getGoblinStartX(), level.getGoblinStartY());
        System.out.println("創建 Goblin1 在位置: " + level.getGoblinStartX() + ", " + level.getGoblinStartY());
        
        // 生成第二個哥布林
        goblin2 = spawn("goblin2", level.getGoblin2StartX(), level.getGoblin2StartY());
        System.out.println("創建 Goblin2 在位置: " + level.getGoblin2StartX() + ", " + level.getGoblin2StartY());
        
        // 如果是網路遊戲，根據顏色偏好設定來調整哥布林顏色
        if (networkGameManager.isNetworkGame()) {
            applyGoblinColors();
        }
        
        // 檢查實體是否正確創建
        if (goblin == null) {
            System.err.println("錯誤：Goblin1 創建失敗！");
        } else {
            System.out.println("Goblin1 創建成功，組件數量: " + goblin.getComponents().size());
        }
        
        if (goblin2 == null) {
            System.err.println("錯誤：Goblin2 創建失敗！");
        } else {
            System.out.println("Goblin2 創建成功，組件數量: " + goblin2.getComponents().size());
        }
        
        if (networkGameManager.isNetworkGame()) {
            networkGameManager.startNetworkGame(goblin, goblin2);
            System.out.println("初始化遊戲 主機: " + networkGameManager.getNetworkManager().isHost());
        } else {
            System.out.println("初始化遊戲");
        }
        
        timer = new com.doggybear.component.Timer();
        goblin.addComponent(timer);
        
        lavaHeight = level.getInitialLavaHeight();
        lavaRiseSpeed = level.getLavaRiseSpeed();
        
        lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
              .put("width", (int)getAppWidth())
              .put("height", (int)lavaHeight));
        
        getPhysicsWorld().setGravity(0, 1500);
        
        getGameScene().getViewport().setBounds(0, -WORLD_HEIGHT, getAppWidth(), WORLD_HEIGHT + getAppHeight());
        
        updateViewport();
    }
    
    public void updateViewport() {
        if (goblin == null || goblin2 == null) return;
        
        double centerX = (goblin.getX() + goblin2.getX()) / 2 + 25;
        double centerY = (goblin.getY() + goblin2.getY()) / 2 + 25;
        
        double targetViewX = centerX - getAppWidth() / 2;
        double targetViewY = centerY - getAppHeight() / 2;
        
        targetViewX = 0;
        
        double minViewY = -WORLD_HEIGHT;
        double maxViewY = 0;
        
        targetViewY = Math.max(minViewY, Math.min(targetViewY, maxViewY));
        
        getGameScene().getViewport().setX(targetViewX);
        getGameScene().getViewport().setY(targetViewY);
    }
    
    // 分離遊戲邏輯更新
    public void updateGameLogic(double tpf) {
        timePassed += tpf;
        
        if (timePassed > 0.5) {
            lavaHeight += lavaRiseSpeed;
            
            // 更新岩漿視覺
            lava.removeFromWorld();
            lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
                .put("width", (int)getAppWidth())
                .put("height", (int)lavaHeight));
            
            timePassed = 0;
        }
    }

    // 客戶端更新岩漿（根據主機端數據）
    public void updateClientLava() {
        if (Math.abs(clientLavaHeight - lavaHeight) > 5) { // 只有差距較大時才更新
            lavaHeight = clientLavaHeight;
            
            lava.removeFromWorld();
            lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
                .put("width", (int)getAppWidth())
                .put("height", (int)lavaHeight));
        }
    }

    // 檢查遊戲結束條件
    public boolean checkGameOverConditions() {
        return (goblin.getY() + goblin.getHeight() > lavaY - lavaHeight) || 
               (goblin2.getY() + goblin2.getHeight() > lavaY - lavaHeight);
    }
    
    private void showGameOver() {
        // 這個方法需要在 Main.java 中實現，因為需要訪問UI相關方法
        // 或者可以通過回調接口來實現
    }
    
    private void applyGoblinColors() {
        try {
            boolean player1IsBlue = networkGameManager.isPlayer1Blue();
            
            if (goblin != null && goblin.getViewComponent() != null) {
                var texture = goblin.getViewComponent().getChildren().get(0);
                if (texture instanceof com.almasb.fxgl.texture.Texture) {
                    if (player1IsBlue) {
                        // 為玩家1添加藍色效果
                        javafx.scene.effect.ColorAdjust colorAdjust = new javafx.scene.effect.ColorAdjust();
                        colorAdjust.setHue(0.5);
                        colorAdjust.setSaturation(0.5);
                        texture.setEffect(colorAdjust);
                    }
                }
            }
            
            if (goblin2 != null && goblin2.getViewComponent() != null) {
                var texture = goblin2.getViewComponent().getChildren().get(0);
                if (texture instanceof com.almasb.fxgl.texture.Texture) {
                    if (!player1IsBlue) {
                        // 為玩家2添加藍色效果
                        javafx.scene.effect.ColorAdjust colorAdjust = new javafx.scene.effect.ColorAdjust();
                        colorAdjust.setHue(0.5);
                        colorAdjust.setSaturation(0.5);
                        texture.setEffect(colorAdjust);
                    }
                }
            }
            
            System.out.println("應用哥布林顏色：玩家1藍色=" + player1IsBlue);
            
        } catch (Exception e) {
            System.err.println("應用哥布林顏色時發生錯誤: " + e.getMessage());
        }
    }
    
    // Getters
    public Entity getGoblin() { return goblin; }
    public Entity getGoblin2() { return goblin2; }
    public com.doggybear.component.Timer getTimer() { return timer; }
    public NetworkGameManager getNetworkGameManager() { return networkGameManager; }
    public Level getLevel() { return level; }
    public double getLavaHeight() { return lavaHeight; }
    public boolean isGameOver() { return isGameOver; }
    public boolean isClientGameOver() { return clientGameOver; }
    public int getClientSurvivalTime() { return clientSurvivalTime; }
    
    // Setters
    public void setGameOver(boolean gameOver) { this.isGameOver = gameOver; }
    public void setClientGameOver(boolean clientGameOver) { this.clientGameOver = clientGameOver; }
}