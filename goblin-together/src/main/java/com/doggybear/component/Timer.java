package com.doggybear.component;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.doggybear.ui.FontManager;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Timer extends Component {
    
    private double elapsedTime = 0;
    private Text timerText;
    private boolean isActive = false;
    
    public Timer() {
        timerText = new Text("時間: 0 秒");
        timerText.setFill(Color.WHITE);
        
        // 使用FontManager載入字型
        try {
            Font customFont = FontManager.getFont(FontManager.FontType.BOLD);
            timerText.setFont(customFont);
        } catch (Exception e) {
            System.err.println("無法載入計時器字型，使用預設字型: " + e.getMessage());
            timerText.setFont(Font.font(24));
        }
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
        timerText.setText("時間: " + seconds + " 秒");
    }
    
    /**
     * 開始計時
     */
    public void start() {
        isActive = true;
        System.out.println("Timer: 開始計時");
    }
    
    /**
     * 停止計時
     */
    public void stop() {
        isActive = false;
        System.out.println("Timer: 停止計時");
    }
    
    /**
     * 重置計時器並開始計時
     */
    public void reset() {
        elapsedTime = 0;
        isActive = true;
        timerText.setText("時間: 0 秒");
        System.out.println("Timer: 重置並開始計時");
    }
    
    /**
     * 重置計時器但不開始計時 - 新增方法
     */
    public void resetWithoutStart() {
        elapsedTime = 0;
        isActive = false;
        timerText.setText("時間: 0秒");
        System.out.println("Timer: 重置但不開始計時");
    }
    
    /**
     * 只重置時間，保持當前啟動狀態 - 新增方法
     */
    public void resetTime() {
        elapsedTime = 0;
        updateDisplay();
        System.out.println("Timer: 僅重置時間");
    }
    
    /**
     * 獲取當前經過的秒數
     * @return 經過的秒數
     */
    public int getElapsedSeconds() {
        return (int) elapsedTime;
    }
    
    public double getElapsedTime() {
        return elapsedTime;
    }
    
    /**
     * 檢查計時器是否正在運行
     * @return 是否正在計時
     */
    public boolean isActive() {
        return isActive;
    }
    
    @Override
    public void onRemoved() {
        if (timerText != null) {
            FXGL.getGameScene().removeUINode(timerText);
        }
    }

    public void setElapsedSeconds(int seconds) {
        this.elapsedTime = seconds;
        updateDisplay();
    }

    private void updateDisplay() {
        int seconds = (int) elapsedTime;
        timerText.setText("時間: " + seconds + " 秒");
    }
}