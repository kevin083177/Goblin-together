// 简化的 RopeFactory.java - 只支持硬约束绳子
package com.doggybear.factory;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.doggybear.component.Rope;
import com.doggybear.type.EntityType;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class RopeFactory implements EntityFactory {

    @Spawns("rope")
    public Entity newRope(SpawnData data) {
        double maxLength = data.get("maxLength");
        
        Rope rope = new Rope(maxLength);
        
        return entityBuilder(data)
                .type(EntityType.ROPE)
                .with(rope)
                .build();
    }
}