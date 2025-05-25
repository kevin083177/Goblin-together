// 更新 MainMenu.java 添加网络游戏选项
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
        
        var root = new StackPane();
        
        var coverPlaceholder = new Rectangle(720, 480, Color.DARKGREEN);
        coverPlaceholder.setStroke(Color.GOLD);
        coverPlaceholder.setStrokeWidth(2);
        
        var buttonPanel = new VBox(20);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setTranslateX(100);
        
        var title = new Text("Goblin Together");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");
        
        var btnLocalCoop = createButton("本地雙人合作");
        var btnNetworkCoop = createButton("網路雙人合作");
        var btnControls = createButton("操作說明");
        var btnExit = createButton("退出");
        
        btnLocalCoop.setOnAction(e -> {
            fireNewGame();
        });
        
        btnNetworkCoop.setOnAction(e -> {
            showNetworkMenu();
        });
        
        btnControls.setOnAction(e -> {
            showControls();
        });
        
        btnExit.setOnAction(e -> {
            fireExit();
        });
        
        buttonPanel.getChildren().addAll(btnLocalCoop, btnNetworkCoop, btnControls, btnExit);
        
        var layout = new HBox(50);
        layout.setAlignment(Pos.CENTER);
        
        var leftPane = new VBox(20);
        leftPane.setAlignment(Pos.CENTER);
        leftPane.getChildren().addAll(title, coverPlaceholder);
        
        layout.getChildren().addAll(leftPane, buttonPanel);
        
        root.getChildren().add(layout);
        
        getContentRoot().getChildren().add(root);
    }
    
    private void showNetworkMenu() {
        getContentRoot().getChildren().clear();
        var networkMenu = new RoomBrowser();
        getContentRoot().getChildren().add(networkMenu.getContentRoot());
    }
    
    private void showControls() {
        var controlsPane = new StackPane();
        
        var bg = new Rectangle(600, 400, Color.color(0.2, 0.2, 0.2, 0.9));
        bg.setArcWidth(20);
        bg.setArcHeight(20);
        
        var content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        
        var title = new Text("操作說明");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        var localGameText = new Text("本地雙人合作:");
        localGameText.setFill(Color.LIGHTBLUE);
        localGameText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        var player1Controls = new Text("玩家 1: 使用 A/D 鍵移動，空白鍵跳躍");
        player1Controls.setFill(Color.WHITE);
        
        var player2Controls = new Text("玩家 2: 使用方向鍵移動，Enter鍵跳躍");
        player2Controls.setFill(Color.WHITE);
        
        var networkGameText = new Text("網路雙人合作:");
        networkGameText.setFill(Color.LIGHTGREEN);
        networkGameText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        var networkInfo = new Text("• 一位玩家創建房間作為主機\\n" + //
                        "• 另一位玩家透過IP位址加入房間\\n" + //
                        "• 兩位玩家在等待房間準備好後開始遊戲");
        networkInfo.setFill(Color.WHITE);
        
        var objective = new Text("目標: 逃避不斷上升的岩漿，存活越久越好！");
        objective.setFill(Color.YELLOW);
        objective.setStyle("-fx-font-weight: bold;");
        
        var closeBtn = createButton("關閉");
        closeBtn.setPrefWidth(100);
        closeBtn.setOnAction(e -> {
            getContentRoot().getChildren().remove(controlsPane);
        });
        
        content.getChildren().addAll(title, localGameText, player1Controls, player2Controls, 
                                   networkGameText, networkInfo, objective, closeBtn);
        
        controlsPane.getChildren().addAll(bg, content);
        controlsPane.setTranslateX(getAppWidth() / 2 - 300);
        controlsPane.setTranslateY(getAppHeight() / 2 - 200);
        
        getContentRoot().getChildren().add(controlsPane);
    }
    
    private Button createButton(String text) {
        var button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(50);
        button.setStyle("-fx-font-size: 20px; -fx-background-color: #4a4a4a; -fx-text-fill: white;");
        
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-font-size: 20px; -fx-background-color: #6a6a6a; -fx-text-fill: white;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-font-size: 20px; -fx-background-color: #4a4a4a; -fx-text-fill: white;");
        });
        
        return button;
    }
}