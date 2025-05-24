package com.doggybear.menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class MainMenu extends FXGLMenu {
    
    public MainMenu() {
        super(MenuType.MAIN_MENU);
        
        // 創建一個容器來保存背景與按鈕
        var root = new StackPane();
        
        // 創建一個封面圖片（這裡使用矩形作為示例）
        // 實際使用時，您需要替換為真實的圖片路徑：
        // var coverImage = new ImageView(new Image("file:assets/textures/cover.png"));
        // coverImage.setFitWidth(720);
        // coverImage.setFitHeight(480);
        
        // 這裡用矩形作為圖片的替代品，在實際應用中請替換為上面的代碼
        var coverPlaceholder = new Rectangle(720, 480, Color.DARKGREEN);
        coverPlaceholder.setStroke(Color.GOLD);
        coverPlaceholder.setStrokeWidth(2);
        
        // 創建按鈕面板
        var buttonPanel = new VBox(20);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setTranslateX(100);
        
        // 創建標題
        var title = new Text("Goblin Together");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");
        
        // 創建子標題，表明支持兩人遊玩
        var subtitle = new Text("雙人本地合作遊戲");
        subtitle.setFill(Color.LIGHTGRAY);
        subtitle.setStyle("-fx-font-size: 24px;");
        
        // 創建按鈕
        var btnLocalCoop = createButton("開始遊戲");
        var btnControls = createButton("操作說明");
        var btnExit = createButton("退出");
        
        // 設置按鈕事件
        btnLocalCoop.setOnAction(e -> {
            fireNewGame();
        });
        
        btnControls.setOnAction(e -> {
            // 顯示控制說明
            showControls();
        });
        
        btnExit.setOnAction(e -> {
            fireExit();
        });
        
        // 添加按鈕到面板
        buttonPanel.getChildren().addAll(btnLocalCoop, btnControls, btnExit);
        
        // 創建佈局
        var layout = new HBox(50);
        layout.setAlignment(Pos.CENTER);
        
        // 創建左側的容器（包含圖片和標題）
        var leftPane = new VBox(20);
        leftPane.setAlignment(Pos.CENTER);
        leftPane.getChildren().addAll(title, subtitle, coverPlaceholder);
        
        // 將左側容器和按鈕面板添加到水平佈局中
        layout.getChildren().addAll(leftPane, buttonPanel);
        
        // 添加佈局到根容器
        root.getChildren().add(layout);
        
        getContentRoot().getChildren().add(root);
    }
    
    private void showControls() {
        var controlsPane = new StackPane();
        
        var bg = new Rectangle(500, 300, Color.color(0.2, 0.2, 0.2, 0.9));
        bg.setArcWidth(20);
        bg.setArcHeight(20);
        
        var content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        
        var title = new Text("操作說明");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        var player1Controls = new Text("玩家 1: 使用 A/D 鍵移動，空白鍵跳躍");
        player1Controls.setFill(Color.WHITE);
        
        var player2Controls = new Text("玩家 2: 使用方向鍵移動，Enter鍵跳躍");
        player2Controls.setFill(Color.WHITE);
        
        var objective = new Text("目標: 逃避不斷上升的岩漿，存活越久越好！");
        objective.setFill(Color.WHITE);
        
        var closeBtn = createButton("關閉");
        closeBtn.setPrefWidth(100);
        closeBtn.setOnAction(e -> {
            getContentRoot().getChildren().remove(controlsPane);
        });
        
        content.getChildren().addAll(title, player1Controls, player2Controls, objective, closeBtn);
        
        controlsPane.getChildren().addAll(bg, content);
        controlsPane.setTranslateX(getAppWidth() / 2 - 250);
        controlsPane.setTranslateY(getAppHeight() / 2 - 150);
        
        getContentRoot().getChildren().add(controlsPane);
    }
    
    private Button createButton(String text) {
        var button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(50);
        button.setStyle("-fx-font-size: 20px; -fx-background-color: #4a4a4a; -fx-text-fill: white;");
        
        // 鼠標懸停效果
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-font-size: 20px; -fx-background-color: #6a6a6a; -fx-text-fill: white;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-font-size: 20px; -fx-background-color: #4a4a4a; -fx-text-fill: white;");
        });
        
        return button;
    }
}