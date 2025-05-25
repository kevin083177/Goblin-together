package com.doggybear.menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.doggybear.network.NetworkManager;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class RoomBrowser extends FXGLMenu {
    private NetworkManager networkManager;
    private TextField roomNameField;
    private TextField ipAddressField;
    private TextField portField;
    private Button createRoomBtn;
    private Button joinRoomBtn;
    private Button backBtn;
    private Text statusText;
    
    public RoomBrowser() {
        super(MenuType.MAIN_MENU);
        networkManager = NetworkManager.getInstance();
        initUI();
    }
    
    private void initUI() {
        var root = new StackPane();
        
        // 背景
        var background = new Rectangle(1080, 720, Color.color(0.1, 0.1, 0.2, 0.9));
        
        // 主容器
        var mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setMaxWidth(600);
        
        // 标题
        var title = new Text("網路雙人合作");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        
        // 创建房间区域
        var createRoomSection = createRoomSection();
        
        // 加入房间区域
        var joinRoomSection = createJoinRoomSection();
        
        // 状态显示
        statusText = new Text("");
        statusText.setFill(Color.YELLOW);
        statusText.setStyle("-fx-font-size: 16px;");
        
        // 返回按钮
        backBtn = createButton("返回首頁");
        backBtn.setOnAction(e -> {
            networkManager.disconnect();
            fireExit();
        });
        
        mainContainer.getChildren().addAll(
            title, createRoomSection, joinRoomSection, statusText, backBtn
        );
        
        root.getChildren().addAll(background, mainContainer);
        getContentRoot().getChildren().add(root);
    }
    
    private VBox createRoomSection() {
        var section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        
        var sectionTitle = new Text("創建房間");
        sectionTitle.setFill(Color.LIGHTBLUE);
        sectionTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // 房間名稱輸入
        var roomNameContainer = new HBox(10);
        roomNameContainer.setAlignment(Pos.CENTER);
        var roomNameLabel = new Text("房間名稱: ");
        roomNameLabel.setFill(Color.WHITE);
        roomNameField = new TextField("我的房間");
        roomNameField.setPrefWidth(200);
        roomNameContainer.getChildren().addAll(roomNameLabel, roomNameField);
        
        // 端口輸入
        var portContainer = new HBox(10);
        portContainer.setAlignment(Pos.CENTER);
        var portLabel = new Text("端口: ");
        portLabel.setFill(Color.WHITE);
        portField = new TextField("12345");
        portField.setPrefWidth(100);
        portContainer.getChildren().addAll(portLabel, portField);
        
        // 創建房間按鈕
        createRoomBtn = createButton("創建房間");
        createRoomBtn.setOnAction(e -> createRoom());
        
        section.getChildren().addAll(sectionTitle, roomNameContainer, portContainer, createRoomBtn);
        
        // 分隔線
        var separator = new Rectangle(400, 2, Color.GRAY);
        section.getChildren().add(separator);
        
        return section;
    }
    
    private VBox createJoinRoomSection() {
        var section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        
        var sectionTitle = new Text("加入房間");
        sectionTitle.setFill(Color.LIGHTGREEN);
        sectionTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // IP位址輸入
        var ipContainer = new HBox(10);
        ipContainer.setAlignment(Pos.CENTER);
        var ipLabel = new Text("主機IP: ");
        ipLabel.setFill(Color.WHITE);
        ipAddressField = new TextField("");
        ipAddressField.setPrefWidth(200);
        ipContainer.getChildren().addAll(ipLabel, ipAddressField);
        
        // 端口輸入
        var portContainer = new HBox(10);
        portContainer.setAlignment(Pos.CENTER);
        var portLabel = new Text("端口: ");
        portLabel.setFill(Color.WHITE);
        var joinPortField = new TextField("12345");
        joinPortField.setPrefWidth(100);
        portContainer.getChildren().addAll(portLabel, joinPortField);
        
        // 加入房間按鈕
        joinRoomBtn = createButton("加入房間");
        joinRoomBtn.setOnAction(e -> joinRoom(ipAddressField.getText(), 
                                             Integer.parseInt(joinPortField.getText())));
        
        section.getChildren().addAll(sectionTitle, ipContainer, portContainer, joinRoomBtn);
        
        return section;
    }
    
    private Button createButton(String text) {
        var button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(40);
        button.setStyle("-fx-font-size: 16px; -fx-background-color: #4a4a4a; -fx-text-fill: white;");
        
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-font-size: 16px; -fx-background-color: #6a6a6a; -fx-text-fill: white;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-font-size: 16px; -fx-background-color: #4a4a4a; -fx-text-fill: white;");
        });
        
        return button;
    }
    
    private void createRoom() {
        String roomName = roomNameField.getText().trim();
        if (roomName.isEmpty()) {
            updateStatus("請輸入房間名稱");
            return;
        }
        
        int port;
        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            updateStatus("請輸入有效端口");
            return;
        }
        
        updateStatus("正在創建房間...");
        disableButtons();
        
        if (networkManager.createRoom(roomName, port)) {
            updateStatus("房間已創建 正在等待玩家進入...\nIP地址: " + networkManager.getHostIP() + ":" + port);
            
            // 设置网络监听器
            networkManager.setNetworkListener(new com.doggybear.network.NetworkListener() {
                @Override
                public void onConnected() {
                    javafx.application.Platform.runLater(() -> {
                        updateStatus("玩家已連線 請開始遊戲...");
                        showWaitingRoom();
                    });
                }
                
                @Override
                public void onDisconnected() {
                    javafx.application.Platform.runLater(() -> {
                        updateStatus("連線中斷");
                        enableButtons();
                    });
                }
                
                @Override
                public void onMessageReceived(com.doggybear.network.NetworkMessage message) {
                    // 處理網路訊息
                }
            });
        } else {
            updateStatus("創建房間失敗");
            enableButtons();
        }
    }
    
    private void joinRoom(String ipAddress, int port) {
        if (ipAddress.trim().isEmpty()) {
            updateStatus("請輸入主機IP地址");
            return;
        }
        
        updateStatus("正在連接至房間...");
        disableButtons();
        
        if (networkManager.joinRoom(ipAddress, port)) {
            updateStatus("成功連接至房間");
            
            // 设置网络监听器
            networkManager.setNetworkListener(new com.doggybear.network.NetworkListener() {
                @Override
                public void onConnected() {
                    javafx.application.Platform.runLater(() -> {
                        showWaitingRoom();
                    });
                }
                
                @Override
                public void onDisconnected() {
                    javafx.application.Platform.runLater(() -> {
                        updateStatus("連線中斷");
                        enableButtons();
                    });
                }
                
                @Override
                public void onMessageReceived(com.doggybear.network.NetworkMessage message) {
                    if (message.getType() == com.doggybear.network.NetworkMessage.MessageType.GAME_START) {
                        javafx.application.Platform.runLater(() -> {
                            // 用戶端收到遊戲開始訊息，設定網路遊戲模式並啟動遊戲
                            com.doggybear.network.NetworkGameManager.getInstance().setNetworkGameMode(true);
                            startNetworkGame();
                        });
                    }
                }
            });
        } else {
            updateStatus("連線中斷");
            enableButtons();
        }
    }
    
    private void showWaitingRoom() {
        // 切換到等待畫面
        getContentRoot().getChildren().clear();
        var waitingRoom = new WaitingRoom(networkManager);
        getContentRoot().getChildren().add(waitingRoom.getContentRoot());
    }
    
    private void startNetworkGame() {
        // 開始網路合作
        com.almasb.fxgl.dsl.FXGL.getGameController().startNewGame();
    }
    
    private void updateStatus(String message) {
        statusText.setText(message);
    }
    
    private void disableButtons() {
        createRoomBtn.setDisable(true);
        joinRoomBtn.setDisable(true);
    }
    
    private void enableButtons() {
        createRoomBtn.setDisable(false);
        joinRoomBtn.setDisable(false);
    }
}