package com.doggybear.levels;

import javafx.scene.paint.Color;

public class LevelManager {
    
    public static Level createLevel() {
        Level level = new Level("關卡一");
        level.setLavaRiseSpeed(5.0)
             .setInitialLavaHeight(100)
             .setGoblinStart(500, 550);
        
        level.createInitialPlatform();
        
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
        
        level.createMovingPlatform(400, 100, 100,20, 100, 200);

        level.createSpikesOnPlatform(450, 500, 150, 20, 25, 2);
        return level;
    }
}