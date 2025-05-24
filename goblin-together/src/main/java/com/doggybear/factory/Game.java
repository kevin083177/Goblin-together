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
import com.doggybear.component.Goblin2;
import com.doggybear.component.Lava;
import com.doggybear.component.Platform;
import com.doggybear.type.EntityType;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

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
    
    @Spawns("goblin2")
    public Entity newGoblin2(SpawnData data) {
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

        // 使用不同顏色的哥布林圖像或者套用顏色濾鏡
        Texture texture = FXGL.getAssetLoader().loadTexture("goblin.png");
        texture.setFitWidth(50);
        texture.setFitHeight(50);
        // 可以設置一個不同的濾鏡來區分
        texture.setEffect(new javafx.scene.effect.ColorAdjust(0.5, 0.5, 0.0, 0.0)); // 調整色調和飽和度

        return entityBuilder(data)
                .type(EntityType.GOBLIN2)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .view(texture)
                .with(physics)
                .with(new Goblin2())
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

    @Spawns("lava")
    public Entity newLava(SpawnData data) {
        int width = data.get("width");
        int height = data.get("height");

        Group lavaGroup = new Group();

        int blockSize = 40;
        int columns = (width + blockSize - 1) / blockSize;
        int rows = (height + blockSize - 1) / blockSize;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                Group animatedBlock = createLavaBlock(blockSize);

                animatedBlock.setTranslateX(col * blockSize);
                animatedBlock.setTranslateY(row * blockSize);

                lavaGroup.getChildren().add(animatedBlock);
            }
        }

        lavaGroup.setClip(new Rectangle(width, height));

        return entityBuilder(data)
                .type(EntityType.LAVA)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .view(lavaGroup)
                .with(new CollidableComponent(true))
                .with(new Lava(lavaGroup))
                .build();
    }

    private Group createLavaBlock(int blockSize) {
        Group group = new Group();

        int frameCount = 2; // lava_1.png ~ lava_3.png
        Texture[] frames = new Texture[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = FXGL.getAssetLoader().loadTexture("lava_" + (i + 1) + ".png");
            frames[i].setFitWidth(blockSize);
            frames[i].setFitHeight(blockSize);
            frames[i].setVisible(i == 0);
            group.getChildren().add(frames[i]);
        }

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        for (int i = 0; i < frameCount; i++) {
            int current = i;
            KeyFrame frame = new KeyFrame(Duration.seconds(0.25 * i), e -> {
                for (int j = 0; j < frameCount; j++) {
                    frames[j].setVisible(j == current);
                }
            });
            timeline.getKeyFrames().add(frame);
        }

        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.25 * frameCount))); // 結束回到起點
        timeline.play();

        return group;
    }
}