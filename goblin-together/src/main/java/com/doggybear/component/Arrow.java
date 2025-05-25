package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;

public class Arrow extends Component {
    private double speedX;
    private double speedY;
    private double lifeTime = 0;
    private static final double MAX_LIFE_TIME = 10.0;
    
    public Arrow(double speed, String direction) {
        // 根据方向设置速度分量
        switch (direction.toLowerCase()) {
            case "left":
                this.speedX = -speed;
                this.speedY = 0;
                break;
            case "right":
                this.speedX = speed;
                this.speedY = 0;
                break;
            case "up":
                this.speedX = 0;
                this.speedY = -speed;
                break;
            case "down":
                this.speedX = 0;
                this.speedY = speed;
                break;
            default:
                this.speedX = speed;
                this.speedY = 0;
                break;
        }
    }
    
    @Override
    public void onUpdate(double tpf) {
        // 手动移动弓箭 - 保证完全直线飞行
        entity.setX(entity.getX() + speedX * tpf);
        entity.setY(entity.getY() + speedY * tpf);
        
        lifeTime += tpf;
        
        // 检查边界
        if (isOutOfBounds() || lifeTime > MAX_LIFE_TIME) {
            entity.removeFromWorld();
        }
    }
    
    private boolean isOutOfBounds() {
        double x = entity.getX();
        double y = entity.getY();
        double width = entity.getWidth();
        double height = entity.getHeight();
        
        return x + width < -50 || x > getAppWidth() + 50 || 
               y + height < -50 || y > getAppHeight() + 50;
    }
}