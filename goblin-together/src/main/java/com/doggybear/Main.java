package com.doggybear;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.doggybear.controller.*;
import com.doggybear.ui.GameOver;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Main extends GameApplication {
    
    // 控制器
    private GameController gameController;
    

    private PhysicsController physicsController;
    private UIController uiController;
    private GameOver gameOver;
    
    @Override
    protected void initSettings(GameSettings settings) {
        // 使用 Settings 類別來初始化設定
        Settings.initSettings(settings);
    }

    @Override
    protected void initGame() {
        // 初始化遊戲控制器
        gameController = new GameController();
        gameController.initGame();
        
        // 初始化物理控制器
        physicsController = new PhysicsController(this::showGameOver);
        
        // 初始化UI控制器
        uiController = new UIController(gameController.getLevel());
    }
   
    @Override
    protected void initPhysics() {
        physicsController.initPhysics();
    }
    
    @Override
    protected void initUI() {
        uiController.initUI();
    }
    
    @Override
    protected void onUpdate(double tpf) {
        gameController.update(tpf);
        
        // 檢查遊戲結束條件
        if (gameController.checkGameOver()) {
            showGameOver();
        }
        
        // 更新視窗
        gameController.updateViewport();
    }

    private void showGameOver() {
        if (gameController.isGameOver()) return;
        
        gameController.setGameOver(true);
        gameController.getTimer().stop();
        
        // 記錄最終存活時間
        int finalSurvivalTime = gameController.getTimer().getElapsedSeconds();
        
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