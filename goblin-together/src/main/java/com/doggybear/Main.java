package com.doggybear;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.input.UserAction;
import com.doggybear.component.Goblin;
import com.doggybear.controller.*;
import com.doggybear.ui.GameOver;

import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Main extends GameApplication {
    private GameController gameController;
    private PhysicsController physicsController;
    private UIController uiController;
    private GameOver gameOver;
    
    @Override
    protected void initSettings(GameSettings settings) {
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
    protected void initInput() {
        getInput().addAction(new UserAction("向右移動") {
            @Override
            protected void onAction() {
                gameController.getGoblin().getComponent(Goblin.class).moveRight();
            }

            @Override
            protected void onActionEnd() {
                gameController.getGoblin().getComponent(Goblin.class).stop();
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("向左移動") {
            @Override
            protected void onAction() {
                gameController.getGoblin().getComponent(Goblin.class).moveLeft();
            }

            @Override
            protected void onActionEnd() {
                gameController.getGoblin().getComponent(Goblin.class).stop();
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("跳躍") {
            @Override
            protected void onActionBegin() {
                gameController.getGoblin().getComponent(Goblin.class).jump();
            }
        }, KeyCode.SPACE);

        getInput().addAction(new UserAction("玩家2向右移動") {
            @Override
            protected void onAction() {
                gameController.getGoblin2().getComponent(Goblin.class).moveRight();
            }

            @Override
            protected void onActionEnd() {
                gameController.getGoblin2().getComponent(Goblin.class).stop();
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("玩家2向左移動") {
            @Override
            protected void onAction() {
                gameController.getGoblin2().getComponent(Goblin.class).moveLeft();
            }

            @Override
            protected void onActionEnd() {
                gameController.getGoblin2().getComponent(Goblin.class).stop();
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("玩家2跳躍") {
            @Override
            protected void onActionBegin() {
                gameController.getGoblin2().getComponent(Goblin.class).jump();
            }
        }, KeyCode.ENTER);
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
        if (gameController.checkGameOver()) {
            showGameOver();
        }
        
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