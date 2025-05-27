package com.doggybear.component;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.doggybear.network.NetworkGameManager;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * 計時器組件，支持網路遊戲的時間同步
 */
public class Timer extends Component {
    
    private double elapsedTime = 0;
    private Text timerText;
    private boolean isActive = true;
    private int networkTime = 0; // 網路同步的時間
    
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
        
        NetworkGameManager networkManager = NetworkGameManager.getInstance();
        
        if (networkManager.isNetworkGame()) {
            if (networkManager.getNetworkManager().isHost()) {
                // 主機端：正常計時
                elapsedTime += tpf;
                int seconds = (int) elapsedTime;
                timerText.setText("時間: " + seconds + "秒 (主機)");
            } else {
                // 客戶端：顯示同步的時間
                timerText.setText("時間: " + networkTime + "秒 (客戶端)");
            }
        } else {
            // 本地遊戲：正常計時
            elapsedTime += tpf;
            int seconds = (int) elapsedTime;
            timerText.setText("時間: " + seconds + "秒");
        }
    }
    
    /**
     * 設置網路同步的時間（客戶端使用）
     */
    public void setNetworkTime(int time) {
        this.networkTime = time;
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
        networkTime = 0;
        isActive = true;
        timerText.setText("時間: 0秒");
    }
    
    /**
     * 獲取當前經過的秒數
     * @return 經過的秒數
     */
    public int getElapsedSeconds() {
        NetworkGameManager networkManager = NetworkGameManager.getInstance();
        
        if (networkManager.isNetworkGame() && !networkManager.getNetworkManager().isHost()) {
            // 客戶端返回同步的時間
            return networkTime;
        } else {
            // 主機端或本地遊戲返回實際計時
            return (int) elapsedTime;
        }
    }
    
    /**
     * 當組件被移除時，同時移除UI元素
     */
    @Override
    public void onRemoved() {
        FXGL.getGameScene().removeUINode(timerText);
    }
}