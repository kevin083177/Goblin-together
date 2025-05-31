package com.doggybear.controller;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;
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
                        player.getComponent(Goblin.class).onGroundCollision();
                    }
                }
            });
        }

        // 彈跳平台碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.BOUNCE) {
                @Override
                protected void onCollisionBegin(Entity player, Entity bounce) {
                    bounce.getComponent(BouncePlatform.class).bounce(player);
                }
            });
        }

        // 移動平台碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.MOVING) {
                @Override
                protected void onCollisionBegin(Entity player, Entity platform) {
                    platform.getComponent(MovingPlatform.class).setPlayerOnPlatform(true);
                    player.getComponent(Goblin.class).onGroundCollision();
                }
                
                @Override
                protected void onCollisionEnd(Entity player, Entity platform) {
                    platform.getComponent(MovingPlatform.class).setPlayerOnPlatform(false);
                }
            });
        }

        // 消失平台碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.DISAPPEARING) {
                @Override
                protected void onCollisionBegin(Entity player, Entity platform) {
                    DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                    if (disappearing != null && disappearing.isVisible()) {
                        player.getComponent(Goblin.class).onGroundCollision();
                        
                        if (player.getBottomY() <= platform.getY() + 5) {
                            disappearing.setPlayerOnPlatform(true, player);
                        }
                    }
                }
                
                @Override
                protected void onCollision(Entity player, Entity platform) {
                    DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                    if (disappearing != null && !disappearing.isVisible()) {
                        player.getComponent(Goblin.class).setOnGround(false);
                        disappearing.setPlayerOnPlatform(false, null);
                    }
                }
                
                @Override
                protected void onCollisionEnd(Entity player, Entity platform) {
                    DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                    if (disappearing != null) {
                        disappearing.setPlayerOnPlatform(false, null);
                    }
                }
            });
        }

        // 冰平台碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.ICE) {
                @Override
                protected void onCollisionBegin(Entity player, Entity ice) {
                    if (isStandingOnIce(player, ice)) {
                        player.getComponent(Goblin.class).setOnIce(true);
                        player.getComponent(Goblin.class).onGroundCollision();
                    }
                }
                
                @Override
                protected void onCollisionEnd(Entity player, Entity ice) {
                    player.getComponent(Goblin.class).setOnIce(false);
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
                
                @Override
                protected void onCollision(Entity player, Entity firePlatform) {
                    FirePlatform platform = firePlatform.getComponent(FirePlatform.class);
                    if (platform != null && platform.isFireState()) {
                        if (gameOverCallback != null) {
                            gameOverCallback.onGameOver();
                        }
                    }
                }
                
                @Override
                protected void onCollisionEnd(Entity player, Entity firePlatform) {
                    FirePlatform platform = firePlatform.getComponent(FirePlatform.class);
                    if (platform != null) {
                        platform.removePlayer(player);
                    }
                }
            });
        }

        // 岩漿碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.LAVA) {
                @Override
                protected void onCollisionBegin(Entity player, Entity lava) {
                    if (gameOverCallback != null) {
                        gameOverCallback.onGameOver();
                    }
                }
            });
        }

        // 尖刺碰撞處理
        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.SPIKE) {
                @Override
                protected void onCollisionBegin(Entity player, Entity spike) {
                    if (gameOverCallback != null) {
                        gameOverCallback.onGameOver();
                    }
                }
            });
        }

        for (EntityType playerType : EntityType.playerTypes) {
            physicsWorld.addCollisionHandler(new CollisionHandler(playerType, EntityType.BULLET) {
                @Override
                protected void onCollision(Entity player, Entity bullet) {
                    if (gameOverCallback != null) {
                        gameOverCallback.onGameOver();
                    }
                    // 移除子彈
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
        };

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.FINISH) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity FinishCircle) {
                FinishCircle finish = FinishCircle.getComponent(FinishCircle.class);
                if (finish != null && !finish.isActivated()) {
                    finish.onPlayerFinish();
                }
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.FINISH) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity FinishCircle) {
                FinishCircle finish = FinishCircle.getComponent(FinishCircle.class);
                if (finish != null && !finish.isActivated()) {
                    finish.onPlayerFinish();
                }
            }
        });
    }
}