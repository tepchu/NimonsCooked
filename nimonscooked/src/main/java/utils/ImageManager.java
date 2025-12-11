package utils;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class ImageManager {
    private static ImageManager instance;
    private Map<String, Image> imageCache;
    private boolean useImages;

    private ImageManager() {
        imageCache = new HashMap<>();
        useImages = true;
        loadAllImages();
    }

    public static ImageManager getInstance() {
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }

    private void loadAllImages() {
        try {
            loadImage("main_menu_bg", "/images/backgrounds/main_menu_bg.png");
//            loadImage("game_bg", "/images/backgrounds/game_bg.png");

//            loadImage("logo", "/images/ui/logo.png");
//            loadImage("score_icon", "/images/ui/score_icon.png");
//            loadImage("timer_icon", "/images/ui/timer_icon.png");

            loadImage("cutting", "/images/stations/cutting.png", 50, 50);
//            loadImage("cooking", "/images/stations/cooking.png", 50, 50);
//            loadImage("assembly", "/images/stations/assembly.png", 50, 50);
//            loadImage("serving", "/images/stations/serving.png", 50, 50);
//            loadImage("washing", "/images/stations/washing.png", 50, 50);
//            loadImage("ingredient_storage", "/images/stations/ingredient_storage.png", 50, 50);
//            loadImage("plate_storage", "/images/stations/plate_storage.png", 50, 50);
//            loadImage("trash", "/images/stations/trash.png", 50, 50);
            loadImage("wall", "/images/stations/wall.png", 50, 50);
//            loadImage("floor", "/images/stations/floor.png", 50, 50);

            String[] ingredients = {"dough", "tomato", "cheese", "sausage", "chicken"};
            String[] states = {"raw", "chopped"};
            for (String ing : ingredients) {
                for (String state : states) {
                    String key = ing + "_" + state;
                    loadImage(key, "/images/ingredients/" + key + ".png", 50, 50);
                }
            }

            String[] chefs = {"chef1", "chef2"};
            String[] directions = {"up", "down", "left", "right"};
            for (String chef : chefs) {
                for (String dir : directions) {
                    String key = chef + "_" + dir;
                    loadImage(key, "/images/players/" + key + ".png", 50, 50);
                }
            }

            loadImage("pizza_margherita", "/images/pizzas/pizza_margherita.png", 40, 40);
            loadImage("pizza_sosis", "/images/pizzas/pizza_sosis.png", 40, 40);
            loadImage("pizza_ayam", "/images/pizzas/pizza_ayam.png", 40, 40);

            System.out.println("✓ All images loaded successfully");
        } catch (Exception e) {
            System.err.println("⚠ Failed to load some images, using fallback colors");
            useImages = false;
        }
    }

    private void loadImage(String key, String path) {
        try {
            var resource = getClass().getResourceAsStream(path);
            if (resource != null) {
                Image img = new Image(resource);
                imageCache.put(key, img);
            } else {
                System.out.println("Image not found: " + path);
            }
        } catch (Exception e) {
            System.out.println("Failed to load: " + path);
        }
    }

    private void loadImage(String key, String path, int width, int height) {
        try {
            var resource = getClass().getResourceAsStream(path);
            if (resource != null) {
                Image img = new Image(resource, width, height, true, true);
                imageCache.put(key, img);
            } else {
                System.out.println("Image not found: " + path);
            }
        } catch (Exception e) {
            System.out.println("Failed to load: " + path);
        }
    }

    public Image getImage(String key) {
        return imageCache.get(key);
    }

    public boolean hasImage(String key) {
        return imageCache.containsKey(key) && imageCache.get(key) != null;
    }

    public boolean isUsingImages() {
        return useImages;
    }
}