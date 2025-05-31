package com.doggybear.factory;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.doggybear.component.Spike;
import com.doggybear.type.EntityType;

import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class SpikeFactory implements EntityFactory {
    @Spawns("spike")
    public Entity newSpike(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        
        Spike spike = new Spike();
        
        double width = spike.getWidth();
        double height = spike.getHeight();
        
        Point2D[] polygonPoints = createSpikeCollisionPoints(width, height);

        return entityBuilder(data)
                .type(EntityType.SPIKE)
                .bbox(new HitBox(BoundingShape.polygon(polygonPoints)))
                .view(spike.getViewNode())
                .with(physics)
                .with(spike)
                .with(new CollidableComponent(true))
                .build();
    }
   
    /**
     * 尖刺碰撞箱
     */
    private Point2D[] createSpikeCollisionPoints(double width, double height) {
        return new Point2D[] {
            new Point2D(width * 0.2, height),
            new Point2D(width * 0.8, height),
            
            new Point2D(width * 0.95, height * 0.8),
            new Point2D(width, height * 0.5),
            new Point2D(width * 0.95, height * 0.2),
            
            new Point2D(width * 0.8, height * 0.05),
            new Point2D(width * 0.5, 0),
            new Point2D(width * 0.2, height * 0.05),
            
            new Point2D(width * 0.05, height * 0.2),
            new Point2D(0, height * 0.5),
            new Point2D(width * 0.05, height * 0.8)
        };
    }
}