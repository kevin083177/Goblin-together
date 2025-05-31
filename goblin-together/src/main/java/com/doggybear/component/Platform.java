package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Platform extends Component {
    private Rectangle viewNode;
    private Color color = Color.RED;
    private int width;
    private int height;

    public Platform(int width, int height) {
        this.width = width;
        this.height = height;
        this.viewNode = new Rectangle(width, height, color);
    }

    public Rectangle getViewNode() {
        return viewNode;
    }

    public Platform setColor(Color color) {
        this.color = color;
        if (viewNode != null) {
            viewNode.setFill(color);
        }
        return this;
    }

    public Color getColor() {
        return color;
    }
}