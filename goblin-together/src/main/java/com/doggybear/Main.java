package com.doggybear;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.doggybear.component.FinishCircle;
import com.doggybear.component.Goblin;
import com.doggybear.controller.*;
import com.doggybear.menu.WaitingRoom;
import com.doggybear.network.NetworkManager;
import com.doggybear.ui.FontManager;
import com.doggybear.ui.GameOver;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.*;

import java.net.Socket;

public class Main extends GameApplication {
    private GameController gameController;
    private PhysicsController physicsController;
    private GameOver gameOver;
    
    private NetworkManager networkManager;
    private boolean isHost;
    private boolean isOnlineMode = false;

    private static final double SYNC_INTERVAL = 0.1; // 減少到100ms，降低殘影
    private double syncAccumulator = 0;

    @Override
    protected void initSettings(GameSettings settings) {
        Settings.initSettings(settings);
    }

    @Override
    protected void initGame() {
        try {
            System.out.println("=== 開始遊戲初始化 ===");
            
            Socket socket = GameData.getSocket();
            isHost = GameData.isHost();
            isOnlineMode = (socket != null);
            
            System.out.println("Main.initGame() - isHost: " + isHost + ", isOnlineMode: " + isOnlineMode);
            
            if (isOnlineMode) {
                if (socket.isClosed()) {
                    System.err.println("Socket 已關閉，無法初始化網絡管理器");
                    throw new RuntimeException("Socket 已關閉");
                }
                
                networkManager = new NetworkManager(socket, isHost);
                networkManager.start();
                
                GameData.markSocketAsManaged();
                
                System.out.println("網絡管理器已啟動");
            }

            FontManager.initialize();
            
            gameController = new GameController();
            gameController.initGame();
            
            physicsController = new PhysicsController(this::showGameOver);
            
            System.out.println("=== 遊戲初始化完成 ===");
            
        } catch (Exception e) {
            System.err.println("=== 遊戲初始化失敗 ===");
            System.err.println("錯誤: " + e.getMessage());
            e.printStackTrace();
            
            if (networkManager != null) {
                networkManager.stop();
                networkManager = null;
            }
            
            Platform.runLater(() -> {
                System.out.println("初始化失敗，回到主選單");
                GameData.reset();
                FXGL.getGameController().gotoMainMenu();
            });
        }
    }
    
    private boolean canAcceptInput() {
        return gameController != null && 
               gameController.isGameStarted() && 
               !gameController.isGameOver();
    }

    @Override
    protected void initInput() {
        if (isOnlineMode) {
            if (isHost) {
                setupPlayer1Controls();
            } else {
                setupPlayer2Controls();
            }
        } else {
            setupPlayer1Controls();
            setupPlayer2Controls();
        }
    }
    
