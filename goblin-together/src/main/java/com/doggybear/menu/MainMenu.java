package com.doggybear.menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class MainMenu extends FXGLMenu {
    
    public MainMenu() {
        super(MenuType.MAIN_MENU);
        
        // 加载背景图片（确保路径正确）
        Image bgImage = FXGL.image("background.png");
        BackgroundImage background = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true)
        );
        
        // 创建主容器并设置背景
        StackPane root = new StackPane();
        root.setBackground(new Background(background));
        root.setPrefSize(getAppWidth(), getAppHeight());
        
        // 添加半透明遮罩层增强文字可读性
        Rectangle overlay = new Rectangle(getAppWidth(), getAppHeight());
        overlay.setFill(Color.color(0, 0, 0, 0.1));
        root.getChildren().add(overlay);
        
        // 创建内容容器（垂直布局）
        VBox contentBox = new VBox(40);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setTranslateY(-50);  // 轻微上移
        
        // 创建按钮容器（水平居中）
        VBox buttonContainer = new VBox(30);
        buttonContainer.setAlignment(Pos.BASELINE_CENTER);
        
        // 创建按钮
        Button btnLocalCoop = createButton("單機合作", "#4CAF50");
        Button btnOnlineCoop = createButton("線上合作", "#F3C5A4");

        Button btnControls = createButton("操作說明", "#2196F3");
        Button btnExit = createButton("退出", "#F44336");
        
        // 设置按钮事件
        btnLocalCoop.setOnAction(e -> fireNewGame());
        btnOnlineCoop.setOnAction(null);
        btnControls.setOnAction(e -> showControls());
        btnExit.setOnAction(e -> fireExit());
        
        // 添加按钮到容器
        buttonContainer.getChildren().addAll(btnLocalCoop, btnOnlineCoop, btnControls, btnExit);
        
        // 添加所有元素到内容容器
        contentBox.getChildren().addAll(buttonContainer);
        
        // 添加内容容器到根容器
        root.getChildren().add(contentBox);
        
        // 设置到场景
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
        
        var closeBtn = createButton("關閉", "#FF9800");
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
    
    private Button createButton(String text, String colorHex) {
        Button button = new Button(text);
        button.setPrefSize(180, 60);
        
        // 现代扁平化按钮样式
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
        
        // 悬停效果
        button.setOnMouseEntered(e -> button.setStyle(String.format(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: derive(%s, 20%%);" +
            "-fx-background-radius: 30;" +
            "-fx-border-radius: 30;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 3);",
            colorHex
        )));
        
        // 鼠标移出效果
        button.setOnMouseExited(e -> button.setStyle(String.format(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: %s;" +
            "-fx-background-radius: 30;" +
            "-fx-border-radius: 30;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 2);",
            colorHex
        )));
        
        return button;
    }
}