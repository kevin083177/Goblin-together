package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FirePlatform extends Component {
    private double fireDuration;  // 火焰持續時間
    private double normalDuration; // 正常持續時間
    private double stateTimer = 0; // 狀態計時
    private boolean isFireState = false;
    private Rectangle view;
    
    public FirePlatform(double fireDuration, double normalDuration) {
        this.fireDuration = fireDuration;
        this.normalDuration = normalDuration;
    }
    
    @Override
    public void onAdded() {
        double width = entity.getWidth();
        double height = entity.getHeight();
        view = new Rectangle(width, height);
        updateView();
    }
    
    @Override
    public void onUpdate(double tpf) {
        stateTimer += tpf;
        
        // 檢查是否需要切換狀態
        double currentDuration = isFireState ? fireDuration : normalDuration;
        if (stateTimer >= currentDuration) {
            isFireState = !isFireState;
            stateTimer = 0;
            updateView();
        }
    }
    
    private void updateView() {
        if (view == null) return;
        
        if (isFireState) {
            view.setFill(Color.RED);
            view.setStroke(Color.DARKRED);
        } else {
            view.setFill(Color.WHITE);
            view.setStroke(Color.GRAY);
        }
    }
    
    public boolean isFireState() {
        return isFireState;
    }
    
    public Rectangle getViewNode() {
        return view;
    }
}