    private void setupPlayer1Controls() {
        getInput().addAction(new UserAction("玩家1向右移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                // 客戶端不控制goblin1，只有主機或單機模式才執行
                if (isOnlineMode && !isHost) return;
                
                Entity targetGoblin = gameController.getGoblin();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).moveRight();
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                if (isOnlineMode && !isHost) return;
                
                Entity targetGoblin = gameController.getGoblin();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("玩家1向左移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                if (isOnlineMode && !isHost) return;
                
                Entity targetGoblin = gameController.getGoblin();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).moveLeft();
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                if (isOnlineMode && !isHost) return;
                
                Entity targetGoblin = gameController.getGoblin();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("玩家1跳躍") {
            @Override
            protected void onActionBegin() {
                if (!canAcceptInput()) return;
                
                if (isOnlineMode && !isHost) return;
                
                Entity targetGoblin = gameController.getGoblin();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).jump();
                }
            }
        }, KeyCode.SPACE);
    }
    
    private void setupPlayer2Controls() {
        getInput().addAction(new UserAction("玩家2向右移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    if (isOnlineMode) {
                        // 線上模式：客戶端執行本地動作，主機只發送輸入指令
                        if (!isHost) {
                            targetGoblin.getComponent(Goblin.class).moveRight();
                        }
                        sendInputCommand(2, "MOVE_RIGHT", true);
                    } else {
                        // 單機模式：直接執行
                        targetGoblin.getComponent(Goblin.class).moveRight();
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    if (isOnlineMode) {
                        if (!isHost) {
                            targetGoblin.getComponent(Goblin.class).stop();
                        }
                        sendInputCommand(2, "MOVE_RIGHT", false);
                    } else {
                        targetGoblin.getComponent(Goblin.class).stop();
                    }
                }
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("玩家2向左移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    if (isOnlineMode) {
                        if (!isHost) {
                            targetGoblin.getComponent(Goblin.class).moveLeft();
                        }
                        sendInputCommand(2, "MOVE_LEFT", true);
                    } else {
                        targetGoblin.getComponent(Goblin.class).moveLeft();
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    if (isOnlineMode) {
                        if (!isHost) {
                            targetGoblin.getComponent(Goblin.class).stop();
                        }
                        sendInputCommand(2, "MOVE_LEFT", false);
                    } else {
                        targetGoblin.getComponent(Goblin.class).stop();
                    }
                }
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("玩家2跳躍") {
            @Override
            protected void onActionBegin() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    if (isOnlineMode) {
                        if (!isHost) {
                            targetGoblin.getComponent(Goblin.class).jump();
                        }
                        sendInputCommand(2, "JUMP", true);
                    } else {
                        targetGoblin.getComponent(Goblin.class).jump();
                    }
                }
            }
        }, KeyCode.ENTER);
    }
    
    private void sendInputCommand(int playerId, String actionType, boolean isPressed) {
        if (networkManager != null) {
            String message = String.format("INPUT:%d:%s:%s", playerId, actionType, isPressed);
            networkManager.sendMessage(message);
        }
    }

    @Override
    protected void initPhysics() {
        physicsController.initPhysics();
        
        // 客戶端延遲禁用goblin1的物理碰撞
        if (isOnlineMode && !isHost) {
            FXGL.getGameTimer().runOnceAfter(() -> {
                Entity goblin1 = gameController.getGoblin();
                if (goblin1 != null) {
                    PhysicsComponent physics = goblin1.getComponent(PhysicsComponent.class);
                    if (physics != null) {
                        // 設置為kinematic並禁用碰撞檢測
                        physics.setBodyType(com.almasb.fxgl.physics.box2d.dynamics.BodyType.KINEMATIC);
                        // 禁用碰撞檢測，避免殘影導致錯誤死亡判定
                        if (goblin1.hasComponent(com.almasb.fxgl.entity.components.CollidableComponent.class)) {
                            goblin1.getComponent(com.almasb.fxgl.entity.components.CollidableComponent.class).setValue(false);
                        }
                    }
                }
            }, javafx.util.Duration.seconds(0.5));
        }
    }
    
    @Override
    protected void onUpdate(double tpf) {
        gameController.update(tpf);
        
        if (networkManager != null) {
            processNetworkMessages();
        }

        if (gameController.isGameStarted() && !gameController.isGameOver()) {
            // 只有主機端或單機模式才檢查遊戲結束條件
            if (!isOnlineMode || isHost) {
                if (gameController.checkGameOver()) {
                    showGameOver();
                }
            }
        }

        if (isHost && isOnlineMode && networkManager != null) {
            syncAccumulator += tpf;
            if (syncAccumulator >= SYNC_INTERVAL) {
                syncGameState();
                syncAccumulator = 0;
            }
        }
        
        // 客戶端也需要同步自己控制的goblin2位置給主機端
        if (!isHost && isOnlineMode && networkManager != null) {
            syncAccumulator += tpf;
            if (syncAccumulator >= SYNC_INTERVAL) {
                syncClientPosition();
                syncAccumulator = 0;
            }
        }
        
        gameController.updateViewport();
    }

    private void syncGameState() {
        if (gameController == null || !gameController.isGameStarted()) return;
        
        Entity goblin = gameController.getGoblin();
        Entity goblin2 = gameController.getGoblin2();
        if (goblin == null || goblin2 == null) return;
        
        String state = String.format("STATE:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%d:%d",
            goblin.getX(), goblin.getY(), 
            goblin.getComponent(PhysicsComponent.class).getVelocityX(),
            goblin.getComponent(PhysicsComponent.class).getVelocityY(),
            goblin2.getX(), goblin2.getY(),
            goblin2.getComponent(PhysicsComponent.class).getVelocityX(),
            goblin2.getComponent(PhysicsComponent.class).getVelocityY(),
            gameController.getLavaHeight(),
            gameController.isGameOver() ? 1 : 0,
            gameController.getTimer().getElapsedSeconds()
        );
        
        networkManager.sendMessage(state);
    }

    private void syncClientPosition() {
        if (gameController == null || !gameController.isGameStarted()) return;
        
        Entity goblin2 = gameController.getGoblin2();
        if (goblin2 == null) return;
        
        String clientPos = String.format("CLIENT_POS:%.2f:%.2f:%.2f:%.2f",
            goblin2.getX(), goblin2.getY(),
            goblin2.getComponent(PhysicsComponent.class).getVelocityX(),
            goblin2.getComponent(PhysicsComponent.class).getVelocityY()
        );
        
        networkManager.sendMessage(clientPos);
    }

    private void processNetworkMessages() {
        String message;
        while ((message = networkManager.pollMessage()) != null) {
            if (message.startsWith("INPUT:")) {
                handleInputCommand(message);
            } else if (message.startsWith("STATE:")) {
                handleStateUpdate(message);
            } else if (message.startsWith("CLIENT_POS:")) {
                handleClientPosition(message);
            } else if ("START_GAME".equals(message)) {
                System.out.println("遊戲中收到START_GAME消息");
            } else if ("CLIENT_READY".equals(message)) {
                if (isHost) {
                    System.out.println("主機收到客戶端就緒通知");
                }
            } else if ("INTRO_COMPLETE".equals(message)) {
                if (gameController != null) {
                    gameController.startActualGame();
                }
            } else if ("GAME_OVER".equals(message)) {
                showGameOver();
            } else if (message.startsWith("GAME_FINISH:")) {
                String[] parts = message.split(":");
                double time = Double.parseDouble(parts[1]);
            }
        }
    }
    
    private void handleClientPosition(String message) {
        if (!isHost) return; // 只有主機處理客戶端位置
        
        String[] parts = message.split(":");
        if (parts.length < 5) return;
        
        try {
            Entity goblin2 = gameController.getGoblin2();
            if (goblin2 != null) {
                double newX = Double.parseDouble(parts[1]);
                double newY = Double.parseDouble(parts[2]);
                
                goblin2.setPosition(newX, newY);
                
                PhysicsComponent physics = goblin2.getComponent(PhysicsComponent.class);
                if (physics != null) {
                    physics.setVelocityX(Double.parseDouble(parts[3]));
                    physics.setVelocityY(Double.parseDouble(parts[4]));
                }
            }
        } catch (Exception e) {
            System.err.println("客戶端位置解析錯誤: " + message);
        }
    }
    
    private void handleInputCommand(String message) {
        String[] parts = message.split(":");
        if (parts.length < 4) return;
        
        try {
            int player = Integer.parseInt(parts[1]);
            String actionType = parts[2];
            boolean isPressed = Boolean.parseBoolean(parts[3]);
            
            if (isHost) {
                Entity target = (player == 1) ? gameController.getGoblin() : gameController.getGoblin2();
                if (target == null) return;
                
                Goblin goblinComponent = target.getComponent(Goblin.class);
                if (goblinComponent == null) return;
                
                switch (actionType) {
                    case "MOVE_RIGHT":
                        if (isPressed) goblinComponent.moveRight();
                        else goblinComponent.stop();
                        break;
                    case "MOVE_LEFT":
                        if (isPressed) goblinComponent.moveLeft();
                        else goblinComponent.stop();
                        break;
                    case "JUMP":
                        if (isPressed) goblinComponent.jump();
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("輸入指令解析錯誤: " + message);
        }
    }

    private void handleStateUpdate(String message) {
        if (isHost) return; // 主機不處理狀態更新
        
        String[] parts = message.split(":");
        if (parts.length < 12) return;
        
        try {
            Entity goblin1 = gameController.getGoblin();
            Entity goblin2 = gameController.getGoblin2();
            
            double lavaHeight = Double.parseDouble(parts[9]);
            gameController.setLavaHeight(lavaHeight);

            int isGameOver = Integer.parseInt(parts[10]);
            if (isGameOver == 1 && !gameController.isGameOver()) {
                showGameOver();
            }
            
            int elapsedSeconds = Integer.parseInt(parts[11]);
            if (gameController.getTimer() != null) {
                gameController.getTimer().setElapsedSeconds(elapsedSeconds);
            }

            // 客戶端只同步主機控制的goblin1，完全不同步自己控制的goblin2
            if (goblin1 != null) {
                double newX = Double.parseDouble(parts[1]);
                double newY = Double.parseDouble(parts[2]);
                
                // 直接設置位置，不通過物理引擎
                goblin1.setPosition(newX, newY);
                
                PhysicsComponent physics = goblin1.getComponent(PhysicsComponent.class);
                if (physics != null) {
                    // 同步速度，確保物理狀態一致
                    physics.setVelocityX(Double.parseDouble(parts[3]));
                    physics.setVelocityY(Double.parseDouble(parts[4]));
                }
            }
            
            // goblin2 完全不同步，只由客戶端本地控制
            
        } catch (Exception e) {
            System.err.println("状态解析错误: " + message);
        }
    }
    
    public void onDestroy() {
        if (networkManager != null) {
            networkManager.stop();
        }
    }

    private void showGameOver() {
        if (gameController == null || gameController.isGameOver()) return;
        
        gameController.setGameOver(true);
        
        if (gameController.getTimer() != null) {
            gameController.getTimer().stop();
        }
        
        int finalSurvivalTime = gameController.getTimer() != null ? 
            gameController.getTimer().getElapsedSeconds() : 0;
        
        if (isOnlineMode && networkManager != null) {
            networkManager.sendMessage("GAME_OVER");
        }

        gameOver = new GameOver(finalSurvivalTime, isOnlineMode, new GameOver.GameOverCallback() {
            @Override
            public void onRestart() {
                if (networkManager != null) {
                    networkManager.stop();
                    networkManager = null;
                }
                getGameController().startNewGame();
            }
            
            @Override
            public void onBackToMenu() {
                if (isOnlineMode) {
                    returnToWaitingRoom();
                } else {
                    returnToMainMenu();
                }
            }
        });
        
        gameOver.show();
    }
    
    private void returnToWaitingRoom() {
        System.out.println("返回等待房間");
        
        if (networkManager != null) {
            networkManager.stop(false);
            networkManager = null;
        }
        
        GameData.markSocketAsUnmanaged();
        
        String hostIP = GameData.getHostIP();
        boolean isHost = GameData.isHost();
        
        getSceneService().pushSubScene(new WaitingRoom(isHost, hostIP));
    }
    
    private void returnToMainMenu() {
        System.out.println("返回主選單");
        
        if (networkManager != null) {
            networkManager.stop();
            networkManager = null;
        }
        
        GameData.reset();
        getGameController().gotoMainMenu();
    }

    public void sendNetworkMessage(String message) {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}