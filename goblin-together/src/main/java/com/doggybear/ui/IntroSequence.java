package com.doggybear.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.doggybear.ui.FontManager;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * 遊戲開場動畫
 */
public class IntroSequence {
    
    private Text instructionText;
    private Text subText;
    private Rectangle textBackground;
    private StackPane textContainer; // 改為 StackPane 來正確疊加背景和文字
    private Timeline introTimeline;
    private boolean isPlaying = false;
    
    public interface IntroCompleteCallback {
        void onIntroComplete();
    }
    
    /**
     * 播放開場動畫序列
     * @param finishX 終點X座標
     * @param finishY 終點Y座標
     * @param playerX 玩家起始X座標
     * @param playerY 玩家起始Y座標
     * @param callback 完成回調
     */
    public void playIntroSequence(double finishX, double finishY, 
                                 double playerX, double playerY, 
                                 IntroCompleteCallback callback) {
        if (isPlaying) return;
        
        isPlaying = true;
        
        createInstructionText();
        
        introTimeline = new Timeline();
        
        KeyFrame moveToFinish = new KeyFrame(Duration.ZERO, e -> {
            // System.out.println("開場動畫開始：移動鏡頭到終點");
            moveCameraToTarget(finishX, finishY);
        });
        
        KeyFrame showText = new KeyFrame(Duration.seconds(1.5), e -> {
            // System.out.println("顯示指示文字");
            showInstructionText();
        });
        
        KeyFrame hideText = new KeyFrame(Duration.seconds(4.0), e -> {
            // System.out.println("隱藏指示文字");
            hideInstructionText();
        });
        
        KeyFrame moveToPlayer = new KeyFrame(Duration.seconds(4.5), e -> {
            // System.out.println("鏡頭回到玩家位置");
            moveCameraToTarget(playerX, playerY);
        });
        
        KeyFrame endIntro = new KeyFrame(Duration.seconds(6.0), e -> {
            // System.out.println("開場動畫結束，開始遊戲");
            cleanup();
            isPlaying = false;
            if (callback != null) {
                callback.onIntroComplete();
            }
        });
        
        introTimeline.getKeyFrames().addAll(
            moveToFinish, showText, hideText, moveToPlayer, endIntro
        );
        
        introTimeline.play();
    }
    
    /**
     * 創建指示文字UI
     */
    private void createInstructionText() {
        instructionText = new Text("在岩漿淹沒前達到終點");
        subText = new Text("岩漿每30秒速度會加快");
        
        try {
            Font customFont = FontManager.getFont(FontManager.FontType.REGULAR, 36);
            Font customSubFont = FontManager.getFont(FontManager.FontType.REGULAR, 26);
            instructionText.setFont(customFont);
            subText.setFont(customSubFont);
        } catch (Exception e) {
            System.err.println("無法載入自定義字型，使用預設字型: " + e.getMessage());
            instructionText.setFont(Font.font("Arial", 36));
            subText.setFont(Font.font("Arial", 26));
        }
        
        instructionText.setFill(Color.WHITE);
        instructionText.setStroke(Color.WHITE);
        
        subText.setFill(Color.WHITE);

        // 創建背景
        textBackground = new Rectangle(650, 200);
        textBackground.setFill(Color.color(0, 0, 0, 0.7));
        textBackground.setArcWidth(15);
        textBackground.setArcHeight(15);
        
        // 創建文字容器（VBox用來垂直排列兩行文字）
        VBox textBox = new VBox(10);
        textBox.setAlignment(Pos.CENTER);
        textBox.getChildren().addAll(instructionText, subText);
        
        // 使用 StackPane 來疊加背景和文字
        textContainer = new StackPane();
        textContainer.getChildren().addAll(textBackground, textBox); // 背景先加入（底層），然後是文字（上層）
        textContainer.setAlignment(Pos.CENTER);
        
        textContainer.setOpacity(0);
        textContainer.setVisible(false);
        
        textContainer.setTranslateX(FXGL.getAppWidth() / 2 - 325);
        textContainer.setTranslateY(FXGL.getAppHeight() / 2 - 60);
    }
    
    /**
     * 顯示指示文字（淡入效果）
     */
    private void showInstructionText() {
        if (textContainer == null) return;
        
        // 添加到遊戲場景
        FXGL.getGameScene().addUINode(textContainer);
        textContainer.setVisible(true);
        
        // 淡入動畫
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), textContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    /**
     * 隱藏指示文字（淡出效果）
     */
    private void hideInstructionText() {
        if (textContainer == null) return;
        
        // 淡出動畫
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), textContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (textContainer != null) {
                FXGL.getGameScene().removeUINode(textContainer);
                textContainer.setVisible(false);
            }
        });
        fadeOut.play();
    }
    
    /**
     * 移動鏡頭到目標位置
     */
    private void moveCameraToTarget(double targetX, double targetY) {
        double viewX = Math.max(0, targetX - FXGL.getAppWidth() / 2);
        double viewY = Math.max(-10000, Math.min(0, targetY - FXGL.getAppHeight() / 2));
        
        viewX = 0;
        
        Timeline cameraAnimation = new Timeline();
        
        final double startX = FXGL.getGameScene().getViewport().getX();
        final double startY = FXGL.getGameScene().getViewport().getY();
        final double endX = viewX;
        final double endY = viewY;
        
        for (int i = 0; i <= 60; i++) {
            double progress = i / 60.0;
            double currentX = startX + (endX - startX) * progress;
            double currentY = startY + (endY - startY) * progress;
            
            KeyFrame frame = new KeyFrame(
                Duration.millis(i * 16.5), 
                e -> {
                    FXGL.getGameScene().getViewport().setX(currentX);
                    FXGL.getGameScene().getViewport().setY(currentY);
                }
            );
            cameraAnimation.getKeyFrames().add(frame);
        }
        
        cameraAnimation.play();
        
        // System.out.println(String.format("鏡頭移動到: (%.1f, %.1f)", endX, endY));
    }
    
    public void stopIntro() {
        if (introTimeline != null) {
            introTimeline.stop();
        }
        cleanup();
        isPlaying = false;
        // System.out.println("開場動畫被強制停止");
    }
    
    /**
     * 清理UI元素
     */
    private void cleanup() {
        if (textContainer != null) {
            try {
                FXGL.getGameScene().removeUINode(textContainer);
            } catch (Exception e) {
            }
            textContainer = null;
        }
        instructionText = null;
        subText = null;
        textBackground = null;
        
        if (introTimeline != null) {
            introTimeline.stop();
            introTimeline = null;
        }
    }
    
    /**
     * 檢查開場動畫是否正在播放
     */
    public boolean isPlaying() {
        return isPlaying;
    }
}