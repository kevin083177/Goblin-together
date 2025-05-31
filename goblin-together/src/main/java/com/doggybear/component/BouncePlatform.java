package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.Texture;
import com.doggybear.type.EntityType;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

/**
 * 彈跳平台組件：當玩家碰到平台時，會施加一個向上的速度。
 */
public class BouncePlatform extends Component {

    private PhysicsComponent physics;

    private double bounceVelocity;// 彈跳高度
    private int width;
    private int height;
    private Texture viewNode;

    public BouncePlatform(int width, int height, double bounceVelocity) {
        this.width = width;
        this.height = height;
        this.bounceVelocity = bounceVelocity;
        this.viewNode = FXGL.getAssetLoader().loadTexture("bounce_platform.png");
        viewNode.setFitHeight(height);
        viewNode.setFitWidth(width);
    }

    public void bounce(Entity player) {
        PhysicsComponent playerPhysics = player.getComponent(PhysicsComponent.class);
        
        // 確保玩家在彈跳床上方
        if (player.getBottomY() <= entity.getY() + 5) {
            playerPhysics.setVelocityY(-bounceVelocity); // 負值表示向上彈跳
            
            // 重置玩家的跳躍狀態（可選）
            if (player.hasComponent(Goblin.class)) {
                player.getComponent(Goblin.class).resetJump();
            }
        }
    }

    @Override
    public void onUpdate(double tpf) {
        // 檢測與玩家的碰撞
        Entity player = FXGL.getGameWorld()
            .getEntitiesByType(EntityType.GOBLIN, EntityType.GOBLIN2)
            .stream()
            .filter(e -> e.isColliding(entity))
            .findFirst()
            .orElse(null);
        
        if (player != null) {
            bounce(player);
        }
    }

    public Texture getViewNode() {
        return viewNode;
    }
}
