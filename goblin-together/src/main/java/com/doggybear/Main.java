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

    private static final double SYNC_INTERVAL = 0.01; 
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
                
                // 標記 Socket 已被 NetworkManager 管理
                GameData.markSocketAsManaged();
                
                System.out.println("網絡管理器已啟動");
            }

            // 初始化字型管理器
            FontManager.initialize();
            
            // 初始化遊戲控制器
            gameController = new GameController();
            gameController.initGame();
            
            // 初始化物理控制器
            physicsController = new PhysicsController(this::showGameOver);
            
            System.out.println("=== 遊戲初始化完成 ===");
            
        } catch (Exception e) {
            System.err.println("=== 遊戲初始化失敗 ===");
            System.err.println("錯誤: " + e.getMessage());
            e.printStackTrace();
            
            // 清理資源
            if (networkManager != null) {
                networkManager.stop();
                networkManager = null;
            }
            
            // 如果初始化失敗，回到主選單
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
        // 線上模式的控制邏輯
        if (isOnlineMode) {
            // 線上模式：每個玩家只能控制自己的角色
            if (isHost) {
                // 主機只能控制 goblin1，使用 A/D/SPACE
                setupPlayer1Controls();
            } else {
                // 客戶端只能控制 goblin2，使用左右方向鍵和 Enter
                setupPlayer2Controls();
            }
        } else {
            // 單機模式：玩家1使用 A/D/SPACE，玩家2使用方向鍵
            setupPlayer1Controls();
            setupPlayer2Controls();
        }
    }
    
    /**
     * 設置玩家1的控制（A/D/SPACE）
     */
    private void setupPlayer1Controls() {
        getInput().addAction(new UserAction("玩家1向右移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).moveRight();
                    
                    if (isOnlineMode) {
                        syncPlayerPosition(targetGoblin);
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).stop();
                    
                    if (isOnlineMode) {
                        syncPlayerPosition(targetGoblin);
                    }
                }
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("玩家1向左移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).moveLeft();
                    
                    if (isOnlineMode) {
                        syncPlayerPosition(targetGoblin);
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).stop();
                    
                    if (isOnlineMode) {
                        syncPlayerPosition(targetGoblin);
                    }
                }
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("玩家1跳躍") {
            @Override
            protected void onActionBegin() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).jump();
                    
                    if (isOnlineMode) {
                        syncPlayerPosition(targetGoblin);
                    }
                }
            }
        }, KeyCode.SPACE);
    }
    
    /**
     * 設置玩家2的控制（方向鍵）
     */
    private void setupPlayer2Controls() {
        getInput().addAction(new UserAction("玩家2向右移動") {
            @Override
            protected void onAction() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).moveRight();
                    
                    if (isOnlineMode) {
                        syncPlayerPosition(targetGoblin);
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).stop();
                    
                    if (isOnlineMode) {
                        syncPlayerPosition(targetGoblin);
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
                    targetGoblin.getComponent(Goblin.class).moveLeft();
                    
                    if (isOnlineMode) {
                        syncPlayerPosition(targetGoblin);
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).stop();
                    
                    if (isOnlineMode) {
                        syncPlayerPosition(targetGoblin);
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
                    targetGoblin.getComponent(Goblin.class).jump();
                    
                    if (isOnlineMode) {
                        syncPlayerPosition(targetGoblin);
                    }
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

        if (isOnlineMode && networkManager != null) {
            syncAccumulator += tpf;
            if (syncAccumulator >= SYNC_INTERVAL) {
                syncGameState();
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
        
        // 包含遊戲狀態標誌
        String state = String.format("STATE:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%.2f:%d:%d",
            goblin.getX(), goblin.getY(), 
            goblin.getComponent(PhysicsComponent.class).getVelocityX(),
            goblin.getComponent(PhysicsComponent.class).getVelocityY(),
            goblin2.getX(), goblin2.getY(),
            goblin2.getComponent(PhysicsComponent.class).getVelocityX(),
            goblin2.getComponent(PhysicsComponent.class).getVelocityY(),
            gameController.getLavaHeight(),
            gameController.isGameOver() ? 1 : 0,  // 遊戲結束狀態
            gameController.getTimer().getElapsedSeconds()  // 計時器時間
        );
        
        networkManager.sendMessage(state);
    }

    private void processNetworkMessages() {
        String message;
        while ((message = networkManager.pollMessage()) != null) {
            System.out.println("收到網絡消息: " + message);
            
            if (message.startsWith("POS:")) {
                handlePositionUpdate(message);
            } else if ("START_GAME".equals(message)) {
                // 這個消息在WaitingRoom中處理，這裡不需要處理
                System.out.println("遊戲中收到START_GAME消息");
            } else if ("CLIENT_READY".equals(message)) {
                if (isHost) {
                    System.out.println("主機收到客戶端就緒通知");
                }
            } else if ("INTRO_COMPLETE".equals(message)) {
                // 開場動畫完成，開始計時
                if (gameController != null) {
                    gameController.startActualGame();
                }
            }

            if (message.startsWith("STATE:")) {
                handleStateUpdate(message);
            } else if ("GAME_OVER".equals(message)) {
                showGameOver();
            } else if (message.startsWith("GAME_FINISH:")) {
                String[] parts = message.split(":");
                double time = Double.parseDouble(parts[1]);
            }
        }
    }

    private void handleStateUpdate(String message) {
        // 格式: STATE:g1X:g1Y:g1VX:g1VY:g2X:g2Y:g2VX:g2VY:lavaHeight
        String[] parts = message.split(":");
        if (parts.length < 10) return;
        
        try {
            // 更新哥布林1
            Entity goblin1 = gameController.getGoblin();
            double lavaHeight = Double.parseDouble(parts[9]);
            gameController.setLavaHeight(lavaHeight);

            int isGameOver = Integer.parseInt(parts[10]);

            if (isGameOver == 1 && !gameController.isGameOver()) {
                showGameOver();
            }
            
            // 同步計時器
            int elapsedSeconds = Integer.parseInt(parts[11]);
            if (gameController.getTimer() != null) {
                gameController.getTimer().setElapsedSeconds(elapsedSeconds);
            }

            if (goblin1 != null) {
                goblin1.setPosition(
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2])
                );
                PhysicsComponent physics = goblin1.getComponent(PhysicsComponent.class);
                if (physics != null) {
                    physics.setVelocityX(Double.parseDouble(parts[3]));
                    physics.setVelocityY(Double.parseDouble(parts[4]));
                }
            }
            
            // 更新哥布林2
            Entity goblin2 = gameController.getGoblin2();
            if (goblin2 != null) {
                goblin2.setPosition(
                    Double.parseDouble(parts[5]),
                    Double.parseDouble(parts[6])
                );
                PhysicsComponent physics = goblin2.getComponent(PhysicsComponent.class);
                if (physics != null) {
                    physics.setVelocityX(Double.parseDouble(parts[7]));
                    physics.setVelocityY(Double.parseDouble(parts[8]));
                }
            }
        } catch (Exception e) {
            System.err.println("状态解析错误: " + message);
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
            
            Entity target = null;
            
            // 根據玩家ID和當前玩家身份決定更新哪個角色
            if (isHost) {
                // 主機接收客戶端(player 2)的位置更新
                if (player == 2) {
                    target = gameController.getGoblin2();
                }
            } else {
                // 客戶端接收主機(player 1)的位置更新
                if (player == 1) {
                    target = gameController.getGoblin();
                }
            }
            
            if (target != null) {
                target.setPosition(x, y);
                PhysicsComponent physics = target.getComponent(PhysicsComponent.class);
                if (physics != null) {
                    physics.setVelocityX(velocityX);
                    physics.setVelocityY(velocityY);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("位置更新格式錯誤: " + message);
        }
    }
    
    private void syncPlayerPosition(Entity entity) {
        if (entity == null || networkManager == null) return;
        
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) return;
        
        // 確定玩家ID
        int playerId;
        if (isHost) {
            playerId = 1; // 主機是玩家1
        } else {
            playerId = 2; // 客戶端是玩家2
        }
        
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
        if (gameController == null || gameController.isGameOver()) return;
        
        gameController.setGameOver(true);
        
        // 停止計時器
        if (gameController.getTimer() != null) {
            gameController.getTimer().stop();
        }
        
        // 記錄最終存活時間
        int finalSurvivalTime = gameController.getTimer() != null ? 
            gameController.getTimer().getElapsedSeconds() : 0;
        
        if (isOnlineMode && networkManager != null) {
            networkManager.sendMessage("GAME_OVER");
        }

        // 創建遊戲結束畫面
        gameOver = new GameOver(finalSurvivalTime, isOnlineMode, new GameOver.GameOverCallback() {
            @Override
            public void onRestart() {
                // 清理網絡連接
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
    
    /**
     * 返回等待房間
     */
    private void returnToWaitingRoom() {
        System.out.println("返回等待房間");
        
        if (networkManager != null) {
            // 停止網絡管理器但不關閉Socket
            networkManager.stop(false);
            networkManager = null;
        }
        
        // 標記Socket未被管理
        GameData.markSocketAsUnmanaged();
        
        // 獲取主機信息
        String hostIP = GameData.getHostIP();
        boolean isHost = GameData.isHost();
        
        // 返回等待室
        getSceneService().pushSubScene(new WaitingRoom(isHost, hostIP));
    }
    
    /**
     * 返回主選單
     */
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