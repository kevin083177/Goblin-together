package com.doggybear.type;

/**
 * 遊戲中所有實體類型的枚舉
 */
public enum EntityType {
    GOBLIN, GOBLIN2, ROPE, // player
    PLATFORM, BOUNCE, ICE, MOVING, DISAPPEARING, FIRE, // platform
    LAVA, // 岩漿
    SPIKE, // 尖刺
    BULLET, LAUNCHER; // 弓箭發射

    public static final EntityType[] playerTypes = {
        EntityType.GOBLIN,
        EntityType.GOBLIN2
    };
    
    public static final EntityType[] removeBulletTypes = {
        EntityType.PLATFORM,
        EntityType.MOVING,
        EntityType.DISAPPEARING,
        EntityType.ICE,
        EntityType.FIRE,
        EntityType.SPIKE
    };
}