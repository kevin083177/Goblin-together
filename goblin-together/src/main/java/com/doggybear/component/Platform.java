package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Platform extends Component {
    private PhysicsComponent physics;
    private Rectangle viewNode;

    private Color color = Color.RED;
    private int width;
    private int height;

    private boolean isMoving = false;
    private double moveSpeed;
    private double moveDistance;
    private double initialX;

    public Platform(int width, int height) {
        this.width = width;
        this.height = height;
        this.viewNode = new Rectangle(width, height, color);
    }

    public Platform setPhysics(PhysicsComponent physics) {
        this.physics = physics;
        return this;
    }

    public Rectangle getViewNode() {
        return viewNode;
    }

    @Override
    public void onAdded() {
        initialX = entity.getX();
    }

    @Override
    public void onUpdate(double tpf) {
        if (!isMoving || physics == null) return;

        physics.setLinearVelocity(moveSpeed, 0);

        if (Math.abs(entity.getX() - initialX) > moveDistance) {
            moveSpeed = -moveSpeed;
            physics.setLinearVelocity(moveSpeed, 0);
        }
    }

    public Platform setColor(Color color) {
        this.color = color;
        if (viewNode != null) {
            viewNode.setFill(color);
        }
        return this;
    }

    public void setMoving(double speed, double distance) {
        this.moveSpeed = speed;
        this.moveDistance = distance;
        this.isMoving = true;
    }

    public Color getColor() {
        return color;
    }

    public boolean isMoving() {
        return isMoving;
    }
}