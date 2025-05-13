package com.doggybear.levels;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.doggybear.factory.EntityType;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Level {
    
    // 預設平台高度
    private static final int STANDARD_HEIGHT = 20;
    
    private String name;
    private double lavaRiseSpeed = 5.0;
    private int initialLavaHeight = 120;
    private int goblinStartX = 500;
    private int goblinStartY = 550;
    
    private List<Entity> platforms = new ArrayList<>();

    public Level(String name) {
        this.name = name;
    }
    
    /**
     * 設置岩漿上升速度
     * @param speed 速度
     */
    public Level setLavaRiseSpeed(double speed) {
        this.lavaRiseSpeed = speed;
        return this;
    }
    
    /**
     * 設置岩漿初始高度
     * @param height 高度
     */
    public Level setInitialLavaHeight(int height) {
        this.initialLavaHeight = height;
        return this;
    }
    
    /**
     * 設置哥布林起始位置
     * @param x X 座標
     * @param y Y 座標
     */
    public Level setGoblinStart(int x, int y) {
        this.goblinStartX = x;
        this.goblinStartY = y;
        return this;
    }
    
    /**
     * 創建初始平台
     */
    public Entity createInitialPlatform() {
        return createPlatform(0, 600, 1080, 400, Color.BROWN, false, 0, 0);
    }


    /**
     * 創建一般平台
     * @param x X 座標
     * @param y Y 座標
     * @param width 寬度
     */
    public Entity createPlatform(double x, double y, int width) {
        return createPlatform(x, y, width, STANDARD_HEIGHT, Color.BROWN, false, 0, 0);
    }
    
    /**
     * 創建移動平台
     * @param x X 座標
     * @param y Y 座標
     * @param width 寬度
     * @param speed 移動速度
     * @param distance 移動距離
     */
    public Entity createMovingPlatform(double x, double y, int width, double speed, double distance) {
        return createPlatform(x, y, width, STANDARD_HEIGHT, Color.BLUE, true, speed, distance);
    }
    
    /**
     * (可調整式)創建一般平台
     * 該方法用於創建靜態平台，若要移動平台請使用 {@link #createMovingPlatform}
     * 
     * @param x X 座標
     * @param y Y 座標
     * @param width 寬度
     * @param height 高度
     * @param color 顏色
     * @param isMoving 是否移動
     * @param moveSpeed 移動速度
     * @param moveDistance 移動距離
     */
    public Entity createPlatform(double x, double y, int width, int height, Color color, 
                                 boolean isMoving, double moveSpeed, double moveDistance) {
        SpawnData data = new SpawnData(x, y)
                .put("width", width)
                .put("height", height);
        
        if (color != null) {
            data.put("color", color);
        }
        
        if (isMoving) {
            data.put("moving", true);
            data.put("moveSpeed", moveSpeed);
            data.put("moveDistance", moveDistance);
        }
        
        Entity platform = FXGL.spawn("platform", data);
        platforms.add(platform);
        return platform;
    }
  
    /**
     * 清除所有平台
     */
    public void clearPlatforms() {
        for (Entity platform : platforms) {
            platform.removeFromWorld();
        }
        platforms.clear();
        
        FXGL.getGameWorld().getEntitiesByType(EntityType.PLATFORM)
             .forEach(Entity::removeFromWorld);
    }
    
    /**
     * 獲取哥布林起始 X 座標
     */
    public int getGoblinStartX() {
        return goblinStartX;
    }
    
    /**
     * 獲取哥布林起始 Y 座標
     */
    public int getGoblinStartY() {
        return goblinStartY;
    }
    
    /**
     * 獲取岩漿上升速度
     */
    public double getLavaRiseSpeed() {
        return lavaRiseSpeed;
    }
    
    /**
     * 獲取岩漿初始高度
     */
    public int getInitialLavaHeight() {
        return initialLavaHeight;
    }
    
    /**
     * 獲取關卡名稱
     */
    public String getName() {
        return name;
    }
}