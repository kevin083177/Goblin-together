package com.doggybear.controller;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.Texture;
import com.doggybear.GameData;
import com.doggybear.Main;
import com.doggybear.Settings;
import com.doggybear.component.FinishCircle;
import com.doggybear.component.Goblin;
import com.doggybear.component.Timer;
import com.doggybear.factory.FactoryManager;
import com.doggybear.levels.Level;
import com.doggybear.levels.LevelManager;
import com.doggybear.ui.GameFinish;
import com.doggybear.ui.IntroSequence;

import javafx.scene.paint.Color;

import static com.almasb.fxgl.dsl.FXGL.*;

import com.almasb.fxgl.dsl.FXGL;

public class GameController {
    
    private Entity goblin;
    private Entity goblin2;
    private Timer timer;
    private Entity lava;
    private Level level;
    private IntroSequence introSequence;
    
    // 遊戲狀態
    private double lavaHeight;
    private double lavaRiseInterval;
    private double lavaY;
    private double timeSinceLastLavaRise = 0;
    private boolean isGameOver = false;
    private boolean gameStarted = false;
    private boolean actualGameStarted = false; // *** 新增：實際遊戲是否開始（Intro完成後）***
    private double gameStartTime;

    private boolean isOnlineMode = false;
    private boolean isHost = false;
    
    public void initGame() {
        System.out.println("=== 開始遊戲初始化 ===");
        
        cleanup();
        
        // 檢查是否為線上模式
        isOnlineMode = (GameData.getSocket() != null);
        isHost = GameData.isHost();
        
        System.out.println("GameController初始化 - isOnlineMode: " + isOnlineMode + ", isHost: " + isHost);
        
        isGameOver = false;
        gameStarted = false;
        actualGameStarted = false; // *** 新增：實際遊戲尚未開始 ***
        gameStartTime = FXGL.getGameTimer().getNow();

        // 確保在正確的場景中
        System.out.println("當前場景: " + FXGL.getSceneService().getCurrentScene().getClass().getSimpleName());

        FactoryManager.addAllFactories(getGameWorld());
        System.out.println("Factory 管理器已添加");
        
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);
        System.out.println("背景顏色已設置");
        
        level = LevelManager.createLevel();
        System.out.println("關卡已創建");
        
        createFinishLine();
        System.out.println("終點線已創建");

        createStretchedBackgroundEntity();
        System.out.println("背景實體已創建");
        
        // 創建玩家角色
        createPlayers();
        System.out.println("玩家角色已創建 - goblin: " + (goblin != null) + ", goblin2: " + (goblin2 != null));
        
        timer = new Timer();
        if (goblin != null) {
            goblin.addComponent(timer);
            System.out.println("計時器已添加到 goblin");
        } else {
            System.err.println("警告：goblin 為 null，無法添加計時器");
        }
        
        // *** 關鍵修改：Timer保持停止狀態，等待Intro完成 ***
        timer.stop();
        timer.resetWithoutStart();
        System.out.println("Timer已初始化但保持停止狀態，等待Intro完成");
        
        // 從 Settings 獲取預設值
        lavaHeight = level.getInitialLavaHeight();
        lavaRiseInterval = level.getLavaRiseInterval();
        lavaY = Settings.LAVA_Y_POSITION;
        
        System.out.println("岩漿設置 - 高度: " + lavaHeight + ", 間隔: " + lavaRiseInterval + ", Y位置: " + lavaY);
        
        lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
              .put("width", (int)getAppWidth())
              .put("height", (int)lavaHeight));
        
        System.out.println("岩漿已創建: " + (lava != null));
        
        getPhysicsWorld().setGravity(0, Settings.GRAVITY);
        System.out.println("重力已設置: " + Settings.GRAVITY);
        
        // 確保兩個玩家都在畫面中
        getGameScene().getViewport().setBounds(0, -Settings.WORLD_HEIGHT, getAppWidth(), Settings.WORLD_HEIGHT + getAppHeight());
        System.out.println("視口邊界已設置");
        
        // 播放開場動畫
        startIntroSequence();
        System.out.println("開場動畫已啟動");
        
        System.out.println("=== 遊戲初始化完成 ===");
    }

    /**
     * 播放開場動畫序列
     */
    private void startIntroSequence() {
        introSequence = new IntroSequence();
        
        double finishX = 10 + 30; // 終點圓心X座標
        double finishY = -1420 + 30; // 終點圓心Y座標
        
        // 玩家起始位置中心點
        double playerCenterX = (level.getGoblinStartX() + level.getGoblin2StartX()) / 2 + 25;
        double playerCenterY = (level.getGoblinStartY() + level.getGoblin2StartY()) / 2 + 25;
        
        System.out.println("開始播放開場動畫");
        System.out.println(String.format("終點位置: (%.1f, %.1f)", finishX, finishY));
        System.out.println(String.format("玩家位置: (%.1f, %.1f)", playerCenterX, playerCenterY));
        
        // 播放開場動畫
        introSequence.playIntroSequence(finishX, finishY, playerCenterX, playerCenterY, 
            new IntroSequence.IntroCompleteCallback() {
                @Override
                public void onIntroComplete() {
                    System.out.println("=== Intro完成，現在開始實際遊戲 ===");
                    
                    // *** 關鍵修改：Intro完成後才真正開始遊戲和計時 ***
                    startActualGameAfterIntro();
                    
                    // 線上模式：主機發送Intro完成消息
                    if (isOnlineMode && isHost) {
                        Main main = (Main) FXGL.getApp();
                        main.sendNetworkMessage("INTRO_COMPLETE");
                        System.out.println("主機發送INTRO_COMPLETE消息");
                    }
                }
            });
    }
    
    /**
     * *** 新方法：Intro完成後才調用，真正開始遊戲和計時 ***
     */
    private void startActualGameAfterIntro() {
        System.out.println("=== startActualGameAfterIntro 開始 ===");
        
        if (actualGameStarted) {
            System.out.println("實際遊戲已經開始，忽略重複調用");
            return;
        }
        
        actualGameStarted = true;
        gameStarted = true; // 確保遊戲狀態正確
        gameStartTime = FXGL.getGameTimer().getNow();
        
        // *** 現在才真正開始計時 ***
        if (timer != null) {
            timer.resetTime(); // 重置時間
            timer.start(); // 開始計時
            System.out.println("Timer現在開始計時（Intro完成後）");
        } else {
            System.err.println("警告：timer為null");
        }
        
        // 確保岩漿從相同高度開始
        lavaHeight = level.getInitialLavaHeight();
        timeSinceLastLavaRise = 0;
        
        System.out.println("實際遊戲已開始，Timer正在計時");
        updateViewport();
        
        System.out.println("=== startActualGameAfterIntro 完成 ===");
    }

    /**
     * *** 修改：這個方法現在只負責設置基本狀態，不啟動Timer ***
     */
    public void startActualGame() {
        System.out.println("=== GameController.startActualGame 開始 ===");
        
        if (gameStarted) {
            System.out.println("遊戲已經開始，忽略重複調用");
            return;
        }
        
        gameStarted = true;
        gameStartTime = FXGL.getGameTimer().getNow();
        
        System.out.println("設置 gameStarted = true（但Timer等待Intro完成）");
        
        // *** 注意：不在這裡啟動Timer，等待Intro完成 ***
        
        // 確保岩漿從相同高度開始
        lavaHeight = level.getInitialLavaHeight();
        timeSinceLastLavaRise = 0;
        
        updateViewport();
        
        System.out.println("=== GameController.startActualGame 完成（Timer待Intro完成）===");
    }
    
    /**
     * *** 新增：處理線上模式的Intro完成事件 ***
     */
    public void onReceiveIntroComplete() {
        System.out.println("收到INTRO_COMPLETE消息");
        if (!actualGameStarted) {
            startActualGameAfterIntro();
        }
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
        if (introSequence != null) {
            introSequence.stopIntro();
            introSequence = null;
        }
        
        goblin = null;
        goblin2 = null;
        lava = null;
        timer = null;
        level = null;
    }
     
    public void update(double tpf) {
        // *** 修改：只有實際遊戲開始後才進行遊戲邏輯更新 ***
        if (!actualGameStarted) {
            return;
        }
        
        if (isGameOver) {
            stopGoblins();
            return;
        }
        
        // 更新經過的時間
        timeSinceLastLavaRise += tpf;
        
        if (timeSinceLastLavaRise > lavaRiseInterval) {
            lavaHeight += 5; // 每次上升5像素
            
            if (lava != null && lava.isActive()) {
                lava.removeFromWorld();
            }
            lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
                .put("width", (int)getAppWidth())
                .put("height", (int)lavaHeight));
            
            timeSinceLastLavaRise = 0;
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
    
    private void createPlayers() {
        try {
            System.out.println("開始創建玩家角色");
            
            // 創建兩個玩家角色
            goblin = spawn("goblin", level.getGoblinStartX(), level.getGoblinStartY());
            goblin2 = spawn("goblin2", level.getGoblin2StartX(), level.getGoblin2StartY());
            
            System.out.println("玩家創建完成");
            
        } catch (Exception e) {
            System.err.println("創建玩家角色失敗: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private void createFinishLine() {
        FinishCircle.FinishCallback finishCallback = new FinishCircle.FinishCallback() {
            @Override
            public void onGameFinish(double totalTime) {
                // 停止計時器
                if (timer != null) {
                    timer.stop();
                }

                setGameOver(true);
                showGameFinish(totalTime);
            }
        };
        
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
            public void onBackToMenu() {
                FXGL.getGameController().gotoMainMenu();
            }
        });
        
        gameFinish.show();
    }

    public boolean checkGameOver() {
        if (isGameOver || !actualGameStarted) return false;
        
        boolean goblinDead = (goblin.getY() >= lavaY - lavaHeight);
        boolean goblin2Dead = (goblin2.getY() >= lavaY - lavaHeight);
        
        if (goblinDead || goblin2Dead) {
            setGameOver(true);
            timer.stop();
            // 在線模式下發送遊戲結束狀態
            if (isOnlineMode) {
                Main main = (Main) FXGL.getApp();
                main.sendNetworkMessage("GAME_OVER");
            }
            
            return true;
        }
        return false;
    }
    
    public double getLavaHeight() {
        return lavaHeight;
    }

    public void setLavaHeight(double height) {
        this.lavaHeight = height;
        if (lava != null) {
            lava.setY(Settings.LAVA_Y_POSITION - lavaHeight);
        }
    }

    public void updateViewport() {
        try {
            if (goblin == null || goblin2 == null || !goblin.isActive() || !goblin2.isActive()) {
                return;
            }
            
            // 如果開場動畫正在播放，不要更新視角
            if (introSequence != null && introSequence.isPlaying()) {
                return;
            }
            
            // 計算兩個哥布林的中心點作為視角中心
            double centerX = (goblin.getX() + goblin2.getX()) / 2 + 25;
            double centerY = (goblin.getY() + goblin2.getY()) / 2 + 25;
            
            // 計算目標視角位置（讓中心點位於畫面中央）
            double targetViewX = centerX - getAppWidth() / 2;
            double targetViewY = centerY - getAppHeight() / 2;
            
            targetViewX = 0; // 固定X軸位置
            
            double minViewY = -Settings.WORLD_HEIGHT;
            double maxViewY = 0;
            
            targetViewY = Math.max(minViewY, Math.min(targetViewY, maxViewY));
            
            getGameScene().getViewport().setX(targetViewX);
            getGameScene().getViewport().setY(targetViewY);
        } catch (Exception e) {
            System.err.println("Error updating viewport: " + e.getMessage());
        }
    }
    
    // Getters
    public Entity getGoblin() { 
        return (goblin != null && goblin.isActive()) ? goblin : null; 
    }
    
    public Entity getGoblin2() { 
        return (goblin2 != null && goblin2.isActive()) ? goblin2 : null; 
    }
    
    public Timer getTimer() { return timer; }
    public Level getLevel() { return level; }
    public boolean isGameOver() { return isGameOver; }
    public boolean isGameStarted() { return gameStarted; }
    public boolean isActualGameStarted() { return actualGameStarted; } // *** 新增 ***
    
    // Setters
    public void setGameOver(boolean gameOver) { this.isGameOver = gameOver; }
}