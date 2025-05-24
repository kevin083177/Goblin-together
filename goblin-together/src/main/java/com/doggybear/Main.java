package com.doggybear;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.PhysicsWorld;
import com.doggybear.component.Goblin;
import com.doggybear.component.Goblin2;
import com.doggybear.component.Timer;
import com.doggybear.type.EntityType;
import com.doggybear.factory.Game;
import com.doggybear.levels.Level;
import com.doggybear.levels.LevelManager;
import com.doggybear.menu.MainMenu;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
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
    private Entity goblin2; // 新增第二個哥布林
    private Timer timer;

    private Entity lava;
    private double lavaHeight = 100; // 初始岩漿高度
    private double lavaRiseSpeed = 5; // 岩漿上升速度
    private double timePassed = 0;
    private double lavaY = 1000; // 岩漿底部的固定Y坐標

    private boolean isGameOver = false;
    private int WORLD_HEIGHT = 10000; // 遊戲世界的總高度

    private Level level;

    @Override
    protected void initGame() {
        isGameOver = false;
        timePassed = 0;
        
        getGameWorld().addEntityFactory(new Game());
        
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);
        
        level = LevelManager.createLevel();
        
        // 生成第一個哥布林
        goblin = spawn("goblin", level.getGoblinStartX(), level.getGoblinStartY());
        
        // 生成第二個哥布林
        goblin2 = spawn("goblin2", level.getGoblin2StartX(), level.getGoblin2StartY());
        
        timer = new Timer();
        goblin.addComponent(timer);
        
        lavaHeight = level.getInitialLavaHeight();
        lavaRiseSpeed = level.getLavaRiseSpeed();
        
        lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
              .put("width", (int)getAppWidth())
              .put("height", (int)lavaHeight));
        
        getPhysicsWorld().setGravity(0, 1000);
        
        // 修改視角，確保兩個玩家都在畫面中
        getGameScene().getViewport().setBounds(0, -WORLD_HEIGHT, getAppWidth(), WORLD_HEIGHT + getAppHeight());
        
        // 動態調整視角以確保兩個玩家都在畫面中
        updateViewport();
    }

    // 動態更新視角，使兩個玩家都在畫面中
    private void updateViewport() {
        if (goblin == null || goblin2 == null) return;
        
        // 計算兩個哥布林的中心點作為視角中心
        double centerX = (goblin.getX() + goblin2.getX()) / 2 + 25; // 加上一半的寬度(50/2)
        double centerY = (goblin.getY() + goblin2.getY()) / 2 + 25; // 加上一半的高度(50/2)
        
        // 使用固定視角位置，而不是綁定到實體
        getGameScene().getViewport().setX(centerX - getAppWidth() / 2);
        getGameScene().getViewport().setY(centerY - getAppHeight() / 2);
    }

    @Override
    protected void initInput() {
        // 第一個玩家控制 - WASD 和空格跳躍
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

        // 第二個玩家控制 - 方向鍵和Enter跳躍
        getInput().addAction(new UserAction("玩家2向右移動") {
            @Override
            protected void onAction() {
                goblin2.getComponent(Goblin2.class).moveRight();
            }

            @Override
            protected void onActionEnd() {
                goblin2.getComponent(Goblin2.class).stop();
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("玩家2向左移動") {
            @Override
            protected void onAction() {
                goblin2.getComponent(Goblin2.class).moveLeft();
            }

            @Override
            protected void onActionEnd() {
                goblin2.getComponent(Goblin2.class).stop();
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("玩家2跳躍") {
            @Override
            protected void onActionBegin() {
                goblin2.getComponent(Goblin2.class).jump();
            }
        }, KeyCode.ENTER);
    }
    
    @Override
    protected void initPhysics() {
        PhysicsWorld physicsWorld = getPhysicsWorld();
        
        // 第一個玩家的碰撞處理
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity platform) {
                goblin.getComponent(Goblin.class).onGroundCollision();
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.LAVA) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity lava) {
                showGameOver();
            }
        });
        
        // 第二個玩家的碰撞處理
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity platform) {
                goblin2.getComponent(Goblin2.class).onGroundCollision();
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.LAVA) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity lava) {
                showGameOver();
            }
        });
    }
    
    @Override
    protected void onUpdate(double tpf) {
        // 如果遊戲已經結束 不再更新岩漿 Goblin 禁止移動
        if (isGameOver) {
            if (goblin != null && goblin.isActive()) {
                goblin.getComponent(Goblin.class).stop();
                goblin.getComponent(PhysicsComponent.class).setVelocityY(0);
            }
            if (goblin2 != null && goblin2.isActive()) {
                goblin2.getComponent(Goblin2.class).stop();
                goblin2.getComponent(PhysicsComponent.class).setVelocityY(0);
            }
            return;
        }
        
        // 更新經過的時間
        timePassed += tpf;
        
        // 每0.5秒更新岩漿高度，讓它變高
        if (timePassed > 0.5) {
            lavaHeight += lavaRiseSpeed;
            
            // 重新設置岩漿的Y坐標和高度
            lava.removeFromWorld();
            lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
                .put("width", (int)getAppWidth())
                .put("height", (int)lavaHeight));
            
            timePassed = 0;
        }
        
        // 檢查兩位玩家是否都掉入岩漿
        if ((goblin.getY() + goblin.getHeight() > lavaY - lavaHeight) || 
            (goblin2.getY() + goblin2.getHeight() > lavaY - lavaHeight)) {
            showGameOver();
        }
        
        // 更新視角位置，使兩個玩家都保持在畫面中
        updateViewport();
    }

    private void showGameOver() {
        if (isGameOver) return;
        
        isGameOver = true;
        
        timer.stop();
        
        // 記錄最終存活時間
        int finalSurvivalTime = timer.getElapsedSeconds();
        
        Rectangle overlay = new Rectangle(getAppWidth(), getAppHeight(), Color.color(0, 0, 0, 0.7));
        
        Rectangle modalBg = new Rectangle(400, 350, Color.DARKGRAY);
        modalBg.setArcWidth(20);
        modalBg.setArcHeight(20);
        modalBg.setStroke(Color.WHITE);
        modalBg.setStrokeWidth(2);
        
        Text gameOverText = new Text("遊戲結束");
        gameOverText.setFont(Font.font(40));
        gameOverText.setFill(Color.WHITE);
        
        Text survivalTimeText = new Text("您存活了 " + finalSurvivalTime + " 秒");
        survivalTimeText.setFont(Font.font(20));
        survivalTimeText.setFill(Color.WHITE);
        
        Button restartBtn = new Button("重來一次");
        restartBtn.setPrefWidth(150);
        restartBtn.setPrefHeight(40);
        restartBtn.setOnAction(e -> {
            getGameController().startNewGame();
        });
        
        Button menuBtn = new Button("回到主頁面");
        menuBtn.setPrefWidth(150);
        menuBtn.setPrefHeight(40);
        menuBtn.setOnAction(e -> {
            getGameController().gotoMainMenu();
        });
        
        VBox modalContent = new VBox(20);
        modalContent.setAlignment(Pos.CENTER);
        modalContent.getChildren().addAll(gameOverText, survivalTimeText, restartBtn, menuBtn);
        
        StackPane modalPane = new StackPane(modalBg, modalContent);
        modalPane.setTranslateX(getAppWidth() / 2 - 200);
        modalPane.setTranslateY(getAppHeight() / 2 - 175);
        
        getGameScene().addUINodes(overlay, modalPane);
    }

    @Override
    protected void initUI() {
        Text title = new Text("Goblin Together");
        title.setTranslateX(10);
        title.setTranslateY(30);
        
        Text helpText = new Text("玩家1: A/D 移動，空白鍵跳躍 | 玩家2: 方向鍵移動，Enter跳躍");
        helpText.setTranslateX(10);
        helpText.setTranslateY(60);
        
        Text levelText = new Text("關卡: " + (level != null ? level.getName() : ""));
        levelText.setTranslateX(10);
        levelText.setTranslateY(90);
        
        getGameScene().addUINodes(title, helpText, levelText);
    }

    public static void main(String[] args) {
        launch(args);
    }
}