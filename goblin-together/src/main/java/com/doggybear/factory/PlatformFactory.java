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
import com.doggybear.component.Platform;
import com.doggybear.component.MovingPlatform;
import com.doggybear.component.DisappearingPlatform;
import com.doggybear.component.FirePlatform;
import com.doggybear.component.BouncePlatform;
import com.doggybear.component.IcePlatform;
import com.doggybear.type.EntityType;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

import com.almasb.fxgl.dsl.FXGL;

public class PlatformFactory implements EntityFactory {

    @Spawns("platform")
    public Entity newPlatform(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        
        FixtureDef fd = new FixtureDef();
        fd.setFriction(1.0f);
        fd.setDensity(1.0f);
        physics.setFixtureDef(fd);

        int width = ((Number) data.get("width")).intValue();
        int height = ((Number) data.get("height")).intValue();
        int imageIndex = data.hasKey("imageIndex") ? ((Number) data.get("imageIndex")).intValue() : 1;

        Platform platform = new Platform(width, height, imageIndex);

        return entityBuilder(data)
                .type(EntityType.PLATFORM)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics)
                .view(platform.getViewNode())
                .with(platform)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("moving")
    public Entity newMovingPlatform(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);
        
        FixtureDef fd = new FixtureDef();
        fd.setFriction(1.0f);
        fd.setDensity(1.0f);
        physics.setFixtureDef(fd);

        int width = ((Number) data.get("width")).intValue();
        int height = ((Number) data.get("height")).intValue();

        MovingPlatform movingPlatform = new MovingPlatform();
        movingPlatform.setPhysics(physics);

        Texture texture = FXGL.getAssetLoader().loadTexture("platform2.png");
        texture.setFitWidth(width);
        texture.setFitHeight(height);

        if (data.hasKey("auto")) {
            movingPlatform.setAuto(data.get("auto"));
        }

        if (data.hasKey("horizontalMoving")) {
            double speed = ((Number) data.get("speedX")).doubleValue();
            double distance = ((Number) data.get("distanceX")).doubleValue();
            movingPlatform.setHorizontalMovement(speed, distance);
        }
        else if (data.hasKey("verticalMoving")) {
            double speedY = ((Number) data.get("speedY")).doubleValue();
            double distanceY = ((Number) data.get("distanceY")).doubleValue();
            movingPlatform.setVerticalMovement(speedY, distanceY);
        }

        return entityBuilder(data)
                .type(EntityType.MOVING)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics)
                .view(texture)
                .with(movingPlatform)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("disappearing")
    public Entity newDisappearingPlatform(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        
        FixtureDef fd = new FixtureDef();
        fd.setFriction(1.0f);
        fd.setDensity(1.0f);
        physics.setFixtureDef(fd);

        int width = ((Number) data.get("width")).intValue();
        int height = ((Number) data.get("height")).intValue();

        DisappearingPlatform disappearingPlatform = new DisappearingPlatform();

        Texture texture = FXGL.getAssetLoader().loadTexture("platform2.png");
        texture.setFitWidth(width);
        texture.setFitHeight(height);

        disappearingPlatform.setPhysics(physics);
        disappearingPlatform.setViewNode(texture);
        disappearingPlatform.setDisappearing(true);

        if (data.hasKey("disappearTime")) {
            double disappearTime = ((Number) data.get("disappearTime")).doubleValue();
            double reappearTime = ((Number) data.get("reappearTime")).doubleValue();
            disappearingPlatform.setDisappearTimes(disappearTime, reappearTime);
        }

        return entityBuilder(data)
                .type(EntityType.DISAPPEARING)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics)
                .view(texture)
                .with(disappearingPlatform)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("bounce")
    public Entity newBouncePlatform(SpawnData data) {
        int width = ((Number) data.get("width")).intValue();
        int height = ((Number) data.get("height")).intValue();
        double bounceVelocity = ((Number) data.get("bounce")).doubleValue();

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        
        FixtureDef fd = new FixtureDef();
        fd.setFriction(0.0f);
        fd.setRestitution(0.0f);
        physics.setFixtureDef(fd);

        BouncePlatform bouncePlatform = new BouncePlatform(width, height, Math.abs(bounceVelocity));


        return entityBuilder(data)
            .type(EntityType.BOUNCE)
            .bbox(new HitBox(BoundingShape.box(width, height)))
            .view(bouncePlatform.getViewNode())
            .with(physics)
            .with(new CollidableComponent(true))
            .with(bouncePlatform)
            .build();
    }

     @Spawns("fire")
    public Entity newFirePlatform(SpawnData data) {
        int width = data.get("width");
        int height = data.get("height");
        double fireDuration = data.get("fireDuration");
        double normalDuration = data.get("normalDuration");
        
        int imageIndex = data.hasKey("imageIndex") ? ((Number) data.get("imageIndex")).intValue() : 1;
        
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        
        FixtureDef fd = new FixtureDef();
        fd.setFriction(1.0f);
        fd.setDensity(1.0f);
        physics.setFixtureDef(fd);
        
        FirePlatform firePlatform = new FirePlatform(fireDuration, normalDuration, imageIndex);
        
        return entityBuilder(data)
                .type(EntityType.FIRE)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics)
                .with(firePlatform)
                .view(firePlatform.getViewNode())
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("ice")
    public Entity newIcePlatform(SpawnData data) {
        int width = ((Number) data.get("width")).intValue();
        int height = ((Number) data.get("height")).intValue();
        
        int imageIndex = data.hasKey("imageIndex") ? ((Number) data.get("imageIndex")).intValue() : 1;
        
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        
        physics.setFixtureDef(IcePlatform.createIceFixtureDef());
        
        IcePlatform ice = new IcePlatform(width, height, imageIndex);
        
        return entityBuilder(data)
            .type(EntityType.ICE)
            .bbox(new HitBox(BoundingShape.box(width, height)))
            .with(physics)
            .view(ice.getViewNode())
            .with(new CollidableComponent(true))
            .build();
    }
}