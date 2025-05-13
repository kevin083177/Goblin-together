package com.doggybear.component;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * 計時器組件，用於計算玩家存活時間
 */
public class Timer extends Component {
    
    private double elapsedTime = 0;
    private Text timerText;
    private boolean isActive = true;
    
    public Timer() {
        timerText = new Text("時間: 0秒");
        timerText.setFill(Color.WHITE);
        timerText.setStroke(Color.BLACK);
        timerText.setStrokeWidth(1);
        timerText.setFont(Font.font(24));
    }
    
    @Override
    public void onAdded() {
        // 初始化文字位置，固定在螢幕右上角
        timerText.setTranslateX(FXGL.getAppWidth() - 150);
        timerText.setTranslateY(50);
        
        // 添加到UI層
        FXGL.getGameScene().addUINode(timerText);
    }
    
    @Override
    public void onUpdate(double tpf) {
        if (!isActive) return;
        
        // 累計經過的時間
        elapsedTime += tpf;
        
        // 更新顯示文字，只顯示整數秒
        int seconds = (int) elapsedTime;
        timerText.setText("時間: " + seconds + "秒");
    }
    
    /**
     * 停止計時
     */
    public void stop() {
        isActive = false;
    }
    
    /**
     * 重置計時器
     */
    public void reset() {
        elapsedTime = 0;
        isActive = true;
        timerText.setText("時間: 0秒");
    }
    
    /**
     * 獲取當前經過的秒數
     * @return 經過的秒數
     */
    public int getElapsedSeconds() {
        return (int) elapsedTime;
    }
    
    /**
     * 當組件被移除時，同時移除UI元素
     */
    @Override
    public void onRemoved() {
        FXGL.getGameScene().removeUINode(timerText);
    }
}