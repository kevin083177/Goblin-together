package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Platform extends Component {
    
    private PhysicsComponent physics;
    private Rectangle viewNode;
    
    private Color color = Color.RED;
    private int width;
    private int height;
    private boolean isMoving = false;    // 是否會移動
    private double moveSpeed = 0;        // 移動速度
    private double initialX;             // 初始 X 位置
    private double moveDistance = 0;     // 移動距離
    private boolean moveRight = true;    // 移動方向

    public Platform(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
        viewNode = (Rectangle) entity.getViewComponent().getChildren().get(0);
        initialX = entity.getX();
        
        updateView();
    }
    
    @Override
    public void onUpdate(double tpf) {
        // 平台移動邏輯
        if (isMoving && moveSpeed > 0) {
            if (moveRight) {
                entity.translateX(moveSpeed * tpf);
                if (entity.getX() > initialX + moveDistance) {
                    moveRight = false;
                }
            } else {
                entity.translateX(-moveSpeed * tpf);
                if (entity.getX() < initialX) {
                    moveRight = true;
                }
            }
        }
    }
    
    // 更新平台視圖變化
    private void updateView() {
        viewNode.setWidth(width);
        viewNode.setHeight(height);
        viewNode.setFill(color);
    }
    
    /**
     * 設置平台顏色
     * @param color 顏色
     */
    public Platform setColor(Color color) {
        this.color = color;
        if (viewNode != null) {
            viewNode.setFill(color);
        }
        return this;
    }
    
    /**
     * 設置平台移動
     * @param speed 速度
     * @param distance 距離
     */
    public Platform setMoving(double speed, double distance) {
        this.isMoving = true;
        this.moveSpeed = speed;
        this.moveDistance = distance;
        return this;
    }
    
    /**
     * 停止平台移動
     */
    public Platform stopMoving() {
        this.isMoving = false;
        return this;
    }
    
    public int getWidth() {
        return width;
    }
    
    /**
     * 設置平台寬度
     * @param width 寬度
     */
    public Platform setWidth(int width) {
        this.width = width;
        if (viewNode != null) {
            viewNode.setWidth(width);
        }
        return this;
    }
    
    public int getHeight() {
        return height;
    }
    
    /**
     * 設置平台高度
     * @param height 高度
     */
    public Platform setHeight(int height) {
        this.height = height;
        if (viewNode != null) {
            viewNode.setHeight(height);
        }
        return this;
    }

    public Color getColor() {
        return color;
    }
    
    public boolean isMoving() {
        return isMoving;
    }
}