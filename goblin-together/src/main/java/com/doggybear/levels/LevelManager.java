package com.doggybear.levels;

public class LevelManager {
    public static Level createLevel() {
        Level level = new Level();

        level.setLavaRiseSpeed(3)
             .setInitialLavaHeight(100)
             .setGoblinStart(1450, 800)
             .setGoblin2Start(1500, 800);
             // initial 1: (1450, 800)
             // initial 2: (1500, 800)
        
        level.createInitialPlatform(); // x: 1300, y: 850 
        level.createPlayerRope(125);

        // 第一排
        level.createPlatform(1150, 850, 75, 100, 3);
        level.createPlatform(1075, 750, 75, 500, 3);

        level.createPlatform(850, 800, 100, 500, 3);
        level.createPlatform(650, 730, 100, 500, 3);
        level.createPlatform(450, 660, 100, 500, 3);

        // LEFT
        level.createPlatform(200, 590, 150, 500, 3);
        level.createSpike(200, 570);

        level.createVericalMovingPlatform(50, 525, 100, 50, -200, 400, false);

        // RIGHT
        level.createDisappearingPlatform(450, 500, 100, 50, 3, 3);
        level.createPlatform(700, 500, 100, 50, 2);

        level.createBouncePlatform(950, 500, 500);
        level.createBouncePlatform(1100, 500, 500);
        level.createBouncePlatform(1250, 500, 500);

        level.createVericalMovingPlatform(1400, 400, 100, 50, -100, 100, true);

        // 第二排
        // RIGHT - CONTINUE
        level.createPlatform(1180, 300, 120, 50, 2);
        level.createIcePlatform(1100, 200, 100, 150, 2);

        level.createHorizontalMovingPlatform(850, 200, 150, 50, -100, 100, false);
        
        // LEFT - CONTINUE
        level.createBouncePlatform(250, 200, 700);
        level.createBouncePlatform(450, 200, 700);

        // RELAY
        level.createVericalMovingPlatform(600, 50, 100, 50, -125, 150, true);

        // 第三排
        // RIGHT
        level.createPlatform(775, -100, 230, 50,2);
        level.createSpike(875, -120);
        level.createPlatform(1050, -170, 100, 50, 2);
        
        level.createFirePlatform(1200, -200, 150, 50, 1, 4, 1);
        level.createFirePlatform(1430, -265, 150, 50, 2.5, 2.5, 1);
        level.createPlatform(1245, -340, 50, 50, 1);
        level.createFirePlatform(1000, -380, 150, 50, 3, 3.5, 1);

        // LEFT
        level.createIcePlatform(430, -100, 70, 70, 1);
        level.createIcePlatform(270, -145, 60, 60, 1);
        level.createIcePlatform(110, -190, 50, 50, 1);
        
        level.createBouncePlatform(0, -190, 1250);

        level.createIcePlatform(210, -320, 100, 50, 2);
        level.createIcePlatform(450, -380, 100, 50, 2);

        // RELAY
        level.createVericalMovingPlatform(725, -425, 100, 50, -150, 700, false);

        level.createLauncher(120, -555, "right", 1.5);
        level.createPlatform(120, -505, 50, 50, 1);
        level.createLauncher(1480, -655, "left", 1.2);
        level.createPlatform(1480, -605, 50, 50, 1);
        level.createLauncher(120, -755, "right", 1.2);
        level.createPlatform(120, -705, 50, 50, 1);
        level.createLauncher(1480, -855, "left", 1.5);
        level.createPlatform(1480, -805, 50, 50, 1);

        // 第四排 RIGHT
        level.createDisappearingPlatform(850, -1100, 200, 50, 3.5, 3.5);
        level.createDisappearingPlatform(1150, -1150, 200, 50, 2, 1);

        level.createHorizontalMovingPlatform(1450, -1220, 150, 50, -150, 800, false);

        // LEFT
        level.createDisappearingPlatform(450, -1220, 120, 50, 1, 2);
        level.createDisappearingPlatform(250, -1280, 100, 50, 1, 2);

        // END
        level.createPlatform(0, -1350, 120, 50, 4);
        return level;
    }
}