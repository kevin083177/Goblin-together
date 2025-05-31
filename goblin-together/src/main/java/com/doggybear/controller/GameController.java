package com.doggybear.controller;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.Texture;
import com.doggybear.Settings;
import com.doggybear.component.FinishCircle;
import com.doggybear.component.Goblin;
import com.doggybear.component.Timer;
import com.doggybear.factory.FactoryManager;
import com.doggybear.levels.Level;
import com.doggybear.levels.LevelManager;
import com.doggybear.ui.GameFinish;

import javafx.scene.paint.Color;

import static com.almasb.fxgl.dsl.FXGL.*;

import com.almasb.fxgl.dsl.FXGL;

public class GameController {
    
    private Entity goblin;
    private Entity goblin2;
    private Timer timer;
    private Entity lava;
    private Level level;
    
    // 遊戲狀態
    private double lavaHeight;
    private double lavaRiseInterval;
    private double lavaY;
    private double timeSinceLastLavaRise = 0;
    private boolean isGameOver = false;
    private double gameStartTime;

    public void initGame() {
        cleanup();
        
        isGameOver = false;
        gameStartTime = FXGL.getGameTimer().getNow();

        FactoryManager.addAllFactories(getGameWorld());
        
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);
        
        level = LevelManager.createLevel();
        createFinishLine();

        createStretchedBackgroundEntity();
        
        // 生成哥布林
        goblin = spawn("goblin", level.getGoblinStartX(), level.getGoblinStartY());
        goblin2 = spawn("goblin2", level.getGoblin2StartX(), level.getGoblin2StartY());
        
        if (goblin.getComponent(Goblin.class) == null) {
            System.err.println("Warning: goblin entity doesn't have Goblin component!");
        }
        if (goblin2.getComponent(Goblin.class) == null) {
            System.err.println("Warning: goblin2 entity doesn't have Goblin component!");
        }
        
        timer = new Timer();
        goblin.addComponent(timer);
        
        // 從 Settings 獲取預設值
        lavaHeight = level.getInitialLavaHeight();
        lavaRiseInterval = level.getLavaRiseInterval();
        lavaY = Settings.LAVA_Y_POSITION;
        
        lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
              .put("width", (int)getAppWidth())
              .put("height", (int)lavaHeight));
        
        getPhysicsWorld().setGravity(0, Settings.GRAVITY);
        
        // 確保兩個玩家都在畫面中
        getGameScene().getViewport().setBounds(0, -Settings.WORLD_HEIGHT, getAppWidth(), Settings.WORLD_HEIGHT + getAppHeight());
        
        // 動態調整視角以確保兩個玩家都在畫面中
        updateViewport();
    }
    private void createStretchedBackgroundEntity() {
        double bgWidth = FXGL.getAppWidth() * 1.1;
        double bgHeight = 3200;
        
        Entity background = FXGL.entityBuilder()
            .at(0, -2200)
            .zIndex(-1000)
            .buildAndAttach();
        
        Texture bgTexture = FXGL.getAssetLoader().loadTexture("game_background.jpg");
        bgTexture.setFitWidth(bgWidth);
        bgTexture.setFitHeight(bgHeight);
        bgTexture.setPreserveRatio(false);
        
        background.getViewComponent().addChild(bgTexture);
    }
    /**
     * 清理舊的遊戲狀態
     */
    private void cleanup() {
        // 清理舊的實體
        goblin = null;
        goblin2 = null;
        lava = null;
        timer = null;
        level = null;
    }
     
    public void update(double tpf) {
        // 如果遊戲已經結束 不再更新岩漿 Goblin 禁止移動
        if (isGameOver) {
            stopGoblins();
            return;
        }
        
        // 更新經過的時間
        timeSinceLastLavaRise += tpf;
        
        // 当计时器超过设定的间隔时间时，上升岩浆
        if (timeSinceLastLavaRise > lavaRiseInterval) {
            lavaHeight += 5; // 每次上升5像素
            
            // 重新设置岩浆的Y坐标和高度
            if (lava != null && lava.isActive()) {
                lava.removeFromWorld();
            }
            lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
                .put("width", (int)getAppWidth())
                .put("height", (int)lavaHeight));
            
            timeSinceLastLavaRise = 0; // 重置计时器
        }
    }
    
    private void stopGoblins() {
        try {
            if (goblin != null && goblin.isActive()) {
                Goblin goblinComponent = goblin.getComponent(Goblin.class);
                PhysicsComponent physicsComponent = goblin.getComponent(PhysicsComponent.class);
                
                if (goblinComponent != null) {
                    goblinComponent.stop();
                }
                if (physicsComponent != null) {
                    physicsComponent.setVelocityY(0);
                }
            }
            
            if (goblin2 != null && goblin2.isActive()) {
                Goblin goblin2Component = goblin2.getComponent(Goblin.class);
                PhysicsComponent physics2Component = goblin2.getComponent(PhysicsComponent.class);
                
                if (goblin2Component != null) {
                    goblin2Component.stop();
                }
                if (physics2Component != null) {
                    physics2Component.setVelocityY(0);
                }
            }
        } catch (Exception e) {
            System.err.println("Error stopping goblins: " + e.getMessage());
        }
    }
    
    private void createFinishLine() {
        // 創建完成回調
        FinishCircle.FinishCallback finishCallback = new FinishCircle.FinishCallback() {
            @Override
            public void onGameFinish(double totalTime) {
                // 停止計時器
                if (timer != null) {
                    timer.stop();
                }
                
                // 設定遊戲完成狀態
                setGameOver(true);
                
                // 顯示完成畫面
                showGameFinish(totalTime);
            }
        };
        
        // 在關卡最高點創建終點 - 根據您的關卡設計調整位置
        level.createFinishCircle(
            10, 
            -1420,
            30.0,
            gameStartTime,
            finishCallback
        );
    }

    private void showGameFinish(double totalTime) {
        GameFinish gameFinish = new GameFinish(totalTime, new GameFinish.GameFinishCallback() {
            @Override
            public void onRestart() {
                // 重新開始當前關卡
                FXGL.getGameController().startNewGame();
            }
            
            @Override
            public void onBackToMenu() {
                // 回到主選單
                FXGL.getGameController().gotoMainMenu();
            }
        });
        
        gameFinish.show();
    }

    public boolean checkGameOver() {
        try {
            if (goblin == null || goblin2 == null || !goblin.isActive() || !goblin2.isActive()) {
                return false;
            }
            
            return (goblin.getY() + goblin.getHeight() > lavaY - lavaHeight) || 
                   (goblin2.getY() + goblin2.getHeight() > lavaY - lavaHeight);
        } catch (Exception e) {
            System.err.println("Error checking game over: " + e.getMessage());
            return false;
        }
    }
    
    public void updateViewport() {
        try {
            if (goblin == null || goblin2 == null || !goblin.isActive() || !goblin2.isActive()) {
                return;
            }
            
            // 計算兩個哥布林的中心點作為視角中心
            double centerX = (goblin.getX() + goblin2.getX()) / 2 + 25; // 加上一半的寬度(50/2)
            double centerY = (goblin.getY() + goblin2.getY()) / 2 + 25; // 加上一半的高度(50/2)
            
            // 計算目標視角位置（讓中心點位於畫面中央）
            double targetViewX = centerX - getAppWidth() / 2;
            double targetViewY = centerY - getAppHeight() / 2;
            
            targetViewX = 0; // 固定X軸位置
            
            double minViewY = -Settings.WORLD_HEIGHT; // 上邊界
            double maxViewY = 0; // 下邊界
            
            targetViewY = Math.max(minViewY, Math.min(targetViewY, maxViewY));
            
            getGameScene().getViewport().setX(targetViewX);
            getGameScene().getViewport().setY(targetViewY);
        } catch (Exception e) {
            System.err.println("Error updating viewport: " + e.getMessage());
        }
    }
    
    public Entity getGoblin() { 
        return (goblin != null && goblin.isActive()) ? goblin : null; 
    }
    
    public Entity getGoblin2() { 
        return (goblin2 != null && goblin2.isActive()) ? goblin2 : null; 
    }
    
    public Timer getTimer() { return timer; }
    public Level getLevel() { return level; }
    public boolean isGameOver() { return isGameOver; }
    
    // Setters
    public void setGameOver(boolean gameOver) { this.isGameOver = gameOver; }
}