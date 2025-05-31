package com.doggybear.controller;

import com.doggybear.levels.Level;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.getGameScene;

public class UIController {
    
    private Level level;
    private Text titleText;
    private Text helpText;
    
    public UIController(Level level) {
        this.level = level;
    }
    
    public void initUI() {
        // 遊戲標題
        titleText = new Text("Goblin Together");
        titleText.setTranslateX(10);
        titleText.setTranslateY(30);
        titleText.setFill(Color.WHITE);
        titleText.setFont(Font.font("Arial", 20));
        
        // 控制說明
        helpText = new Text("玩家1: A/D 移動，空白鍵跳躍 | 玩家2: 方向鍵移動，Enter跳躍");
        helpText.setTranslateX(10);
        helpText.setTranslateY(60);
        helpText.setFill(Color.LIGHTBLUE);
        helpText.setFont(Font.font("Arial", 14));
        
        getGameScene().addUINodes(titleText, helpText);
    }
    
      
    /**
     * 顯示遊戲提示
     */
    public void showGameTip(String tip) {
        Text tipText = new Text(tip);
        tipText.setTranslateX(getGameScene().getViewport().getX() + 50);
        tipText.setTranslateY(getGameScene().getViewport().getY() + 200);
        tipText.setFill(Color.ORANGE);
        tipText.setFont(Font.font("Arial", 18));
        
        getGameScene().addUINode(tipText);
        
        // 3秒後移除提示
        com.almasb.fxgl.time.LocalTimer localTimer = com.almasb.fxgl.dsl.FXGL.newLocalTimer();
        localTimer.capture();
        
        // 在遊戲更新循環中檢查是否到時間
        com.almasb.fxgl.dsl.FXGL.runOnce(() -> {
            getGameScene().removeUINode(tipText);
        }, javafx.util.Duration.seconds(3));
    }
    
    /**
     * 清理所有UI元素（遊戲重新開始時使用）
     */
    public void clearUI() {
        if (titleText != null) getGameScene().removeUINode(titleText);
        if (helpText != null) getGameScene().removeUINode(helpText);
    }
}