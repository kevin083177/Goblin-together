package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.dsl.FXGL;
import com.doggybear.ui.GameFinish;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

public class FinishCircle extends Component {
    private double radius;
    private Node view;
    private boolean isActivated = false;
    
    private double gameStartTime;
    
    public interface FinishCallback {
        void onGameFinish(double totalTime);
    }
    
    private FinishCallback finishCallback;
    
    public FinishCircle(double radius) {
        this.radius = radius;
        initializeView();
    }
    
    public FinishCircle(double radius, FinishCallback callback) {
        this.radius = radius;
        this.finishCallback = callback;
        initializeView();
    }
    
    @Override
    public void onAdded() {
        if (view == null) {
            initializeView();
        }
    }
    
    private void initializeView() {
        try {
            Texture texture = FXGL.getAssetLoader().loadTexture("end.png");
            
            double diameter = radius * 2;
            texture.setFitWidth(diameter);
            texture.setFitHeight(diameter);
            texture.setPreserveRatio(true);
            
            view = texture;
        } catch (Exception e) {
            createDefaultView();
        }
    }
    
    private void createDefaultView() {
        Circle circle = new Circle(radius);
        circle.setFill(Color.GOLD);
        circle.setStroke(Color.DARKGOLDENROD);
        circle.setStrokeWidth(3);
        
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(0.5), 
                e -> circle.setFill(Color.GOLD)),
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1.0), 
                e -> circle.setFill(Color.YELLOW))
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
        
        view = circle;
    }
    
    public void onPlayerFinish() {
        if (!isActivated) {
            isActivated = true;
            
            double currentTime = FXGL.getGameTimer().getNow();
            double totalTime = currentTime - gameStartTime;
            
            addFinishEffect();
            
            if (finishCallback != null) {
                finishCallback.onGameFinish(totalTime);
            } else {
                showDefaultCompletionScreen(totalTime);
            }
            
            System.out.println("Game completed in: " + formatTime(totalTime));
        }
    }
    
    /**
     * 設定遊戲開始時間
     */
    public void setGameStartTime(double startTime) {
        this.gameStartTime = startTime;
    }
    
    /**
     * 設定完成回調
     */
    public void setFinishCallback(FinishCallback callback) {
        this.finishCallback = callback;
    }
    
    /**
     * 格式化時間顯示
     */
    private String formatTime(double totalSeconds) {
        int minutes = (int) (totalSeconds / 60);
        double seconds = totalSeconds % 60;
        
        if (minutes > 0) {
            return String.format("%d:%06.3f", minutes, seconds);
        } else {
            return String.format("%.3f", seconds);
        }
    }
    
    /**
     * 預設的完成畫面顯示
     */
    private void showDefaultCompletionScreen(double totalTime) {
        GameFinish gameFinish = new GameFinish(totalTime, new GameFinish.GameFinishCallback() {
            @Override
            public void onRestart() {
                // 重新開始
                FXGL.getGameController().startNewGame();
            }
            
            @Override
            public void onBackToMenu() {
                // 回到主選單
                FXGL.getGameController().gotoMainMenu();
            }
        });
        
        gameFinish.show();
    }
    
    /**
     * 添加完成特效
     */
    private void addFinishEffect() {
        FXGL.animationBuilder()
            .duration(javafx.util.Duration.seconds(0.5))
            .scale(entity)
            .from(new javafx.geometry.Point2D(1.0, 1.0))
            .to(new javafx.geometry.Point2D(1.2, 1.2))
            .buildAndPlay();
        
        FXGL.getGameTimer().runOnceAfter(() -> {
            FXGL.animationBuilder()
                .duration(javafx.util.Duration.seconds(0.5))
                .scale(entity)
                .from(new javafx.geometry.Point2D(1.2, 1.2))
                .to(new javafx.geometry.Point2D(1.0, 1.0))
                .buildAndPlay();
        }, javafx.util.Duration.seconds(0.5));
        
    }
    
    public boolean isActivated() {
        return isActivated;
    }
    
    /**
     * 重置終點狀態（用於重新開始遊戲）
     */
    public void reset() {
        isActivated = false;
    }
    
    public double getRadius() {
        return radius;
    }
    
    public Node getViewNode() {
        return view;
    }
}