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
                System.out.println("客戶端嘗試連接到: " + hostIP + ":" + port);
                clientSocket = new Socket(hostIP, port);
                
                // 設置Socket選項
                clientSocket.setTcpNoDelay(true);
                clientSocket.setKeepAlive(true);
                clientSocket.setSoTimeout(0);
                
                System.out.println("客戶端Socket創建成功:");
                System.out.println("  本地地址: " + clientSocket.getLocalAddress());
                System.out.println("  本地端口: " + clientSocket.getLocalPort());
                System.out.println("  遠端地址: " + clientSocket.getRemoteSocketAddress());
                System.out.println("  連接狀態: " + clientSocket.isConnected());
                
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
                System.err.println("客戶端連接異常: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    private void handleServerMessage(String message) {
        System.out.println("WaitingRoom收到服務器消息: " + message);
        
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
            System.out.println("WaitingRoom: 客戶端收到遊戲開始信號");
            closeAndStartGame();
        } else if ("INTRO_COMPLETE".equals(message)) {
            // 收到開場動畫完成消息，說明遊戲已經開始了
            System.out.println("WaitingRoom: 收到INTRO_COMPLETE，遊戲已在主機端開始");
            if (!gameStarting) {
                System.out.println("WaitingRoom: 客戶端開始進入遊戲");
                closeAndStartGame();
            } else {
                System.out.println("WaitingRoom: 遊戲已在啟動中，忽略INTRO_COMPLETE");
            }
        } else if (message.startsWith("STATE:")) {
            // 收到遊戲狀態消息，說明遊戲已經在運行
            System.out.println("WaitingRoom: 收到STATE消息，遊戲已在運行");
            if (!gameStarting) {
                System.out.println("WaitingRoom: 強制啟動遊戲");
                closeAndStartGame();
            } else {
                System.out.println("WaitingRoom: 遊戲已在啟動中，忽略STATE");
            }
        } else {
            System.out.println("WaitingRoom: 未知服務器消息: " + message);
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
        
        Platform.runLater(() -> {
            try {
                System.out.println("關閉等待室 subscene");
                this.onDestroy();
                FXGL.getSceneService().popSubScene();
                
                // 延遲啟動遊戲，確保場景切換完成
                javafx.animation.Timeline timeline = new javafx.animation.Timeline();
                timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(200),
                    e -> {
                        try {
                            System.out.println("啟動新遊戲");
                            FXGL.getGameController().startNewGame();
                            System.out.println("遊戲啟動成功");
                        } catch (Exception ex) {
                            System.err.println("遊戲啟動失敗: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                ));
                timeline.play();
                
            } catch (Exception e) {
                System.err.println("場景切換失敗: " + e.getMessage());
                e.printStackTrace();
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
                System.out.println("=== 主機開始遊戲流程 ===");
                System.out.println("gameServer連接狀態: " + gameServer.isConnected());
                
                // 設置主機的遊戲數據
                Socket clientSocket = gameServer.getClientSocket();
                if (clientSocket != null && !clientSocket.isClosed()) {
                    System.out.println("設置主機Socket到GameData");
                    GameData.setSocket(clientSocket, true);
                } else {
                    System.err.println("警告：客戶端Socket無效");
                }
                
                // 發送遊戲開始消息給客戶端
                System.out.println("發送GAME_START消息給客戶端");
                gameServer.sendGameStart();
                
                // 等待一小段時間確保消息發送
                Thread.sleep(100);
                
                // 主機也開始遊戲
                System.out.println("主機開始自己的遊戲");
                closeAndStartGame();
            } else {
                System.err.println("遊戲啟動條件不滿足:");
                System.err.println("  isHost: " + isHost);
                System.err.println("  gameServer: " + (gameServer != null ? "存在" : "null"));
                System.err.println("  connected: " + (gameServer != null ? gameServer.isConnected() : "N/A"));
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
            System.out.println("WaitingRoom 遊戲啟動中，保持Socket連接");
            running = false;
            // 不關閉socket，讓遊戲繼續使用
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