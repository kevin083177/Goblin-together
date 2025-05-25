package com.doggybear.component;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.doggybear.type.EntityType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class Rope extends Component {
    private Entity player1;
    private Entity player2;
    private double maxLength;
    private Line ropeVisual;
    private Color ropeColor = Color.BROWN;
    private double ropeWidth = 3.0;
    
    private double elasticBuffer = 20.0;
    
    public Rope(double maxLength) {
        this.maxLength = maxLength;
        
        ropeVisual = new Line();
        ropeVisual.setStroke(ropeColor);
        ropeVisual.setStrokeWidth(ropeWidth);
    }
    
    @Override
    public void onAdded() {
        findPlayers();
        
        if (ropeVisual != null && entity != null) {
            entity.getViewComponent().addChild(ropeVisual);
        }
    }
    
    @Override
    public void onUpdate(double tpf) {
        if (player1 == null || player2 == null) {
            findPlayers();
            return;
        }
        
        double player1CenterX = player1.getX() + player1.getWidth() / 2;
        double player1CenterY = player1.getY() + player1.getHeight() / 2;
        double player2CenterX = player2.getX() + player2.getWidth() / 2;
        double player2CenterY = player2.getY() + player2.getHeight() / 2;
        
        double deltaX = player2CenterX - player1CenterX;
        double deltaY = player2CenterY - player1CenterY;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        updateRopeVisual(player1CenterX, player1CenterY, player2CenterX, player2CenterY, distance);
        
        if (distance > maxLength) {
            enforceRigidConstraint(deltaX, deltaY, distance);
        }
        else if (distance > maxLength - elasticBuffer) {
            applyElasticForce(deltaX, deltaY, distance, tpf);
        }
    }
    
    private void findPlayers() {
        var goblins = FXGL.getGameWorld().getEntitiesByType(EntityType.GOBLIN);
        var goblins2 = FXGL.getGameWorld().getEntitiesByType(EntityType.GOBLIN2);
        
        if (!goblins.isEmpty() && !goblins2.isEmpty()) {
            player1 = goblins.get(0);
            player2 = goblins2.get(0);
        }
    }
    
    private void updateRopeVisual(double x1, double y1, double x2, double y2, double distance) {
        if (ropeVisual == null) return;
        
        double midX = (x1 + x2) / 2;
        double midY = (y1 + y2) / 2;
        
        entity.setX(midX);
        entity.setY(midY);
        
        ropeVisual.setStartX(x1 - midX);
        ropeVisual.setStartY(y1 - midY);
        ropeVisual.setEndX(x2 - midX);
        ropeVisual.setEndY(y2 - midY);
        
        if (distance >= maxLength) {
            ropeVisual.setStroke(Color.RED);
            ropeVisual.setStrokeWidth(ropeWidth + 2);
        } else if (distance > maxLength - elasticBuffer) {
            ropeVisual.setStroke(Color.ORANGE);
            ropeVisual.setStrokeWidth(ropeWidth + 1);
        } else {
            ropeVisual.setStroke(ropeColor);
            ropeVisual.setStrokeWidth(ropeWidth);
        }
    }

    private void enforceRigidConstraint(double deltaX, double deltaY, double distance) {
        double dirX = deltaX / distance;
        double dirY = deltaY / distance;
        
        double midX = (player1.getX() + player2.getX()) / 2;
        double midY = (player1.getY() + player2.getY()) / 2;
        
        double halfLength = maxLength / 2;
        
        player1.setX(midX - dirX * halfLength - player1.getWidth() / 2);
        player1.setY(midY - dirY * halfLength - player1.getHeight() / 2);
        player2.setX(midX + dirX * halfLength - player2.getWidth() / 2);
        player2.setY(midY + dirY * halfLength - player2.getHeight() / 2);
        
        PhysicsComponent physics1 = player1.getComponent(PhysicsComponent.class);
        PhysicsComponent physics2 = player2.getComponent(PhysicsComponent.class);
        
        if (physics1 != null && physics2 != null) {
            double vel1X = physics1.getVelocityX();
            double vel1Y = physics1.getVelocityY();
            double vel2X = physics2.getVelocityX();
            double vel2Y = physics2.getVelocityY();
            
            double vel1AlongRope = vel1X * (-dirX) + vel1Y * (-dirY);
            double vel2AlongRope = vel2X * dirX + vel2Y * dirY;
            
            if (vel1AlongRope > 0) {
                physics1.setVelocityX(vel1X - vel1AlongRope * (-dirX));
                physics1.setVelocityY(vel1Y - vel1AlongRope * (-dirY));
            }
            if (vel2AlongRope > 0) {
                physics2.setVelocityX(vel2X - vel2AlongRope * dirX);
                physics2.setVelocityY(vel2Y - vel2AlongRope * dirY);
            }
        }
    }
    
    private void applyElasticForce(double deltaX, double deltaY, double distance, double tpf) {
        double dirX = deltaX / distance;
        double dirY = deltaY / distance;
        
        double elasticRatio = (distance - (maxLength - elasticBuffer)) / elasticBuffer;
        double elasticForce = elasticRatio * 2000;
        
        PhysicsComponent physics1 = player1.getComponent(PhysicsComponent.class);
        PhysicsComponent physics2 = player2.getComponent(PhysicsComponent.class);
        
        if (physics1 != null && physics2 != null) {
            double forceX = elasticForce * dirX;
            double forceY = elasticForce * dirY;
            
            physics1.setVelocityX(physics1.getVelocityX() + forceX * tpf);
            physics1.setVelocityY(physics1.getVelocityY() + forceY * tpf);
            physics2.setVelocityX(physics2.getVelocityX() - forceX * tpf);
            physics2.setVelocityY(physics2.getVelocityY() - forceY * tpf);
        }
    }
    
    @Override
    public void onRemoved() {
        if (ropeVisual != null && entity != null) {
            entity.getViewComponent().removeChild(ropeVisual);
        }
    }
    
    public double getMaxLength() {
        return maxLength;
    }
    
    public void setMaxLength(double maxLength) {
        this.maxLength = maxLength;
    }
    
    public double getCurrentDistance() {
        if (player1 == null || player2 == null) return 0;
        
        double player1X = player1.getX() + player1.getWidth() / 2;
        double player1Y = player1.getY() + player1.getHeight() / 2;
        double player2X = player2.getX() + player2.getWidth() / 2;
        double player2Y = player2.getY() + player2.getHeight() / 2;
        
        double deltaX = player2X - player1X;
        double deltaY = player2Y - player1Y;
        
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}