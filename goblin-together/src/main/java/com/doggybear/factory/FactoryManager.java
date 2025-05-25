package com.doggybear.factory;

import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.GameWorld;

public class FactoryManager {
    
    public static void addAllFactories(GameWorld gameWorld) {
        gameWorld.addEntityFactory(new GoblinFactory());
        gameWorld.addEntityFactory(new PlatformFactory());
        gameWorld.addEntityFactory(new LavaFactory());
        gameWorld.addEntityFactory(new SpikeFactory());
        gameWorld.addEntityFactory(new ArrowLauncherFactory());
        gameWorld.addEntityFactory(new RopeFactory());
    }
    
    public static EntityFactory[] getAllFactories() {
        return new EntityFactory[] {
            new GoblinFactory(),
            new PlatformFactory(),
            new LavaFactory(),
            new SpikeFactory(),
            new ArrowLauncherFactory(),
            new RopeFactory(),
            
        };
    }
}