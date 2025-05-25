package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Spike extends Component {
    private Polygon viewNode;
    private int width;
    private int height;
    private Color color = Color.DARKRED;

    public Spike(int width, int height) {
        this.width = width;
        this.height = height;
        createSpikeShape();
    }

    private void createSpikeShape() {
        viewNode = new Polygon();
        
        // 定义三角形的三个顶点（尖端向上）
        Double[] points = {
            0.0, (double)height,
            (double)width/2, 0.0,
            (double)width, (double)height
        };
        
        viewNode.getPoints().addAll(points);
        viewNode.setFill(color);
        viewNode.setStroke(Color.DARKRED.darker());
        viewNode.setStrokeWidth(1);
    }

    public Polygon getViewNode() {
        return viewNode;
    }

    public Spike setColor(Color color) {
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