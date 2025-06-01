package com.doggybear.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 字型管理器 - 統一管理遊戲中使用的字型
 */
public class FontManager {
    
    private static final Map<String, Font> loadedFonts = new HashMap<>();
    private static boolean debugMode = true; // 開啟調試模式
    
    // 預定義的字型類型
    public enum FontType {
        BOLD("fonts/NotoSansTC-SemiBold.ttf", 24),
        REGULAR("fonts/NotoSansTC-Medium.ttf", 32);
        
        private final String path;
        private final double defaultSize;
        
        FontType(String path, double defaultSize) {
            this.path = path;
            this.defaultSize = defaultSize;
        }
        
        public String getPath() { return path; }
        public double getDefaultSize() { return defaultSize; }
    }
    
    /**
     * 載入指定類型的字型
     */
    public static Font getFont(FontType type) {
        return getFont(type, type.getDefaultSize());
    }
    
    /**
     * 載入指定類型和大小的字型
     */
    public static Font getFont(FontType type, double size) {
        String key = type.name() + "_" + size;
        
        if (loadedFonts.containsKey(key)) {
            if (debugMode) {
                System.out.println("Using cached font: " + key);
            }
            return loadedFonts.get(key);
        }
        
        Font font = loadCustomFont(type.getPath(), size);
        loadedFonts.put(key, font);
        return font;
    }
    
    /**
     * 載入自訂字型檔案
     */
    private static Font loadCustomFont(String fontPath, double size) {
        if (debugMode) {
            System.out.println("Attempting to load font: " + fontPath + " (size: " + size + ")");
        }
        
        Font customFont = null;
        
        // 方法1: 使用 FXGL AssetLoader
        try {
            if (FXGL.getAssetLoader() != null) {
                InputStream fontStream = FXGL.getAssetLoader().getStream(fontPath);
                if (fontStream != null) {
                    customFont = Font.loadFont(fontStream, size);
                    fontStream.close();
                    
                    if (customFont != null) {
                        if (debugMode) {
                            System.out.println("✓ Successfully loaded font via FXGL AssetLoader: " + fontPath);
                            System.out.println("  Font family: " + customFont.getFamily());
                            System.out.println("  Font name: " + customFont.getName());
                        }
                        return customFont;
                    }
                }
            }
        } catch (Exception e) {
            if (debugMode) {
                System.out.println("FXGL AssetLoader method failed: " + e.getMessage());
            }
        }
        
        // 方法2: 使用 ClassLoader 直接載入
        try {
            String resourcePath = "/assets/" + fontPath;
            InputStream fontStream = FontManager.class.getResourceAsStream(resourcePath);
            
            if (fontStream != null) {
                customFont = Font.loadFont(fontStream, size);
                fontStream.close();
                
                if (customFont != null) {
                    if (debugMode) {
                        System.out.println("✓ Successfully loaded font via ClassLoader: " + resourcePath);
                        System.out.println("  Font family: " + customFont.getFamily());
                        System.out.println("  Font name: " + customFont.getName());
                    }
                    return customFont;
                }
            }
        } catch (Exception e) {
            if (debugMode) {
                System.out.println("ClassLoader method failed: " + e.getMessage());
            }
        }
        
        // 方法3: 使用 URL 載入
        try {
            String resourcePath = "/assets/" + fontPath;
            URL fontUrl = FontManager.class.getResource(resourcePath);
            
            if (fontUrl != null) {
                customFont = Font.loadFont(fontUrl.toExternalForm(), size);
                
                if (customFont != null) {
                    if (debugMode) {
                        System.out.println("✓ Successfully loaded font via URL: " + fontUrl.toExternalForm());
                        System.out.println("  Font family: " + customFont.getFamily());
                        System.out.println("  Font name: " + customFont.getName());
                    }
                    return customFont;
                }
            }
        } catch (Exception e) {
            if (debugMode) {
                System.out.println("URL method failed: " + e.getMessage());
            }
        }
        
        // 方法4: 嘗試不同的路徑組合
        String[] pathVariants = {
            fontPath,
            "/" + fontPath,
            "assets/" + fontPath,
            "/assets/" + fontPath
        };
        
        for (String path : pathVariants) {
            try {
                InputStream fontStream = FontManager.class.getResourceAsStream(path);
                if (fontStream != null) {
                    customFont = Font.loadFont(fontStream, size);
                    fontStream.close();
                    
                    if (customFont != null) {
                        if (debugMode) {
                            System.out.println("✓ Successfully loaded font with path variant: " + path);
                            System.out.println("  Font family: " + customFont.getFamily());
                            System.out.println("  Font name: " + customFont.getName());
                        }
                        return customFont;
                    }
                }
            } catch (Exception e) {
                // 繼續嘗試下一個路徑
            }
        }
        
        // 所有方法都失敗，使用備用字型
        System.err.println("All font loading methods failed for: " + fontPath);
        System.err.println("Please ensure the font file is located in one of these paths:");
        System.err.println("  - src/main/resources/assets/" + fontPath);
        System.err.println("  - src/main/resources/" + fontPath);
        
        return createFallbackFont(size);
    }
    
