package models.level;

import models.map.MapType;
import java.util.ArrayList;
import java. util.List;

public class LevelManager {
    private static LevelManager instance;
    private final List<Level> predefinedLevels;
    private Level currentLevel;

    private LevelManager() {
        predefinedLevels = new ArrayList<>();
        initializePredefinedLevels();
    }

    public static LevelManager getInstance() {
        if (instance == null) {
            instance = new LevelManager();
        }
        return instance;
    }

    private void initializePredefinedLevels(){
        predefinedLevels.add(new Level(
                1, "Level 1 - Tutorial",
                MapType. PIZZA,
                180,    // 3 menit
                200,    // target score
                5,      // max failed orders
                Level. Difficulty.EASY,
                30,     // 30 sec per order spawn
                5,
                60
        ));

        predefinedLevels. add(new Level(
                2, "Level 2 - Getting Started",
                MapType.PIZZA,
                180,
                350,    // target score
                5,      // max failed orders
                Level.Difficulty.EASY,
                30,     // 30 sec per order spawn
                5,       // max 5 orders at once
                60
        ));

        predefinedLevels. add(new Level(
                3, "Level 3 - Warming Up",
                MapType. PIZZA,
                180,
                500,
                4,
                Level.Difficulty.MEDIUM,
                25,     // faster spawn
                5,
                50      // 50 sec order timeout (MEDIUM)
        ));

        predefinedLevels.add(new Level(
                4, "Level 4 - Rush Hour",
                MapType.PIZZA,
                150,    // less time
                650,
                4,
                Level.Difficulty. MEDIUM,
                25,
                5,
                50       // max 5 orders at once
        ));

        predefinedLevels.add(new Level(
                5, "Level 5 - Master Chef",
                MapType.PIZZA,
                150,
                800,
                3,
                Level.Difficulty. HARD,
                20,     // fastest spawn
                6,
                40
        ));
    }

    public List<Level> getPredefinedLevels() {
        return new ArrayList<>(predefinedLevels);
    }

    public Level generateRandomEasy() {
        return new Level(
                100, "Random - Easy",
                MapType.PIZZA,
                180,    // 3 minutes
                300,
                6,
                Level. Difficulty.EASY,
                30,
                5,
                60
        );
    }

    public Level generateRandomHard() {
        return new Level(
                101, "Random - Hard",
                MapType.PIZZA,
                120,    // 2 minutes
                500,
                3,
                Level.Difficulty.HARD,
                20,
                6,
                40
        );
    }

    public void setCurrentLevel(Level level) {
        this.currentLevel = level;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public Level getLevelById(int id) {
        return predefinedLevels.stream()
                .filter(l -> l.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
