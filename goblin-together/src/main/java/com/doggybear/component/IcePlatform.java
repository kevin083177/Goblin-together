package com.doggybear.component;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.almasb.fxgl.texture.Texture;

public class IcePlatform extends Component {
    private Texture viewNode;
    private int width;
    private int height;
    
    private static final double ICE_FRICTION = 0.1;
    private static final double ICE_RESTITUTION = 0.0;
    
    public IcePlatform(int width, int height, int imageIndex) {
        this.width = width;
        this.height = height;
        
        String imageName = "ice_platform" + imageIndex + ".png";
        this.viewNode = FXGL.getAssetLoader().loadTexture(imageName);
        
        viewNode.setFitHeight(height);
        viewNode.setFitWidth(width);
    }
    
    public static FixtureDef createIceFixtureDef() {
        FixtureDef fd = new FixtureDef();
        fd.setFriction((float) ICE_FRICTION);
        fd.setRestitution((float) ICE_RESTITUTION);
        return fd;
    }

    public Texture getViewNode() {
        return viewNode;
    }
    
    public static double getIceFriction() {
        return ICE_FRICTION;
    }
}