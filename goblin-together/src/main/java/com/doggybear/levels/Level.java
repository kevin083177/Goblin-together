package com.doggybear.levels;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.doggybear.component.FinishCircle;
import com.doggybear.type.EntityType;

import java.util.ArrayList;
import java.util.List;

public class Level {
    private String name;
    private double lavaRiseInterval;
    private int initialLavaHeight;

    private int goblinStartX;
    private int goblinStartY;
    private int goblin2StartX;
    private int goblin2StartY;
    
    private List<Entity> platforms = new ArrayList<>();

    public Level() {

    }
    
    /**
     * 設置岩漿上升速度
     * @param speed 速度
     */
    public Level setLavaRiseSpeed(double seconds) {
        this.lavaRiseInterval = seconds;
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
     * 設置第二個哥布林起始位置
     * @param x X 座標
     * @param y Y 座標
     */
    public Level setGoblin2Start(int x, int y) {
        this.goblin2StartX = x;
        this.goblin2StartY = y;
        return this;
    }
    
    /**
     * 創建初始平台
     */
    public Entity createInitialPlatform() {
        return createPlatform(1300, 850, 400, 400,1);
    }


    /**
     * 創建一般平台
     * @param x X 座標
     * @param y Y 座標
     * @param width 寬度
     */
    public Entity createPlatform(double x, double y, int width, int imageIndex) {
        return createPlatform(x, y, width, 50, imageIndex);
    }
    
    /**
     * 創建垂直移動平台
     * @param x X 座標
     * @param y Y 座標
     * @param width 寬度
     * @param speed 移動速度
     * @param distance 移動距離
     * @param auto 是否自動移動
     */
    public Level createVericalMovingPlatform(double x, double y, int width, int height, double speedY, double distanceY, boolean auto) {
        SpawnData data = new SpawnData(x, y)
                .put("width", width)
                .put("height", height)
                .put("verticalMoving", true)
                .put("speedY", speedY)
                .put("auto", auto)
                .put("distanceY", distanceY);
        
        Entity platform = FXGL.spawn("moving", data);
        platforms.add(platform);
        return this;
    }

    /**
     * 創建水平移動平台
     * @param x X 座標
     * @param y Y 座標
     * @param width 寬度
     * @param speed 移動速度
     * @param distance 移動距離
     * @param auto 是否自動移動
     */
    public Level createHorizontalMovingPlatform(double x, double y, int width, int height, double speedX, double distanceX, boolean auto) {
        SpawnData data = new SpawnData(x, y)
            .put("width", width)
            .put("height", height)
            .put("horizontalMoving", true)
            .put("speedX", speedX)
            .put("auto", auto)
            .put("distanceX", distanceX);
        
        Entity platform = FXGL.spawn("moving", data);
        platforms.add(platform);
        return this;
    }
    
    /**
     * (可調整式)創建一般平台
     * 該方法用於創建靜態平台，若要移動平台請使用 {@link #createMovingPlatform}
     * 
     * @param x X 座標
     * @param y Y 座標
     * @param width 寬度
     * @param height 高度
     */
    public Entity createPlatform(double x, double y, int width, int height, int imageIndex) {
        SpawnData data = new SpawnData(x, y)
                .put("width", width)
                .put("height", height)
                .put("imageIndex", imageIndex);
        
        Entity platform = FXGL.spawn("platform", data);
        platforms.add(platform);
        return platform;
    }
  
    /**
     * 創建單個刺（固定大小）
     * @param x X坐標
     * @param y Y坐標
     */
    public Entity createSpike(double x, double y) {
        SpawnData data = new SpawnData(x, y);
        
        Entity spike = FXGL.spawn("spike", data);
        platforms.add(spike);
        return spike;
    }

    /**
     * 在平台上創建多個刺
     * @param platformX 平台的X坐標
     * @param platformY 平台的Y坐標
     * @param platformWidth 平台寬度
     * @param spikeCount 刺的數量
     */
    public Level createSpikesOnPlatform(double platformX, double platformY, int platformWidth, int spikeCount) {
        double totalSpacing = platformWidth - (40 * spikeCount);
        double spacing = totalSpacing / (spikeCount + 1);
        
        for (int i = 0; i < spikeCount; i++) {
            double spikeX = platformX + spacing + (i * (40 + spacing));
            double spikeY = platformY - 30; // 刺的高度
            
            createSpike(spikeX, spikeY);
        }
        
        return this;
    }

    /**
     * 創建發射器（固定大小50x50）
     * @param x X座標
     * @param y Y座標
     * @param direction 發射方向（"left", "right", "up", "down"）
     * @param fireRate 發射頻率（秒）
     * @param bulletSpeed 子彈速度
     */
    public Entity createLauncher(double x, double y, String direction, double fireRate) {
        SpawnData data = new SpawnData(x, y)
                .put("fireRate", fireRate)
                .put("direction", direction);
        
        Entity launcher = FXGL.spawn("launcher", data);
        platforms.add(launcher);
        return launcher;
    }

    /**
     * 創建連接兩個玩家的硬约束繩子
     * @param maxLength 繩子最大長度
     */
    public Entity createPlayerRope(double maxLength) {
        SpawnData data = new SpawnData(0, 0) // 繩子位置不重要，它會自動跟隨玩家
                .put("maxLength", maxLength);
        
        Entity rope = FXGL.spawn("rope", data);
        platforms.add(rope); // 將繩子加入實體列表，方便清理
        return rope;
    }

    /**
     * 創建彈跳床
     * 
     */
    public Entity createBouncePlatform(int x, int y, double bounceHeight) {
        SpawnData data = new SpawnData(x, y)
            .put("width", 50)
            .put("height", 50)
            .put("bounce", -bounceHeight);

        Entity bounce = FXGL.spawn("bounce", data);
        platforms.add(bounce);
        return bounce;
    }

    /**
     * 創建會消失的平台
     * @param x X座標
     * @param y Y座標
     * @param width 寬度
     * @param height 高度
     * @param disappearTime 消失所需時間（秒）
     * @param reappearTime 重新出現所需時間（秒）
     */
    public Entity createDisappearingPlatform(double x, double y, int width, int height, 
                                           double disappearTime, double reappearTime) {
        SpawnData data = new SpawnData(x, y)
                .put("width", width)
                .put("height", height)
                .put("disappearTime", disappearTime)
                .put("reappearTime", reappearTime);
        
        Entity platform = FXGL.spawn("disappearing", data);
        platforms.add(platform);
        return platform;
    }


    public Entity createIcePlatform(double x, double y, int width, int height, int imageIndex) {
        SpawnData data = new SpawnData(x, y)
                .put("width", width)
                .put("height", height)
                .put("imageIndex", imageIndex);
        
        Entity ice = FXGL.spawn("ice", data);
        platforms.add(ice);
        return ice;
    }

    public Entity createFirePlatform(double x, double y, int width, int height, 
                                    double fireDuration, double normalDuration, int imageIndex) {
        SpawnData data = new SpawnData(x, y)
                .put("width", width)
                .put("height", height)
                .put("fireDuration", fireDuration)
                .put("normalDuration", normalDuration)
                .put("imageIndex", imageIndex);
        
        Entity platform = FXGL.spawn("fire", data);
        platforms.add(platform);
        return platform;
    }
    /**
    * 創建終點
    * @param x X 座標
    * @param y Y 座標
    * @param radius 碰撞半徑
    * @param gameStartTime 遊戲開始時間
    * @param callback 完成回調
    */
    public Entity createFinishCircle(double x, double y, double radius, double gameStartTime, 
                                FinishCircle.FinishCallback callback) {
        SpawnData data = new SpawnData(x, y)
                .put("radius", radius)
                .put("gameStartTime", gameStartTime)
                .put("finishCallback", callback);
        
        Entity finish = FXGL.spawn("finish", data);
        platforms.add(finish);
        return finish;
    }

    /**
     * 清除所有平台、刺、弓箭和发射器
     */
    public void clearPlatforms() {
        for (Entity platform : platforms) {
            platform.removeFromWorld();
        }
        platforms.clear();
        
        // 清理所有类型的实体
        FXGL.getGameWorld().getEntitiesByType(EntityType.PLATFORM)
            .forEach(Entity::removeFromWorld);
        FXGL.getGameWorld().getEntitiesByType(EntityType.SPIKE)
            .forEach(Entity::removeFromWorld);
        FXGL.getGameWorld().getEntitiesByType(EntityType.LAUNCHER)
            .forEach(Entity::removeFromWorld);
        FXGL.getGameWorld().getEntitiesByType(EntityType.BULLET)
            .forEach(Entity::removeFromWorld);
        FXGL.getGameWorld().getEntitiesByType(EntityType.ROPE)
         .forEach(Entity::removeFromWorld);
        FXGL.getGameWorld().getEntitiesByType(EntityType.BOUNCE)
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
     * 獲取第二個哥布林起始 X 座標
     */
    public int getGoblin2StartX() {
        return goblin2StartX;
    }
    
    /**
     * 獲取第二個哥布林起始 Y 座標
     */
    public int getGoblin2StartY() {
        return goblin2StartY;
    }
    
    /**
     * 獲取岩漿上升速度
     */
    public double getLavaRiseInterval() {
        return lavaRiseInterval;
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