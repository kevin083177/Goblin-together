// 创建新文件：src/main/java/com/doggybear/factory/ArrowLauncherFactory.java
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
import com.doggybear.component.Arrow;
import com.doggybear.component.ArrowLauncher;
import com.doggybear.type.EntityType;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class ArrowLauncherFactory implements EntityFactory {

    @Spawns("arrow")
    public Entity newArrow(SpawnData data) {
        double speed = data.get("speed");
        String direction = data.get("direction");
        
        // 不使用物理组件，手动控制移动
        // 创建弓箭的视觉效果（三角形箭头）
        Polygon arrowShape = createArrowShape(direction);
        
        return entityBuilder(data)
                .type(EntityType.ARROW)
                .bbox(new HitBox(BoundingShape.box(20, 5))) // 弓箭的碰撞箱
                .view(arrowShape)
                .with(new Arrow(speed, direction))
                .with(new CollidableComponent(true))
                .build();
    }
    
    @Spawns("launcher")
    public Entity newLauncher(SpawnData data) {
        int width = data.get("width");
        int height = data.get("height");
        String direction = data.get("direction");
        
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC); // 发射器是静态的
        
        ArrowLauncher launcher = new ArrowLauncher(width, height, direction);
        
        // 设置发射频率（如果有指定）
        if (data.hasKey("fireRate")) {
            launcher.setFireRate((Double) data.get("fireRate"));
        }
        
        // 设置弓箭速度（如果有指定）
        if (data.hasKey("arrowSpeed")) {
            launcher.setArrowSpeed((Double) data.get("arrowSpeed"));
        }
        
        // 设置颜色（如果有指定）
        if (data.hasKey("color")) {
            launcher.setColor((Color) data.get("color"));
        }
        
        return entityBuilder(data)
                .type(EntityType.LAUNCHER)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .view(launcher.getViewNode())
                .with(physics)
                .with(launcher)
                .with(new CollidableComponent(true))
                .build();
    }
    
    // 创建弓箭的形状
    private Polygon createArrowShape(String direction) {
        Polygon arrow = new Polygon();
        
        // 根据方向创建不同朝向的箭头
        switch (direction.toLowerCase()) {
            case "right":
                arrow.getPoints().addAll(new Double[]{
                    0.0, 2.0,    // 箭尾左上
                    15.0, 2.0,   // 箭身右上
                    20.0, 0.0,   // 箭头顶点
                    15.0, -2.0,  // 箭身右下
                    0.0, -2.0    // 箭尾左下
                });
                break;
            case "left":
                arrow.getPoints().addAll(new Double[]{
                    20.0, 2.0,   // 箭尾右上
                    5.0, 2.0,    // 箭身左上
                    0.0, 0.0,    // 箭头顶点
                    5.0, -2.0,   // 箭身左下
                    20.0, -2.0   // 箭尾右下
                });
                break;
            case "up":
                arrow.getPoints().addAll(new Double[]{
                    -1.0, 20.0,  // 箭尾左下
                    -1.0, 5.0,   // 箭身左上
                    0.0, 0.0,    // 箭头顶点
                    1.0, 5.0,    // 箭身右上
                    1.0, 20.0    // 箭尾右下
                });
                break;
            case "down":
                arrow.getPoints().addAll(new Double[]{
                    -1.0, 0.0,   // 箭尾左上
                    -1.0, 15.0,  // 箭身左下
                    0.0, 20.0,   // 箭头顶点
                    1.0, 15.0,   // 箭身右下
                    1.0, 0.0     // 箭尾右上
                });
                break;
            default:
                // 默认向右
                arrow.getPoints().addAll(new Double[]{
                    0.0, 2.0, 15.0, 2.0, 20.0, 0.0, 15.0, -2.0, 0.0, -2.0
                });
        }
        
        arrow.setFill(Color.BROWN);
        arrow.setStroke(Color.DARKRED);
        arrow.setStrokeWidth(1);
        
        return arrow;
    }
}