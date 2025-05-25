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
import com.doggybear.factory.FactoryManager;
import com.doggybear.levels.Level;
import com.doggybear.levels.LevelManager;
import com.doggybear.menu.MainMenu;
import com.doggybear.network.NetworkGameManager;

import com.doggybear.event.NetworkGameStartEvent;

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
    private Entity goblin2;
    private Timer timer;
    private NetworkGameManager networkGameManager;

    private Entity lava;
    private double lavaHeight = 100;
    private double lavaRiseSpeed = 5;
    private double timePassed = 0;
    private double lavaY = 1000;

    private boolean isGameOver = false;
    private int WORLD_HEIGHT = 10000;

    private Level level;
    
    private double syncTimer = 0;
    private static final double SYNC_INTERVAL = 1.0 / 60.0;

    @Override
    protected void initGame() {
        isGameOver = false;
        timePassed = 0;
        syncTimer = 0;
        
        networkGameManager = NetworkGameManager.getInstance();
        
        getEventBus().addEventHandler(NetworkGameStartEvent.NETWORK_GAME_START, 
            e -> {
                // System.out.println("收到網路遊戲監聽");
            });
        
        FactoryManager.addAllFactories(getGameWorld());
        
        getGameScene().setBackgroundColor(Color.LIGHTBLUE);
        
        level = LevelManager.createLevel();
        
        // 生成第一個哥布林
        goblin = spawn("goblin", level.getGoblinStartX(), level.getGoblinStartY());
        
        // 生成第二個哥布林
        goblin2 = spawn("goblin2", level.getGoblin2StartX(), level.getGoblin2StartY());
        
        if (networkGameManager.isNetworkGame()) {
            networkGameManager.startNetworkGame(goblin, goblin2);
            System.out.println("初始化遊戲 主機: " + networkGameManager.getNetworkManager().isHost());
        } else {
            System.out.println("初始化遊戲");
        }
        
        timer = new Timer();
        goblin.addComponent(timer);
        
        lavaHeight = level.getInitialLavaHeight();
        lavaRiseSpeed = level.getLavaRiseSpeed();
        
        lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
              .put("width", (int)getAppWidth())
              .put("height", (int)lavaHeight));
        
        getPhysicsWorld().setGravity(0, 1500);
        
        getGameScene().getViewport().setBounds(0, -WORLD_HEIGHT, getAppWidth(), WORLD_HEIGHT + getAppHeight());
        
        updateViewport();
    }

    private void updateViewport() {
        if (goblin == null || goblin2 == null) return;
        
        double centerX = (goblin.getX() + goblin2.getX()) / 2 + 25;
        double centerY = (goblin.getY() + goblin2.getY()) / 2 + 25;
        
        double targetViewX = centerX - getAppWidth() / 2;
        double targetViewY = centerY - getAppHeight() / 2;
        
        targetViewX = 0;
        
        double minViewY = -WORLD_HEIGHT;
        double maxViewY = 0;
        
        targetViewY = Math.max(minViewY, Math.min(targetViewY, maxViewY));
        
        getGameScene().getViewport().setX(targetViewX);
        getGameScene().getViewport().setY(targetViewY);
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("向右移動") {
            @Override
            protected void onAction() {
                if (networkGameManager.isNetworkGame()) {
                    networkGameManager.handleLocalInput(KeyCode.D, true);
                } else {
                    goblin.getComponent(Goblin.class).moveRight();
                }
            }

            @Override
            protected void onActionEnd() {
                if (networkGameManager.isNetworkGame()) {
                    networkGameManager.handleLocalInput(KeyCode.D, false);
                } else {
                    goblin.getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("向左移動") {
            @Override
            protected void onAction() {
                if (networkGameManager.isNetworkGame()) {
                    networkGameManager.handleLocalInput(KeyCode.A, true);
                } else {
                    goblin.getComponent(Goblin.class).moveLeft();
                }
            }

            @Override
            protected void onActionEnd() {
                if (networkGameManager.isNetworkGame()) {
                    networkGameManager.handleLocalInput(KeyCode.A, false);
                } else {
                    goblin.getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("跳躍") {
            @Override
            protected void onActionBegin() {
                if (networkGameManager.isNetworkGame()) {
                    networkGameManager.handleLocalInput(KeyCode.SPACE, true);
                } else {
                    goblin.getComponent(Goblin.class).jump();
                }
            }
            
            @Override
            protected void onActionEnd() {
                if (networkGameManager.isNetworkGame()) {
                    networkGameManager.handleLocalInput(KeyCode.SPACE, false);
                }
            }
        }, KeyCode.SPACE);

        getInput().addAction(new UserAction("玩家2向右移動") {
            @Override
            protected void onAction() {
                if (!networkGameManager.isNetworkGame()) {
                    goblin2.getComponent(Goblin.class).moveRight();
                }
            }

            @Override
            protected void onActionEnd() {
                if (!networkGameManager.isNetworkGame()) {
                    goblin2.getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("玩家2向左移動") {
            @Override
            protected void onAction() {
                if (!networkGameManager.isNetworkGame()) {
                    goblin2.getComponent(Goblin.class).moveLeft();
                }
            }

            @Override
            protected void onActionEnd() {
                if (!networkGameManager.isNetworkGame()) {
                    goblin2.getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("玩家2跳躍") {
            @Override
            protected void onActionBegin() {
                if (!networkGameManager.isNetworkGame()) {
                    goblin2.getComponent(Goblin.class).jump();
                }
            }
        }, KeyCode.ENTER);
    }
    
    @Override
    protected void initPhysics() {
        PhysicsWorld physicsWorld = getPhysicsWorld();
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity platform) {
                goblin.getComponent(Goblin.class).onGroundCollision();
                
                PhysicsComponent physics = goblin.getComponent(PhysicsComponent.class);
                if (physics.getVelocityY() > -100) {
                    goblin.getComponent(Goblin.class).resetJump();
                }
            }
            
            @Override
            protected void onCollision(Entity goblin, Entity platform) {
                PhysicsComponent physics = goblin.getComponent(PhysicsComponent.class);
                if (Math.abs(physics.getVelocityY()) < 100) {
                    goblin.getComponent(Goblin.class).resetJump();
                }
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.LAVA) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity lava) {
                showGameOver();
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity platform) {
                goblin2.getComponent(Goblin.class).onGroundCollision();
                
                PhysicsComponent physics = goblin2.getComponent(PhysicsComponent.class);
                if (physics.getVelocityY() > -100) {
                    goblin2.getComponent(Goblin.class).resetJump();
                }
            }
            
            @Override
            protected void onCollision(Entity goblin2, Entity platform) {
                PhysicsComponent physics = goblin2.getComponent(PhysicsComponent.class);
                if (Math.abs(physics.getVelocityY()) < 100) {
                    goblin2.getComponent(Goblin.class).resetJump();
                }
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.LAVA) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity lava) {
                showGameOver();
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity spike) {
                showGameOver();
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity spike) {
                showGameOver();
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN, EntityType.ARROW) {
            @Override
            protected void onCollisionBegin(Entity goblin, Entity arrow) {
                arrow.removeFromWorld();
                showGameOver();
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.GOBLIN2, EntityType.ARROW) {
            @Override
            protected void onCollisionBegin(Entity goblin2, Entity arrow) {
                arrow.removeFromWorld();
                showGameOver();
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.ARROW, EntityType.PLATFORM) {
            @Override
            protected void onCollisionBegin(Entity arrow, Entity platform) {
                arrow.removeFromWorld();
            }
        });
        
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.ARROW, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity arrow, Entity spike) {
                arrow.removeFromWorld();
            }
        });
    }
    
    @Override
    protected void onUpdate(double tpf) {
        if (isGameOver) {
            if (goblin != null && goblin.isActive()) {
                goblin.getComponent(Goblin.class).stop();
                goblin.getComponent(PhysicsComponent.class).setVelocityY(0);
            }
            if (goblin2 != null && goblin2.isActive()) {
                goblin2.getComponent(Goblin.class).stop();
                goblin2.getComponent(PhysicsComponent.class).setVelocityY(0);
            }
            return;
        }
        
        if (networkGameManager.isNetworkGame()) {
            syncTimer += tpf;
            
            if (syncTimer >= SYNC_INTERVAL) {
                if (goblin != null) {
                    networkGameManager.syncPlayerPosition(goblin, 1);
                }
                if (goblin2 != null) {
                    networkGameManager.syncPlayerPosition(goblin2, 2);
                }
                syncTimer = 0;
            }
        }
        
        timePassed += tpf;
        
        if (timePassed > 0.5) {
            lavaHeight += lavaRiseSpeed;
            
            lava.removeFromWorld();
            lava = spawn("lava", new SpawnData(0, lavaY - lavaHeight)
                .put("width", (int)getAppWidth())
                .put("height", (int)lavaHeight));
            
            timePassed = 0;
        }
        
        if ((goblin.getY() + goblin.getHeight() > lavaY - lavaHeight) || 
            (goblin2.getY() + goblin2.getHeight() > lavaY - lavaHeight)) {
            showGameOver();
        }
        
        updateViewport();
    }

    private void showGameOver() {
        if (isGameOver) return;
        
        isGameOver = true;
        
        if (networkGameManager.isNetworkGame()) {
            networkGameManager.sendGameOver();
        }
        
        timer.stop();
        
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
        
        String gameTypeText = networkGameManager.isNetworkGame() ? "網路合作" : "本地合作";
        Text survivalTimeText = new Text(gameTypeText + "\n存活了 " + finalSurvivalTime + " 秒");
        survivalTimeText.setFont(Font.font(20));
        survivalTimeText.setFill(Color.WHITE);
        
        Button restartBtn = new Button("重來一次");
        restartBtn.setPrefWidth(150);
        restartBtn.setPrefHeight(40);
        restartBtn.setOnAction(e -> {
            networkGameManager.stopNetworkGame();
            getGameController().startNewGame();
        });
        
        Button menuBtn = new Button("回到主頁面");
        menuBtn.setPrefWidth(150);
        menuBtn.setPrefHeight(40);
        menuBtn.setOnAction(e -> {
            networkGameManager.stopNetworkGame();
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
        
        String controlText = networkGameManager.isNetworkGame() ? 
            "网络游戏 - 使用 WASD 移動，空白鍵跳躍" : 
            "玩家1: A/D 移動，空白鍵跳躍 | 玩家2: 方向鍵移動，Enter跳躍";
        
        Text helpText = new Text(controlText);
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