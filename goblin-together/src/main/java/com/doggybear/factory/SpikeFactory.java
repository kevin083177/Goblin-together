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
import com.doggybear.component.Spike;
import com.doggybear.type.EntityType;

import javafx.scene.paint.Color;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class SpikeFactory implements EntityFactory {

    @Spawns("spike")
    public Entity newSpike(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        
        int width = data.get("width");
        int height = data.get("height");
        
        Spike spike = new Spike(width, height);
        
        if (data.hasKey("color")) {
            spike.setColor((Color) data.get("color"));
        }

        return entityBuilder(data)
                .type(EntityType.SPIKE)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .view(spike.getViewNode())
                .with(physics)
                .with(spike)
                .with(new CollidableComponent(true))
                .build();
    }
}