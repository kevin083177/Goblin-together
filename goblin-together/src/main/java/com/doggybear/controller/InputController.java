package com.doggybear.controller;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.doggybear.component.Goblin;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.getInput;

public class InputController {
    
    private Entity goblin;
    private Entity goblin2;
    private static boolean isGloballyInitialized = false;
    
    public InputController(Entity goblin, Entity goblin2) {
        this.goblin = goblin;
        this.goblin2 = goblin2;
    }
    
    public void initInput() {
        // 只在第一次初始化時添加輸入綁定
        if (!isGloballyInitialized) {
            setupInputBindings();
            isGloballyInitialized = true;
        }
    }

    public void resetInput() {
        isGloballyInitialized = false;
    }
    
    private void setupInputBindings() {
        // 第一個玩家控制 - WASD 和空格跳躍
        getInput().addAction(new UserAction("向右移動") {
            @Override
            protected void onAction() {
                Entity currentGoblin = getCurrentGoblin();
                if (currentGoblin != null && currentGoblin.getComponent(Goblin.class) != null) {
                    currentGoblin.getComponent(Goblin.class).moveRight();
                }
            }

            @Override
            protected void onActionEnd() {
                Entity currentGoblin = getCurrentGoblin();
                if (currentGoblin != null && currentGoblin.getComponent(Goblin.class) != null) {
                    currentGoblin.getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("向左移動") {
            @Override
            protected void onAction() {
                Entity currentGoblin = getCurrentGoblin();
                if (currentGoblin != null && currentGoblin.getComponent(Goblin.class) != null) {
                    currentGoblin.getComponent(Goblin.class).moveLeft();
                }
            }

            @Override
            protected void onActionEnd() {
                Entity currentGoblin = getCurrentGoblin();
                if (currentGoblin != null && currentGoblin.getComponent(Goblin.class) != null) {
                    currentGoblin.getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("跳躍") {
            @Override
            protected void onActionBegin() {
                Entity currentGoblin = getCurrentGoblin();
                if (currentGoblin != null && currentGoblin.getComponent(Goblin.class) != null) {
                    currentGoblin.getComponent(Goblin.class).jump();
                }
            }
        }, KeyCode.SPACE);

        // 第二個玩家控制 - 方向鍵和Enter跳躍
        getInput().addAction(new UserAction("玩家2向右移動") {
            @Override
            protected void onAction() {
                Entity currentGoblin2 = getCurrentGoblin2();
                if (currentGoblin2 != null && currentGoblin2.getComponent(Goblin.class) != null) {
                    currentGoblin2.getComponent(Goblin.class).moveRight();
                }
            }

            @Override
            protected void onActionEnd() {
                Entity currentGoblin2 = getCurrentGoblin2();
                if (currentGoblin2 != null && currentGoblin2.getComponent(Goblin.class) != null) {
                    currentGoblin2.getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("玩家2向左移動") {
            @Override
            protected void onAction() {
                Entity currentGoblin2 = getCurrentGoblin2();
                if (currentGoblin2 != null && currentGoblin2.getComponent(Goblin.class) != null) {
                    currentGoblin2.getComponent(Goblin.class).moveLeft();
                }
            }

            @Override
            protected void onActionEnd() {
                Entity currentGoblin2 = getCurrentGoblin2();
                if (currentGoblin2 != null && currentGoblin2.getComponent(Goblin.class) != null) {
                    currentGoblin2.getComponent(Goblin.class).stop();
                }
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("玩家2跳躍") {
            @Override
            protected void onActionBegin() {
                Entity currentGoblin2 = getCurrentGoblin2();
                if (currentGoblin2 != null && currentGoblin2.getComponent(Goblin.class) != null) {
                    currentGoblin2.getComponent(Goblin.class).jump();
                }
            }
        }, KeyCode.ENTER);
    }
    
    /**
     * 獲取當前的 goblin 實體
     */
    private Entity getCurrentGoblin() {
        return goblin;
    }
    
    /**
     * 獲取當前的 goblin2 實體
     */
    private Entity getCurrentGoblin2() {
        return goblin2;
    }
    
    /**
     * 更新實體引用（當遊戲重新開始時使用）
     */
    public void updateEntities(Entity goblin, Entity goblin2) {
        this.goblin = goblin;
        this.goblin2 = goblin2;
    }
}