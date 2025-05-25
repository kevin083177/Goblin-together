package com.doggybear.factory;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.Texture;
import com.doggybear.component.Lava;
import com.doggybear.type.EntityType;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import com.almasb.fxgl.dsl.FXGL;

public class LavaFactory implements EntityFactory {

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

        int frameCount = 2; // lava_1.png ~ lava_2.png
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