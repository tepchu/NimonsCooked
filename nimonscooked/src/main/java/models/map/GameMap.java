package models.map;

public class GameMap {

    public static final int WIDTH = 14;
    public static final int HEIGHT = 10;

    private final char[][] grid;

    public GameMap(char[][] grid) {
        this.grid = grid;
    }

    public char getTile(int x, int y) {
        return grid[y][x];
    }

    public boolean isWalkable(int x, int y) {
        char c = grid[y][x];
        return c == '.'
                || c == 'V'; // bisa tambah kondisi lain
    }
}
