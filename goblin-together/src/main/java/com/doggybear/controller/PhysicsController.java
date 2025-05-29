package com.doggybear.controller;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;
import com.doggybear.component.Goblin;
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
        
        // 第一個玩家的碰撞處理
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity platform) {
                goblin.getComponent(Goblin.class).onGroundCollision();
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.LAVA) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity lava) {
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        // 第二個玩家的碰撞處理
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity platform) {
                goblin2.getComponent(Goblin.class).onGroundCollision();
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
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.ARROW) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity arrow) {
                // 移除弓箭
                arrow.removeFromWorld();
                // 觸發遊戲結束
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.ARROW) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity arrow) {
                // 移除弓箭
                arrow.removeFromWorld();
                // 觸發遊戲結束
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        // 弓箭與平台的碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.ARROW, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity arrow, Entity platform) {
                arrow.removeFromWorld();
            }
        });
        
        // 弓箭與刺的碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.ARROW, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity arrow, Entity spike) {
                arrow.removeFromWorld();
            }
        });
    }
}