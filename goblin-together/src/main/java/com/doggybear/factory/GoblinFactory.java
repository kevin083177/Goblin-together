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
    private Texture texture;

    @Spawns("goblin")
    public Entity newGoblin(SpawnData data) {
        return createPlayer(data, EntityType.GOBLIN, 1);
    }
    
    @Spawns("goblin2")
    public Entity newGoblin2(SpawnData data) {
        return createPlayer(data, EntityType.GOBLIN2, 2);
    }
    
    private Entity createPlayer(SpawnData data, EntityType entityType, int playerId) {
        PhysicsComponent physics = new PhysicsComponent();
        
        FixtureDef fd = new FixtureDef();
        fd.setFriction(0.0f); // 摩擦力
        fd.setDensity(2.0f); // 密度
        fd.setRestitution(0.0f); // 彈性
        
        physics.setFixtureDef(fd);
        physics.setBodyType(BodyType.DYNAMIC);
        
        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setFixedRotation(true);
            physics.getBody().setLinearDamping(5.0f);
        });

        switch (playerId) {
            case 1:
                texture = FXGL.getAssetLoader().loadTexture("goblin.png");
                break;
            case 2:
                texture = FXGL.getAssetLoader().loadTexture("goblin2.png");
        }
        texture.setFitWidth(50);
        texture.setFitHeight(50);

        return entityBuilder(data)
                .type(entityType)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .view(texture)
                .with(physics)
                .with(new Goblin(playerId))
                .with(new CollidableComponent(true))
                .build();
    }
}