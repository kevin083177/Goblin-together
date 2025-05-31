// LauncherFactory.java
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
import com.almasb.fxgl.texture.Texture;
import com.doggybear.component.Bullet;
import com.doggybear.component.Launcher;
import com.doggybear.type.EntityType;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.texture;

public class LauncherFactory implements EntityFactory {

    @Spawns("bullet")
    public Entity newBullet(SpawnData data) {
        double speed = data.get("speed");
        String direction = data.get("direction");
        
        return entityBuilder(data)
                .type(EntityType.BULLET)
                .bbox(new HitBox(BoundingShape.circle(10)))
                .view(texture("bullet.png", 20, 20))
                .with(new Bullet(speed, direction))
                .with(new CollidableComponent(true))
                .build();
    }
    
    @Spawns("launcher")
    public Entity newLauncher(SpawnData data) {
        String direction = data.get("direction");
        
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        
        Launcher launcher = new Launcher(direction);
        
        if (data.hasKey("fireRate")) {
            launcher.setFireRate((Double) data.get("fireRate"));
        }
        
        if (data.hasKey("arrowSpeed")) {
            launcher.setArrowSpeed((Double) data.get("arrowSpeed"));
        }
        
        // 發射器圖片
        Texture launcherView = texture("launcher.png", 50, 50);

        // 水平翻轉
        if (direction.toLowerCase().equals("right")) {
            launcherView.setScaleX(-1);
        }

        return entityBuilder(data)
                .type(EntityType.LAUNCHER)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .view(launcherView)
                .with(physics)
                .with(launcher)
                .with(new CollidableComponent(true))
                .build();
    }
}