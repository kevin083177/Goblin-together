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

    private static final double SYNC_INTERVAL = 0.05; // 提高到50ms，減少延遲
    private static final double INPUT_SYNC_INTERVAL = 0.02; // 輸入指令20ms同步
    private double syncAccumulator = 0;
    private double inputSyncAccumulator = 0;

    // 輸入指令緩存
    private java.util.Queue<String> pendingInputs = new java.util.concurrent.ConcurrentLinkedQueue<>();

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
                
                if (isOnlineMode) {
                    // 客戶端立即執行本地預測
                    Entity targetGoblin = gameController.getGoblin2();
                    if (targetGoblin != null && !isHost) {
                        targetGoblin.getComponent(Goblin.class).moveRight();
                    }
                    // 發送輸入指令
                    sendInputCommand(2, "MOVE_RIGHT", true);
                } else {
                    Entity targetGoblin = gameController.getGoblin2();
                    if (targetGoblin != null) {
                        targetGoblin.getComponent(Goblin.class).moveRight();
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                if (isOnlineMode) {
                    Entity targetGoblin = gameController.getGoblin2();
                    if (targetGoblin != null && !isHost) {
                        targetGoblin.getComponent(Goblin.class).stop();
                    }
                    sendInputCommand(2, "MOVE_RIGHT", false);
                } else {
                    Entity targetGoblin = gameController.getGoblin2();
                    if (targetGoblin != null) {
                        targetGoblin.getComponent(Goblin.class).stop();
                    }
                }
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("玩家2向左移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                if (isOnlineMode) {
                    Entity targetGoblin = gameController.getGoblin2();
                    if (targetGoblin != null && !isHost) {
                        targetGoblin.getComponent(Goblin.class).moveLeft();
                    }
                    sendInputCommand(2, "MOVE_LEFT", true);
                } else {
                    Entity targetGoblin = gameController.getGoblin2();
                    if (targetGoblin != null) {
                        targetGoblin.getComponent(Goblin.class).moveLeft();
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                if (isOnlineMode) {
                    Entity targetGoblin = gameController.getGoblin2();
                    if (targetGoblin != null && !isHost) {
                        targetGoblin.getComponent(Goblin.class).stop();
                    }
                    sendInputCommand(2, "MOVE_LEFT", false);
                } else {
                    Entity targetGoblin = gameController.getGoblin2();
                    if (targetGoblin != null) {
                        targetGoblin.getComponent(Goblin.class).stop();
                    }
                }
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("玩家2跳躍") {
            @Override
            protected void onActionBegin() {
                if (!canAcceptInput()) return;
                
                if (isOnlineMode) {
                    // 客戶端立即執行跳躍預測，提供即時反饋
                    Entity targetGoblin = gameController.getGoblin2();
                    if (targetGoblin != null && !isHost) {
                        // 先執行本地跳躍，給玩家即時反饋
                        targetGoblin.getComponent(Goblin.class).jump();
                    }
                    // 緊急發送跳躍指令
                    sendUrgentInputCommand(2, "JUMP", true);
                } else {
                    Entity targetGoblin = gameController.getGoblin2();
                    if (targetGoblin != null) {
                        targetGoblin.getComponent(Goblin.class).jump();
                    }
                }
            }
        }, KeyCode.UP);
    }
    
    private void sendInputCommand(int playerId, String actionType, boolean isPressed) {
        if (networkManager != null) {
            String message = String.format("INPUT:%d:%s:%s", playerId, actionType, isPressed);
            pendingInputs.offer(message);
        }
    }
    
    /**
     * 緊急發送跳躍等關鍵輸入，立即發送不等待
     */
    private void sendUrgentInputCommand(int playerId, String actionType, boolean isPressed) {
        if (networkManager != null) {
            String message = String.format("URGENT:%d:%s:%s", playerId, actionType, isPressed);
            networkManager.sendMessage(message); // 立即發送
        }
    }

    @Override
    protected void initPhysics() {
        physicsController.initPhysics();
        
        // 客戶端立即禁用goblin1的所有物理計算
        if (isOnlineMode && !isHost) {
            // 立即執行，不延遲
            Entity goblin1 = gameController.getGoblin();
            if (goblin1 != null) {
                disableClientGoblin1Physics(goblin1);
            }
            
            // 延遲再次確保（防止初始化順序問題）
            FXGL.getGameTimer().runOnceAfter(() -> {
                Entity goblin1Delayed = gameController.getGoblin();
                if (goblin1Delayed != null) {
                    disableClientGoblin1Physics(goblin1Delayed);
                }
            }, javafx.util.Duration.seconds(0.1));
        }
    }
    
    /**
     * 完全禁用客戶端goblin1的物理計算
     */
    private void disableClientGoblin1Physics(Entity goblin1) {
        PhysicsComponent physics = goblin1.getComponent(PhysicsComponent.class);
        if (physics != null) {
            // 設為靜態物體，不受物理引擎影響
            physics.setBodyType(com.almasb.fxgl.physics.box2d.dynamics.BodyType.KINEMATIC);
            
            // 禁用碰撞檢測
            if (goblin1.hasComponent(com.almasb.fxgl.entity.components.CollidableComponent.class)) {
                goblin1.getComponent(com.almasb.fxgl.entity.components.CollidableComponent.class).setValue(false);
            }
            
            // 清零所有物理屬性
            physics.setVelocityX(0);
            physics.setVelocityY(0);
            physics.getBody().setLinearDamping(0);
            physics.getBody().setGravityScale(0);
        }
        
        // 禁用Goblin組件的自主更新
        Goblin goblinComponent = goblin1.getComponent(Goblin.class);
        if (goblinComponent != null) {
            // 標記為網絡控制模式
            goblinComponent.setNetworkControlled(true);
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

        // 只有主機端發送遊戲狀態，確保權威性
        if (isHost && isOnlineMode && networkManager != null) {
            syncAccumulator += tpf;
            if (syncAccumulator >= SYNC_INTERVAL) {
                syncGameState();
                syncAccumulator = 0;
            }
        }
        
        // 客戶端發送輸入指令（高頻率）
        if (!isHost && isOnlineMode && networkManager != null) {
            inputSyncAccumulator += tpf;
            if (inputSyncAccumulator >= INPUT_SYNC_INTERVAL) {
                sendPendingInputs();
                inputSyncAccumulator = 0;
            }
        }
        
        // 客戶端強制確保goblin1位置不被本地計算影響
        if (!isHost && isOnlineMode) {
            enforceClientGoblin1Position();
        }
        
        gameController.updateViewport();
    }
    
    /**
     * 發送待處理的輸入指令
     */
    private void sendPendingInputs() {
        while (!pendingInputs.isEmpty()) {
            String input = pendingInputs.poll();
            if (input != null) {
                networkManager.sendMessage(input);
            }
        }
    }
    
    /**
     * 客戶端強制確保goblin1位置不被任何本地計算影響
     */
    private void enforceClientGoblin1Position() {
        Entity goblin1 = gameController.getGoblin();
        if (goblin1 != null) {
            PhysicsComponent physics = goblin1.getComponent(PhysicsComponent.class);
            if (physics != null) {
                // 強制清零任何可能的本地速度計算
                if (Math.abs(physics.getVelocityX()) > 0.1 || Math.abs(physics.getVelocityY()) > 0.1) {
                    // 如果檢測到非網絡同步的速度變化，立即清零
                    physics.setVelocityX(0);
                    physics.setVelocityY(0);
                }
                
                // 確保重力影響為0
                physics.getBody().setGravityScale(0);
            }
        }
    }

    private void syncGameState() {
        if (gameController == null || !gameController.isGameStarted()) return;
        
        Entity goblin = gameController.getGoblin();
        Entity goblin2 = gameController.getGoblin2();
        if (goblin == null || goblin2 == null) return;
        
        // 獲取物理狀態信息
        Goblin goblin1Component = goblin.getComponent(Goblin.class);
        Goblin goblin2Component = goblin2.getComponent(Goblin.class);
        
        int goblin1OnGround = (goblin1Component != null && goblin1Component.isOnGround()) ? 1 : 0;
        int goblin2OnGround = (goblin2Component != null && goblin2Component.isOnGround()) ? 1 : 0;
        
        String state = String.format("STATE:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%d:%d:%d:%d",
            goblin.getX(), goblin.getY(), 
            goblin.getComponent(PhysicsComponent.class).getVelocityX(),
            goblin.getComponent(PhysicsComponent.class).getVelocityY(),
            goblin2.getX(), goblin2.getY(),
            goblin2.getComponent(PhysicsComponent.class).getVelocityX(),
            goblin2.getComponent(PhysicsComponent.class).getVelocityY(),
            gameController.getLavaHeight(),
            gameController.isGameOver() ? 1 : 0,
            gameController.getTimer().getElapsedSeconds(),
            goblin1OnGround,
            goblin2OnGround
        );
        
        networkManager.sendMessage(state);
    }

    private void processNetworkMessages() {
        String message;
        while ((message = networkManager.pollMessage()) != null) {
            if (message.startsWith("INPUT:") || message.startsWith("URGENT:")) {
                handleInputCommand(message);
            } else if (message.startsWith("STATE:")) {
                handleStateUpdate(message);
            } else if ("START_GAME".equals(message)) {
                System.out.println("遊戲中收到START_GAME消息");
            } else if ("CLIENT_READY".equals(message)) {
                if (isHost) {
                    System.out.println("主機收到客戶端就緒通知");
                }
            } else if ("INTRO_COMPLETE".equals(message)) {
                if (gameController != null) {
                    // 客戶端收到主機的INTRO_COMPLETE消息
                    if (!isHost) {
                        gameController.onReceiveIntroComplete();
                    }
                }
            } else if ("GAME_OVER".equals(message)) {
                showGameOver();
            } else if (message.startsWith("GAME_FINISH:")) {
                String[] parts = message.split(":");
                double time = Double.parseDouble(parts[1]);
            }
        }
    }
    
    private void handleInputCommand(String message) {
        // 只有主機處理輸入指令
        if (!isHost) return;
        
        boolean isUrgent = message.startsWith("URGENT:");
        String[] parts = message.split(":");
        if (parts.length < 4) return;
        
        try {
            int player = Integer.parseInt(parts[1]);
            String actionType = parts[2];
            boolean isPressed = Boolean.parseBoolean(parts[3]);
            
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
                    if (isPressed) {
                        // 強制重置跳躍狀態，確保跳躍能執行
                        if (isUrgent) {
                            goblinComponent.forceResetJumpState();
                        }
                        goblinComponent.jump();
                        
                        // 緊急跳躍立即同步狀態
                        if (isUrgent) {
                            Platform.runLater(() -> {
                                FXGL.getGameTimer().runOnceAfter(() -> {
                                    syncGameState();
                                }, javafx.util.Duration.millis(10));
                            });
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("輸入指令解析錯誤: " + message);
        }
    }

    private void handleStateUpdate(String message) {
        // 只有客戶端處理狀態更新
        if (isHost) return;
        
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

            // 強制設置goblin1位置為主機權威位置
            if (goblin1 != null) {
                double hostX1 = Double.parseDouble(parts[1]);
                double hostY1 = Double.parseDouble(parts[2]);
                double hostVX1 = Double.parseDouble(parts[3]);
                double hostVY1 = Double.parseDouble(parts[4]);
                
                // 強制設置位置，無條件接受主機位置
                forceSetEntityPosition(goblin1, hostX1, hostY1, hostVX1, hostVY1);
            }
            
            // 同樣強制設置goblin2位置
            if (goblin2 != null) {
                double hostX2 = Double.parseDouble(parts[5]);
                double hostY2 = Double.parseDouble(parts[6]);
                double hostVX2 = Double.parseDouble(parts[7]);
                double hostVY2 = Double.parseDouble(parts[8]);
                      
                forceSetEntityPosition(goblin2, hostX2, hostY2, hostVX2, hostVY2);
            }
            
        } catch (Exception e) {
            System.err.println("状态解析错误: " + message);
        }
    }
    
    /**
     * 強制設置實體位置，忽略所有本地物理計算
     */
    private void forceSetEntityPosition(Entity entity, double x, double y, double vx, double vy) {
        // 直接設置位置
        entity.setX(x);
        entity.setY(y);
        
        // 強制設置物理組件速度（如果存在）
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics != null) {
            // 強制覆蓋物理引擎計算的位置
            physics.overwritePosition(new javafx.geometry.Point2D(x, y));
            physics.setVelocityX(vx);
            physics.setVelocityY(vy);
        }
        
        // 確保視覺組件也更新到正確位置
        entity.getTransformComponent().setPosition(x, y);
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