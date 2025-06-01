package com.doggybear.menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.doggybear.ui.FontManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MainMenu extends FXGLMenu {
    
    public MainMenu() {
        super(MenuType.MAIN_MENU);
        
        Image bgImage = FXGL.image("background.png");
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
        
        Rectangle overlay = new Rectangle(getAppWidth(), getAppHeight());
        overlay.setFill(Color.color(0, 0, 0, 0.1));
        root.getChildren().add(overlay);
        
        VBox contentBox = new VBox(40);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setTranslateY(75);
        
        VBox buttonContainer = new VBox(30);
        buttonContainer.setAlignment(Pos.CENTER);
        
        Button btnLocalCoop = createImageButton("localButton.png", "單機合作");
        Button btnOnlineCoop = createImageButton("onlineButton.png", "線上合作");
        Button btnControls = createImageButton("helpButton.png", "操作說明");
        Button btnExit = createImageButton("exitButton.png", "退出");
        
        btnLocalCoop.setOnAction(e -> fireNewGame());
        btnOnlineCoop.setOnAction(null);
        btnControls.setOnAction(e -> showControls());
        btnExit.setOnAction(e -> fireExit());
        
        buttonContainer.getChildren().addAll(btnLocalCoop, btnOnlineCoop, btnControls, btnExit);
        
        contentBox.getChildren().addAll(buttonContainer);
        
        root.getChildren().add(contentBox);
        
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
        
        var closeBtn = createTextButton("關閉", "#FF9800");
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
    
    private Button createImageButton(String imagePath, String buttonText) {
        Button button = new Button();
        button.setPrefSize(240, 80);
        
        StackPane buttonContent = new StackPane();
        
        Image buttonImage = FXGL.image(imagePath);
        ImageView imageView = new ImageView(buttonImage);
        imageView.setFitWidth(240);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(false);
        
        Text text = new Text(buttonText);
        Font buttonFont = FontManager.getFont(FontManager.FontType.REGULAR);
        text.setFont(buttonFont);
        text.setFill(Color.WHITE);
        text.setStroke(Color.WHITE);
        text.setTranslateY(-5);
        
        // 將圖片和文字疊放在StackPane中
        buttonContent.getChildren().addAll(imageView, text);
        
        button.setGraphic(buttonContent);
        
        // 移除按鈕的預設背景和邊框
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        // 滑鼠懸停效果 - 放大
        button.setOnMouseEntered(e -> {
            button.setScaleX(1.1);
            button.setScaleY(1.1);
        });
        
        // 滑鼠移出效果 - 恢復原大小
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        
        return button;
    }
    
    private Button createTextButton(String text, String colorHex) {
        Button button = new Button(text);
        button.setPrefSize(180, 60);
        
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