// 创建新文件：src/main/java/com/doggybear/factory/PlayerFactory.java
package com.doggybear.factory;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.almasb.fxgl.texture.Texture;
import com.doggybear.component.Goblin;
import com.doggybear.type.EntityType;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import com.almasb.fxgl.dsl.FXGL;

public class GoblinFactory implements EntityFactory {
    
    @Spawns("goblin")
    public Entity newGoblin(SpawnData data) {
        return createPlayer(data, EntityType.GOBLIN, 1, false);
    }
    
    @Spawns("goblin2")
    public Entity newGoblin2(SpawnData data) {
        return createPlayer(data, EntityType.GOBLIN2, 2, true);
    }
    
    private Entity createPlayer(SpawnData data, EntityType entityType, int playerId, boolean useColorEffect) {
        PhysicsComponent physics = new PhysicsComponent();
        
        FixtureDef fd = new FixtureDef();
        fd.setFriction(0.0f); // 摩擦力
        fd.setDensity(2.0f); // 密度，让哥布林更稳定
        fd.setRestitution(0.0f); // 彈性
        
        physics.setFixtureDef(fd);
        physics.setBodyType(BodyType.DYNAMIC);
        
        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setFixedRotation(true);
            // 设置线性阻尼，减少滑动
            physics.getBody().setLinearDamping(5.0f);
        });

        Texture texture = FXGL.getAssetLoader().loadTexture("goblin.png");
        texture.setFitWidth(50);
        texture.setFitHeight(50);
        
        // 为第二个玩家添加颜色效果以区分
        if (useColorEffect) {
            texture.setEffect(new javafx.scene.effect.ColorAdjust(0.5, 0.5, 0.0, 0.0));
        }

        return entityBuilder(data)
                .type(entityType)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .view(texture)
                .with(physics)
                .with(new Goblin(playerId)) // 使用统一的Player组件
                .with(new CollidableComponent(true))
                .build();
    }
}