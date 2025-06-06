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
    private static final double JUMP_HEIGHT = 800;
    private static final int WORLD_HEIGHT = 10000;
    
    private boolean canJump = true;
    private LocalTimer jumpTimer;
    private final Duration jumpTimeout = Duration.seconds(0.3);
    private boolean isJumping = false;
    
    private boolean onGround = false;
    
    private boolean isStandingOnPlayer = false;
    private Entity standingOnPlayer = null;
    
    private int playerId;
    private EntityType playerType;
    private EntityType otherPlayerType;
    
    private boolean onIce = false;
    private double iceAcceleration = 0.3;
    private double iceDeceleration = 0.1;
    
    // 新增：網絡控制模式標誌
    private boolean isNetworkControlled = false;

    public Goblin(int playerId) {
        this.playerId = playerId;
        
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
        // 如果是網絡控制模式，跳過所有本地邏輯
        if (isNetworkControlled) {
            return;
        }
        
        keepWithinScreenBounds();
        updateStandingState();
        
        canJump = (onGround || isStandingOnPlayer) && !isJumping;
    }
    
    /**
     * 設置網絡控制模式
     * 在此模式下，角色位置完全由網絡同步控制，不執行任何本地邏輯
     */
    public void setNetworkControlled(boolean networkControlled) {
        this.isNetworkControlled = networkControlled;
        
        if (networkControlled) {
            // 禁用所有本地狀態
            canJump = false;
            isJumping = false;
            onGround = false;
            isStandingOnPlayer = false;
            standingOnPlayer = null;
            onIce = false;
        }
    }
    
    public boolean isNetworkControlled() {
        return isNetworkControlled;
    }
    
    public void setOnGround(boolean onGround) {
        if (isNetworkControlled) return;
        
        this.onGround = onGround;
        
        if (onGround) {
            canJump = true;
            isJumping = false;
            jumpTimer.capture();
        }
    }
    
    public boolean isOnGround() {
        return onGround || isStandingOnPlayer;
    }
    
    public void moveRight() {
        if (isNetworkControlled) return;
        
        if (onIce) {
            physics.setVelocityX(physics.getVelocityX() + MOVE_SPEED * iceAcceleration);
        } else {
            physics.setVelocityX(MOVE_SPEED);
        }
    }
    
    public void moveLeft() {
        if (isNetworkControlled) return;
        
        if (onIce) {
            physics.setVelocityX(physics.getVelocityX() - MOVE_SPEED * iceAcceleration);
        } else {
            physics.setVelocityX(-MOVE_SPEED);
        }
    }
    
    public void jump() {
        if (isNetworkControlled) return;
        
        if (canJump) {
            double jumpForce = JUMP_HEIGHT;
            
            if (isStandingOnPlayer) {
                isStandingOnPlayer = false;
                standingOnPlayer = null;
            }
            
            physics.setVelocityY(-jumpForce);
            canJump = false;
            isJumping = true;
            onGround = false;
            jumpTimer.capture();
        }
    }
    
    public void onGroundCollision() {
        if (isNetworkControlled) return;
        
        setOnGround(true);
        
        if (isJumping && jumpTimer.elapsed(jumpTimeout)) {
            canJump = true;
            isJumping = false;
        }
    }
    
    public void stop() {
        if (isNetworkControlled) return;
        
        if (onIce) {
            double currentVX = physics.getVelocityX();
            double newVX = currentVX * (1 - iceDeceleration);
            
            if (Math.abs(newVX) < 10) newVX = 0;
            
            physics.setVelocityX(newVX);
        } else {
            physics.setVelocityX(0);
        }
    }
    
    private void updateStandingState() {
        if (isNetworkControlled) return;
        
        Entity otherPlayer = getOtherPlayer();
        if (otherPlayer == null) return;
        
        if (isStandingOnOtherPlayer(otherPlayer)) {
            if (!isStandingOnPlayer) {
                isStandingOnPlayer = true;
                standingOnPlayer = otherPlayer;
                canJump = true;
                isJumping = false;
                jumpTimer.capture();
            }
        } else {
            isStandingOnPlayer = false;
            standingOnPlayer = null;
        }
    }
    
    private boolean isStandingOnOtherPlayer(Entity otherPlayer) {
        double thisX = entity.getX();
        double thisY = entity.getY();
        double thisWidth = entity.getWidth();
        double thisHeight = entity.getHeight();
        
        double otherX = otherPlayer.getX();
        double otherY = otherPlayer.getY();
        double otherWidth = otherPlayer.getWidth();
        double otherHeight = otherPlayer.getHeight();
        
        boolean horizontalOverlap = thisX < otherX + otherWidth && thisX + thisWidth > otherX;
        
        double bottomY = thisY + thisHeight;
        double otherTopY = otherY;
        boolean isOnTop = bottomY >= otherTopY && bottomY <= otherTopY + 10;
        
        boolean isAbove = thisY < otherY;
        
        return horizontalOverlap && isOnTop && isAbove;
    }
    
    private void keepWithinScreenBounds() {
        if (isNetworkControlled) return;
        
        double currentX = entity.getX();
        double currentY = entity.getY();
        double entityWidth = entity.getWidth();
        double entityHeight = entity.getHeight();
        double screenWidth = getAppWidth();
        double screenHeight = getAppHeight();
        
        double velocityX = physics.getVelocityX();
        double velocityY = physics.getVelocityY();
        
        Entity otherPlayer = getOtherPlayer();
        
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
        
        if (otherPlayer != null && isSideOverlapping(otherPlayer)) {
            resolveSideOverlap(otherPlayer);
        }
    }
    
    private boolean isSideOverlapping(Entity otherPlayer) {
        double thisX = entity.getX();
        double thisY = entity.getY();
        double thisWidth = entity.getWidth();
        double thisHeight = entity.getHeight();
        
        double otherX = otherPlayer.getX();
        double otherY = otherPlayer.getY();
        double otherWidth = otherPlayer.getWidth();
        double otherHeight = otherPlayer.getHeight();
        
        boolean horizontalOverlap = thisX < otherX + otherWidth && thisX + thisWidth > otherX;
        
        boolean verticalOverlap = thisY < otherY + otherHeight && thisY + thisHeight > otherY;
        
        if (horizontalOverlap && verticalOverlap) {
            boolean thisOnTop = isStandingOnOtherPlayer(otherPlayer);
            boolean otherOnThis = isOtherPlayerStandingOnThis(otherPlayer);
            
            return !(thisOnTop || otherOnThis);
        }
        
        return false;
    }
    
    private boolean isOtherPlayerStandingOnThis(Entity otherPlayer) {
        double thisX = entity.getX();
        double thisY = entity.getY();
        double thisWidth = entity.getWidth();
        
        double otherX = otherPlayer.getX();
        double otherY = otherPlayer.getY();
        double otherWidth = otherPlayer.getWidth();
        double otherHeight = otherPlayer.getHeight();
        
        boolean horizontalOverlap = thisX < otherX + otherWidth && thisX + thisWidth > otherX;
        
        double otherBottomY = otherY + otherHeight;
        double thisTopY = thisY;
        boolean otherOnTop = otherBottomY >= thisTopY && otherBottomY <= thisTopY + 10;
        
        boolean otherAbove = otherY < thisY;
        
        return horizontalOverlap && otherOnTop && otherAbove;
    }
    
    private void resolveSideOverlap(Entity otherPlayer) {
        double thisX = entity.getX();
        double thisY = entity.getY();
        double thisWidth = entity.getWidth();
        double thisHeight = entity.getHeight();
        
        double otherX = otherPlayer.getX();
        double otherY = otherPlayer.getY();
        double otherWidth = otherPlayer.getWidth();
        double otherHeight = otherPlayer.getHeight();
        
        double screenWidth = getAppWidth();
        
        double horizontalOverlap = Math.min(thisX + thisWidth - otherX, otherX + otherWidth - thisX);
        double verticalOverlap = Math.min(thisY + thisHeight - otherY, otherY + otherHeight - thisY);
        
        if (horizontalOverlap <= verticalOverlap) {
            physics.setVelocityX(0);
            
            if (thisX < otherX) {
                double newX = otherX - thisWidth - 1;
                if (newX < 0) newX = 0;
                entity.setX(newX);
            } else {
                double newX = otherX + otherWidth + 1;
                if (newX + thisWidth > screenWidth) newX = screenWidth - thisWidth;
                entity.setX(newX);
            }
        }
    }
    
    private Entity getOtherPlayer() {
        try {
            var otherPlayers = FXGL.getGameWorld().getEntitiesByType(otherPlayerType);
            return otherPlayers.isEmpty() ? null : otherPlayers.get(0);
        } catch (Exception e) {
            return null;
        }
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public void resetJump() {
        if (isNetworkControlled) return;
        
        canJump = true;
        isJumping = false;
        jumpTimer.capture();
    }
    
    /**
     * 強制重置跳躍狀態，用於網絡同步中確保跳躍能執行
     */
    public void forceResetJumpState() {
        canJump = true;
        isJumping = false;
        onGround = true; // 強制設置為在地面上
        jumpTimer.capture();
    }

    public void setOnIce(boolean onIce) {
        if (isNetworkControlled) return;
        
        this.onIce = onIce;
    }

    public void setPlatformDisappeared() {
        if (isNetworkControlled) return;
        
        setOnGround(false);
        physics.setVelocityY(0.1);
    }
}