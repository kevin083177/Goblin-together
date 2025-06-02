package com.doggybear;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.doggybear.component.Goblin;
import com.doggybear.controller.*;
import com.doggybear.network.NetworkManager;
import com.doggybear.ui.FontManager;
import com.doggybear.ui.GameOver;

import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.*;

import java.net.Socket;

public class Main extends GameApplication {
    private GameController gameController;
    private PhysicsController physicsController;
    private GameOver gameOver;
    
    private NetworkManager networkManager;
    private boolean isHost;

    @Override
    protected void initSettings(GameSettings settings) {
        Settings.initSettings(settings);
    }

    @Override
    protected void initGame() {
        isHost = GameData.isHost();
        Socket socket = GameData.getSocket();
        
        if (socket != null) {
            networkManager = new NetworkManager(socket, isHost);
            networkManager.start();
        }

        // 初始化字型管理器
        FontManager.initialize();
        
        // 初始化遊戲控制器
        gameController = new GameController();
        gameController.initGame();
        
        // 初始化物理控制器
        physicsController = new PhysicsController(this::showGameOver);
        
    }
    
    private boolean canAcceptInput() {
        return gameController != null && 
               gameController.isGameStarted() && 
               !gameController.isGameOver();
    }

    private boolean isOnline() {
        return networkManager != null;
    }

     @Override
    protected void initInput() {
        getInput().addAction(new UserAction("向右移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                if (gameController.getGoblin() != null) {
                    gameController.getGoblin().getComponent(Goblin.class).moveRight();
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                if (gameController.getGoblin() != null) {
                    gameController.getGoblin().getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("向左移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                if (gameController.getGoblin() != null) {
                    gameController.getGoblin().getComponent(Goblin.class).moveLeft();
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                if (gameController.getGoblin() != null) {
                    gameController.getGoblin().getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("跳躍") {
            @Override
            protected void onActionBegin() {
                if (!canAcceptInput()) return;
                
                if (gameController.getGoblin() != null) {
                    gameController.getGoblin().getComponent(Goblin.class).jump();
                }
            }
        }, KeyCode.SPACE);

        getInput().addAction(new UserAction("玩家2向右移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                if (gameController.getGoblin2() != null) {
                    gameController.getGoblin2().getComponent(Goblin.class).moveRight();
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                if (gameController.getGoblin2() != null) {
                    gameController.getGoblin2().getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("玩家2向左移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                if (gameController.getGoblin2() != null) {
                    gameController.getGoblin2().getComponent(Goblin.class).moveLeft();
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                if (gameController.getGoblin2() != null) {
                    gameController.getGoblin2().getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("玩家2跳躍") {
            @Override
            protected void onActionBegin() {
                if (!canAcceptInput()) return;
                
                if (gameController.getGoblin2() != null) {
                    gameController.getGoblin2().getComponent(Goblin.class).jump();
                }
            }
        }, KeyCode.ENTER);
    }

    @Override
    protected void initPhysics() {
        physicsController.initPhysics();
    }
    
    @Override
    protected void onUpdate(double tpf) {
        gameController.update(tpf);
        
        if (networkManager != null) {
            processNetworkMessages();
        }

        // 只有在遊戲真正開始且未結束時才檢查遊戲結束條件
        if (gameController.isGameStarted() && !gameController.isGameOver()) {
            if (gameController.checkGameOver()) {
                showGameOver();
            }
        }
        
        gameController.updateViewport();

        if (isOnline() && gameController.isGameStarted() && !gameController.isGameOver()) {
            syncPlayerPositions();
        }
    }

    private void processNetworkMessages() {
        String message;
        while ((message = networkManager.pollMessage()) != null) {
            System.out.println("收到网络消息: " + message);
            
            if (message.startsWith("POS:")) {
                // 处理位置更新
                handlePositionUpdate(message);
            } else if ("START_GAME".equals(message)) {
                // 客户端收到游戏开始命令
                if (!isHost) {
                    FXGL.getGameController().startNewGame();
                }
            } else if ("CLIENT_READY".equals(message)) {
                // 主机收到客户端就绪通知
                if (isHost) {
                    // 可以在这里做额外处理
                }
            }
        }
    }

    private void handlePositionUpdate(String message) {
        // 格式: POS:player:x:y:velocityX:velocityY
        String[] parts = message.split(":");
        if (parts.length < 6) return;
        
        try {
            int player = Integer.parseInt(parts[1]);
            double x = Double.parseDouble(parts[2]);
            double y = Double.parseDouble(parts[3]);
            double velocityX = Double.parseDouble(parts[4]);
            double velocityY = Double.parseDouble(parts[5]);
            
            Entity target = (player == 1) ? gameController.getGoblin() : gameController.getGoblin2();
            if (target != null) {
                target.setPosition(x, y);
                PhysicsComponent physics = target.getComponent(PhysicsComponent.class);
                if (physics != null) {
                    physics.setVelocityX(velocityX);
                    physics.setVelocityY(velocityY);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("位置更新格式错误: " + message);
        }
    }
    
    private void syncPlayerPositions() {
        Entity goblin = gameController.getGoblin();
        Entity goblin2 = gameController.getGoblin2();
        
        if (goblin == null || goblin2 == null) return;
        
        if (isHost) {
            sendPlayerPosition(1, goblin);
        } else {
            sendPlayerPosition(2, goblin2);
        }
    }
    
    private void sendPlayerPosition(int playerId, Entity entity) {
        if (entity == null || networkManager == null) return;
        
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) return;
        
        String message = String.format("POS:%d:%.2f:%.2f:%.2f:%.2f",
            playerId,
            entity.getX(),
            entity.getY(),
            physics.getVelocityX(),
            physics.getVelocityY());
        
        networkManager.sendMessage(message);
    }
    
    public void onDestroy() {
        if (networkManager != null) {
            networkManager.stop();
        }
    }

    private void showGameOver() {
        if (gameController.isGameOver()) return;
        
        gameController.setGameOver(true);
        
        // 停止計時器
        if (gameController.getTimer() != null) {
            gameController.getTimer().stop();
        }
        
        // 記錄最終存活時間
        int finalSurvivalTime = gameController.getTimer() != null ? 
            gameController.getTimer().getElapsedSeconds() : 0;
        
        // 創建遊戲結束畫面
        gameOver = new GameOver(finalSurvivalTime, new GameOver.GameOverCallback() {
            @Override
            public void onRestart() {
                getGameController().startNewGame();
            }
            
            @Override
            public void onBackToMenu() {
                getGameController().gotoMainMenu();
            }
        });
        
        gameOver.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}