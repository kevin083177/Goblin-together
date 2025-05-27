package com.doggybear;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.doggybear.component.Goblin;
import com.doggybear.component.Timer;
import com.doggybear.controller.GameController;
import com.doggybear.controller.InputController;
import com.doggybear.controller.PhysicsController;
import com.doggybear.controller.UIController;
import com.doggybear.menu.MainMenu;
import com.doggybear.network.NetworkGameManager;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Main extends GameApplication {
    
    // 控制器
    private GameController gameController;
    private InputController inputController;
    private PhysicsController physicsController;
    private UIController uiController;
    
    // 遊戲更新相關
    private double timePassed = 0;
    
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

    @Override
    protected void initGame() {
        System.out.println("=== Main.initGame() 開始 ===");
        
        // 初始化遊戲控制器
        gameController = new GameController();
        gameController.initGame();
        
        // 初始化物理控制器
        physicsController = new PhysicsController(
            gameController.getNetworkGameManager(),
            this::showGameOver  // 遊戲結束回調
        );
        physicsController.setGravity(0, 1500);
        
        // 初始化UI控制器
        uiController = new UIController(
            gameController.getNetworkGameManager(),
            gameController.getLevel()
        );
        
        // 在這裡直接初始化輸入控制器
        System.out.println("在 initGame() 中初始化輸入...");
        Entity goblin = gameController.getGoblin();
        Entity goblin2 = gameController.getGoblin2();
        
        if (goblin != null && goblin2 != null) {
            inputController = new InputController(
                goblin,
                goblin2,
                gameController.getNetworkGameManager()
            );
            
            System.out.println("調用 InputController.initInput()...");
            inputController.initInput();
        } else {
            System.err.println("錯誤：無法在 initGame() 中初始化輸入，實體為 null");
        }
        
        System.out.println("=== Main.initGame() 完成 ===");
    }

    @Override
    protected void initInput() {
        // 確保遊戲控制器已經初始化且實體已創建
        if (gameController == null) {
            System.err.println("GameController 未初始化！");
            return;
        }
        
        Entity goblin = gameController.getGoblin();
        Entity goblin2 = gameController.getGoblin2();
        
        if (goblin == null || goblin2 == null) {
            System.err.println("Goblin 實體未創建！");
            return;
        }
        
        // 現在創建輸入控制器
        inputController = new InputController(
            goblin,
            goblin2,
            gameController.getNetworkGameManager()
        );
        
        // 初始化輸入
        inputController.initInput();
        
        System.out.println("輸入控制器初始化完成");
    }
    
    @Override
    protected void initPhysics() {
        physicsController.initPhysics();
    }
    
    @Override
    protected void initUI() {
        // 確保控制器已經初始化
        if (uiController != null) {
            uiController.initUI();
        }
    }
    
    @Override
    protected void onUpdate(double tpf) {
        // 確保控制器已初始化
        if (gameController == null) return;
        
        Entity goblin = gameController.getGoblin();
        Entity goblin2 = gameController.getGoblin2();
        com.doggybear.component.Timer timer = gameController.getTimer();
        NetworkGameManager networkGameManager = gameController.getNetworkGameManager();
        
        if (gameController.isGameOver() || gameController.isClientGameOver()) {
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
            // 獲取當前存活時間
            int currentSurvivalTime = timer != null ? timer.getElapsedSeconds() : 0;
            
            // 更新網路遊戲（包含插值和狀態同步）
            networkGameManager.updateNetworkGame(tpf, goblin, goblin2, gameController.getLavaHeight(), currentSurvivalTime);
            
            // 只有主機端執行遊戲邏輯
            if (networkGameManager.getNetworkManager().isHost()) {
                gameController.updateGameLogic(tpf);
                
                // 主機端檢查遊戲結束條件
                if (gameController.checkGameOverConditions()) {
                    showGameOver();
                }
            } else {
                // 客戶端只更新岩漿視覺效果（根據主機端同步的數據）
                gameController.updateClientLava();
            }
        } else {
            // 本地遊戲，正常執行所有邏輯
            gameController.updateGameLogic(tpf);
            
            if (gameController.checkGameOverConditions()) {
                showGameOver();
            }
        }
        
        // 更新視窗
        gameController.updateViewport();
        
        // 更新除錯資訊（可選）
        updateDebugInfo(tpf);
    }
    
    private void updateDebugInfo(double tpf) {
        // 確保 UI 控制器已初始化
        if (uiController == null || gameController == null) return;
        
        // 每秒更新一次除錯資訊
        timePassed += tpf;
        if (timePassed >= 1.0) {
            double fps = 1.0 / tpf;
            int entityCount = getGameWorld().getEntities().size();
            String networkInfo = gameController.getNetworkGameManager().isNetworkGame() ? 
                "網路: " + (gameController.getNetworkGameManager().getNetworkManager().isHost() ? "主機" : "客戶端") :
                "本地遊戲";
                
            uiController.showDebugInfo(fps, entityCount, networkInfo);
            timePassed = 0;
        }
    }

    private void showGameOver() {
        if (gameController.isGameOver() || gameController.isClientGameOver()) return;
        
        NetworkGameManager networkGameManager = gameController.getNetworkGameManager();
        com.doggybear.component.Timer timer = gameController.getTimer();
        
        if (networkGameManager.isNetworkGame() && !networkGameManager.getNetworkManager().isHost()) {
            gameController.setClientGameOver(true);
        } else {
            gameController.setGameOver(true);
        }
        
        if (networkGameManager.isNetworkGame() && networkGameManager.getNetworkManager().isHost()) {
            networkGameManager.sendGameOver();
        }
        
        timer.stop();
        
        // 獲取最終存活時間
        int finalSurvivalTime;
        if (networkGameManager.isNetworkGame() && !networkGameManager.getNetworkManager().isHost()) {
            finalSurvivalTime = gameController.getClientSurvivalTime(); // 客戶端使用主機端的時間
        } else {
            finalSurvivalTime = timer.getElapsedSeconds();
        }
        
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
        String roleText = "";
        if (networkGameManager.isNetworkGame()) {
            roleText = networkGameManager.getNetworkManager().isHost() ? " (主機)" : " (客戶端)";
        }
        
        Text survivalTimeText = new Text(gameTypeText + roleText + "\n存活了 " + finalSurvivalTime + " 秒");
        survivalTimeText.setFont(Font.font(20));
        survivalTimeText.setFill(Color.WHITE);
        
        Button restartBtn = new Button("重來一次");
        restartBtn.setPrefWidth(150);
        restartBtn.setPrefHeight(40);
        restartBtn.setOnAction(e -> {
            networkGameManager.stopNetworkGame();
            restartGame();
        });
        
        Button menuBtn = new Button("回到主頁面");
        menuBtn.setPrefWidth(150);
        menuBtn.setPrefHeight(40);
        menuBtn.setOnAction(e -> {
            networkGameManager.stopNetworkGame();
            getGameController().gotoMainMenu(); // 使用 FXGL 的 GameController
        });
        
        VBox modalContent = new VBox(20);
        modalContent.setAlignment(Pos.CENTER);
        modalContent.getChildren().addAll(gameOverText, survivalTimeText, restartBtn, menuBtn);
        
        StackPane modalPane = new StackPane(modalBg, modalContent);
        modalPane.setTranslateX(getAppWidth() / 2 - 200);
        modalPane.setTranslateY(getAppHeight() / 2 - 175);
        
        getGameScene().addUINodes(overlay, modalPane);
    }
    
    /**
     * 重新開始遊戲
     */
    private void restartGame() {
        // 清理現有狀態
        if (inputController != null) {
            inputController.clearInput();
        }
        if (uiController != null) {
            uiController.clearUI();
        }
        if (physicsController != null) {
            physicsController.resetPhysics();
        }
        
        // 重新開始遊戲 - 使用 FXGL 的 GameController
        getGameController().startNewGame();
    }
    
    /**
     * 獲取遊戲控制器實例（用於外部訪問）
     * 重新命名避免與 FXGL 的 GameController 衝突
     */
    public GameController getMyGameController() { return gameController; }
    public InputController getInputController() { return inputController; }
    public PhysicsController getPhysicsController() { return physicsController; }
    public UIController getUIController() { return uiController; }

    public static void main(String[] args) {
        launch(args);
    }
}