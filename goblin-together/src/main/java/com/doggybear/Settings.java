package com.doggybear;

import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.doggybear.menu.MainMenu;

public class Settings {
    
    // 遊戲視窗設定
    public static final String GAME_TITLE = "Goblin Together";
    public static final int GAME_WIDTH = 1080;
    public static final int GAME_HEIGHT = 720;
    
    // 遊戲世界設定
    public static final int WORLD_HEIGHT = 10000;
    public static final double GRAVITY = 1000;
    
    // 岩漿預設設定
    public static final double DEFAULT_LAVA_HEIGHT = 100;
    public static final double DEFAULT_LAVA_RISE_SPEED = 5;
    public static final double LAVA_Y_POSITION = 1000;
    public static final double LAVA_UPDATE_INTERVAL = 0.5;
    
    // 玩家設定
    public static final int PLAYER_WIDTH = 50;
    public static final int PLAYER_HEIGHT = 50;
    
    /**
     * 初始化遊戲設定
     */
    public static void initSettings(GameSettings settings) {
        settings.setTitle(GAME_TITLE);
        settings.setWidth(GAME_WIDTH);
        settings.setHeight(GAME_HEIGHT);
        settings.setMainMenuEnabled(true);
        
        // 設定自定義選單
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new MainMenu();
            }
        });
        
        // 可以在這裡添加更多設定
        // settings.setVersion("1.0");
        // settings.setIntroEnabled(false);
        // settings.setDeveloperMenuEnabled(true);
        // settings.setFullScreenAllowed(true);
        // settings.setFullScreenFromStart(false);
    }
    
    /**
     * 獲取遊戲設定資訊
     */
    public static String getSettingsInfo() {
        return String.format("遊戲: %s | 解析度: %dx%d", 
            GAME_TITLE, GAME_WIDTH, GAME_HEIGHT);
    }
}