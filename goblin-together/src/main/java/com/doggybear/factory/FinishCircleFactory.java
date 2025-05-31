package com.doggybear.factory;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.doggybear.component.FinishCircle;
import com.doggybear.type.EntityType;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class FinishCircleFactory implements EntityFactory {

    @Spawns("finish")
    public Entity newFinishCircle(SpawnData data) {
        double radius = data.hasKey("radius") ? 
            ((Number) data.get("radius")).doubleValue() : 50.0;
        
        FixtureDef fd = new FixtureDef();
        fd.setFriction(0.0f);
        fd.setDensity(1.0f);
        fd.setRestitution(0.0f);
        
        FinishCircle FinishCircle = new FinishCircle(radius);
        
        if (data.hasKey("gameStartTime")) {
            FinishCircle.setGameStartTime(((Number) data.get("gameStartTime")).doubleValue());
        }
        
        if (data.hasKey("finishCallback")) {
            FinishCircle.setFinishCallback((FinishCircle.FinishCallback) data.get("finishCallback"));
        }
        
        javafx.scene.Node view = FinishCircle.getViewNode();
        if (view == null) {
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(radius);
            circle.setFill(javafx.scene.paint.Color.GOLD);
            circle.setStroke(javafx.scene.paint.Color.DARKGOLDENROD);
            circle.setStrokeWidth(3);
            view = circle;
        }
        
        return entityBuilder(data)
                .type(EntityType.FINISH)
                .bbox(new HitBox(BoundingShape.circle(radius)))
                .view(view)
                .with(FinishCircle)
                .with(new CollidableComponent(true))
                .build();
    }
}