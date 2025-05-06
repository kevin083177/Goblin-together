package com.doggybear.component;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.time.LocalTimer;
import javafx.util.Duration;

public class Goblin extends Component {
    private PhysicsComponent physics;
    
    private static final double MOVE_SPEED = 150;
    private static final double JUMP_HEIGHT = 300;
    
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
        // 檢查是否已在地面上
        if (physics.getVelocityY() == 0) {
            if (isJumping && jumpTimer.elapsed(jumpTimeout)) {
                canJump = true;
                isJumping = false;
                // System.out.println("跳躍重置");
            }
        }
        
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
}