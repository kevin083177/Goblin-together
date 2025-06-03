package com.doggybear.menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.doggybear.GameData;
import com.doggybear.network.GameServer;
import com.doggybear.ui.FontManager;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WaitingRoom extends FXGLMenu {
    
    private final boolean isHost;
    private final String hostIP;
    private GameServer gameServer;
    private Button startGameBtn;
    private Text statusText;
    private Text connectedIPText;
    private boolean playerConnected = false;
    private Font font;
    private int port = 12345;
    private ImageView goblin1;
    private ImageView goblin2;
    private ColorAdjust dimEffect = new ColorAdjust(0, 0, -0.5, 0);
    private volatile boolean running = true;
    private volatile boolean gameStarting = false;

    // 網絡相關
    private Socket clientSocket;
    private PrintWriter clientOut;
    private BufferedReader clientIn;

    public WaitingRoom() {
        this(true, null);
    }

    public WaitingRoom(boolean isHost, String hostIP) {
        super(MenuType.MAIN_MENU);
        this.isHost = isHost;
        this.hostIP = hostIP;
        font = FontManager.getFont(FontManager.FontType.REGULAR, 24);
        
        // 保存主機信息到GameData
        if (isHost) {
            // 主機保存自己的IP
            GameData.setHostIP(getServerIP());
        } else {
            // 客戶端保存主機IP
            GameData.setHostIP(hostIP);
        }

        // 初始化 UI 元素
        Image bgImage = FXGL.image("waiting_background.jpg");
        BackgroundImage background = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true)
        );
        
        StackPane root = new StackPane();
        root.setBackground(new Background(background));
        root.setPrefSize(getAppWidth(), getAppHeight());
        
        VBox mainContainer = new VBox(50);
        mainContainer.setAlignment(Pos.CENTER);
        
        // 初始化 connectedIPText
        connectedIPText = new Text("");
        connectedIPText.setFont(font);
        connectedIPText.setFill(Color.LIGHTGRAY);
        
        HBox topArea = createTopArea();
        statusText = new Text("");
        statusText.setFont(font);
        statusText.setFill(Color.WHITE);
        
        HBox bottomButtons = createBottomButtons();
        
        mainContainer.getChildren().addAll(topArea, statusText, connectedIPText, bottomButtons);
        root.getChildren().add(mainContainer);
        getContentRoot().getChildren().add(root);
        
        Platform.runLater(() -> {
            if (isHost) {
                startServer();
            } else {
                statusText.setText("連接中...");
                statusText.setFill(Color.YELLOW);
                connectToServer();
            }
        });
    }
    
   private HBox createTopArea() {
        HBox topArea = new HBox(100);
        topArea.setAlignment(Pos.CENTER);
        
        goblin1 = new ImageView(FXGL.image("goblin.png"));
        goblin1.setFitWidth(150);
        goblin1.setFitHeight(150);
        goblin1.setPreserveRatio(true);
        
        goblin2 = new ImageView(FXGL.image("goblin2.png"));
        goblin2.setFitWidth(150);
        goblin2.setFitHeight(150);
        goblin2.setPreserveRatio(true);
        
        if (isHost) {
            goblin2.setEffect(dimEffect);
        } else {
            goblin1.setEffect(dimEffect);
        }
        
        topArea.getChildren().addAll(goblin1, goblin2);
        return topArea;
    }
    
    private void connectToServer() {
        new Thread(() -> {
            try {
                clientSocket = new Socket(hostIP, port);
                clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
                Platform.runLater(() -> {
                    statusText.setText("連接中...");
                    statusText.setFill(Color.YELLOW);
                });

                // 監聽主機指令
                String message;
                while (running && (message = clientIn.readLine()) != null) {
                    final String finalMessage = message;
                    Platform.runLater(() -> handleServerMessage(finalMessage));
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusText.setText("連接失敗: " + e.getMessage());
                    statusText.setFill(Color.RED);
                });
            }
        }).start();
    }
    
    private void handleServerMessage(String message) {
        System.out.println("收到服務器消息: " + message);
        
        if ("CONNECTION_CONFIRMED".equals(message)) {
            // 連接確認，設置遊戲數據但不開始遊戲
            GameData.setSocket(clientSocket, false);
            statusText.setText("已連接到主機，等待開始...");
            statusText.setFill(Color.GREEN);
            connectedIPText.setText("主機 IP: " + hostIP);
            updateConnectionStatus(true);
            
            // 發送客戶端就緒消息
            if (clientOut != null) {
                clientOut.println("CLIENT_READY");
            }
        } else if ("GAME_START".equals(message)) {
            // 客戶端收到遊戲開始命令
            System.out.println("客戶端收到遊戲開始信號");
            closeAndStartGame();
        }
    }
    
    /**
     * 關閉等待室並開始遊戲
     */
    private void closeAndStartGame() {
        if (gameStarting) {
            System.out.println("遊戲已在啟動中，忽略重複請求");
            return;
        }
        
        System.out.println("開始關閉等待室並啟動遊戲 - isHost: " + isHost);
        gameStarting = true;
        
        // 停止所有活動
        running = false;
        stopServer();
        
        if (isHost) {
            // 主機端需要特殊處理，因為它有更複雜的場景狀態
            handleHostGameStart();
        } else {
            // 客戶端使用原有的簡單處理
            handleClientGameStart();
        }
    }
    
    /**
     * 處理主機端遊戲啟動
     */
    private void handleHostGameStart() {
        System.out.println("主機端遊戲啟動流程");
        
        Platform.runLater(() -> {
            try {
                // 確保 Socket 已正確設置
                if (gameServer != null && gameServer.isConnected()) {
                    Socket clientSocket = gameServer.getClientSocket();
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        // 重新設置 Socket，確保它是最新的
                        GameData.setSocket(clientSocket, true);
                        System.out.println("主機端：Socket 已重新設置");
                    }
                }
                
                // 主機端需要更激進的場景清理
                System.out.println("主機端：開始激進清理所有場景");
                
                // 第一輪：清理當前 subscene
                this.onDestroy();
                
                // 使用 Timeline 進行分步清理，避免阻塞
                javafx.animation.Timeline cleanupTimeline = new javafx.animation.Timeline();
                
                for (int i = 0; i < 10; i++) {
                    final int index = i;
                    cleanupTimeline.getKeyFrames().add(
                        new javafx.animation.KeyFrame(
                            javafx.util.Duration.millis(i * 50), // 每50ms執行一次
                            e -> {
                                try {
                                    FXGL.getSceneService().popSubScene();
                                    System.out.println("主機端清理 subscene " + (index + 1));
                                } catch (Exception ex) {
                                    System.out.println("主機端清理 subscene " + (index + 1) + " 完成或失敗: " + ex.getMessage());
                                }
                            }
                        )
                    );
                }
                
                // 清理完成後啟動遊戲
                cleanupTimeline.setOnFinished(e -> {
                    System.out.println("主機端：場景清理完成，準備啟動遊戲");
                    
                    // 再次確認 Socket 狀態
                    if (GameData.getSocket() != null && !GameData.getSocket().isClosed()) {
                        System.out.println("主機端：Socket 狀態正常，開始啟動遊戲");
                        scheduleHostGameStart(0);
                    } else {
                        System.err.println("主機端：Socket 無效，無法啟動遊戲");
                        forceStartGame();
                    }
                });
                
                cleanupTimeline.play();
                
            } catch (Exception e) {
                System.err.println("主機端場景清理失敗: " + e.getMessage());
                e.printStackTrace();
                forceStartGame();
            }
        });
    }
    
    /**
     * 處理客戶端遊戲啟動
     */
    private void handleClientGameStart() {
        System.out.println("客戶端遊戲啟動流程");
        
        Platform.runLater(() -> {
            try {
                // 客戶端使用簡單的清理
                System.out.println("客戶端：關閉等待室 subscene");
                this.onDestroy();
                FXGL.getSceneService().popSubScene();
                
                // 客戶端使用較短的延遲（因為它工作得很好）
                scheduleGameStart(0);
                
            } catch (Exception e) {
                System.err.println("客戶端場景切換失敗: " + e.getMessage());
                e.printStackTrace();
                forceStartGame();
            }
        });
    }
    
    /**
     * 主機端專用的遊戲啟動調度
     */
    private void scheduleHostGameStart(int attempt) {
        if (attempt > 10) { // 主機端允許更多嘗試
            System.err.println("主機端多次嘗試啟動遊戲失敗，強制啟動");
            forceStartGame();
            return;
        }
        
        // 主機端使用更長的延遲間隔
        int delay = 500 + (attempt * 300); // 500ms, 800ms, 1100ms...
        
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(delay),
            e -> {
                try {
                    System.out.println("主機端嘗試啟動遊戲 (第 " + (attempt + 1) + " 次，延遲 " + delay + "ms)");
                    
                    // 啟動前再次清理
                    try {
                        for (int i = 0; i < 3; i++) {
                            FXGL.getSceneService().popSubScene();
                        }
                    } catch (Exception cleanEx) {
                        System.out.println("啟動前清理：" + cleanEx.getMessage());
                    }
                    
                    FXGL.getGameController().startNewGame();
                    System.out.println("主機端遊戲啟動成功");
                } catch (Exception ex) {
                    System.err.println("主機端遊戲啟動失敗: " + ex.getMessage());
                    
                    if (ex.getMessage() != null && ex.getMessage().contains("subscenes are present")) {
                        System.out.println("主機端檢測到 subscene 問題，重試...");
                        
                        // 在重試前再次大力清理
                        Platform.runLater(() -> {
                            try {
                                for (int i = 0; i < 10; i++) {
                                    try {
                                        FXGL.getSceneService().popSubScene();
                                        Thread.sleep(20);
                                    } catch (Exception cleanupEx) {
                                        // 忽略清理錯誤
                                    }
                                }
                            } catch (Exception clearEx) {
                                System.err.println("重試前清理失敗: " + clearEx.getMessage());
                            }
                        });
                        
                        scheduleHostGameStart(attempt + 1);
                    } else {
                        forceStartGame();
                    }
                }
            }
        ));
        timeline.play();
    }
    
    /**
     * 分階段啟動遊戲（客戶端專用）
     */
    private void scheduleGameStart(int attempt) {
        if (attempt > 5) {
            System.err.println("客戶端多次嘗試啟動遊戲失敗，強制啟動");
            forceStartGame();
            return;
        }
        
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(200 * (attempt + 1)), // 客戶端使用原有的遞增延遲
            e -> {
                try {
                    System.out.println("客戶端嘗試啟動遊戲 (第 " + (attempt + 1) + " 次)");
                    FXGL.getGameController().startNewGame();
                    System.out.println("客戶端遊戲啟動成功");
                } catch (Exception ex) {
                    System.err.println("客戶端遊戲啟動失敗: " + ex.getMessage());
                    
                    if (ex.getMessage() != null && ex.getMessage().contains("subscenes are present")) {
                        System.out.println("客戶端檢測到 subscene 問題，重試...");
                        scheduleGameStart(attempt + 1);
                    } else {
                        forceStartGame();
                    }
                }
            }
        ));
        timeline.play();
    }
    
    /**
     * 強制啟動遊戲
     */
    private void forceStartGame() {
        System.err.println("強制啟動遊戲流程");
        
        Platform.runLater(() -> {
            try {
                // 多次嘗試清理場景
                for (int i = 0; i < 3; i++) {
                    try {
                        FXGL.getSceneService().popSubScene();
                    } catch (Exception e) {
                        // 忽略錯誤，繼續嘗試
                    }
                }
                
                // 最後嘗試啟動遊戲
                FXGL.getGameController().startNewGame();
                
            } catch (Exception e) {
                System.err.println("強制啟動也失敗，回到主選單: " + e.getMessage());
                GameData.reset();
                gameStarting = false;
                FXGL.getGameController().gotoMainMenu();
            }
        });
    }

    private void updateConnectionStatus(boolean connected) {
        if (connected) {
            // 移除變暗效果
            if (isHost) {
                goblin2.setEffect(null);
            } else {
                goblin1.setEffect(null);
            }
        } else {
            // 應用變暗效果
            if (isHost) {
                goblin2.setEffect(dimEffect);
            } else {
                goblin1.setEffect(dimEffect);
            }
        }
    }

    private HBox createBottomButtons() {
        HBox buttonBox = new HBox(50);
        buttonBox.setAlignment(Pos.CENTER);
        
        startGameBtn = createStyledButton("開始遊戲", "#4CAF50");
        startGameBtn.setPrefSize(180, 60);
        
        if (isHost) {
            startGameBtn.setDisable(true);
            startGameBtn.setOpacity(0.5);
            startGameBtn.setOnAction(e -> {
                if (playerConnected) {
                    startGame();
                }
            });
        } else {
            // 客戶端隱藏開始遊戲按鈕
            startGameBtn.setVisible(false);
            startGameBtn.setManaged(false);
        }
        
        // 返回主畫面按鈕
        Button backBtn = createStyledButton("返回主畫面", "#F44336");
        backBtn.setPrefSize(180, 60);
        backBtn.setOnAction(e -> {
            System.out.println("返回主畫面按鈕被點擊");
            running = false;
            stopServer();
            
            if (!isHost && clientSocket != null) {
                try {
                    if (clientOut != null) clientOut.close();
                    if (clientIn != null) clientIn.close();
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            
            // 重置遊戲數據
            GameData.reset();
            
            // 關閉等待室並返回主選單
            FXGL.getSceneService().popSubScene();
        });
        
        if (isHost) {
            buttonBox.getChildren().addAll(startGameBtn, backBtn);
        } else {
            buttonBox.getChildren().add(backBtn);
        }
        
        return buttonBox;
    }
    
    private void startServer() {
        try {
            gameServer = new GameServer(port);
            
            gameServer.setClientDisconnectedCallback(() -> {
                Platform.runLater(() -> resetServerState());
            });
            
            new Thread(() -> {
                try {
                    gameServer.start(() -> {
                        Platform.runLater(() -> {
                            playerConnected = true;
                            statusText.setText("IP: " + gameServer.getServerIP());
                            
                            String clientIP = gameServer.getClientSocket().getInetAddress().getHostAddress();
                            connectedIPText.setText("已連接: " + clientIP);
                            connectedIPText.setFill(Color.YELLOW);
                            
                            startGameBtn.setDisable(false);
                            startGameBtn.setOpacity(1.0);
                            
                            updateConnectionStatus(true);
                        });
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        statusText.setText("伺服器啟動失敗: " + e.getMessage());
                        statusText.setFill(Color.RED);
                    });
                }
            }).start();
            
            String serverIP = gameServer.getServerIP();
            statusText.setText("IP: " + serverIP);
            
        } catch (IOException e) {
            statusText.setText("無法創建伺服器: " + e.getMessage());
            statusText.setFill(Color.RED);
        }
    }
    
    private void resetServerState() {
        playerConnected = false;
        statusText.setText("IP: " + gameServer.getServerIP());
        connectedIPText.setText("等待玩家連接...");
        connectedIPText.setFill(Color.LIGHTGRAY);
        startGameBtn.setDisable(true);
        startGameBtn.setOpacity(0.5);
        updateConnectionStatus(false);
        
        if (gameServer != null) {
            try {
                gameServer.stop();
                gameServer = new GameServer(port);
                gameServer.setClientDisconnectedCallback(() -> {
                    Platform.runLater(() -> resetServerState());
                });
                
                new Thread(() -> {
                    try {
                        gameServer.start(() -> {
                            Platform.runLater(() -> {
                                playerConnected = true;
                                statusText.setText("IP: " + gameServer.getServerIP());
                                
                                String clientIP = gameServer.getClientSocket().getInetAddress().getHostAddress();
                                connectedIPText.setText("已連接: " + clientIP);
                                connectedIPText.setFill(Color.YELLOW);
                                
                                startGameBtn.setDisable(false);
                                startGameBtn.setOpacity(1.0);
                                
                                updateConnectionStatus(true);
                            });
                        });
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            statusText.setText("伺服器啟動失敗: " + e.getMessage());
                            statusText.setFill(Color.RED);
                        });
                    }
                }).start();
                
            } catch (IOException e) {
                statusText.setText("重啟伺服器失敗: " + e.getMessage());
                statusText.setFill(Color.RED);
            }
        }
    }
    
    private void stopServer() {
        if (gameServer != null && !gameStarting) {
            // 只有在不是啟動遊戲的情況下才完全停止服務器
            gameServer.stop();
            gameServer = null;
        }
    }
    
    private void startGame() {
        try {
            if (isHost && gameServer != null && gameServer.isConnected()) {
                System.out.println("主機開始遊戲流程");
                
                // 設置主機的遊戲數據
                GameData.setSocket(gameServer.getClientSocket(), true);
                
                // 發送遊戲開始消息給客戶端
                gameServer.sendGameStart();
                
                // 主機也開始遊戲
                closeAndStartGame();
            }
        } catch (Exception e) {
            System.err.println("開始遊戲失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        
        button.setStyle(String.format(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: %s;" +
            "-fx-background-radius: 30;" +
            "-fx-border-radius: 30;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 2);",
            color
        ));
        
        button.setOnMouseEntered(e -> {
            if (!button.isDisabled()) {
                button.setStyle(String.format(
                    "-fx-font-size: 20px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-color: derive(%s, 20%%);" +
                    "-fx-background-radius: 30;" +
                    "-fx-border-radius: 30;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 3);",
                    color
                ));
            }
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(String.format(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: %s;" +
                "-fx-background-radius: 30;" +
                "-fx-border-radius: 30;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 2);",
                color
            ));
        });
        
        return button;
    }
    
    @Override
    public void onDestroy() {
        // 只有在遊戲未啟動時才執行完整清理
        if (!gameStarting) {
            System.out.println("WaitingRoom 正在完整銷毀");
            running = false;
            stopServer();
            
            if (clientSocket != null) {
                try {
                    if (clientOut != null) {
                        clientOut.close();
                    }
                    if (clientIn != null) {
                        clientIn.close();
                    }
                    clientSocket.close();
                } catch (IOException e) {
                    // 忽略關閉時的錯誤
                }
            }
        } else {
            System.out.println("WaitingRoom 遊戲啟動中，跳過完整清理");
        }
        
        super.onDestroy();
    }
    
    private String getServerIP() {
        try {
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (java.net.UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}