package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.dsl.FXGL;

public class Launcher extends Component {
    private double fireRate = 1.0;
    private double timeSinceLastShot = 0;
    private String direction;
    private double arrowSpeed = 400;
    private static final int SIZE = 50;
    
    public Launcher(String direction) {
        this.direction = direction;
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
        
        double arrowX, arrowY;
        
        switch (direction.toLowerCase()) {
            case "left":
                arrowX = launcherX - 20;
                arrowY = launcherY + SIZE / 2 - 10;
                break;
            case "right":
                arrowX = launcherX + SIZE + 5;
                arrowY = launcherY + SIZE / 2 - 10;
                break;
            default:
                arrowX = launcherX + SIZE;
                arrowY = launcherY + SIZE / 2 - 10;
                break;
        }
        
        SpawnData arrowData = new SpawnData(arrowX, arrowY)
                .put("speed", arrowSpeed)
                .put("direction", direction);
        
        FXGL.spawn("bullet", arrowData);
    }
    
    public Launcher setFireRate(double fireRate) {
        this.fireRate = fireRate;
        return this;
    }
    
    public Launcher setArrowSpeed(double speed) {
        this.arrowSpeed = speed;
        return this;
    }
    
    public String getDirection() {
        return direction;
    }
}