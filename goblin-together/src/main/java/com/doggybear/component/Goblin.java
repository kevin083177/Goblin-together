// 创建新文件：src/main/java/com/doggybear/component/Player.java
package com.doggybear.component;

import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.time.LocalTimer;
import com.doggybear.type.EntityType;
import javafx.util.Duration;

public class Goblin extends Component {
    private PhysicsComponent physics;
    
    private static final double MOVE_SPEED = 300;
    private static final double JUMP_HEIGHT = 600;
    private static final int WORLD_HEIGHT = 10000;
    
    private boolean canJump = true;
    private LocalTimer jumpTimer;
    private final Duration jumpTimeout = Duration.seconds(0.3);
    private boolean isJumping = false;
    
    // 玩家标识
    private int playerId;
    private EntityType playerType;
    private EntityType otherPlayerType;
    
    public Goblin(int playerId) {
        this.playerId = playerId;
        
        // 根据玩家ID设置对应的实体类型
        if (playerId == 1) {
            this.playerType = EntityType.GOBLIN;
            this.otherPlayerType = EntityType.GOBLIN2;
        } else {
            this.playerType = EntityType.GOBLIN2;
            this.otherPlayerType = EntityType.GOBLIN;
        }
    }
    
    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
        jumpTimer = FXGL.newLocalTimer();
        jumpTimer.capture();
    }
    
    @Override
    public void onUpdate(double tpf) {
        keepWithinScreenBounds();
    }
    
    public void moveRight() {
        physics.setVelocityX(MOVE_SPEED);
    }
    
    public void moveLeft() {
        physics.setVelocityX(-MOVE_SPEED);
    }
    
    public void jump() {
        if (canJump) {
            physics.setVelocityY(-JUMP_HEIGHT);
            canJump = false;
            isJumping = true;
            jumpTimer.capture();
        }
    }
    
    public void onGroundCollision() {
        if (isJumping && jumpTimer.elapsed(jumpTimeout)) {
            canJump = true;
            isJumping = false;
        }
    }
    
    public void stop() {
        physics.setVelocityX(0);
    }
    
    // 完整的边界检测（包含上下和玩家重叠检测）
    private void keepWithinScreenBounds() {
        double currentX = entity.getX();
        double currentY = entity.getY();
        double entityWidth = entity.getWidth();
        double entityHeight = entity.getHeight();
        double screenWidth = getAppWidth();
        double screenHeight = getAppHeight();
        
        double velocityX = physics.getVelocityX();
        double velocityY = physics.getVelocityY();
        
        // 获取另一个玩家
        Entity otherPlayer = getOtherPlayer();
        
        // 左右边界检测
        if (currentX < 0) {
            entity.setX(0);
            if (velocityX < 0) {
                physics.setVelocityX(0);
            }
        }
        else if (currentX + entityWidth > screenWidth) {
            entity.setX(screenWidth - entityWidth);
            if (velocityX > 0) {
                physics.setVelocityX(0);
            }
        }
        
        // 上下边界检测
        if (currentY < -WORLD_HEIGHT) {
            entity.setY(-WORLD_HEIGHT);
            if (velocityY < 0) {
                physics.setVelocityY(0);
            }
        }
        else if (currentY + entityHeight > screenHeight + 1000) {
            entity.setY(screenHeight + 1000 - entityHeight);
            if (velocityY > 0) {
                physics.setVelocityY(Math.min(0, velocityY));
            }
        }
        
        // 检查与另一个玩家的重叠
        if (otherPlayer != null && isOverlappingWith(otherPlayer)) {
            resolveOverlap(otherPlayer);
        }
    }
    
    // 获取另一个玩家
    private Entity getOtherPlayer() {
        try {
            var otherPlayers = FXGL.getGameWorld().getEntitiesByType(otherPlayerType);
            return otherPlayers.isEmpty() ? null : otherPlayers.get(0);
        } catch (Exception e) {
            return null;
        }
    }
    
    // 检查是否与另一个玩家重叠
    private boolean isOverlappingWith(Entity otherPlayer) {
        double thisX = entity.getX();
        double thisY = entity.getY();
        double thisWidth = entity.getWidth();
        double thisHeight = entity.getHeight();
        
        double otherX = otherPlayer.getX();
        double otherY = otherPlayer.getY();
        double otherWidth = otherPlayer.getWidth();
        double otherHeight = otherPlayer.getHeight();
        
        // 水平重叠检测
        boolean horizontalOverlap = thisX < otherX + otherWidth && thisX + thisWidth > otherX;
        
        // 垂直重叠检测
        boolean verticalOverlap = thisY < otherY + otherHeight && thisY + thisHeight > otherY;
        
        return horizontalOverlap && verticalOverlap;
    }
    
    // 解决重叠问题
    private void resolveOverlap(Entity otherPlayer) {
        double thisX = entity.getX();
        double thisY = entity.getY();
        double thisWidth = entity.getWidth();
        double thisHeight = entity.getHeight();
        
        double otherX = otherPlayer.getX();
        double otherY = otherPlayer.getY();
        double otherWidth = otherPlayer.getWidth();
        double otherHeight = otherPlayer.getHeight();
        
        double screenWidth = getAppWidth();
        double screenHeight = getAppHeight();
        
        // 计算重叠量
        double horizontalOverlap = Math.min(thisX + thisWidth - otherX, otherX + otherWidth - thisX);
        double verticalOverlap = Math.min(thisY + thisHeight - otherY, otherY + otherHeight - thisY);
        
        // 选择重叠量较小的方向进行分离
        if (horizontalOverlap < verticalOverlap) {
            // 水平分离
            physics.setVelocityX(0);
            
            if (thisX < otherX) {
                // 当前玩家在左边
                double newX = otherX - thisWidth - 1;
                if (newX < 0) newX = 0;
                entity.setX(newX);
            } else {
                // 当前玩家在右边
                double newX = otherX + otherWidth + 1;
                if (newX + thisWidth > screenWidth) newX = screenWidth - thisWidth;
                entity.setX(newX);
            }
        } else {
            // 垂直分离
            if (thisY < otherY) {
                // 当前玩家在上方
                double newY = otherY - thisHeight - 1;
                if (newY < -WORLD_HEIGHT) newY = -WORLD_HEIGHT;
                entity.setY(newY);
                physics.setVelocityY(Math.min(0, physics.getVelocityY()));
            } else {
                // 当前玩家在下方
                double newY = otherY + otherHeight + 1;
                if (newY + thisHeight > screenHeight + 1000) newY = screenHeight + 1000 - thisHeight;
                entity.setY(newY);
                physics.setVelocityY(Math.max(0, physics.getVelocityY()));
            }
        }
    }
    
    // 获取玩家ID
    public int getPlayerId() {
        return playerId;
    }
}