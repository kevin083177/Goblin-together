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
import java.net.SocketException;

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
    private volatile boolean running = true; // 控制线程运行

    public WaitingRoom() {
        this(true, null);
    }

    public WaitingRoom(boolean isHost, String hostIP) {
        super(MenuType.MAIN_MENU);
        this.isHost = isHost;
        this.hostIP = hostIP;
        font = FontManager.getFont(FontManager.FontType.REGULAR, 24);

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
                // 显示连接中状态
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
        
        Button middleBtn = createStyledButton("房間資訊", "#9C27B0");
        middleBtn.setPrefSize(200, 80);
        
        goblin2 = new ImageView(FXGL.image("goblin2.png"));
        goblin2.setFitWidth(150);
        goblin2.setFitHeight(150);
        goblin2.setPreserveRatio(true);
        
        if (isHost) {
            goblin2.setEffect(dimEffect);
        } else {
            // 客户端立即显示连接效果
            updateConnectionStatus(true);
        }
        
        topArea.getChildren().addAll(goblin1, middleBtn, goblin2);
        return topArea;
    }
    
    private void connectToServer() {
        new Thread(() -> {
            try (Socket socket = new Socket(hostIP, port)) {
                GameData.setSocket(socket, false);
                
                Platform.runLater(() -> {
                    statusText.setText("已連接到主機");
                    statusText.setFill(Color.GREEN);
                    connectedIPText.setText("主機 IP: " + hostIP);
                    updateConnectionStatus(true);
                });

                // 监听主机指令，而不是直接开始游戏
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while (running && (message = in.readLine()) != null) {
                    if ("GAME_START".equals(message)) {
                        Platform.runLater(() -> {
                            FXGL.getGameController().startNewGame();
                        });
                        break;
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusText.setText("連接失敗: " + e.getMessage());
                    statusText.setFill(Color.RED);
                });
            }
        }).start();
    }

    private void updateConnectionStatus(boolean connected) {
        if (connected) {
            // 移除变暗效果
            goblin1.setEffect(null);
            goblin2.setEffect(null);
        } else {
            // 应用变暗效果
            if (isHost) {
                goblin2.setEffect(dimEffect);
            }
        }
    }

    private HBox createBottomButtons() {
        HBox buttonBox = new HBox(50);
        buttonBox.setAlignment(Pos.CENTER);
        
        startGameBtn = createStyledButton("開始遊戲", "#4CAF50");
        startGameBtn.setPrefSize(180, 60);
        startGameBtn.setDisable(true);
        startGameBtn.setOpacity(0.5);
        startGameBtn.setOnAction(e -> {
            if (playerConnected) {
                startGame();
            }
        });
        
        // 返回主畫面按鈕
        Button backBtn = createStyledButton("返回主畫面", "#F44336");
        backBtn.setPrefSize(180, 60);
        backBtn.setOnAction(e -> {
            // 停止所有线程和服务器
            running = false;
            stopServer();
            
            // 如果是客户端，关闭socket连接
            if (!isHost && gameServer != null && gameServer.isConnected()) {
                try {
                    gameServer.getClientSocket().close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            
            fireExitToMainMenu();
        });
        
        buttonBox.getChildren().addAll(startGameBtn, backBtn);
        
        return buttonBox;
    }
    
    private void startServer() {
        try {
            gameServer = new GameServer(port);
            
            // 设置客户端断开回调
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
                            
                            // 更新图片效果
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
    
    // 重置服务器状态的方法
    private void resetServerState() {
        playerConnected = false;
        statusText.setText("IP: " + gameServer.getServerIP());
        connectedIPText.setText("等待玩家連接...");
        connectedIPText.setFill(Color.LIGHTGRAY);
        startGameBtn.setDisable(true);
        startGameBtn.setOpacity(0.5);
        updateConnectionStatus(false); // 重置哥布林图片状态
        
        // 准备接受新的连接
        if (gameServer != null) {
            try {
                // 创建新的服务器实例，而不是重用旧的
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
                                
                                // 更新图片效果
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
        if (gameServer != null) {
            gameServer.stop();
        }
    }
    
    private void startGame() {
        try {
            if (isHost) {
                PrintWriter out = new PrintWriter(gameServer.getClientSocket().getOutputStream(), true);
                out.println("GAME_START"); // 通知客户机开始游戏
            }
            // 主机自己开始游戏
            FXGL.getGameController().startNewGame();
        } catch (IOException e) {
            System.err.println("開始遊戲失敗: " + e.getMessage());
        }
    }
    
    private Button createStyledButton(String text, String colorHex) {
        Button button = new Button(text);
        
        button.setStyle(String.format(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: %s;" +
            "-fx-background-radius: 30;" +
            "-fx-border-radius: 30;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 2);",
            colorHex
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
                    colorHex
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
                colorHex
            ));
        });
        
        return button;
    }
    
    @Override
    public void onDestroy() {
        running = false; // 停止所有线程
        stopServer();
        super.onDestroy();
    }
}