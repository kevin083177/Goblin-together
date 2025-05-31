package com.doggybear.component;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;

public class MovingPlatform extends Component {
    private PhysicsComponent physics;

    private boolean isMovingHorizontal = false;
    private boolean isMovingVertical = false;

    private double moveSpeedX;
    private double moveDistanceX;
    private double initialX;

    private double moveSpeedY;
    private double moveDistanceY;
    private double initialY;

    private boolean auto = false;
    private boolean movingOutward = true;

    private boolean playerOnPlatform = false;
    private boolean hasStartedMoving = false; // 記錄是否已經開始移動
    private boolean isReturning = false; // 記錄是否在返回過程中
    
    private double initialSpeedX; // 記錄初始速度方向
    private double initialSpeedY; // 記錄初始速度方向

    public void setPhysics(PhysicsComponent physics) {
        this.physics = physics;
    }

    @Override
    public void onAdded() {
        initialX = entity.getX();
        initialY = entity.getY();
    }

    @Override
    public void onUpdate(double tpf) {
        if (!isMoving() || physics == null) {
            return;
        }

        // 自動移動平台持續移動
        if (auto) {
            handleAutoMovement();
            return;
        }

        if (!hasStartedMoving && !playerOnPlatform) {
            physics.setLinearVelocity(0, 0);
            return;
        }

        if (playerOnPlatform && !hasStartedMoving) {
            hasStartedMoving = true;
            movingOutward = true;
        }

        if (hasStartedMoving) {
            if (movingOutward) {
                // 前往目標位置
                handleOutwardMovement();
            } else if (isReturning) {
                // 返回初始位置
                handleReturnMovement();
            }
        }
    }

    private void handleAutoMovement() {
        double vx = 0, vy = 0;

        // 水平移動
        if (isMovingHorizontal) {
            vx = moveSpeedX;
            
            // 根據初始速度方向判斷邊界
            if (moveSpeedX > 0) {
                // 初始向右移動
                if (entity.getX() - initialX >= moveDistanceX) {
                    moveSpeedX = -Math.abs(moveSpeedX);
                    vx = moveSpeedX;
                } else if (entity.getX() <= initialX) {
                    moveSpeedX = Math.abs(moveSpeedX);
                    vx = moveSpeedX;
                }
            } else {
                // 初始向左移動
                if (initialX - entity.getX() >= moveDistanceX) {
                    moveSpeedX = Math.abs(moveSpeedX);
                    vx = moveSpeedX;
                } else if (entity.getX() >= initialX) {
                    moveSpeedX = -Math.abs(moveSpeedX);
                    vx = moveSpeedX;
                }
            }
        }

        // 垂直移動
        if (isMovingVertical) {
            vy = moveSpeedY;
            
            // 根據初始速度方向判斷邊界
            if (moveSpeedY > 0) {
                // 初始向下移動
                if (entity.getY() - initialY >= moveDistanceY) {
                    moveSpeedY = -Math.abs(moveSpeedY);
                    vy = moveSpeedY;
                } else if (entity.getY() <= initialY) {
                    moveSpeedY = Math.abs(moveSpeedY);
                    vy = moveSpeedY;
                }
            } else {
                // 初始向上移動
                if (initialY - entity.getY() >= moveDistanceY) {
                    moveSpeedY = Math.abs(moveSpeedY);
                    vy = moveSpeedY;
                } else if (entity.getY() >= initialY) {
                    moveSpeedY = -Math.abs(moveSpeedY);
                    vy = moveSpeedY;
                }
            }
        }

        physics.setLinearVelocity(vx, vy);
    }

    private void handleOutwardMovement() {
        double vx = 0, vy = 0;
        boolean reachedDestination = true;

        // 水平移動
        if (isMovingHorizontal) {
            if (initialSpeedX > 0) {
                // 向右移動
                double movedDistance = entity.getX() - initialX;
                if (movedDistance < moveDistanceX) {
                    vx = Math.abs(initialSpeedX);
                    reachedDestination = false;
                }
            } else {
                // 向左移動
                double movedDistance = initialX - entity.getX();
                if (movedDistance < moveDistanceX) {
                    vx = -Math.abs(initialSpeedX);
                    reachedDestination = false;
                }
            }
        }

        // 垂直移動
        if (isMovingVertical) {
            if (initialSpeedY > 0) {
                // 向下移動
                double movedDistance = entity.getY() - initialY;
                if (movedDistance < moveDistanceY) {
                    vy = Math.abs(initialSpeedY);
                    reachedDestination = false;
                }
            } else {
                // 向上移動
                double movedDistance = initialY - entity.getY();
                if (movedDistance < moveDistanceY) {
                    vy = -Math.abs(initialSpeedY);
                    reachedDestination = false;
                }
            }
        }

        physics.setLinearVelocity(vx, vy);

        // 到達目標位置後返回
        if (reachedDestination) {
            movingOutward = false;
            isReturning = true;
        }
    }

    private void handleReturnMovement() {
        double vx = 0, vy = 0;
        boolean reachedStart = true;

        // 水平返回
        if (isMovingHorizontal) {
            double deltaX = entity.getX() - initialX;
            
            if (Math.abs(deltaX) > 1) {
                vx = deltaX > 0 ? -Math.abs(moveSpeedX) : Math.abs(moveSpeedX);
                reachedStart = false;
            }
        }

        // 垂直返回
        if (isMovingVertical) {
            double deltaY = entity.getY() - initialY;
            
            if (Math.abs(deltaY) > 1) {
                vy = deltaY > 0 ? -Math.abs(moveSpeedY) : Math.abs(moveSpeedY);
                reachedStart = false;
            }
        }

        physics.setLinearVelocity(vx, vy);

        if (reachedStart) {
            entity.setPosition(initialX, initialY);
            physics.setLinearVelocity(0, 0);
            hasStartedMoving = false;
            isReturning = false;
            movingOutward = true;
        }
    }

    public void setHorizontalMovement(double speed, double distance) {
        this.moveSpeedX = speed;
        this.initialSpeedX = speed;
        this.moveDistanceX = distance;
        this.isMovingHorizontal = true;
    }

    public void setVerticalMovement(double speed, double distance) {
        this.moveSpeedY = speed;
        this.initialSpeedY = speed;
        this.moveDistanceY = distance;
        this.isMovingVertical = true;
    }

    public void setPlayerOnPlatform(boolean onPlatform) {
        this.playerOnPlatform = onPlatform;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public boolean isMoving() {
        return isMovingHorizontal || isMovingVertical;
    }
}