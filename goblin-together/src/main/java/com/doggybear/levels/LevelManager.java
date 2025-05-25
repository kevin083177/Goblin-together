// 更新 LevelManager.java 添加弓箭发射器
package com.doggybear.levels;

import javafx.scene.paint.Color;

public class LevelManager {
    
    public static Level createLevel() {
        Level level = new Level("關卡一");
        level.setLavaRiseSpeed(5.0)
             .setInitialLavaHeight(100)
             .setGoblinStart(500, 550);
        
        level.createInitialPlatform();
        
        // 创建平台
        level.createPlatform(200, 500, 150);
        level.createPlatform(450, 500, 150);
        level.createPlatform(700, 500, 150);
        
        level.createPlatform(300, 400, 150);
        level.createPlatform(600, 400, 150);
        
        level.createPlatform(200, 300, 150);
        level.createPlatform(450, 300, 150);
        level.createPlatform(700, 300, 150);
        
        level.createPlatform(300, 200, 150);
        level.createPlatform(600, 200, 150);
        
        level.createMovingPlatform(400, 100, 100, 20, 100, 200);

        // 添加刺
        level.createSpikesOnPlatform(450, 500, 150, 20, 25, 2);
        
        // ===== 新增：添加弓箭发射器 =====
        // 在左侧添加向右发射的发射器
        // level.createArrowLauncher(50, 450, 30, 20, "right");
        
        // 在右侧添加向左发射的发射器
        // level.createArrowLauncher(1000, 350, 30, 20, "left");
        
        // 在上方添加向下发射的发射器
        // level.createArrowLauncher(400, 50, 20, 30, "down");
        
        // 创建一个快速发射的发射器（每0.5秒发射一次）
        level.createCustomArrowLauncher(100, 250, 25, 25, "right", 2.0, 500, Color.RED);

        level.createPlayerRope(150);
        return level;
    }
}