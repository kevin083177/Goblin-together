package com.doggybear;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.PhysicsComponent;
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
                if (socket == null || socket.isClosed()) {
                    System.err.println("Socket 無效，無法初始化網絡管理器");
                    throw new RuntimeException("Socket 無效");
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
            
            // *** 關鍵修復：如果是客戶端，直接啟動遊戲 ***
            if (isOnlineMode && !isHost) {
                System.out.println("客戶端直接啟動遊戲");
                // 延遲一點時間確保所有初始化完成
                javafx.application.Platform.runLater(() -> {
                    if (gameController != null) {
                        System.out.println("客戶端延遲啟動遊戲");
                        gameController.startActualGame();
                    }
                });
            }
            
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
        boolean result = gameController != null && 
            gameController.isGameStarted() && 
            !gameController.isGameOver();
        
        // 只在返回false時打印調試信息，避免過多輸出
        if (!result && gameController != null) {
            System.out.println("輸入被拒絕 - gameStarted: " + gameController.isGameStarted() + 
                            ", gameOver: " + gameController.isGameOver());
        }
        
        return result;
    }

    @Override
    protected void initInput() {
        System.out.println("=== initInput 開始 ===");
        System.out.println("線上模式: " + isOnlineMode + ", 主機: " + isHost);
        
        // 線上模式的控制邏輯
        if (isOnlineMode) {
            // 線上模式：每個玩家只能控制自己的角色
            if (isHost) {
                // 主機只能控制 goblin1，使用 A/D/SPACE
                setupPlayer1Controls();
                System.out.println("主機輸入控制已設置 (A/D/Space)");
            } else {
                // 客戶端只能控制 goblin2，使用左右方向鍵和 Enter
                setupPlayer2Controls();
                System.out.println("客戶端輸入控制已設置 (方向鍵/Enter)");
            }
        } else {
            // 單機模式：玩家1使用 A/D/SPACE，玩家2使用方向鍵
            setupPlayer1Controls();
            setupPlayer2Controls();
            System.out.println("單機模式輸入控制已設置");
        }
        
        System.out.println("=== initInput 完成 ===");
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
                        // 主機控制goblin1，發送玩家ID=1的位置更新
                        syncPlayerPositionWithId(targetGoblin, 1);
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
                        syncPlayerPositionWithId(targetGoblin, 1);
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
                        syncPlayerPositionWithId(targetGoblin, 1);
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
                        syncPlayerPositionWithId(targetGoblin, 1);
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
                        syncPlayerPositionWithId(targetGoblin, 1);
                    }
                }
            }
        }, KeyCode.SPACE);
    }
    
    /**
     * 設置玩家2的控制（方向鍵）
     */
    private void setupPlayer2Controls() {
        System.out.println("開始設置玩家2控制 (方向鍵/Enter)");
        
        getInput().addAction(new UserAction("玩家2向右移動") {
            @Override
            protected void onAction() {
                System.out.println("客戶端：收到右移按鍵輸入");
                
                if (!canAcceptInput()) {
                    System.out.println("客戶端：拒絕右移輸入");
                    return;
                }
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    System.out.println("客戶端：執行goblin2右移");
                    targetGoblin.getComponent(Goblin.class).moveRight();
                    
                    if (isOnlineMode) {
                        syncPlayerPositionWithId(targetGoblin, 2);
                    }
                } else {
                    System.err.println("客戶端：無法獲取goblin2實體進行右移");
                }
            }

            @Override
            protected void onActionEnd() {
                System.out.println("客戶端：右移按鍵釋放");
                
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).stop();
                    
                    if (isOnlineMode) {
                        syncPlayerPositionWithId(targetGoblin, 2);
                    }
                }
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("玩家2向左移動") {
            @Override
            protected void onAction() {
                System.out.println("客戶端：收到左移按鍵輸入");
                
                if (!canAcceptInput()) {
                    System.out.println("客戶端：拒絕左移輸入");
                    return;
                }
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    System.out.println("客戶端：執行goblin2左移");
                    targetGoblin.getComponent(Goblin.class).moveLeft();
                    
                    if (isOnlineMode) {
                        syncPlayerPositionWithId(targetGoblin, 2);
                    }
                } else {
                    System.err.println("客戶端：無法獲取goblin2實體進行左移");
                }
            }

            @Override
            protected void onActionEnd() {
                System.out.println("客戶端：左移按鍵釋放");
                
                if (!canAcceptInput()) return;
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    targetGoblin.getComponent(Goblin.class).stop();
                    
                    if (isOnlineMode) {
                        syncPlayerPositionWithId(targetGoblin, 2);
                    }
                }
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("玩家2跳躍") {
            @Override
            protected void onActionBegin() {
                System.out.println("客戶端：收到跳躍按鍵輸入");
                
                if (!canAcceptInput()) {
                    System.out.println("客戶端：拒絕跳躍輸入");
                    return;
                }
                
                Entity targetGoblin = gameController.getGoblin2();
                if (targetGoblin != null) {
                    System.out.println("客戶端：執行goblin2跳躍");
                    targetGoblin.getComponent(Goblin.class).jump();
                    
                    if (isOnlineMode) {
                        syncPlayerPositionWithId(targetGoblin, 2);
                    }
                } else {
                    System.err.println("客戶端：無法獲取goblin2實體進行跳躍");
                }
            }
        }, KeyCode.ENTER);
        
        System.out.println("玩家2控制設置完成");
    }

    @Override
    protected void initPhysics() {
        physicsController.initPhysics();
    }
    
    @Override
    protected void onUpdate(double tpf) {
        // 客戶端特殊處理：如果是線上模式且不是主機，檢查遊戲是否需要強制啟動
        if (isOnlineMode && !isHost && gameController != null && !gameController.isGameStarted()) {
            // 檢查是否有網絡活動，如果有則強制啟動遊戲
            if (networkManager != null && networkManager.isConnected()) {
                String testMessage = networkManager.pollMessage();
                if (testMessage != null) {
                    // 有網絡消息說明主機遊戲已開始，強制啟動客戶端遊戲
                    System.out.println("客戶端強制啟動遊戲，因為收到網絡消息: " + testMessage);
                    gameController.startActualGame();
                    
                    // 重新處理這個消息
                    if (testMessage.startsWith("STATE:")) {
                        handleStateUpdate(testMessage);
                    } else if (testMessage.startsWith("POS:")) {
                        handlePositionUpdate(testMessage);
                    }
                }
            }
        }
        
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
                System.out.println("遊戲中收到START_GAME消息");
            } else if ("CLIENT_READY".equals(message)) {
                if (isHost) {
                    System.out.println("主機收到客戶端就緒通知");
                }
            } else if ("INTRO_COMPLETE".equals(message)) {
                // 開場動畫完成，開始計時
                System.out.println("收到INTRO_COMPLETE消息，開始實際遊戲");
                if (gameController != null) {
                    System.out.println("調用 gameController.startActualGame()");
                    gameController.startActualGame();
                } else {
                    System.err.println("警告：gameController為null，無法開始遊戲");
                }
            } else if (message.startsWith("STATE:")) {
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
            
            // 明確的同步邏輯：
            // 主機(isHost=true)：接收玩家2的位置更新goblin2
            // 客戶端(isHost=false)：接收玩家1的位置更新goblin1
            if (isHost && player == 2) {
                // 主機接收客戶端控制的goblin2位置
                target = gameController.getGoblin2();
                System.out.println("主機更新goblin2位置: " + x + "," + y);
            } else if (!isHost && player == 1) {
                // 客戶端接收主機控制的goblin1位置
                target = gameController.getGoblin();
                System.out.println("客戶端更新goblin1位置: " + x + "," + y);
            }
            
            if (target != null) {
                target.setPosition(x, y);
                PhysicsComponent physics = target.getComponent(PhysicsComponent.class);
                if (physics != null) {
                    physics.setVelocityX(velocityX);
                    physics.setVelocityY(velocityY);
                }
            } else {
                System.err.println("找不到目標實體 - 玩家ID:" + player + ", isHost:" + isHost);
            }
        } catch (NumberFormatException e) {
            System.err.println("位置更新格式錯誤: " + message);
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

    private void syncPlayerPositionWithId(Entity entity, int playerId) {
        if (entity == null || networkManager == null) {
            System.err.println("同步失敗：entity或networkManager為null");
            return;
        }
        
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            System.err.println("同步失敗：PhysicsComponent為null");
            return;
        }
        
        String message = String.format("POS:%d:%.2f:%.2f:%.2f:%.2f",
            playerId,
            entity.getX(),
            entity.getY(),
            physics.getVelocityX(),
            physics.getVelocityY());
        
        networkManager.sendMessage(message);
        System.out.println("同步玩家" + playerId + "位置: " + entity.getX() + "," + entity.getY());
    }


    public static void main(String[] args) {
        launch(args);
    }
}