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
import com.doggybear.component.Platform;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

import com.almasb.fxgl.dsl.FXGL;

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

        Texture texture = FXGL.getAssetLoader().loadTexture("goblin.png");
        texture.setFitWidth(50);
        texture.setFitHeight(50);

        return entityBuilder(data)
                .type(EntityType.GOBLIN)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .view(texture)
                .with(physics)
                .with(new Goblin())
                .with(new CollidableComponent(true))
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
        
        int width = data.get("width");
        int height = data.get("height");
        
        Platform platform = new Platform(width, height);
        
        if (data.hasKey("color")) {
            platform.setColor(data.get("color"));
        }
        
        if (data.hasKey("moving")) {
            double speed = data.hasKey("moveSpeed") ? data.get("moveSpeed") : 100.0;
            double distance = data.hasKey("moveDistance") ? data.get("moveDistance") : 200.0;
            platform.setMoving(speed, distance);
        }
        
        return entityBuilder(data)
                .type(EntityType.PLATFORM)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics)
                .viewWithBBox(new Rectangle(width, height, Color.BROWN))
                .with(platform)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("lava")
    public Entity newLava(SpawnData data) {
        int width, height;
        
        Object widthObj = data.get("width");
        Object heightObj = data.get("height");
        
        if (widthObj instanceof Number) {
            width = ((Number)widthObj).intValue();
        } else {
            width = Integer.parseInt(widthObj.toString());
        }
        
        if (heightObj instanceof Number) {
            height = ((Number)heightObj).intValue();
        } else {
            height = Integer.parseInt(heightObj.toString());
        }
        
        Group lavaGroup = new Group();
        
        int blockSize = 40; // 40x40
        
        int columns = (width + blockSize - 1) / blockSize;
        int rows = (height + blockSize - 1) / blockSize;
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                Texture lavaBlock = FXGL.getAssetLoader().loadTexture("lava.png");
                
                lavaBlock.setFitWidth(blockSize);
                lavaBlock.setFitHeight(blockSize);
                
                lavaBlock.setTranslateX(col * blockSize);
                lavaBlock.setTranslateY(row * blockSize);
                
                lavaGroup.getChildren().add(lavaBlock);
            }
        }
        
        Rectangle clipRect = new Rectangle(width, height);
        lavaGroup.setClip(clipRect);
        
        return entityBuilder(data)
                .type(EntityType.LAVA)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .view(lavaGroup)
                .with(new CollidableComponent(true))
                .build();
    }
}