package com.doggybear.component;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;

import java.util.ArrayList;
import java.util.List;

public class FirePlatform extends Component {
    private double fireDuration;
    private double normalDuration;
    private double stateTimer = 0;
    private boolean isFireState = false;
    private Texture view;
    private int imageIndex;
    
    // 記錄當前站在平台上的玩家
    private List<Entity> playersOnPlatform = new ArrayList<>();
    
    public FirePlatform(double fireDuration, double normalDuration, int imageIndex) {
        this.fireDuration = fireDuration;
        this.normalDuration = normalDuration;
        this.imageIndex = imageIndex;
    }
    
    @Override
    public void onAdded() {
        double width = entity.getWidth();
        double height = entity.getHeight();
        
        // 根據初始狀態載入對應圖片
        String initialTexturePath = isFireState ? 
            "fire_platform" + imageIndex + ".png" : 
            "platform" + imageIndex + ".png";
            
        view = FXGL.getAssetLoader().loadTexture(initialTexturePath);
        view.setFitWidth(width);
        view.setFitHeight(height);
        updateView();
    }
    
    @Override
    public void onUpdate(double tpf) {
        stateTimer += tpf;
        
        double currentDuration = isFireState ? fireDuration : normalDuration;
        if (stateTimer >= currentDuration) {
            isFireState = !isFireState;
            stateTimer = 0;
            updateView();
        }
    }
    
    private void updateView() {
        if (view == null) return;
        
        String texturePath;
        if (isFireState) {
            texturePath = "fire_platform" + imageIndex + ".png";
        } else {
            texturePath = "platform" + imageIndex + ".png";
        }
        
        view.setImage(FXGL.getAssetLoader().loadTexture(texturePath).getImage());
    }
    
    // 添加玩家到平台
    public void addPlayer(Entity player) {
        if (!playersOnPlatform.contains(player)) {
            playersOnPlatform.add(player);
        }
    }
    
    // 從平台移除玩家
    public void removePlayer(Entity player) {
        playersOnPlatform.remove(player);
    }
        
    public boolean isFireState() {
        return isFireState;
    }
    
    public Texture getViewNode() {
        return view;
    }
    
    public int getImageIndex() {
        return imageIndex;
    }
    
    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
        updateView();
    }
}