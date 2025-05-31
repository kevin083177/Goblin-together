package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import static com.almasb.fxgl.dsl.FXGL.getAssetLoader;
import javafx.scene.Node;

public class Spike extends Component {
    private Texture texture;
    private int width = 40;
    private int height = 20;

    public Spike() {
        createSpikeTexture();
    }

    private void createSpikeTexture() {
        texture = getAssetLoader().loadTexture("Spike.png");
        
        texture.setFitWidth(width);
        texture.setFitHeight(height);
        texture.setPreserveRatio(true);
    }

    public Node getViewNode() {
        return texture;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}