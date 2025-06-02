package com.doggybear.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameOver {
    
    private Rectangle overlay;
    private StackPane modalPane;
    private int survivalTime;
    
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
        overlay = new Rectangle(getAppWidth(), getAppHeight(), Color.color(0, 0, 0, 0.6));
        
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        
        Text gameOverText = new Text("遊戲結束!");
        try {
            Font titleFont = FontManager.getFont(FontManager.FontType.REGULAR, 42);
            gameOverText.setFont(titleFont);
        } catch (Exception e) {
            gameOverText.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        }
        gameOverText.setFill(Color.web("#FF6B35"));
        
        VBox scoreCard = createScoreCard();
        
        HBox buttonContainer = createButtonContainer();
        
        mainContainer.getChildren().addAll(gameOverText, scoreCard, buttonContainer);
        
        Rectangle cardBg = new Rectangle(400, 350);
        cardBg.setFill(Color.WHITE);
        cardBg.setArcWidth(20);
        cardBg.setArcHeight(20);
        cardBg.setEffect(new javafx.scene.effect.DropShadow(15, Color.color(0, 0, 0, 0.3)));
        
        modalPane = new StackPane();
        modalPane.getChildren().addAll(cardBg, mainContainer);
        modalPane.setTranslateX(getAppWidth() / 2 - 200);
        modalPane.setTranslateY(getAppHeight() / 2 - 175);
    }
    
    /**
     * 創建分數卡片
     */
    private VBox createScoreCard() {
        VBox scoreCard = new VBox(15);
        scoreCard.setAlignment(Pos.CENTER);
        
        Text scoreLabel = new Text("存活時間");
        try {
            Font labelFont = FontManager.getFont(FontManager.FontType.REGULAR, 18);
            scoreLabel.setFont(labelFont);
        } catch (Exception e) {
            scoreLabel.setFont(Font.font("Arial", 18));
        }
        scoreLabel.setFill(Color.web("#666666"));
        
        Text scoreNumber = new Text(survivalTime + "秒");
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
    
    /**
     * 創建按鈕容器
     */
    private HBox createButtonContainer() {
        HBox buttonContainer = new HBox(40);
        buttonContainer.setAlignment(Pos.CENTER);
        
        Button homeBtn = createIconButton("🏠", "#4CAF50");
        homeBtn.setOnAction(e -> {
            if (callback != null) {
                hide();
                callback.onBackToMenu();
            }
        });
        
        Button restartBtn = createIconButton("🔄", "#FF9800");
        restartBtn.setOnAction(e -> {
            if (callback != null) {
                hide();
                callback.onRestart();
            }
        });
        
        buttonContainer.getChildren().addAll(restartBtn, homeBtn);
        
        return buttonContainer;
    }
    
    /**
     * 創建圓形圖標按鈕
     */
    private Button createIconButton(String icon, String color) {
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
    }
}