package com.doggybear;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;
import com.doggybear.component.Goblin;
import com.doggybear.factory.EntityType;
import com.doggybear.factory.Game;
import com.doggybear.menu.MainMenu;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Main extends GameApplication {
    
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Goblin Together");
        settings.setHeight(720);
        settings.setWidth(1080);
        settings.setMainMenuEnabled(true);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new MainMenu();
            }
        });
    }

    private Entity goblin;

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new Game());
        
        // 設置背景顏色
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);
        
        // 創建地面和平台
        spawn("platform", new SpawnData(0, 600).put("width", 1080).put("height", 100));
        spawn("platform", new SpawnData(100, 500).put("width", 200).put("height", 20));
        spawn("platform", new SpawnData(400, 400).put("width", 200).put("height", 20));
        spawn("platform", new SpawnData(700, 300).put("width", 200).put("height", 20));
        
        goblin = spawn("goblin", 100, 300);
        
        // 設置重力
        getPhysicsWorld().setGravity(0, 700);
        
        // 相機跟隨哥布林
        getGameScene().getViewport().bindToEntity(goblin, getAppWidth() / 2, getAppHeight() / 2);
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("向右移動") {
            @Override
            protected void onAction() {
                goblin.getComponent(Goblin.class).moveRight();
            }

            @Override
            protected void onActionEnd() {
                goblin.getComponent(Goblin.class).stop();
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("向左移動") {
            @Override
            protected void onAction() {
                goblin.getComponent(Goblin.class).moveLeft();
            }

            @Override
            protected void onActionEnd() {
                goblin.getComponent(Goblin.class).stop();
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("跳躍") {
            @Override
            protected void onActionBegin() {
                goblin.getComponent(Goblin.class).jump();
            }
        }, KeyCode.SPACE);
    }
    
    @Override
    protected void initPhysics() {
        PhysicsWorld physicsWorld = getPhysicsWorld();
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity platform) {
                // System.out.println("落地");
                
                goblin.getComponent(Goblin.class).onGroundCollision();
            }
        });
    }

    @Override
    protected void initUI() {
        Text text = new Text("Goblin Together");
        text.setTranslateX(10);
        text.setTranslateY(30);

        getGameScene().addUINode(text);
        
        Text helpText = new Text("使用 A/D 移動，空格鍵跳躍");
        helpText.setTranslateX(10);
        helpText.setTranslateY(60);
        
        getGameScene().addUINode(helpText);
    }

    public static void main(String[] args) {
        launch(args);
    }
}