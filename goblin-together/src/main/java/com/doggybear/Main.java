package com.doggybear;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.input.UserAction;
import com.doggybear.component.Goblin;
import com.doggybear.controller.*;
import com.doggybear.ui.FontManager;
import com.doggybear.ui.GameOver;

import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Main extends GameApplication {
    private GameController gameController;
    private PhysicsController physicsController;
    private GameOver gameOver;
    
    @Override
    protected void initSettings(GameSettings settings) {
        Settings.initSettings(settings);
    }

    @Override
    protected void initGame() {
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
        
        // 只有在遊戲真正開始且未結束時才檢查遊戲結束條件
        if (gameController.isGameStarted() && !gameController.isGameOver()) {
            if (gameController.checkGameOver()) {
                showGameOver();
            }
        }
        
        gameController.updateViewport();
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