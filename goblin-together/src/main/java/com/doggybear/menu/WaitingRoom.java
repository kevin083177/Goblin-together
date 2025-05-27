package com.doggybear.menu;

import com.doggybear.network.NetworkManager;
import com.doggybear.network.NetworkMessage;
import com.doggybear.network.NetworkGameManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class WaitingRoom extends StackPane {
    private NetworkManager networkManager;
    private Text roomInfoText;
    private Text statusText;
    private Button startGameBtn;
    private Button swapColorsBtn;
    private Button leaveRoomBtn;
    
    // 玩家顯示區域
    private VBox player1Box;
    private VBox player2Box;
    private ImageView goblin1Preview;
    private ImageView goblin2Preview;
    private Text player1StatusText;
    private Text player2StatusText;
    
    // 顏色狀態
    private boolean player1IsBlue = false; // false = 原色, true = 藍色調
    private boolean player2Connected = false;
    
    public WaitingRoom(NetworkManager networkManager) {
        this.networkManager = networkManager;
        initUI();
        updatePlayerStatus();
        
        // 設置網路監聽器
        setupNetworkListener();
    }
    
    private void initUI() {
        var background = new Rectangle(1080, 720, Color.color(0.05, 0.1, 0.2, 0.95));
        
        var mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setMaxWidth(800);
        
        // 房間標題 - 使用房間名稱
        var title = new Text(networkManager.getRoomName() != null ? networkManager.getRoomName() : "未命名的房間");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", 36));
        title.setEffect(createGlowEffect(Color.LIGHTBLUE));
        
        // 房間資訊
        roomInfoText = new Text();
        roomInfoText.setFill(Color.LIGHTBLUE);
        roomInfoText.setFont(Font.font("Arial", 18));
        
        // 玩家預覽區域
        var playersContainer = createPlayersContainer();
        
        // 狀態文字
        statusText = new Text("等待玩家加入...");
        statusText.setFill(Color.YELLOW);
        statusText.setFont(Font.font("Arial", 16));
        
        // 控制按鈕
        var buttonsContainer = createButtonsContainer();
        
        mainContainer.getChildren().addAll(
            title, roomInfoText, playersContainer, statusText, buttonsContainer
        );
        
        getChildren().addAll(background, mainContainer);
    }
    
    private HBox createPlayersContainer() {
        var container = new HBox(50);
        container.setAlignment(Pos.CENTER);
        
        // 玩家1方框
        player1Box = createPlayerBox("玩家 1", true);
        
        // 交換按鈕
        swapColorsBtn = createSwapButton();
        
        // 玩家2方框
        player2Box = createPlayerBox("玩家 2", false);
        
        container.getChildren().addAll(player1Box, swapColorsBtn, player2Box);
        return container;
    }
    
    private VBox createPlayerBox(String playerName, boolean isPlayer1) {
        var box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(250);
        box.setPrefHeight(300);
        
        // 背景框
        var background = new Rectangle(240, 290, Color.color(0.2, 0.3, 0.4, 0.7));
        background.setArcWidth(20);
        background.setArcHeight(20);
        background.setStroke(Color.LIGHTBLUE);
        background.setStrokeWidth(2);
        
        // 玩家名稱
        var nameText = new Text(playerName);
        nameText.setFill(Color.WHITE);
        nameText.setFont(Font.font("Arial", 20));
        
        // 狀態文字
        Text statusText = new Text();
        statusText.setFill(Color.LIGHTGREEN);
        statusText.setFont(Font.font("Arial", 14));
        
        if (isPlayer1) {
            player1StatusText = statusText;
            statusText.setText(networkManager.isHost() ? "主機 (你)" : "玩家");
        } else {
            player2StatusText = statusText;
            statusText.setText("等待中...");
        }
        
        // 哥布林預覽
        ImageView goblinPreview = createGoblinPreview(isPlayer1);
        if (isPlayer1) {
            goblin1Preview = goblinPreview;
        } else {
            goblin2Preview = goblinPreview;
        }
        
        var content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(nameText, statusText, goblinPreview);
        
        var stackPane = new StackPane(background, content);
        box.getChildren().add(stackPane);
        
        return box;
    }
    
    private ImageView createGoblinPreview(boolean isPlayer1) {
        try {
            // 玩家1使用 goblin.png，玩家2使用 goblin2.png
            String imagePath = isPlayer1 ? "/assets/textures/goblin.png" : "/assets/textures/goblin2.png";
            Image goblinImage = new Image(getClass().getResourceAsStream(imagePath));
            ImageView imageView = new ImageView(goblinImage);
            
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);
            
            // 添加發光效果
            DropShadow glow = new DropShadow();
            glow.setColor(isPlayer1 ? Color.LIGHTBLUE : Color.LIGHTGREEN);
            glow.setRadius(15);
            imageView.setEffect(glow);
            
            return imageView;
        } catch (Exception e) {
            // 如果圖片載入失敗，創建一個佔位符
            System.err.println("無法載入哥布林圖片: " + e.getMessage());
            System.err.println("嘗試載入的路徑: " + (isPlayer1 ? "goblin.png" : "goblin2.png"));
            
            ImageView placeholder = new ImageView();
            Rectangle rect = new Rectangle(80, 80, isPlayer1 ? Color.LIGHTBLUE : Color.LIGHTGREEN);
            rect.setArcWidth(10);
            rect.setArcHeight(10);
            
            placeholder.setFitWidth(80);
            placeholder.setFitHeight(80);
            
            return placeholder;
        }
    }
    
    private Button createSwapButton() {
        var button = new Button("⇄");
        button.setPrefWidth(60);
        button.setPrefHeight(60);
        button.setFont(Font.font("Arial", 24));
        button.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: white; -fx-background-radius: 30;");
        
        // hover 效果
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: #6a6a6a; -fx-text-fill: white; -fx-background-radius: 30;");
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.1);
            scale.setToY(1.1);
            scale.play();
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: white; -fx-background-radius: 30;");
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        // 交換功能
        button.setOnAction(e -> swapGoblinColors());
        
        // 初始時禁用，等兩個玩家都連接後才啟用
        button.setDisable(true);
        
        return button;
    }
    
    private VBox createButtonsContainer() {
        var container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        
        // 開始遊戲按鈕（只有主機可見）
        startGameBtn = createStyledButton("開始遊戲", Color.GREEN);
        startGameBtn.setOnAction(e -> startGame());
        startGameBtn.setVisible(networkManager.isHost());
        startGameBtn.setDisable(true); // 初始禁用，等兩個玩家都連接後啟用
        
        // 離開房間按鈕
        leaveRoomBtn = createStyledButton("離開房間", Color.ORANGE);
        leaveRoomBtn.setOnAction(e -> leaveRoom());
        
        container.getChildren().addAll(startGameBtn, leaveRoomBtn);
        return container;
    }
    
    private Button createStyledButton(String text, Color color) {
        var button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(50);
        button.setFont(Font.font("Arial", 18));
        
        String colorHex = String.format("#%02X%02X%02X", 
            (int)(color.getRed() * 255), 
            (int)(color.getGreen() * 255), 
            (int)(color.getBlue() * 255));
        
        button.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-background-radius: 10;");
        
        button.setOnMouseEntered(e -> {
            Color brighter = color.brighter();
            String brightHex = String.format("#%02X%02X%02X", 
                (int)(brighter.getRed() * 255), 
                (int)(brighter.getGreen() * 255), 
                (int)(brighter.getBlue() * 255));
            button.setStyle("-fx-background-color: " + brightHex + "; -fx-text-fill: white; -fx-background-radius: 10;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-background-radius: 10;");
        });
        
        return button;
    }
    
    private DropShadow createGlowEffect(Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(10);
        glow.setSpread(0.3);
        return glow;
    }
       
    private void updatePlayerStatus() {
        if (player2Connected) {
            // 更新玩家狀態文字
            if (networkManager.isHost()) {
                // 主機端：玩家1是自己，玩家2是客戶端
                player1StatusText.setText("主機 (你)");
                player2StatusText.setText("玩家已連線");
            } else {
                // 客戶端：玩家1是主機，玩家2是自己
                player1StatusText.setText("玩家");
                player2StatusText.setText("主機 (你)");
            }
            player2StatusText.setFill(Color.LIGHTGREEN);
            
            // 顯示第二隻哥布林
            if (goblin2Preview != null) {
                FadeTransition fade = new FadeTransition(Duration.millis(500), goblin2Preview);
                fade.setFromValue(0.3);
                fade.setToValue(1.0);
                fade.play();
            }
            
            // 啟用交換按鈕和開始按鈕
            swapColorsBtn.setDisable(false);
            if (networkManager.isHost()) {
                startGameBtn.setDisable(false);
            }
            
            // 顯示連線資訊
            String connectionInfo = "所有玩家已就緒！可以開始遊戲";
            if (networkManager.isHost()) {
                connectionInfo += "\n對方已從客戶端連線";
            } else {
                connectionInfo += "\n已連線至主機: " + networkManager.getHostIP();
            }
            
            statusText.setText(connectionInfo);
            statusText.setFill(Color.LIGHTGREEN);
            
        } else {
            // 重置狀態
            if (networkManager.isHost()) {
                player1StatusText.setText("主機 (你)");
                player2StatusText.setText("等待中...");
            } else {
                player1StatusText.setText("玩家");
                player2StatusText.setText("等待中...");
            }
            player2StatusText.setFill(Color.GRAY);
            
            // 隱藏第二隻哥布林
            if (goblin2Preview != null) {
                goblin2Preview.setOpacity(0.3);
            }
            
            // 禁用按鈕
            swapColorsBtn.setDisable(true);
            startGameBtn.setDisable(true);
            
            if (networkManager.isHost()) {
                statusText.setText("房間 IP： " + networkManager.getHostIP());
            } else {
                statusText.setText("正在連線到主機...");
            }
            statusText.setFill(Color.YELLOW);
        }
    }
    
    private void swapGoblinColors() {
        if (!player2Connected) return;
        
        // 交換顏色狀態
        player1IsBlue = !player1IsBlue;
        
        // 更新哥布林顏色
        updateGoblinColors();
        
        // 發送顏色交換消息給對方
        if (networkManager.isConnected()) {
            NetworkMessage swapMessage = new NetworkMessage(NetworkMessage.MessageType.ROOM_INFO)
                .put("action", "swap_colors")
                .put("player1IsBlue", player1IsBlue);
            networkManager.sendMessage(swapMessage);
        }
        
        // 添加交換動畫
        animateSwap();
    }
    
    private void updateGoblinColors() {
        if (goblin1Preview != null) {
            DropShadow glow1 = new DropShadow();
            if (player1IsBlue) {
                glow1.setColor(Color.BLUE);
            } else {
                glow1.setColor(Color.LIGHTBLUE);
            }
            glow1.setRadius(15);
            goblin1Preview.setEffect(glow1);
        }
        
        if (goblin2Preview != null) {
            DropShadow glow2 = new DropShadow();
            if (player1IsBlue) {
                // 玩家1變藍色，玩家2恢復綠色
                glow2.setColor(Color.LIGHTGREEN);
            } else {
                // 玩家1原色，玩家2變藍色
                glow2.setColor(Color.BLUE);
            }
            glow2.setRadius(15);
            goblin2Preview.setEffect(glow2);
        }
    }
    
    private void animateSwap() {
        // 創建交換動畫
        ScaleTransition scale1 = new ScaleTransition(Duration.millis(200), goblin1Preview);
        scale1.setToX(1.2);
        scale1.setToY(1.2);
        scale1.setAutoReverse(true);
        scale1.setCycleCount(2);
        
        ScaleTransition scale2 = new ScaleTransition(Duration.millis(200), goblin2Preview);
        scale2.setToX(1.2);
        scale2.setToY(1.2);
        scale2.setAutoReverse(true);
        scale2.setCycleCount(2);
        
        scale1.play();
        scale2.play();
    }
    
    private void setupNetworkListener() {
        networkManager.setNetworkListener(new com.doggybear.network.NetworkListener() {
            @Override
            public void onConnected() {
                javafx.application.Platform.runLater(() -> {
                    player2Connected = true;
                    updatePlayerStatus();
                });
            }
            
            @Override
            public void onDisconnected() {
                javafx.application.Platform.runLater(() -> {
                    player2Connected = false;
                    updatePlayerStatus();
                });
            }
            
            @Override
            public void onMessageReceived(NetworkMessage message) {
                javafx.application.Platform.runLater(() -> {
                    handleNetworkMessage(message);
                });
            }
        });
        
        // 如果不是主機，表示已經有連接
        if (!networkManager.isHost()) {
            player2Connected = true;
            updatePlayerStatus();
        }
    }
    
    private void handleNetworkMessage(NetworkMessage message) {
        switch (message.getType()) {
            case ROOM_INFO:
                String action = message.get("action");
                if ("swap_colors".equals(action)) {
                    player1IsBlue = message.get("player1IsBlue");
                    updateGoblinColors();
                    animateSwap();
                }
                break;
            case GAME_START:
                startNetworkGame();
                break;
        }
    }
    
    private void startGame() {
        if (networkManager.isHost() && player2Connected) {
            // 發送遊戲開始消息
            NetworkMessage startMessage = new NetworkMessage(NetworkMessage.MessageType.GAME_START)
                .put("player1IsBlue", player1IsBlue);
            networkManager.sendMessage(startMessage);
            
            // 主機啟動遊戲
            startNetworkGame();
        }
    }
    
    private void startNetworkGame() {
        NetworkGameManager networkGameManager = NetworkGameManager.getInstance();
        networkGameManager.setNetworkGameMode(true);
        
        // 設置玩家顏色偏好
        networkGameManager.setPlayerColorPreference(player1IsBlue);
        
        System.out.println("開始網路遊戲，玩家1藍色: " + player1IsBlue);
        
        com.almasb.fxgl.dsl.FXGL.getGameController().startNewGame();
    }
    
    private void leaveRoom() {
        System.out.println("返回房間選擇頁面");
        
        // 斷開網路連接
        networkManager.disconnect();
        
        // 返回到 RoomBrowser 頁面
        getContentRoot().getChildren().clear();
        var roomBrowser = new RoomBrowser();
        getContentRoot().getChildren().add(roomBrowser.getContentRoot());
    }
    
    public StackPane getContentRoot() {
        return this;
    }
}