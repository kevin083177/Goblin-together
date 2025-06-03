package com.doggybear.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameFinish {
    
    private Rectangle overlay;
    private StackPane modalPane;
    private double completionTime;
    
    public interface GameFinishCallback {
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
        
        Rectangle modalBg = new Rectangle(450, 400, Color.color(1, 1, 1, 0.95));
        modalBg.setArcWidth(20);
        modalBg.setArcHeight(20);
        modalBg.setStroke(Color.GOLD);
        modalBg.setStrokeWidth(3);
        
        Text congratsText = new Text("成功通關");
        congratsText.setFont(FontManager.getFont(FontManager.FontType.REGULAR, 32));
        congratsText.setFill(Color.GOLD);
        
        String timeText = formatTime(completionTime);
        VBox scoreCard = createScoreCard(timeText);
        
        Button menuBtn = createIconButton("🏠", "返回主選單", "#4CAF50");
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
            scoreCard,
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
    
    private VBox createScoreCard(String time) {
        VBox scoreCard = new VBox(15);
        scoreCard.setAlignment(Pos.CENTER);
        
        Text scoreLabel = new Text("通關時間");
        try {
            Font labelFont = FontManager.getFont(FontManager.FontType.REGULAR, 18);
            scoreLabel.setFont(labelFont);
        } catch (Exception e) {
            scoreLabel.setFont(Font.font("Arial", 18));
        }
        scoreLabel.setFill(Color.web("#666666"));
        
        Text scoreNumber = new Text(time);
        try {
            Font scoreFont = FontManager.getFont(FontManager.FontType.REGULAR, 48);
            scoreNumber.setFont(scoreFont);
        } catch (Exception e) {
            scoreNumber.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        }
        scoreNumber.setFill(Color.web("#333333"));
        
        scoreCard.getChildren().addAll(scoreLabel, scoreNumber);
        
        return scoreCard;
    }

    private Button createIconButton(String icon, String tooltipText, String color) {
        Button button = new Button(icon);
        
        button.setPrefSize(70, 70);
        button.setMinSize(70, 70);
        button.setMaxSize(70, 70);
        
        button.setStyle(String.format(
            "-fx-background-color: %s;" +
            "-fx-background-radius: 35;" +
            "-fx-border-radius: 35;" +
            "-fx-font-size: 28px;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);",
            color
        ));
        
        // 添加工具提示
        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(button, tooltip);
        
        button.setOnMouseEntered(e -> {
            button.setStyle(String.format(
                "-fx-background-color: derive(%s, -10%%);" +
                "-fx-background-radius: 35;" +
                "-fx-border-radius: 35;" +
                "-fx-font-size: 28px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3);" +
                "-fx-scale-x: 1.05;" +
                "-fx-scale-y: 1.05;",
                color
            ));
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(String.format(
                "-fx-background-color: %s;" +
                "-fx-background-radius: 35;" +
                "-fx-border-radius: 35;" +
                "-fx-font-size: 28px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);",
                color
            ));
        });
        
        button.setOnMousePressed(e -> {
            button.setStyle(String.format(
                "-fx-background-color: derive(%s, -20%%);" +
                "-fx-background-radius: 35;" +
                "-fx-border-radius: 35;" +
                "-fx-font-size: 28px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 3, 0, 0, 1);" +
                "-fx-scale-x: 0.95;" +
                "-fx-scale-y: 0.95;",
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