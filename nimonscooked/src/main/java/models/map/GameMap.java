package models.map;

import models.station.*;
import models.core.Position;
import models.enums.IngredientType;
import java.util.*;

public class GameMap {

    public static final int WIDTH = 14;
    public static final int HEIGHT = 10;

    private final char[][] grid;
    private final Map<Position, Station> stations;
    private final List<Position> chefSpawns;

    public GameMap(char[][] grid) {
        this.grid = grid;
        this.stations = new HashMap<>();
        this.chefSpawns = new ArrayList<>();
        parseStations();
    }

    private void parseStations() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Position pos = new Position(x, y);
                char tile = grid[y][x];

                switch (tile) {
                    case 'C' -> stations.put(pos, new CuttingStation(pos));
                    case 'R' -> {
                        CookingStation cs = new CookingStation(pos);
                        // For Type D, place Oven on cooking station
                        cs.placeDevice(new models.item.kitchenutensils.Oven());
                        stations.put(pos, cs);
                    }
                    case 'A' -> stations.put(pos, new AssemblyStation(pos));
                    case 'S' -> stations.put(pos, new ServingCounter(pos, null)); // Stage set later
                    case 'W' -> stations.put(pos, new WashingStation(pos));
                    case 'P' -> stations.put(pos, new PlateStorage(pos, 3));
                    case 'T' -> stations.put(pos, new TrashStation(pos));
                    case 'I' -> {
                        IngredientType type = assignIngredientType(x, y);
                        stations.put(pos, new IngredientStorage(pos, type));
                    }
                    case 'V' -> {
                        chefSpawns.add(pos);
                        grid[y][x] = '.'; // Make spawn walkable
                    }
                }
            }
        }
    }

    private IngredientType assignIngredientType(int x, int y) {
        // Distribute ingredients across storage stations
        IngredientType[] types = {
                IngredientType.DOUGH,
                IngredientType.TOMATO,
                IngredientType.CHEESE,
                IngredientType.SAUSAGE,
                IngredientType.CHICKEN
        };
        return types[(x + y) % types.length];
    }

    public boolean isWalkable(int x, int y) {
        if (!inBounds(x, y)) return false;

        char tile = grid[y][x];
        if (tile == 'X') return false;

        Position pos = new Position(x, y);
        if (stations.containsKey(pos)) return false;

        return tile == '.' || tile == 'V';
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    public Station getStationAt(Position pos) {
        return stations.get(pos);
    }

    public Station getStationAt(int x, int y) {
        return stations.get(new Position(x, y));
    }

    public List<Position> getChefSpawns() {
        return new ArrayList<>(chefSpawns);
    }

    public char getTile(int x, int y) {
        if (!inBounds(x, y)) return 'X';
        return grid[y][x];
    }

    public char[][] getGrid() {
        return grid;
    }

    public Map<Position, Station> getAllStations() {
        return new HashMap<>(stations);
    }

    public void setStageForServingCounters(controllers.Stage stage) {
        for (Station station : stations.values()) {
            if (station instanceof ServingCounter) {
                ((ServingCounter) station).setStage(stage);
            }
        }
    }
}
