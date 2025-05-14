package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.util.Duration;

public class Lava extends Component {

    private Group group;
    private int blockSize = 40;

    public Lava(Group group) {
        this.group = group;
    }

    @Override
    public void onAdded() {
        startLavaAnimation(group);
    }

    private void startLavaAnimation(Group lavaGroup) {
        for (var node : lavaGroup.getChildren()) {
            if (node instanceof Group) {
                Group block = (Group) node;

                if (block.getChildren().size() < 3) continue;

                Texture lava1 = (Texture) block.getChildren().get(0);
                Texture lava2 = (Texture) block.getChildren().get(1);
                Texture lava3 = (Texture) block.getChildren().get(2);

                Timeline timeline = new Timeline();
                timeline.setCycleCount(Timeline.INDEFINITE);

                KeyFrame frame1 = new KeyFrame(Duration.ZERO,
                        e -> { lava1.setVisible(true); lava2.setVisible(false); lava3.setVisible(false); });
                KeyFrame frame2 = new KeyFrame(Duration.seconds(0.5),
                        e -> { lava1.setVisible(false); lava2.setVisible(true); lava3.setVisible(false); });
                KeyFrame frame3 = new KeyFrame(Duration.seconds(1.0),
                        e -> { lava1.setVisible(false); lava2.setVisible(false); lava3.setVisible(true); });
                KeyFrame frameEnd = new KeyFrame(Duration.seconds(1.5));

                timeline.getKeyFrames().addAll(frame1, frame2, frame3, frameEnd);
                timeline.play();
            }
        }
    }
}