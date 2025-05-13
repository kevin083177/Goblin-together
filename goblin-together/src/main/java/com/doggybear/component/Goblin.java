package com.doggybear.component;

import static com.almasb.fxgl.dsl.FXGL.getAppWidth;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.time.LocalTimer;
import javafx.util.Duration;

public class Goblin extends Component {
    private PhysicsComponent physics;
    
    private static final double MOVE_SPEED = 300;
    private static final double JUMP_HEIGHT = 500;
    
    private boolean canJump = true;
    private LocalTimer jumpTimer;
    private final Duration jumpTimeout = Duration.seconds(0.3);
    
    private boolean isJumping = false;
    
    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
        jumpTimer = FXGL.newLocalTimer();
        jumpTimer.capture();
    }
    
    @Override
    public void onUpdate(double tpf) {
        // 檢查 Goblin 不會超出邊界
        keepWithinScreenBounds();
    }
    
    public void moveRight() {
        physics.setVelocityX(MOVE_SPEED);
    }
    
    public void moveLeft() {
        physics.setVelocityX(-MOVE_SPEED);
    }
    
    public void jump() {
        // System.out.println("canJump = " + canJump + ", Y-Velocity = " + physics.getVelocityY());
        
        if (canJump) {
            physics.setVelocityY(-JUMP_HEIGHT);
            canJump = false;
            isJumping = true;
            jumpTimer.capture();
            // System.out.println("跳躍成功");
        }
    }
    
    // 檢測是否在地面上
    public void onGroundCollision() {
        if (isJumping && jumpTimer.elapsed(jumpTimeout)) {
            canJump = true;
            isJumping = false;
            // System.out.println("跳躍重置");
        }
    }
    
    public void stop() {
        physics.setVelocityX(0);
    }
    // 確保玩家不會跑出邊界外
    private void keepWithinScreenBounds() {
        double currentX = entity.getX();
        double entityWidth = entity.getWidth();
        double screenWidth = getAppWidth();
        
        double velocityX = physics.getVelocityX();
        
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
    }
}