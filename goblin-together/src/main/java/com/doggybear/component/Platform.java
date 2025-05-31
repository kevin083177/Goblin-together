package com.doggybear.component;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;

public class Platform extends Component {
    private Texture viewNode;
    private int width;
    private int height;

    public Platform(int width, int height, int imageIndex) {
        this.width = width;
        this.height = height;
        
        String imageName = "platform" + imageIndex + ".png";
        this.viewNode = FXGL.getAssetLoader().loadTexture(imageName);
        
        viewNode.setFitHeight(height);
        viewNode.setFitWidth(width);
    }

    public Texture getViewNode() {
        return viewNode;
    }
}