package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;

public class Bullet extends Component {
    private double speedX;
    private double speedY;
    private double lifeTime = 0;
    private static final double MAX_LIFE_TIME = 10.0;
    
    public Bullet(double speed, String direction) {
        switch (direction.toLowerCase()) {
            case "left":
                this.speedX = -speed;
                this.speedY = 0;
                break;
            case "right":
                this.speedX = speed;
                this.speedY = 0;
                break;
            default:
                this.speedX = speed;
                this.speedY = 0;
                break;
        }
    }
    
    @Override
    public void onUpdate(double tpf) {
        entity.setX(entity.getX() + speedX * tpf);
        entity.setY(entity.getY() + speedY * tpf);
        
        lifeTime += tpf;
        
        if (isOutOfBounds() || lifeTime > MAX_LIFE_TIME) {
            entity.removeFromWorld();
        }
    }
    
    private boolean isOutOfBounds() {
        double x = entity.getX();
        double width = entity.getWidth();
        
        return x + width < -50 || x > getAppWidth() + 50;
    }
}