package com.doggybear.component;

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
            // 只有當玩家站在平台上時才開始倒數
            if (playerOnPlatform && !isCountingDown) {
                isCountingDown = true;
                disappearTimer = 0.0; // 重置計時器
            }
            
            // 只有在倒數開始後才更新計時器
            if (isCountingDown) {
                disappearTimer += tpf;
                
                // 平台正在消失
                // 计算当前不透明度 (1.0 -> 0.0)
                double opacity = Math.max(0.0, 1.0 - (disappearTimer / disappearDuration));
                viewNode.setOpacity(opacity);
                
                // 当计时器达到消失时间
                if (disappearTimer >= disappearDuration) {
                    // 完全消失
                    disappear();
                }
            }
        } else {
            // 平台已消失，等待後自動重新出現
            disappearTimer += tpf;
            
            // 當計時器達到重新出現時間，直接完全恢復
            if (disappearTimer >= reappearDuration) {
                reappear();
            }
        }
    }

    public void setPlayerOnPlatform(boolean onPlatform) {
        this.playerOnPlatform = onPlatform;
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
        viewNode.setOpacity(0.0); // 完全透明
        viewNode.setVisible(false); // 隱藏視圖
        
        // 禁用碰撞
        if (entity.hasComponent(CollidableComponent.class)) {
            entity.getComponent(CollidableComponent.class).setValue(false);
        }
        
        // 停止物理效果並移動到遠處
        if (physics != null) {
            physics.setLinearVelocity(0, 0);
            physics.overwritePosition(new Point2D(entity.getX(), -10000)); // 移動到畫面外很遠的地方
        }
    }
    
    private void reappear() {
        isVisible = true;
        isCountingDown = false;
        disappearTimer = 0.0;
        viewNode.setOpacity(1.0); // 直接恢復完全不透明
        viewNode.setVisible(true); // 顯示視圖
        
        // 启用碰撞
        if (entity.hasComponent(CollidableComponent.class)) {
            entity.getComponent(CollidableComponent.class).setValue(true);
        }
        
        // 恢復物理位置
        if (physics != null) {
            physics.overwritePosition(new Point2D(initialX, initialY)); // 恢復到原始位置
        }
    }

    public boolean isVisible() {
        return isVisible;
    }
}