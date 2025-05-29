package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Launcher extends Component {
    private double fireRate = 1.0;
    private double timeSinceLastShot = 0;
    private String direction;
    private double arrowSpeed = 400;
    private Rectangle viewNode;
    private int width;
    private int height;
    private Color color = Color.DARKGOLDENROD;
    
    public Launcher(int width, int height, String direction) {
        this.width = width;
        this.height = height;
        this.direction = direction;
        createLauncherView();
    }
    
    private void createLauncherView() {
        viewNode = new Rectangle(width, height, color);
        viewNode.setStroke(Color.BLACK);
        viewNode.setStrokeWidth(2);
    }
    
    @Override
    public void onUpdate(double tpf) {
        timeSinceLastShot += tpf;
        
        if (timeSinceLastShot >= fireRate) {
            fireArrow();
            timeSinceLastShot = 0;
        }
    }
    
    private void fireArrow() {
        double launcherX = entity.getX();
        double launcherY = entity.getY();
        double launcherWidth = entity.getWidth();
        double launcherHeight = entity.getHeight();
        
        double arrowX, arrowY;
        
        switch (direction.toLowerCase()) {
            case "left":
                arrowX = launcherX - 20;
                arrowY = launcherY + launcherHeight / 2 - 10;
                break;
            case "right":
                arrowX = launcherX + launcherWidth;
                arrowY = launcherY + launcherHeight / 2 - 10;
                break;
            case "up":
                arrowX = launcherX + launcherWidth / 2 - 10;
                arrowY = launcherY - 20;
                break;
            case "down":
                arrowX = launcherX + launcherWidth / 2 - 10;
                arrowY = launcherY + launcherHeight;
                break;
            default:
                arrowX = launcherX + launcherWidth;
                arrowY = launcherY + launcherHeight / 2 - 10;
                break;
        }
        
        SpawnData arrowData = new SpawnData(arrowX, arrowY)
                .put("speed", arrowSpeed)
                .put("direction", direction);
        
        FXGL.spawn("arrow", arrowData);
        
    }
    
    public Rectangle getViewNode() {
        return viewNode;
    }
    
    public Launcher setFireRate(double fireRate) {
        this.fireRate = fireRate;
        return this;
    }
    
    public Launcher setArrowSpeed(double speed) {
        this.arrowSpeed = speed;
        return this;
    }
    
    public Launcher setColor(Color color) {
        this.color = color;
        if (viewNode != null) {
            viewNode.setFill(color);
        }
        return this;
    }
    
    public String getDirection() {
        return direction;
    }
}