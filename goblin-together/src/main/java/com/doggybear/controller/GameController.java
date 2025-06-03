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
    private double gameStartTime;

    private boolean isOnlineMode = false;
    private boolean isHost = false;
    
    public void initGame() {
        System.out.println("GameController.initGame() 開始");
        
        cleanup();
        
        // 檢查是否為線上模式
        isOnlineMode = (GameData.getSocket() != null);
        isHost = GameData.isHost();
        
        System.out.println("GameController初始化 - isOnlineMode: " + isOnlineMode + ", isHost: " + isHost);
        
        isGameOver = false;
        gameStarted = false; // 遊戲尚未開始
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
        
        // 初始時停止計時器
        timer.stop();
        
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
        
        System.out.println("GameController.initGame() 完成");
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
                // 只在主機端觸發遊戲開始
                    if (isOnlineMode && isHost) {
                        // 發送開場動畫完成消息
                        Main main = (Main) FXGL.getApp();
                        main.sendNetworkMessage("INTRO_COMPLETE");
                        startActualGame();
                    } else if (!isOnlineMode) {
                        // 單機模式直接開始
                        startActualGame();
                    }
                    // 客戶端會等待主機的 INTRO_COMPLETE 消息
                }
            });
    }
    
   private void createPlayers() {
        try {
            System.out.println("開始創建玩家角色");
            System.out.println("  遊戲模式: " + (isOnlineMode ? "線上" : "單機"));
            System.out.println("  玩家身份: " + (isHost ? "主機" : "客戶端"));
            
            // 創建兩個玩家角色
            goblin = spawn("goblin", level.getGoblinStartX(), level.getGoblinStartY());
            goblin2 = spawn("goblin2", level.getGoblin2StartX(), level.getGoblin2StartY());
            
            System.out.println("玩家創建結果:");
            System.out.println("  Goblin (玩家1) 位置: (" + level.getGoblinStartX() + ", " + level.getGoblinStartY() + ") - " + (goblin != null ? "成功" : "失敗"));
            System.out.println("  Goblin2 (玩家2) 位置: (" + level.getGoblin2StartX() + ", " + level.getGoblin2StartY() + ") - " + (goblin2 != null ? "成功" : "失敗"));
            
            if (isOnlineMode) {
                if (isHost) {
                    System.out.println("主機控制: goblin (玩家1)");
                } else {
                    System.out.println("客戶端控制: goblin2 (玩家2)");
                }
            } else {
                System.out.println("單機模式: 玩家1控制goblin, 玩家2控制goblin2");
            }
            
        } catch (Exception e) {
            System.err.println("創建玩家角色失敗: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新拋出異常，讓上層處理
        }
    }

    public void startActualGame() {
        gameStarted = true;
        gameStartTime = FXGL.getGameTimer().getNow();
        
        // 重置並啟動計時器
        if (timer != null) {
            timer.reset();
        }
        
        // 確保岩漿從相同高度開始
        lavaHeight = level.getInitialLavaHeight();
        timeSinceLastLavaRise = 0;
        
        // 更新視角
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
        if (!gameStarted) {
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
        if (isGameOver) return true;
        
        boolean goblinDead = (goblin.getY() + goblin.getHeight() > lavaY - lavaHeight);
        boolean goblin2Dead = (goblin2.getY() + goblin2.getHeight() > lavaY - lavaHeight);
        
        if (goblinDead || goblin2Dead) {
            setGameOver(true);
            
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

    public void onReceiveIntroComplete() {
        if (!gameStarted) {
            startActualGame();
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
    
    // Setters
    public void setGameOver(boolean gameOver) { this.isGameOver = gameOver; }
}