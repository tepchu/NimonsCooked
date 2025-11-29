package models.map;

public class MapLoader {
    public static GameMap loadPizzaMap() {
        char[][] layout = {
                {'X','A','T','A','C','A','A','A','C','A','A','A','X','X'},
                {'X','.','.','.','.','.','.','.','.','.','.','.','X','X'},
                {'X','.','.','.','.','.','A','.','V','.','.','.','.','X'},
                {'X','.','.','.','.','.','.','.','.','.','.','.','.','X'},
                {'X','W','W','A','I','A','I','A','I','A','I','A','P','X'},
                {'X','.','.','.','.','.','.','.','.','.','.','.','.','X'},
                {'X','X','X','X','.','.','A','.','.','.','.','X','X','X'},
                {'X','R','.','.','.','V','.','.','.','.','.','.','.','X'},
                {'X','X','X','X','.','.','.','.','.','.','.','.','X','X'},
                {'X','X','X','X','A','A','I','A','A','A','X','X','X','X'}
        };

        return new GameMap(layout);
    }
    /*
     * Map Legend:
     * X = Wall
     * . = Floor (walkable)
     * V = Chef spawn point
     * C = Cutting Station
     * R = Cooking Station (Oven)
     * A = Assembly Station
     * S = Serving Counter
     * W = Washing Station
     * I = Ingredient Storage
     * P = Plate Storage
     * T = Trash Station
     */
}
