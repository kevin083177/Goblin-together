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
import com.doggybear.component.Platform;
import com.doggybear.type.EntityType;

import javafx.scene.paint.Color;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class PlatformFactory implements EntityFactory {

    @Spawns("platform")
    public Entity newPlatform(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        FixtureDef fd = new FixtureDef();
        fd.setFriction(1.0f);
        fd.setDensity(1.0f);
        physics.setFixtureDef(fd);

        int width = ((Number) data.get("width")).intValue();
        int height = ((Number) data.get("height")).intValue();

        Platform platform = new Platform(width, height)
            .setPhysics(physics);

        if (data.hasKey("color")) {
            platform.setColor((Color) data.get("color"));
        }

        if (data.hasKey("moving")) {
            physics.setBodyType(BodyType.KINEMATIC);

            double speed = ((Number) data.get("speed")).doubleValue();
            double distance = ((Number) data.get("distance")).doubleValue();
            platform.setMoving(speed, distance);
        } else {
            physics.setBodyType(BodyType.STATIC);
        }

        return entityBuilder(data)
                .type(EntityType.PLATFORM)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics)
                .view(platform.getViewNode())
                .with(platform)
                .with(new CollidableComponent(true))
                .build();
    }
}