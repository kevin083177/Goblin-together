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
import com.doggybear.component.Goblin;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class Game implements EntityFactory {
    
    @Spawns("goblin")
    public Entity newGoblin(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        
        FixtureDef fd = new FixtureDef();
        fd.setFriction(0.0f); // 摩擦力
        fd.setDensity(0.5f); // 密度
        fd.setRestitution(0.0f); // 彈性
        
        physics.setFixtureDef(fd);
        physics.setBodyType(BodyType.DYNAMIC);
        
        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setFixedRotation(true);
        });

        return entityBuilder(data)
                .type(EntityType.GOBLIN)
                .bbox(new HitBox(BoundingShape.box(30, 30)))
                .view(new Rectangle(30, 30, Color.GREEN))
                .with(physics)
                .with(new Goblin())
                .build();
    }
    
    @Spawns("platform")
    public Entity newPlatform(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        
        FixtureDef fd = new FixtureDef();
        fd.setFriction(1.0f);
        fd.setDensity(1.0f);
        physics.setFixtureDef(fd);
        
        physics.setBodyType(BodyType.STATIC);
        
        return entityBuilder(data)
                .type(EntityType.PLATFORM)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(physics)
                .viewWithBBox(new Rectangle(data.<Integer>get("width"), data.<Integer>get("height"), 
                        Color.BROWN))
                .with(new CollidableComponent(true))
                .build();
    }
}