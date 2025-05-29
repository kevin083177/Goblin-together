package com.doggybear.controller;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.doggybear.Settings;
import com.doggybear.component.Goblin;
import com.doggybear.component.Timer;
import com.doggybear.factory.FactoryManager;
import com.doggybear.levels.Level;
import com.doggybear.levels.LevelManager;
import javafx.scene.paint.Color;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameController {
    
    private Entity goblin;
    private Entity goblin2;
    private Timer timer;
    private Entity lava;
    private Level level;
    private InputController inputController;
    
    // 遊戲狀態
    private double lavaHeight;
    private double lavaRiseSpeed;
    private double lavaY;
    private double timePassed = 0;
    private boolean isGameOver = false;
    
    public void initGame() {
        isGameOver = false;
        timePassed = 0;
        
        FactoryManager.addAllFactories(getGameWorld());
        
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);
        
        level = LevelManager.createLevel();
        
        // 生成第一個哥布林
        goblin = spawn("goblin", level.getGoblinStartX(), level.getGoblinStartY());
        
        // 生成第二個哥布林
        goblin2 = spawn("goblin2", level.getGoblin2StartX(), level.getGoblin2StartY());
        timer = new Timer();
        goblin.addComponent(timer);
        // 從 Settings 獲取預設值
        lavaHeight = level.getInitialLavaHeight();
        lavaRiseSpeed = level.getLavaRiseSpeed();
        lavaY = Settings.LAVA_Y_POSITION;
        
        lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
              .put("width", (int)getAppWidth())
              .put("height", (int)lavaHeight));
        
        getPhysicsWorld().setGravity(0, Settings.GRAVITY);
        
        // 修改視角，確保兩個玩家都在畫面中
        getGameScene().getViewport().setBounds(0, -Settings.WORLD_HEIGHT, getAppWidth(), Settings.WORLD_HEIGHT + getAppHeight());
        
        // 動態調整視角以確保兩個玩家都在畫面中
        updateViewport();
        
        initInputController();
    }
    
    /**
     * 初始化輸入控制器
     */
    private void initInputController() {
        if (inputController == null) {
            inputController = new InputController(getGoblin(), getGoblin2());
            inputController.initInput();
        } else {
            inputController.updateEntities(goblin, goblin2);
        }
    }
    
    public void update(double tpf) {
        // 如果遊戲已經結束 不再更新岩漿 Goblin 禁止移動
        if (isGameOver) {
            if (goblin != null && goblin.isActive()) {
                goblin.getComponent(Goblin.class).stop();
                goblin.getComponent(PhysicsComponent.class).setVelocityY(0);
            }
            if (goblin2 != null && goblin2.isActive()) {
                goblin2.getComponent(Goblin.class).stop();
                goblin2.getComponent(PhysicsComponent.class).setVelocityY(0);
            }
            return;
        }
        
        // 更新經過的時間
        timePassed += tpf;
        
        // 每0.5秒更新岩漿高度，讓它變高
        if (timePassed > Settings.LAVA_UPDATE_INTERVAL) {
            lavaHeight += lavaRiseSpeed;
            
            // 重新設置岩漿的Y坐標和高度
            lava.removeFromWorld();
            lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
                .put("width", (int)getAppWidth())
                .put("height", (int)lavaHeight));
            
            timePassed = 0;
        }
    }
    
    public boolean checkGameOver() {
        // 檢查兩位玩家是否都掉入岩漿
        return (goblin.getY() + goblin.getHeight() > lavaY - lavaHeight) || 
               (goblin2.getY() + goblin2.getHeight() > lavaY - lavaHeight);
    }
    
    public void updateViewport() {
        if (goblin == null || goblin2 == null) return;
        
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
    }
    
    // Getters
    public Entity getGoblin() { return goblin; }
    public Entity getGoblin2() { return goblin2; }
    public Timer getTimer() { return timer; }
    public Level getLevel() { return level; }
    public boolean isGameOver() { return isGameOver; }
    
    // Setters
    public void setGameOver(boolean gameOver) { this.isGameOver = gameOver; }
}