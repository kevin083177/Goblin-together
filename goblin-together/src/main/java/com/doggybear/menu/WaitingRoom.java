package com.doggybear.menu;

import com.doggybear.network.NetworkManager;
import com.doggybear.network.NetworkMessage;
import com.doggybear.network.NetworkGameManager;
import com.doggybear.event.NetworkGameStartEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class WaitingRoom extends StackPane {
    private NetworkManager networkManager;
    private Text roomInfoText;
    private Text playersText;
    private Button startGameBtn;
    private Button leaveRoomBtn;
    
    public WaitingRoom(NetworkManager networkManager) {
        this.networkManager = networkManager;
        initUI();
    }
    
    private void initUI() {
        var background = new Rectangle(1080, 720, Color.color(0.1, 0.1, 0.3, 0.9));
        
        var mainContainer = new VBox(40);
        mainContainer.setAlignment(Pos.CENTER);
        
        var title = new Text("房間名稱");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 42px; -fx-font-weight: bold;");
        
        roomInfoText = new Text();
        roomInfoText.setFill(Color.LIGHTBLUE);
        roomInfoText.setStyle("-fx-font-size: 20px;");
        updateRoomInfo();
        
        playersText = new Text("玩家列表:\n• 玩家1 (主機)\n• 玩家2");
        playersText.setFill(Color.LIGHTGREEN);
        playersText.setStyle("-fx-font-size: 18px;");
        
        var gameInfo = new Text("遊戲說明:\n" +
                    "• 兩位玩家將化身為哥布林\n" +
                    "• 玩家1: WASD移動, 空格跳躍\n" +
                    "• 玩家2: 方向鍵移動, Enter跳躍\n" +
                    "• 目標: 逃避岩漿，存活越久越好！");
        gameInfo.setFill(Color.YELLOW);
        gameInfo.setStyle("-fx-font-size: 16px;");
        
        var buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        
        // 開始遊戲 (主機可見)
        startGameBtn = createButton("開始遊戲");
        startGameBtn.setOnAction(e -> startGame());
        startGameBtn.setVisible(networkManager.isHost());
        
        // 離開房間
        leaveRoomBtn = createButton("離開房間");
        leaveRoomBtn.setOnAction(e -> leaveRoom());
        
        buttonContainer.getChildren().addAll(startGameBtn, leaveRoomBtn);
        
        mainContainer.getChildren().addAll(
            title, roomInfoText, playersText, gameInfo, buttonContainer
        );
        
        getChildren().addAll(background, mainContainer);
    }
    
    private Button createButton(String text) {
        var button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(50);
        button.setStyle("-fx-font-size: 18px; -fx-background-color: #2a5a2a; -fx-text-fill: white;");
        
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-font-size: 18px; -fx-background-color: #3a7a3a; -fx-text-fill: white;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-font-size: 18px; -fx-background-color: #2a5a2a; -fx-text-fill: white;");
        });
        
        return button;
    }
    
    private void updateRoomInfo() {
        String info = "房間: " + (networkManager.getRoomName() != null ? networkManager.getRoomName() : "网络房间");
        if (networkManager.isHost()) {
            info += "\nIP地址: " + networkManager.getHostIP();
            info += "\n身份: 主機";
        } else {
            info += "\n連接到: " + networkManager.getHostIP();
            info += "\n身份: 客戶端";
        }
        roomInfoText.setText(info);
    }
    
    private void startGame() {
        if (networkManager.isHost()) {
            NetworkMessage startMessage = new NetworkMessage(NetworkMessage.MessageType.GAME_START);
            networkManager.sendMessage(startMessage);
            
            // 主機啟動遊戲
            startNetworkGame();
        }
    }
    
    private void startNetworkGame() {
        NetworkGameManager.getInstance().setNetworkGameMode(true);
        
        // System.out.println("開啟網路合作!");
        
        com.almasb.fxgl.dsl.FXGL.getGameController().startNewGame();
    }
    
    private void leaveRoom() {
        networkManager.disconnect();
        
        System.out.println("離開房間");

        // 返回主菜單或其他界面
        com.almasb.fxgl.dsl.FXGL.getGameController().gotoMainMenu();
    }
    
    public StackPane getContentRoot() {
        return this;
    }
}