    /**
     * 創建備用字型
     */
    private static Font createFallbackFont(double size) {
        String[] chineseFonts = {
            "Microsoft YaHei",  // Windows
            "PingFang SC",      // macOS
            "Noto Sans CJK TC", // Linux
            "SimHei",           // Windows 備用
            "Arial Unicode MS", // 通用備用
            "Arial"             // 最終備用
        };
        
        for (String fontFamily : chineseFonts) {
            try {
                Font font = Font.font(fontFamily, FontWeight.BOLD, size);
                if (font != null && !font.getFamily().equals("System")) {
                    if (debugMode) {
                        System.out.println("Using fallback font: " + fontFamily + " (size: " + size + ")");
                    }
                    return font;
                }
            } catch (Exception e) {
                // 繼續嘗試下一個字型
            }
        }
        
        // 最終備用
        if (debugMode) {
            System.out.println("Using final fallback font: Arial (size: " + size + ")");
        }
        return Font.font("Arial", FontWeight.BOLD, size);
    }
    
    /**
     * 檢查字型檔案是否存在
     */
    public static boolean checkFontExists(String fontPath) {
        // 使用多種方法檢查字型是否存在
        
        // 方法1: FXGL AssetLoader
        try {
            if (FXGL.getAssetLoader() != null) {
                InputStream stream = FXGL.getAssetLoader().getStream(fontPath);
                if (stream != null) {
                    stream.close();
                    return true;
                }
            }
        } catch (Exception e) {
            // 繼續嘗試其他方法
        }
        
        // 方法2: ClassLoader
        String[] pathVariants = {
            "/" + fontPath,
            "/assets/" + fontPath,
            "assets/" + fontPath,
            fontPath
        };
        
        for (String path : pathVariants) {
            try {
                InputStream stream = FontManager.class.getResourceAsStream(path);
                if (stream != null) {
                    stream.close();
                    return true;
                }
            } catch (Exception e) {
                // 繼續嘗試下一個路徑
            }
        }
        
        return false;
    }
    
    /**
     * 初始化字型管理器（在遊戲啟動時調用）
     */
    public static void initialize() {
        System.out.println("=== FontManager Initialization ===");
        
        // 檢查所有預定義字型是否存在
        for (FontType type : FontType.values()) {
            boolean exists = checkFontExists(type.getPath());
            System.out.println(type.name() + " (" + type.getPath() + "): " + (exists ? "✓ Found" : "✗ Not found"));
            
            if (!exists) {
                System.out.println("  Searching in common locations...");
                String[] locations = {
                    "src/main/resources/assets/" + type.getPath(),
                    "src/main/resources/" + type.getPath(),
                    "assets/" + type.getPath()
                };
                for (String location : locations) {
                    System.out.println("    - " + location);
                }
            }
        }
        
        // 測試載入每個字型
        System.out.println("\n=== Testing Font Loading ===");
        for (FontType type : FontType.values()) {
            Font testFont = getFont(type);
            if (testFont != null) {
                System.out.println(type.name() + ": ✓ Loaded successfully (" + testFont.getFamily() + ")");
            } else {
                System.out.println(type.name() + ": ✗ Failed to load");
            }
        }
        
        System.out.println("=== FontManager Ready ===");
    }
    
    /**
     * 設置調試模式
     */
    public static void setDebugMode(boolean debug) {
        debugMode = debug;
    }
    
    /**
     * 清理已載入的字型（遊戲結束時調用）
     */
    public static void clearFonts() {
        loadedFonts.clear();
        if (debugMode) {
            System.out.println("FontManager: Cleared all cached fonts");
        }
    }
    
    /**
     * 獲取可用的系統字型列表（用於調試）
     */
    public static void printAvailableSystemFonts() {
        System.out.println("Available system fonts:");
        Font.getFamilies().forEach(family -> {
            if (family.contains("Noto") || family.contains("Microsoft") || 
                family.contains("PingFang") || family.contains("SimHei") ||
                family.contains("Arial") || family.contains("華康") || 
                family.contains("微軟正黑體") || family.contains("新細明體")) {
                System.out.println("  " + family);
            }
        });
    }
    
    /**
     * 強制重新載入字型（用於調試）
     */
    public static void reloadFont(FontType type) {
        // 移除快取中的字型
        loadedFonts.entrySet().removeIf(entry -> entry.getKey().startsWith(type.name() + "_"));
        System.out.println("Cleared cache for font type: " + type.name());
    }
}