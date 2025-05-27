package com.doggybear.controller;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.PhysicsWorld;
import com.doggybear.component.Goblin;
import com.doggybear.network.NetworkGameManager;
import com.doggybear.type.EntityType;

import static com.almasb.fxgl.dsl.FXGL.getPhysicsWorld;

public class PhysicsController {
    
    private NetworkGameManager networkGameManager;
    private GameOverCallback gameOverCallback;
    
    public interface GameOverCallback {
        void onGameOver();
    }
    
    public PhysicsController(NetworkGameManager networkGameManager, GameOverCallback gameOverCallback) {
        this.networkGameManager = networkGameManager;
        this.gameOverCallback = gameOverCallback;
    }
    
    public void initPhysics() {
        PhysicsWorld physicsWorld = getPhysicsWorld();
        
        // 哥布林與平台碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity platform) {
                goblin.getComponent(Goblin.class).onGroundCollision();
                
                PhysicsComponent physics = goblin.getComponent(PhysicsComponent.class);
                if (physics.getVelocityY() > -100) {
                    goblin.getComponent(Goblin.class).resetJump();
                }
            }
            
            @Override
            protected void onCollision(Entity goblin, Entity platform) {
                PhysicsComponent physics = goblin.getComponent(PhysicsComponent.class);
                if (Math.abs(physics.getVelocityY()) < 100) {
                    goblin.getComponent(Goblin.class).resetJump();
                }
            }
        });

        // 哥布林2與平台碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity platform) {
                goblin2.getComponent(Goblin.class).onGroundCollision();
                
                PhysicsComponent physics = goblin2.getComponent(PhysicsComponent.class);
                if (physics.getVelocityY() > -100) {
                    goblin2.getComponent(Goblin.class).resetJump();
                }
            }
            
            @Override
            protected void onCollision(Entity goblin2, Entity platform) {
                PhysicsComponent physics = goblin2.getComponent(PhysicsComponent.class);
                if (Math.abs(physics.getVelocityY()) < 100) {
                    goblin2.getComponent(Goblin.class).resetJump();
                }
            }
        });

        // 哥布林與岩漿碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.LAVA) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity lava) {
                if (networkGameManager.isNetworkGame() && !networkGameManager.getNetworkManager().isHost()) {
                    // 客戶端不處理碰撞，等待主機端判定
                    return;
                }
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });

        // 哥布林2與岩漿碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.LAVA) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity lava) {
                if (networkGameManager.isNetworkGame() && !networkGameManager.getNetworkManager().isHost()) {
                    // 客戶端不處理碰撞，等待主機端判定
                    return;
                }
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        // 哥布林與刺碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity spike) {
                if (networkGameManager.isNetworkGame() && !networkGameManager.getNetworkManager().isHost()) {
                    return;
                }
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        // 哥布林2與刺碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity spike) {
                if (networkGameManager.isNetworkGame() && !networkGameManager.getNetworkManager().isHost()) {
                    return;
                }
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        // 哥布林與箭碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.ARROW) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity arrow) {
                arrow.removeFromWorld();
                if (networkGameManager.isNetworkGame() && !networkGameManager.getNetworkManager().isHost()) {
                    return;
                }
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        // 哥布林2與箭碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.ARROW) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity arrow) {
                arrow.removeFromWorld();
                if (networkGameManager.isNetworkGame() && !networkGameManager.getNetworkManager().isHost()) {
                    return;
                }
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver();
                }
            }
        });
        
        // 箭與平台碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.ARROW, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity arrow, Entity platform) {
                arrow.removeFromWorld();
            }
        });
        
        // 箭與刺碰撞
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.ARROW, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity arrow, Entity spike) {
                arrow.removeFromWorld();
            }
        });
        
        // 繩索相關碰撞（如果需要的話）
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.GOBLIN2) {
            @Override
            protected void onCollisionBegin(Entity goblin1, Entity goblin2) {
                // 處理兩個玩家之間的碰撞
                handlePlayerCollision(goblin1, goblin2);
            }
        });
    }
    
    /**
     * 處理玩家間碰撞
     */
    private void handlePlayerCollision(Entity player1, Entity player2) {
        PhysicsComponent physics1 = player1.getComponent(PhysicsComponent.class);
        PhysicsComponent physics2 = player2.getComponent(PhysicsComponent.class);
        
        if (physics1 == null || physics2 == null) return;
        
        // 簡單的推開邏輯
        double deltaX = player2.getX() - player1.getX();
        double deltaY = player2.getY() - player1.getY();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        
        if (distance > 0 && distance < 60) { // 如果太近
            double pushForce = 100;
            double normalX = deltaX / distance;
            double normalY = deltaY / distance;
            
            // 輕微推開
            physics1.setVelocityX(physics1.getVelocityX() - normalX * pushForce);
            physics2.setVelocityX(physics2.getVelocityX() + normalX * pushForce);
        }
    }
    
    /**
     * 重置物理世界（遊戲重新開始時使用）
     */
    public void resetPhysics() {
        PhysicsWorld physicsWorld = getPhysicsWorld();
        physicsWorld.clearCollisionHandlers();
        
        // 重新初始化
        initPhysics();
    }
    
    /**
     * 設置重力
     */
    public void setGravity(double x, double y) {
        getPhysicsWorld().setGravity(x, y);
    }
}