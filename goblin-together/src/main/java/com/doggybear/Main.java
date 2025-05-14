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
        
        goblin = spawn("goblin", level.getGoblinStartX(), level.getGoblinStartY());
        
        timer = new Timer();
        goblin.addComponent(timer);
        
        lavaHeight = level.getInitialLavaHeight();
        lavaRiseSpeed = level.getLavaRiseSpeed();
        
        lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
              .put("width", (int)getAppWidth())
              .put("height", (int)lavaHeight));
        
        getPhysicsWorld().setGravity(0, 1000);
        
        getGameScene().getViewport().setBounds(0, -WORLD_HEIGHT, getAppWidth(), WORLD_HEIGHT + getAppHeight());
        getGameScene().getViewport().bindToEntity(goblin, getAppWidth() / 2, getAppHeight() / 2);
        
        // updateLevelUI();
    }
    // 顯示關卡名稱
    // private void updateLevelUI() {
    //     Text levelText = getGameScene().getUINodes().stream()
    //             .filter(node -> node instanceof Text && ((Text) node).getText().startsWith("关卡:"))
    //             .map(node -> (Text) node)
    //             .findFirst()
    //             .orElse(null);
        
    //     if (levelText != null && level != null) {
    //         levelText.setText("关卡: " + level.getName());
    //     }
    // }

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

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.LAVA) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity lava) {
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
        
        if (goblin.getY() + goblin.getHeight() > lavaY - lavaHeight) {
            showGameOver();
        }
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

    // @Override
    // 修改成進入遊戲後顯示 Modal 或是更好的方法
    // protected void initUI() {
    //     Text title = new Text("Goblin Together");
    //     title.setTranslateX(10);
    //     title.setTranslateY(30);
        
    //     Text helpText = new Text("使用 A/D 移動，空白鍵跳躍");
    //     helpText.setTranslateX(10);
    //     helpText.setTranslateY(60);
        
    //     Text levelText = new Text("關卡: " + (level != null ? level.getName() : ""));
    //     levelText.setTranslateX(10);
    //     levelText.setTranslateY(90);
        
    //     getGameScene().addUINodes(title, helpText, levelText);
    // }

    
    public static void main(String[] args) {
        launch(args);
    }
}