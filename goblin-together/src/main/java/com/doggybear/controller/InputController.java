package com.doggybear.controller;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.doggybear.component.Goblin;
import com.doggybear.network.NetworkGameManager;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.getInput;

public class InputController {
    
    private Entity goblin;
    private Entity goblin2;
    private NetworkGameManager networkGameManager;
    
    // 靜態計數器，確保每次重新初始化時動作名稱都是唯一的
    private static int initCounter = 0;
    
    public InputController(Entity goblin, Entity goblin2, NetworkGameManager networkGameManager) {
        this.goblin = goblin;
        this.goblin2 = goblin2;
        this.networkGameManager = networkGameManager;
    }
    
    public void initInput() {
        System.out.println("=== 開始初始化輸入控制器 ===");
        
        // 增加初始化計數器
        initCounter++;
        String suffix = "_" + initCounter;
        
        // 首先清除所有現有的輸入綁定（避免重複綁定錯誤）
        System.out.println("清除現有輸入綁定...");
        try {
            getInput().clearAll();
            // 等待一小段時間確保清理完成
            Thread.sleep(50);
        } catch (Exception e) {
            System.err.println("清理輸入綁定時發生錯誤: " + e.getMessage());
        }
        
        // 檢查實體是否正確初始化
        if (goblin == null || goblin2 == null) {
            System.err.println("錯誤：Goblin 實體未正確初始化！");
            System.err.println("Goblin1: " + goblin);
            System.err.println("Goblin2: " + goblin2);
            return;
        }
        
        if (networkGameManager == null) {
            System.err.println("錯誤：NetworkGameManager 未正確初始化！");
            return;
        }
        
        // 檢查 Goblin 組件
        System.out.println("檢查 Goblin 組件...");
        Goblin goblinComponent1 = goblin.getComponent(Goblin.class);
        Goblin goblinComponent2 = goblin2.getComponent(Goblin.class);
        
        if (goblinComponent1 == null) {
            System.err.println("錯誤：Goblin1 沒有 Goblin 組件！");
            System.err.println("Goblin1 的所有組件: " + goblin.getComponents());
        } else {
            System.out.println("✓ Goblin1 組件正常");
        }
        
        if (goblinComponent2 == null) {
            System.err.println("錯誤：Goblin2 沒有 Goblin 組件！");
            System.err.println("Goblin2 的所有組件: " + goblin2.getComponents());
        } else {
            System.out.println("✓ Goblin2 組件正常");
        }
        
        boolean isNetworkGame = networkGameManager.isNetworkGame();
        System.out.println("網路遊戲模式: " + isNetworkGame);
        
        System.out.println("設置新的輸入綁定（第" + initCounter + "次初始化）...");
        
        // ===== 玩家1控制 (WASD + 空格) =====
        
        // D鍵 - 向右（使用唯一名稱）
        getInput().addAction(new UserAction("玩家1向右" + suffix) {
            @Override
            protected void onAction() {
                System.out.println(">>> D鍵被按下！");
                if (isNetworkGame) {
                    System.out.println("  -> 網路模式：發送輸入");
                    networkGameManager.handleLocalInput(KeyCode.D, true);
                } else {
                    System.out.println("  -> 本地模式：直接控制");
                    if (goblin != null && goblinComponent1 != null) {
                        System.out.println("  -> 調用 moveRight()");
                        goblinComponent1.moveRight();
                    } else {
                        System.err.println("  -> 錯誤：goblin 或組件為 null");
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                System.out.println(">>> D鍵釋放！");
                if (isNetworkGame) {
                    networkGameManager.handleLocalInput(KeyCode.D, false);
                } else {
                    if (goblin != null && goblinComponent1 != null) {
                        goblinComponent1.stop();
                    }
                }
            }
        }, KeyCode.D);

        // A鍵 - 向左
        getInput().addAction(new UserAction("玩家1向左" + suffix) {
            @Override
            protected void onAction() {
                System.out.println(">>> A鍵被按下！");
                if (isNetworkGame) {
                    networkGameManager.handleLocalInput(KeyCode.A, true);
                } else {
                    if (goblin != null && goblinComponent1 != null) {
                        System.out.println("  -> 調用 moveLeft()");
                        goblinComponent1.moveLeft();
                    } else {
                        System.err.println("  -> 錯誤：goblin 或組件為 null");
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                System.out.println(">>> A鍵釋放！");
                if (isNetworkGame) {
                    networkGameManager.handleLocalInput(KeyCode.A, false);
                } else {
                    if (goblin != null && goblinComponent1 != null) {
                        goblinComponent1.stop();
                    }
                }
            }
        }, KeyCode.A);

        // 空格鍵 - 跳躍
        getInput().addAction(new UserAction("玩家1跳躍" + suffix) {
            @Override
            protected void onActionBegin() {
                System.out.println(">>> 空格鍵被按下！");
                if (isNetworkGame) {
                    networkGameManager.handleLocalInput(KeyCode.SPACE, true);
                } else {
                    if (goblin != null && goblinComponent1 != null) {
                        System.out.println("  -> 調用 jump()");
                        goblinComponent1.jump();
                    } else {
                        System.err.println("  -> 錯誤：goblin 或組件為 null");
                    }
                }
            }
            
            @Override
            protected void onActionEnd() {
                if (isNetworkGame) {
                    networkGameManager.handleLocalInput(KeyCode.SPACE, false);
                }
            }
        }, KeyCode.SPACE);

        // W鍵 - 向上（網路遊戲用）
        getInput().addAction(new UserAction("玩家1向上" + suffix) {
            @Override
            protected void onAction() {
                if (isNetworkGame) {
                    networkGameManager.handleLocalInput(KeyCode.W, true);
                } else {
                    // 本地遊戲暫時不使用W鍵
                }
            }

            @Override
            protected void onActionEnd() {
                if (isNetworkGame) {
                    networkGameManager.handleLocalInput(KeyCode.W, false);
                }
            }
        }, KeyCode.W);

        // S鍵 - 向下（網路遊戲用）
        getInput().addAction(new UserAction("玩家1向下" + suffix) {
            @Override
            protected void onAction() {
                if (isNetworkGame) {
                    networkGameManager.handleLocalInput(KeyCode.S, true);
                } else {
                    // 本地遊戲暫時不使用S鍵
                }
            }

            @Override
            protected void onActionEnd() {
                if (isNetworkGame) {
                    networkGameManager.handleLocalInput(KeyCode.S, false);
                }
            }
        }, KeyCode.S);

        // ===== 玩家2控制 (方向鍵 + Enter) - 只在本地遊戲中有效 =====
        
        // 右方向鍵
        getInput().addAction(new UserAction("玩家2向右" + suffix) {
            @Override
            protected void onAction() {
                System.out.println(">>> 右方向鍵被按下！");
                if (!isNetworkGame) {
                    if (goblin2 != null && goblinComponent2 != null) {
                        System.out.println("  -> 玩家2調用 moveRight()");
                        goblinComponent2.moveRight();
                    } else {
                        System.err.println("  -> 錯誤：goblin2 或組件為 null");
                    }
                } else {
                    System.out.println("  -> 網路模式下忽略玩家2控制");
                }
            }

            @Override
            protected void onActionEnd() {
                System.out.println(">>> 右方向鍵釋放！");
                if (!isNetworkGame) {
                    if (goblin2 != null && goblinComponent2 != null) {
                        goblinComponent2.stop();
                    }
                }
            }
        }, KeyCode.RIGHT);

        // 左方向鍵
        getInput().addAction(new UserAction("玩家2向左" + suffix) {
            @Override
            protected void onAction() {
                System.out.println(">>> 左方向鍵被按下！");
                if (!isNetworkGame) {
                    if (goblin2 != null && goblinComponent2 != null) {
                        System.out.println("  -> 玩家2調用 moveLeft()");
                        goblinComponent2.moveLeft();
                    } else {
                        System.err.println("  -> 錯誤：goblin2 或組件為 null");
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                System.out.println(">>> 左方向鍵釋放！");
                if (!isNetworkGame) {
                    if (goblin2 != null && goblinComponent2 != null) {
                        goblinComponent2.stop();
                    }
                }
            }
        }, KeyCode.LEFT);

        // 上方向鍵
        getInput().addAction(new UserAction("玩家2向上" + suffix) {
            @Override
            protected void onAction() {
                if (!isNetworkGame) {
                    // 本地遊戲暫時不使用上方向鍵
                }
            }
        }, KeyCode.UP);

        // 下方向鍵
        getInput().addAction(new UserAction("玩家2向下" + suffix) {
            @Override
            protected void onAction() {
                if (!isNetworkGame) {
                    // 本地遊戲暫時不使用下方向鍵
                }
            }
        }, KeyCode.DOWN);

        // Enter鍵 - 玩家2跳躍
        getInput().addAction(new UserAction("玩家2跳躍" + suffix) {
            @Override
            protected void onActionBegin() {
                System.out.println(">>> Enter鍵被按下！");
                if (!isNetworkGame) {
                    if (goblin2 != null && goblinComponent2 != null) {
                        System.out.println("  -> 玩家2調用 jump()");
                        goblinComponent2.jump();
                    } else {
                        System.err.println("  -> 錯誤：goblin2 或組件為 null");
                    }
                }
            }
        }, KeyCode.ENTER);
        
        // ===== 測試按鍵 =====
        
        // T鍵 - 測試按鍵
        getInput().addAction(new UserAction("測試按鍵" + suffix) {
            @Override
            protected void onActionBegin() {
                System.out.println("🎉 測試按鍵 T 被按下！輸入系統正常工作！");
            }
        }, KeyCode.T);
        
        System.out.println("=== 輸入控制器初始化完成 ===");
        System.out.println("玩家1控制：A/D移動，空格跳躍");
        System.out.println("玩家2控制：左右方向鍵移動，Enter跳躍");
        System.out.println("測試按鍵：T鍵");
        System.out.println("當前輸入綁定數量: " + getInput().getAllBindings().size());
        System.out.println("請嘗試按鍵測試...");
    }
    
    /**
     * 更新實體引用（當遊戲重新開始時使用）
     */
    public void updateEntities(Entity goblin, Entity goblin2) {
        System.out.println("更新實體引用");
        this.goblin = goblin;
        this.goblin2 = goblin2;
    }
    
    /**
     * 清理輸入綁定
     */
    public void clearInput() {
        System.out.println("=== 清理輸入綁定 ===");
        try {
            getInput().clearAll();
            System.out.println("✓ 輸入綁定已清理");
        } catch (Exception e) {
            System.err.println("清理輸入綁定時發生錯誤: " + e.getMessage());
        }
    }
    
    /**
     * 獲取當前初始化次數（用於除錯）
     */
    public static int getInitCounter() {
        return initCounter;
    }
    
    /**
     * 重置初始化計數器（僅用於測試）
     */
    public static void resetInitCounter() {
        initCounter = 0;
        System.out.println("初始化計數器已重置");
    }
}