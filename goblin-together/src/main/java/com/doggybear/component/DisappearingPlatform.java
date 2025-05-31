package com.doggybear.component;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.Node;

public class DisappearingPlatform extends Component {
    private PhysicsComponent physics;
    private Node viewNode;
    
    private boolean playerOnPlatform = false;
    private boolean isDisappearing = false;
    private double disappearTimer = 0.0;
    private double disappearDuration = 3.0;
    private double reappearDuration = 3.0;
    private boolean isVisible = true;
    private boolean isCountingDown = false;
    
    private double initialX;
    private double initialY;
    
    private Entity playerEntity = null;

    public void setPhysics(PhysicsComponent physics) {
        this.physics = physics;
    }

    public void setViewNode(Node viewNode) {
        this.viewNode = viewNode;
    }

    @Override
    public void onAdded() {
        initialX = entity.getX();
        initialY = entity.getY();
    }

    @Override
    public void onUpdate(double tpf) {
        if (!isDisappearing) return;
        
        if (isVisible) {
            if (playerOnPlatform && !isCountingDown) {
                isCountingDown = true;
                disappearTimer = 0.0;
            }
            
            if (isCountingDown) {
                disappearTimer += tpf;
                
                double opacity = Math.max(0.0, 1.0 - (disappearTimer / disappearDuration));
                viewNode.setOpacity(opacity);
                
                if (disappearTimer >= disappearDuration) {
                    disappear();
                }
            }
        } else {
            disappearTimer += tpf;
            
            if (disappearTimer >= reappearDuration) {
                reappear();
            }
        }
    }

    public void setPlayerOnPlatform(boolean onPlatform, Entity player) {
        this.playerOnPlatform = onPlatform;
        this.playerEntity = onPlatform ? player : null;
    }

    public void setDisappearing(boolean disappearing) {
        this.isDisappearing = disappearing;
        this.disappearTimer = 0.0;
    }
    
    public void setDisappearTimes(double disappearTime, double reappearTime) {
        this.disappearDuration = disappearTime;
        this.reappearDuration = reappearTime;
    }

    private void disappear() {
        isVisible = false;
        isCountingDown = false;
        disappearTimer = 0.0;
        playerOnPlatform = false;
        viewNode.setOpacity(0.0);
        viewNode.setVisible(false);
        
        if (entity.hasComponent(CollidableComponent.class)) {
            entity.getComponent(CollidableComponent.class).setValue(false);
        }
        
        if (physics != null) {
            physics.setLinearVelocity(0, 0);
            physics.overwritePosition(new Point2D(entity.getX(), -10000));
        }
        
        if (playerEntity != null) {
            Goblin goblin = playerEntity.getComponent(Goblin.class);
            if (goblin != null) {
                goblin.setOnGround(false);
                goblin.setPlatformDisappeared();
            }
            playerEntity = null;
        }
    }
    
    private void reappear() {
        isVisible = true;
        isCountingDown = false;
        disappearTimer = 0.0;
        viewNode.setOpacity(1.0);
        viewNode.setVisible(true);
        
        if (entity.hasComponent(CollidableComponent.class)) {
            entity.getComponent(CollidableComponent.class).setValue(true);
        }
        
        if (physics != null) {
            physics.overwritePosition(new Point2D(initialX, initialY));
        }
    }

    public boolean isVisible() {
        return isVisible;
    }
}