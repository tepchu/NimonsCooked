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
    private Random random;


    public GameMap(char[][] grid) {
        this.grid = grid;
        this.stations = new HashMap<>();
        this.chefSpawns = new ArrayList<>();
        this.random = new Random();
        parseStations();
        debugPrintMapGrid(grid);
    }

    private void parseStations() {
        List<Position> ingredientStoragePositions = new ArrayList<>();
        List<Position> washingStationPositions = new ArrayList<>();
        int stationCount = 0;

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Position pos = new Position(x, y);
                char tile = grid[y][x];

                switch (tile) {
                    case 'C' -> {
                        stations.put(pos, new CuttingStation(pos));
                        System.out.println("✓ CUTTING station at (" + x + ", " + y + ")");
                        stationCount++;
                    }
                    case 'R' -> {
                        CookingStation cs = new CookingStation(pos);
                        cs.placeDevice(new models.item.kitchenutensils.Oven());
                        stations.put(pos, cs);
                        System.out.println("✓ COOKING station at (" + x + ", " + y + ")");
                        stationCount++;
                    }
                    case 'A' -> {
                        stations.put(pos, new AssemblyStation(pos));
                        System.out.println("✓ ASSEMBLY station at (" + x + ", " + y + ")");
                        stationCount++;
                    }
                    case 'S' -> {
                        stations.put(pos, new ServingCounter(pos, null));
                        System.out.println("✓ SERVING station at (" + x + ", " + y + ")");
                        stationCount++;
                    }
                    case 'W' -> {
                        washingStationPositions.add(pos); // CHANGED
                        System.out.println("✓ WASHING station marker at (" + x + ", " + y + ")");
                    }
                    case 'P' -> {
                        stations.put(pos, new PlateStorage(pos, 3));
                        System.out.println("✓ PLATE STORAGE at (" + x + ", " + y + ")");
                        stationCount++;
                    }
                    case 'T' -> {
                        stations.put(pos, new TrashStation(pos));
                        System.out.println("✓ TRASH station at (" + x + ", " + y + ")");
                        stationCount++;
                    }
                    case 'I' -> {
                        ingredientStoragePositions.add(pos);
                        System.out.println("✓ INGREDIENT STORAGE marker at (" + x + ", " + y + ")");
                    }
                    case 'V' -> {
                        chefSpawns.add(pos);
                        grid[y][x] = '.';
                        System.out.println("✓ CHEF SPAWN at (" + x + ", " + y + ")");
                    }
                }
            }
        }

        assignRandomizedIngredients(ingredientStoragePositions);
        createWashingStations(washingStationPositions);

        // Print summary
        System.out.println("===========================================");
        System.out.println("MAP LOADING SUMMARY:");
        System.out.println("Total stations loaded: " + stations.size());
        System.out.println("Chef spawn points: " + chefSpawns.size());
        System.out.println("Ingredient storage positions: " + ingredientStoragePositions.size());
        System.out.println("===========================================");
    }

    private void createWashingStations(List<Position> washPositions) {
        System.out.println("[DEBUG] Creating washing stations from " + washPositions.size() + " W markers");

        for (int i = 0; i < washPositions.size(); i++) {
            Position pos1 = washPositions.get(i);

            if (stations.containsKey(pos1)) {
                System.out.println("[DEBUG] Position " + pos1 + " already has a station, skipping");
                continue;
            }

            Position pos2 = null;
            for (int j = i + 1; j < washPositions.size(); j++) {
                Position candidate = washPositions.get(j);

                if (pos1.getY() == candidate.getY() &&
                        Math.abs(pos1.getX() - candidate.getX()) == 1) {
                    pos2 = candidate;
                    break;
                }
            }

            if (pos2 != null) {
                Position washPos = pos1.getX() < pos2.getX() ? pos1 : pos2;
                Position cleanPos = pos1.getX() < pos2.getX() ? pos2 : pos1;

                WashingStation washStation = new WashingStation(washPos, cleanPos);

                stations.put(washPos, washStation);
                stations.put(cleanPos, washStation);

                System.out.println("✓ WASHING STATION created: Wash(" + washPos.getX() + "," + washPos.getY() +
                        ") -> Clean(" + cleanPos.getX() + "," + cleanPos.getY() + ")");
                System.out.println("[DEBUG] Registered at positions: " + washPos + " and " + cleanPos);
            } else {
                System.out.println("[WARNING] Could not find pair for washing station at " + pos1);
            }
        }

        // Verify after creation
        System.out.println("[DEBUG] Total stations after washing creation: " + stations.size());
        for (Map.Entry<Position, Station> entry : stations.entrySet()) {
            if (entry.getValue() instanceof WashingStation) {
                System.out.println("[DEBUG] Washing station found at: " + entry.getKey());
            }
        }
    }

    private void assignRandomizedIngredients(List<Position> positions) {
        IngredientType[] types = {
                IngredientType.DOUGH,
                IngredientType.TOMATO,
                IngredientType.CHEESE,
                IngredientType.SAUSAGE,
                IngredientType.CHICKEN
        };
        List<IngredientType> ingredientList = Arrays.asList(types);
        Collections.shuffle(ingredientList, random);

        for (int i = 0; i < positions.size(); i++) {
            Position pos = positions.get(i);
            IngredientType ingredientType = ingredientList.get(i % ingredientList.size());
            stations.put(pos, new IngredientStorage(pos, ingredientType));
        }
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

    public boolean inBounds(Position pos) {
        return inBounds(pos.getX(), pos.getY());
    }

    public Station getStationAt(Position pos) {
        return stations.get(pos);
    }

    public Station getStationAt(int x, int y) {
        if (!inBounds(x, y)) {
            return null;
        }

        Position pos = new Position(x, y);
        return stations.get(pos);
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

    private void debugPrintMapGrid(char[][] grid) {
        System.out.println("\nMAP GRID LAYOUT:");
        System.out.println("Legend: X=Wall, . =Floor, V=Spawn, C=Cut, R=Cook, A=Assembly, S=Serve, W=Wash, I=Ingredient, P=Plate, T=Trash");
        System.out.println("-------------------------------------------");
        for (int y = 0; y < GameMap.HEIGHT; y++) {
            System.out.print("Row " + y + ": ");
            for (int x = 0; x < GameMap.WIDTH; x++) {
                System.out.print(grid[y][x] + " ");
            }
            System.out.println();
        }
        System.out.println("-------------------------------------------\n");
    }

    public char getTile(Position pos) {
        return getTile(pos.getX(), pos.getY());
    }

    public boolean setTile(int x, int y, char tile) {
        if (!inBounds(x, y)) {
            return false;
        }
        grid[y][x] = tile;
        return true;
    }

    public boolean hasStation(int x, int y) {
        return getStationAt(x, y) != null;
    }

    public boolean isWall(int x, int y) {
        return getTile(x, y) == 'X';
    }

    public Map<Position, Station> getStations() {
        return stations;
    }
}
