package com.doggybear.controller;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.PhysicsWorld;
import com.doggybear.GameData;
import com.doggybear.component.*;
import com.doggybear.type.EntityType;

import static com.almasb.fxgl.dsl.FXGL.getPhysicsWorld;

public class PhysicsController {
    
    private GameOverCallback gameOverCallback;
    
    public interface GameOverCallback {
        void onGameOver();
    }
    
    public PhysicsController(GameOverCallback gameOverCallback) {
        this.gameOverCallback = gameOverCallback;
    }
    
    public void initPhysics() {
        PhysicsWorld physicsWorld = getPhysicsWorld();
        
        // 平台碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.PLATFORM) {
                @Override
                protected void onCollisionBegin(Entity player, Entity platform) {
                    if (player.getBottomY() <= platform.getY() + 5) {
                        // 在線模式下，只有主機或單機模式才處理碰撞邏輯
                        if (shouldProcessCollision(player)) {
                            player.getComponent(Goblin.class).onGroundCollision();
                        }
                    }
                }
            });
        }

        // 彈跳平台碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.BOUNCE) {
                @Override
                protected void onCollisionBegin(Entity player, Entity bounce) {
                    if (shouldProcessCollision(player)) {
                        bounce.getComponent(BouncePlatform.class).bounce(player);
                    }
                }
            });
        }

        // 移動平台碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.MOVING) {
                @Override
                protected void onCollisionBegin(Entity player, Entity platform) {
                    if (shouldProcessCollision(player)) {
                        platform.getComponent(MovingPlatform.class).setPlayerOnPlatform(true);
                        player.getComponent(Goblin.class).onGroundCollision();
                    }
                }
                
                @Override
                protected void onCollisionEnd(Entity player, Entity platform) {
                    if (shouldProcessCollision(player)) {
                        platform.getComponent(MovingPlatform.class).setPlayerOnPlatform(false);
                    }
                }
            });
        }

        // 消失平台碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.DISAPPEARING) {
                @Override
                protected void onCollisionBegin(Entity player, Entity platform) {
                    if (shouldProcessCollision(player)) {
                        DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                        if (disappearing != null && disappearing.isVisible()) {
                            player.getComponent(Goblin.class).onGroundCollision();
                            
                            if (player.getBottomY() <= platform.getY() + 5) {
                                disappearing.setPlayerOnPlatform(true, player);
                            }
                        }
                    }
                }
                
                @Override
                protected void onCollision(Entity player, Entity platform) {
                    if (shouldProcessCollision(player)) {
                        DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                        if (disappearing != null && !disappearing.isVisible()) {
                            player.getComponent(Goblin.class).setOnGround(false);
                            disappearing.setPlayerOnPlatform(false, null);
                        }
                    }
                }
                
                @Override
                protected void onCollisionEnd(Entity player, Entity platform) {
                    if (shouldProcessCollision(player)) {
                        DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                        if (disappearing != null) {
                            disappearing.setPlayerOnPlatform(false, null);
                        }
                    }
                }
            });
        }

        // 冰平台碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.ICE) {
                @Override
                protected void onCollisionBegin(Entity player, Entity ice) {
                    if (shouldProcessCollision(player) && isStandingOnIce(player, ice)) {
                        player.getComponent(Goblin.class).setOnIce(true);
                        player.getComponent(Goblin.class).onGroundCollision();
                    }
                }
                
                @Override
                protected void onCollisionEnd(Entity player, Entity ice) {
                    if (shouldProcessCollision(player)) {
                        player.getComponent(Goblin.class).setOnIce(false);
                    }
                }
                
                private boolean isStandingOnIce(Entity player, Entity ice) {
                    double playerBottom = player.getBottomY();
                    double iceTop = ice.getY();
                    
                    return playerBottom >= iceTop - 5 && 
                        playerBottom <= iceTop + 5 &&
                        player.getX() < ice.getRightX() &&
                        player.getRightX() > ice.getX();
                }
            });
        }

        // 火焰平台碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.FIRE) {
                @Override
                protected void onCollisionBegin(Entity player, Entity firePlatform) {
                    if (shouldProcessCollision(player)) {
                        FirePlatform platform = firePlatform.getComponent(FirePlatform.class);
                        if (platform != null) {
                            platform.addPlayer(player);
                            player.getComponent(Goblin.class).onGroundCollision();

                            if (platform.isFireState()) {
                                if (gameOverCallback != null) {
                                    gameOverCallback.onGameOver();
                                }
                            }
                        }
                    }
                }
                
                @Override
                protected void onCollision(Entity player, Entity firePlatform) {
                    if (shouldProcessCollision(player)) {
                        FirePlatform platform = firePlatform.getComponent(FirePlatform.class);
                        if (platform != null && platform.isFireState()) {
                            if (gameOverCallback != null) {
                                gameOverCallback.onGameOver();
                            }
                        }
                    }
                }
                
                @Override
                protected void onCollisionEnd(Entity player, Entity firePlatform) {
                    if (shouldProcessCollision(player)) {
                        FirePlatform platform = firePlatform.getComponent(FirePlatform.class);
                        if (platform != null) {
                            platform.removePlayer(player);
                        }
                    }
                }
            });
        }

        // 岩漿碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.LAVA) {
                @Override
                protected void onCollisionBegin(Entity player, Entity lava) {
                    // 遊戲結束邏輯只在主機端或單機模式執行
                    if (shouldProcessGameLogic()) {
                        if (gameOverCallback != null) {
                            gameOverCallback.onGameOver();
                        }
                    }
                }
            });
        }

        // 尖刺碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.SPIKE) {
                @Override
                protected void onCollisionBegin(Entity player, Entity spike) {
                    if (shouldProcessGameLogic()) {
                        if (gameOverCallback != null) {
                            gameOverCallback.onGameOver();
                        }
                    }
                }
            });
        }

        // 子彈碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.BULLET) {
                @Override
                protected void onCollision(Entity player, Entity bullet) {
                    if (shouldProcessGameLogic()) {
                        if (gameOverCallback != null) {
                            gameOverCallback.onGameOver();
                        }
                    }
                    // 子彈移除在所有客戶端都執行，保持視覺一致性
                    bullet.removeFromWorld();
                }
            });
        }
        
        // 子彈與平台的碰撞
        for (EntityType platformType : EntityType.removeBulletTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.BULLET, platformType) {
                @Override
                protected void onCollisionBegin(Entity bullet, Entity platform) {
                    bullet.removeFromWorld();
                }
            });
        }

        // 終點碰撞處理
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.FINISH) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity finishCircle) {
                if (shouldProcessGameLogic()) {
                    FinishCircle finish = finishCircle.getComponent(FinishCircle.class);
                    if (finish != null && !finish.isActivated()) {
                        finish.onPlayerFinish();
                    }
                }
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.FINISH) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity finishCircle) {
                if (shouldProcessGameLogic()) {
                    FinishCircle finish = finishCircle.getComponent(FinishCircle.class);
                    if (finish != null && !finish.isActivated()) {
                        finish.onPlayerFinish();
                    }
                }
            }
        });
    }
    
    /**
     * 判斷是否應該處理該玩家的碰撞邏輯
     * 在線模式下，主機處理所有邏輯，客戶端只處理視覺效果
     */
    private boolean shouldProcessCollision(Entity player) {
        boolean isOnlineMode = (GameData.getSocket() != null);
        boolean isHost = GameData.isHost();
        
        if (!isOnlineMode) {
            // 單機模式：處理所有碰撞
            return true;
        }
        
        if (isHost) {
            // 主機：處理所有玩家的碰撞
            return true;
        } else {
            // 客戶端：不處理任何物理邏輯，只顯示
            return false;
        }
    }
    
    /**
     * 判斷是否應該處理遊戲邏輯（如遊戲結束、完成等）
     */
    private boolean shouldProcessGameLogic() {
        boolean isOnlineMode = (GameData.getSocket() != null);
        boolean isHost = GameData.isHost();
        
        // 只有主機端或單機模式才處理遊戲邏輯
        return !isOnlineMode || isHost;
    }
}