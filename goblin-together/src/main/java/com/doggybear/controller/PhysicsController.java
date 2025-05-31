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
        
        // 玩家碰撞平台
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity platform) {
                if (goblin.getBottomY() <= platform.getY() + 5) {
                    goblin.getComponent(Goblin.class).onGroundCollision();
                }
            }
        });
       
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity platform) {
                if (goblin2.getBottomY() <= platform.getY() + 5) {
                    goblin2.getComponent(Goblin.class).onGroundCollision();
                }
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.BOUNCE) {
            @Override
            protected void onCollisionBegin(Entity player, Entity bounce) {
                bounce.getComponent(BouncePlatform.class).bounce(player);
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.BOUNCE) {
            @Override
            protected void onCollisionBegin(Entity player, Entity bounce) {
                bounce.getComponent(BouncePlatform.class).bounce(player);
            }
        });

        // 移動平台碰撞處理
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.MOVING) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity platform) {
                platform.getComponent(MovingPlatform.class).setPlayerOnPlatform(true);
                goblin.getComponent(Goblin.class).onGroundCollision();
            }
            @Override
            protected void onCollisionEnd(Entity goblin, Entity platform) {
                platform.getComponent(MovingPlatform.class).setPlayerOnPlatform(false);
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.MOVING) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity platform) {
                platform.getComponent(MovingPlatform.class).setPlayerOnPlatform(true);
                goblin.getComponent(Goblin.class).onGroundCollision();
            }
            @Override
            protected void onCollisionEnd(Entity goblin, Entity platform) {
                platform.getComponent(MovingPlatform.class).setPlayerOnPlatform(false);
            }
        });

        // 消失平台碰撞處理
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.DISAPPEARING) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity platform) {
                DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                if (disappearing != null && disappearing.isVisible()) {
                    goblin.getComponent(Goblin.class).onGroundCollision();
                    
                    if (goblin.getBottomY() <= platform.getY() + 5) {
                        disappearing.setPlayerOnPlatform(true);
                    }
                }
            }
            
            @Override
            protected void onCollision(Entity goblin, Entity platform) {
                DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                if (disappearing != null && !disappearing.isVisible()) {
                    goblin.getComponent(Goblin.class).setOnGround(false);
                    disappearing.setPlayerOnPlatform(false);
                }
            }
            
            @Override
            protected void onCollisionEnd(Entity goblin, Entity platform) {
                DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                if (disappearing != null) {
                    disappearing.setPlayerOnPlatform(false);
                }
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.DISAPPEARING) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity platform) {
                DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                if (disappearing != null && disappearing.isVisible()) {
                    goblin2.getComponent(Goblin.class).onGroundCollision();
                    
                    if (goblin2.getBottomY() <= platform.getY() + 5) {
                        disappearing.setPlayerOnPlatform(true);
                    }
                }
            }
            
            @Override
            protected void onCollision(Entity goblin2, Entity platform) {
                DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                if (disappearing != null && !disappearing.isVisible()) {
                    goblin2.getComponent(Goblin.class).setOnGround(false);
                    disappearing.setPlayerOnPlatform(false);
                }
            }
            
            @Override
            protected void onCollisionEnd(Entity goblin2, Entity platform) {
                DisappearingPlatform disappearing = platform.getComponent(DisappearingPlatform.class);
                if (disappearing != null) {
                    disappearing.setPlayerOnPlatform(false);
                }
            }
        });

        // 冰面碰撞處理
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.ICE) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity ice) {
                if (isStandingOnIce(goblin, ice)) {
                    goblin.getComponent(Goblin.class).setOnIce(true);
                    goblin.getComponent(Goblin.class).onGroundCollision();
                }
            }
            
            @Override
            protected void onCollisionEnd(Entity goblin, Entity ice) {
                goblin.getComponent(Goblin.class).setOnIce(false);
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

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.ICE) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity ice) {
                if (isStandingOnIce(goblin2, ice)) {
                    goblin2.getComponent(Goblin.class).setOnIce(true);
                    goblin2.getComponent(Goblin.class).onGroundCollision();
                }
            }
            
            @Override
            protected void onCollisionEnd(Entity goblin2, Entity ice) {
                goblin2.getComponent(Goblin.class).setOnIce(false);
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

        // 岩漿碰撞處理
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.LAVA) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity lava) {
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.LAVA) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity lava) {
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        // 刺的碰撞處理
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity spike) {
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity spike) {
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });

        // 弓箭的碰撞處理
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.BULLET) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity arrow) {
                arrow.removeFromWorld();
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.BULLET) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity arrow) {
                arrow.removeFromWorld();
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        // 弓箭與平台的碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity arrow, Entity platform) {
                arrow.removeFromWorld();
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.MOVING) {
            @Override
            protected void onCollisionBegin(Entity arrow, Entity platform) {
                arrow.removeFromWorld();
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.DISAPPEARING) {
            @Override
            protected void onCollisionBegin(Entity arrow, Entity platform) {
                arrow.removeFromWorld();
            }
        });
        
        // 弓箭與刺的碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity arrow, Entity spike) {
                arrow.removeFromWorld();
            }
        });
    }
}