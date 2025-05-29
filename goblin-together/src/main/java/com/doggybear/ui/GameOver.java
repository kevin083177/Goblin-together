package com.doggybear.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameOver {
    
    private Rectangle overlay;
    private StackPane modalPane;
    private int survivalTime;
    
    // 回調接口
    public interface GameOverCallback {
        void onRestart();
        void onBackToMenu();
    }
    
    private GameOverCallback callback;
    
    public GameOver(int survivalTime, GameOverCallback callback) {
        this.survivalTime = survivalTime;
        this.callback = callback;
        createGameOverUI();
    }
    
    private void createGameOverUI() {
        // 半透明背景覆蓋層
        overlay = new Rectangle(getAppWidth(), getAppHeight(), Color.color(0, 0, 0, 0.7));
        
        // 模態對話框背景
        Rectangle modalBg = new Rectangle(400, 350, Color.DARKGRAY);
        modalBg.setArcWidth(20);
        modalBg.setArcHeight(20);
        modalBg.setStroke(Color.WHITE);
        modalBg.setStrokeWidth(2);
        
        // 遊戲結束標題
        Text gameOverText = new Text("遊戲結束");
        gameOverText.setFont(Font.font(40));
        gameOverText.setFill(Color.WHITE);
        
        // 存活時間文字
        Text survivalTimeText = new Text("您存活了 " + survivalTime + " 秒");
        survivalTimeText.setFont(Font.font(20));
        survivalTimeText.setFill(Color.WHITE);
        
        // 重新開始按鈕
        Button restartBtn = createStyledButton("重來一次", "#4CAF50");
        restartBtn.setOnAction(e -> {
            if (callback != null) {
                hide();
                callback.onRestart();
            }
        });
        
        // 返回主選單按鈕
        Button menuBtn = createStyledButton("回到主頁面", "#2196F3");
        menuBtn.setOnAction(e -> {
            if (callback != null) {
                hide();
                callback.onBackToMenu();
            }
        });
        
        // 內容容器
        VBox modalContent = new VBox(20);
        modalContent.setAlignment(Pos.CENTER);
        modalContent.getChildren().addAll(
            gameOverText, 
            survivalTimeText, 
            restartBtn, 
            menuBtn
        );
        
        // 模態對話框
        modalPane = new StackPane(modalBg, modalContent);
        modalPane.setTranslateX(getAppWidth() / 2 - 200);
        modalPane.setTranslateY(getAppHeight() / 2 - 175);
    }
        
    /**
     * 創建樣式化按鈕
     */
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(150);
        button.setPrefHeight(40);
        button.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-background-radius: 5px; " +
            "-fx-cursor: hand;",
            color
        ));
        
        // 滑鼠懸停效果
        button.setOnMouseEntered(e -> {
            button.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-background-radius: 5px; " +
                "-fx-cursor: hand; " +
                "-fx-opacity: 0.8;",
                color
            ));
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-background-radius: 5px; " +
                "-fx-cursor: hand;",
                color
            ));
        });
        
        return button;
    }
    
    /**
     * 顯示遊戲結束畫面
     */
    public void show() {
        getGameScene().addUINodes(overlay, modalPane);
    }
    
    /**
     * 隱藏遊戲結束畫面
     */
    public void hide() {
        getGameScene().removeUINodes(overlay, modalPane);
    }
    
    /**
     * 更新存活時間
     */
    public void updateSurvivalTime(int newTime) {
        this.survivalTime = newTime;
        // 如果需要動態更新UI，可以在這裡實現
    }
}