package com.doggybear.controller;

import com.doggybear.levels.Level;
import com.doggybear.network.NetworkGameManager;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.getGameScene;

public class UIController {
    
    private NetworkGameManager networkGameManager;
    private Level level;
    
    // UI元素
    private Text titleText;
    private Text helpText;
    private Text levelText;
    private Text statusText;
    private Text debugText;
    
    public UIController(NetworkGameManager networkGameManager, Level level) {
        this.networkGameManager = networkGameManager;
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
        String controlText = getControlText();
        helpText = new Text(controlText);
        helpText.setTranslateX(10);
        helpText.setTranslateY(60);
        helpText.setFill(Color.LIGHTBLUE);
        helpText.setFont(Font.font("Arial", 14));
        
        // 關卡資訊
        levelText = new Text("關卡: " + (level != null ? level.getName() : "未知"));
        levelText.setTranslateX(10);
        levelText.setTranslateY(90);
        levelText.setFill(Color.YELLOW);
        levelText.setFont(Font.font("Arial", 16));
        
        // 狀態文字
        statusText = new Text("");
        statusText.setTranslateX(10);
        statusText.setTranslateY(120);
        statusText.setFill(Color.LIGHTGREEN);
        statusText.setFont(Font.font("Arial", 14));
        
        // 除錯資訊（可選）
        debugText = new Text("");
        debugText.setTranslateX(10);
        debugText.setTranslateY(150);
        debugText.setFill(Color.GRAY);
        debugText.setFont(Font.font("Arial", 12));
        
        // 添加到遊戲場景
        getGameScene().addUINodes(titleText, helpText, levelText, statusText, debugText);
        
        // 更新網路狀態
        updateNetworkStatus();
    }
    
    private String getControlText() {
        if (networkGameManager.isNetworkGame()) {
            if (networkGameManager.getNetworkManager().isHost()) {
                return "網路遊戲 (主機) - WASD移動，空白鍵跳躍";
            } else {
                return "網路遊戲 (客戶端) - WASD移動，空白鍵跳躍";
            }
        } else {
            return "本地雙人 - 玩家1: WASD+空格 | 玩家2: 方向鍵+Enter";
        }
    }
    
    /**
     * 更新網路狀態顯示
     */
    public void updateNetworkStatus() {
        if (networkGameManager.isNetworkGame()) {
            String status = "網路狀態: 已連線";
            if (networkGameManager.getNetworkManager().isHost()) {
                status += " | 身份: 主機";
                status += " | IP: " + networkGameManager.getNetworkManager().getHostIP();
            } else {
                status += " | 身份: 客戶端";
                status += " | 連接至: " + networkGameManager.getNetworkManager().getHostIP();
            }
            statusText.setText(status);
        } else {
            statusText.setText("本地遊戲模式");
        }
    }
    
    /**
     * 更新除錯資訊
     */
    public void updateDebugInfo(String info) {
        if (debugText != null) {
            debugText.setText("除錯: " + info);
        }
    }
    
    /**
     * 顯示遊戲提示
     */
    public void showGameTip(String tip) {
        // 創建臨時提示文字
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
     * 顯示網路連接狀態
     */
    public void showNetworkConnectionStatus(boolean connected) {
        String message = connected ? "網路連接成功!" : "網路連接斷開!";
        Color color = connected ? Color.GREEN : Color.RED;
        
        Text connectionText = new Text(message);
        connectionText.setTranslateX(getGameScene().getViewport().getX() + 50);
        connectionText.setTranslateY(getGameScene().getViewport().getY() + 250);
        connectionText.setFill(color);
        connectionText.setFont(Font.font("Arial", 16));
        
        getGameScene().addUINode(connectionText);
        
        // 5秒後移除狀態訊息
        com.almasb.fxgl.dsl.FXGL.runOnce(() -> {
            getGameScene().removeUINode(connectionText);
        }, javafx.util.Duration.seconds(5));
    }
    
    /**
     * 更新關卡資訊
     */
    public void updateLevelInfo(Level newLevel) {
        this.level = newLevel;
        if (levelText != null) {
            levelText.setText("關卡: " + (level != null ? level.getName() : "未知"));
        }
    }
    
    /**
     * 顯示暫停畫面
     */
    public void showPauseScreen() {
        Text pauseText = new Text("遊戲暫停");
        pauseText.setTranslateX(getGameScene().getViewport().getX() + getGameScene().getViewport().getWidth() / 2 - 50);
        pauseText.setTranslateY(getGameScene().getViewport().getY() + getGameScene().getViewport().getHeight() / 2);
        pauseText.setFill(Color.WHITE);
        pauseText.setFont(Font.font("Arial", 32));
        
        getGameScene().addUINode(pauseText);
    }
    
    /**
     * 隱藏暫停畫面
     */
    public void hidePauseScreen() {
        // 移除暫停相關的UI元素
        getGameScene().getUINodes().removeIf(node -> 
            node instanceof Text && ((Text) node).getText().equals("遊戲暫停"));
    }
    
    /**
     * 顯示FPS和除錯資訊
     */
    public void showDebugInfo(double fps, int entityCount, String networkInfo) {
        String debugInfo = String.format("FPS: %.1f | 實體數: %d | %s", fps, entityCount, networkInfo);
        updateDebugInfo(debugInfo);
    }
    
    /**
     * 清理所有UI元素
     */
    public void clearUI() {
        if (titleText != null) getGameScene().removeUINode(titleText);
        if (helpText != null) getGameScene().removeUINode(helpText);
        if (levelText != null) getGameScene().removeUINode(levelText);
        if (statusText != null) getGameScene().removeUINode(statusText);
        if (debugText != null) getGameScene().removeUINode(debugText);
    }
    
    /**
     * 重新初始化UI（遊戲重新開始時使用）
     */
    public void reinitUI() {
        clearUI();
        initUI();
    }
    
    /**
     * 更新控制說明文字
     */
    public void updateControlText() {
        if (helpText != null) {
            helpText.setText(getControlText());
        }
    }
}