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

public class GameFinish {
    
    private Rectangle overlay;
    private StackPane modalPane;
    private double completionTime;
    
    public interface GameFinishCallback {
        void onRestart();
        void onBackToMenu();
    }
    
    private GameFinishCallback callback;
    
    public GameFinish(double completionTime, GameFinishCallback callback) {
        this.completionTime = completionTime;
        this.callback = callback;
        createGameFinishUI();
    }
    
    private void createGameFinishUI() {
        overlay = new Rectangle(getAppWidth(), getAppHeight(), Color.color(0, 0, 0, 0.7));
        
        Rectangle modalBg = new Rectangle(450, 400, Color.color(0.1, 0.1, 0.2, 0.95));
        modalBg.setArcWidth(20);
        modalBg.setArcHeight(20);
        modalBg.setStroke(Color.GOLD);
        modalBg.setStrokeWidth(3);
        
        Text congratsText = new Text("恭喜通關!");
        congratsText.setFont(Font.font("Arial", 36));
        congratsText.setFill(Color.GOLD);
        
        String timeText = formatTime(completionTime);
        Text completionTimeText = new Text("完成時間: " + timeText);
        completionTimeText.setFont(Font.font("Arial", 22));
        completionTimeText.setFill(Color.WHITE);
        
        Button restartBtn = createStyledButton("重新挑戰", "#FF9800");
        restartBtn.setOnAction(e -> {
            if (callback != null) {
                hide();
                callback.onRestart();
            }
        });
        
        Button menuBtn = createStyledButton("回到主選單", "#2196F3");
        menuBtn.setOnAction(e -> {
            if (callback != null) {
                hide();
                callback.onBackToMenu();
            }
        });
        
        VBox modalContent = new VBox(20);
        modalContent.setAlignment(Pos.CENTER);
        modalContent.getChildren().addAll(
            congratsText, 
            completionTimeText,
            restartBtn, 
            menuBtn
        );
        
        modalPane = new StackPane(modalBg, modalContent);
        modalPane.setTranslateX(getAppWidth() / 2 - 225);
        modalPane.setTranslateY(getAppHeight() / 2 - 200);
    }
    
    /**
     * 格式化時間顯示
     */
    private String formatTime(double totalSeconds) {
        int minutes = (int) (totalSeconds / 60);
        double seconds = totalSeconds % 60;
        
        if (minutes > 0) {
            return String.format("%d分%.2f秒", minutes, seconds);
        } else {
            return String.format("%.2f秒", seconds);
        }
    }
       
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(180);
        button.setPrefHeight(45);
        button.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand; " +
            "-fx-font-weight: bold;",
            color
        ));
        
        // 滑鼠懸停效果
        button.setOnMouseEntered(e -> {
            button.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-font-weight: bold; " +
                "-fx-opacity: 0.8; " +
                "-fx-scale-x: 1.05; " +
                "-fx-scale-y: 1.05;",
                color
            ));
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-font-weight: bold;",
                color
            ));
        });
        
        return button;
    }
    
    /**
     * 顯示遊戲完成畫面
     */
    public void show() {
        getGameScene().addUINodes(overlay, modalPane);
    }
    
    /**
     * 隱藏遊戲完成畫面
     */
    public void hide() {
        getGameScene().removeUINodes(overlay, modalPane);
    }
    
    /**
     * 更新完成時間
     */
    public void updateCompletionTime(double newTime) {
        this.completionTime = newTime;
    }
}