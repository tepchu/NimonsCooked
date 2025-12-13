package utils;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized Image Manager - Singleton Pattern
 * Loads and caches ALL game images at startup
 */
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
        System.out.println("[ImageManager] Starting to load all images...");

        try {
            // CHEF 1 - Base directions
            loadImage("chef1_front", "/images/chef1/chef1_front.png");
            loadImage("chef1_back", "/images/chef1/chef1_back.png");
            loadImage("chef1_left", "/images/chef1/chef1_left.png");
            loadImage("chef1_right", "/images/chef1/chef1_right.png");

            // CHEF 1 - With items (Front)
            loadImage("chef1_front_plate", "/images/chef1/chef1_front_plate.png");
            loadImage("chef1_front_cheese", "/images/chef1/chef1_front_cheese.png");
            loadImage("chef1_front_chicken", "/images/chef1/chef1_front_chicken.png");
            loadImage("chef1_front_cooked_chicken", "/images/chef1/chef1_front_cooked_chicken.png");
            loadImage("chef1_front_dough", "/images/chef1/chef1_front_dough.png");
            loadImage("chef1_front_sausage", "/images/chef1/chef1_front_sausage.png");
            loadImage("chef1_front_tomato", "/images/chef1/chef1_front_tomato.png");

            // CHEF 1 - With items (Left)
            loadImage("chef1_left_plate", "/images/chef1/chef1_left_plate.png");
            loadImage("chef1_left_cheese", "/images/chef1/chef1_left_cheese.png");
            loadImage("chef1_left_chicken", "/images/chef1/chef1_left_chicken.png");
            loadImage("chef1_left_cooked_chicken", "/images/chef1/chef1_left_cooked_chicken.png");
            loadImage("chef1_left_dough", "/images/chef1/chef1_left_dough.png");
            loadImage("chef1_left_sausage", "/images/chef1/chef1_left_sausage.png");
            loadImage("chef1_left_tomato", "/images/chef1/chef1_Left_tomato.png");

            // CHEF 1 - With items (Right)
            loadImage("chef1_right_plate", "/images/chef1/chef1_right_plate.png");
            loadImage("chef1_right_cheese", "/images/chef1/chef1_right_cheese.png");
            loadImage("chef1_right_chicken", "/images/chef1/chef1_right_chicken.png");
            loadImage("chef1_right_cooked_chicken", "/images/chef1/chef1_right_cooked_chicken.png");
            loadImage("chef1_right_dough", "/images/chef1/chef1_right_dough.png");
            loadImage("chef1_right_sausage", "/images/chef1/chef1_right_sausage.png");
            loadImage("chef1_right_tomato", "/images/chef1/chef1_right_tomato.png");

            // CHEF 2 - Base directions
            loadImage("chef2_front", "/images/chef2/chef2_front.png");
            loadImage("chef2_back", "/images/chef2/chef2_back.png");
            loadImage("chef2_left", "/images/chef2/chef2_left.png");
            loadImage("chef2_right", "/images/chef2/chef2_right.png");

            // CHEF 2 - With items (Front)
            loadImage("chef2_front_plate", "/images/chef2/chef2_front_plate.png");
            loadImage("chef2_front_cheese", "/images/chef2/chef2_front_cheese.png");
            loadImage("chef2_front_chicken", "/images/chef2/chef2_front_chicken.png");
            loadImage("chef2_front_cooked_chicken", "/images/chef2/chef2_front_cooked_chicken.png");
            loadImage("chef2_front_dough", "/images/chef2/chef2_front_dough.png");
            loadImage("chef2_front_sausage", "/images/chef2/chef2_front_sausage.png");
            loadImage("chef2_front_tomato", "/images/chef2/chef2_front_tomato.png");

            // CHEF 2 - With items (Left)
            loadImage("chef2_left_plate", "/images/chef2/chef2_left_plate.png");
            loadImage("chef2_left_cheese", "/images/chef2/chef2_left_cheese.png");
            loadImage("chef2_left_chicken", "/images/chef2/chef2_left_chicken.png");
            loadImage("chef2_left_cooked_chicken", "/images/chef2/chef2_left_cooked_chicken.png");
            loadImage("chef2_left_dough", "/images/chef2/chef2_left_dough.png");
            loadImage("chef2_left_sausage", "/images/chef2/chef2_left_sausage.png");
            loadImage("chef2_left_tomato", "/images/chef2/chef2_Left_tomato.png");

            // CHEF 2 - With items (Right)
            loadImage("chef2_right_plate", "/images/chef2/chef2_right_plate.png");
            loadImage("chef2_right_cheese", "/images/chef2/chef2_right_cheese.png");
            loadImage("chef2_right_chicken", "/images/chef2/chef2_right_chicken.png");
            loadImage("chef2_right_cooked_chicken", "/images/chef2/chef2_right_cooked_chicken.png");
            loadImage("chef2_right_dough", "/images/chef2/chef2_right_dough.png");
            loadImage("chef2_right_sausage", "/images/chef2/chef2_right_sausage.png");
            loadImage("chef2_right_tomato", "/images/chef2/chef2_right_tomato.png");

            // INGREDIENTS - Raw
            loadImage("ingredient_dough_raw", "/images/ingredients/dough_raw.png");
            loadImage("ingredient_tomato_raw", "/images/ingredients/tomato_raw.png");
            loadImage("ingredient_cheese_raw", "/images/ingredients/cheese_raw.png");
            loadImage("ingredient_sausage_raw", "/images/ingredients/sausage_raw.png");
            loadImage("ingredient_chicken_raw", "/images/ingredients/chicken_raw.png");

            // INGREDIENTS - Chopped
            loadImage("ingredient_dough_chopped", "/images/ingredients/dough_slice.png");
            loadImage("ingredient_tomato_chopped", "/images/ingredients/tomato_slice.png");
            loadImage("ingredient_cheese_chopped", "/images/ingredients/cheese_slice.png");
            loadImage("ingredient_sausage_chopped", "/images/ingredients/sausage_slice.png");
            loadImage("ingredient_chicken_chopped", "/images/ingredients/chicken_slice.png");

            // PIZZAS
            loadImage("pizza_margherita", "/images/pizza/pizza_margherita.png");
            loadImage("pizza_sosis", "/images/pizza/pizza_sosis.png");
            loadImage("pizza_ayam", "/images/pizza/pizza_ayam.png");
            loadImage("pizza_burned", "/images/pizza/pizza_burned.png");

            // STATIONS
            loadImage("station_assembly", "/images/stations/assembly.png");
            loadImage("station_washing", "/images/stations/washing.png");
            loadImage("station_cutting", "/images/stations/cutting.png");
            loadImage("station_cooking", "/images/stations/oven.png");
            loadImage("station_serving", "/images/stations/serving.png");
            loadImage("station_plate", "/images/stations/plate.png");
            loadImage("station_trash", "/images/stations/trash.png");

            // INGREDIENT STORAGES
            loadImage("station_ingredient_tomato", "/images/stations/tomato_storage.png");
            loadImage("station_ingredient_cheese", "/images/stations/cheese_storage.png");
            loadImage("station_ingredient_dough", "/images/stations/dough_storage.png");
            loadImage("station_ingredient_sausage", "/images/stations/sausage_storage.png");
            loadImage("station_ingredient_chicken", "/images/stations/chicken_storage.png");

            // MAP TILES
            loadImage("floor", "/images/stations/floor.png");
            loadImage("wall", "/images/stations/wall.png");

            // UTENSILS
            loadImage("plate_empty", "/images/utensils/plate.png");
            loadImage("plate_dirty", "/images/utensils/plate_dirty.png");

            // MENU IMAGES
            loadImage("menu_background", "/images/menu/background.png");
            loadImage("button_start", "/images/menu/button_start.png");
            loadImage("button_start_hover", "/images/menu/button_start_hover.png");
            loadImage("button_howtoplay", "/images/menu/button_howtoplay.png");
            loadImage("button_howtoplay_hover", "/images/menu/button_howtoplay_hover.png");
            loadImage("button_exit", "/images/menu/button_exit.png");
            loadImage("button_exit_hover", "/images/menu/button_exit_hover.png");

            // RESULT SCREEN IMAGES
            loadImage("level_succeed_bg", "/images/results/level_succeed_bg.png");
            loadImage("level_fail_bg", "/images/results/level_fail_bg.png");
            loadImage("star_empty", "/images/results/star_empty.png");
            loadImage("star_filled", "/images/results/star_filled.png");
            loadImage("congrats_text", "/images/results/congrats_text.png");
            loadImage("failed_text", "/images/results/failed_text.png");

            System.out.println("[ImageManager] ✓ Successfully loaded " + imageCache.size() + " images");

            if (imageCache.isEmpty()) {
                useImages = false;
                System.out.println("[ImageManager] No images loaded, using fallback colors");
            }

        } catch (Exception e) {
            useImages = false;
            System.err.println("[ImageManager] ⚠ Failed to load images: " + e.getMessage());
            System.err.println("[ImageManager] Using fallback colors");
        }
    }

    private void loadImage(String key, String path) {
        try {
            var resource = getClass().getResourceAsStream(path);
            if (resource != null) {
                Image img = new Image(resource);
                if (!img.isError()) {
                    imageCache.put(key, img);
                } else {
                    System.out.println("[ImageManager] ✗ Error loading: " + path);
                }
            } else {
                System.out.println("[ImageManager] ✗ Not found: " + path);
            }
        } catch (Exception e) {
            System.out.println("[ImageManager] ✗ Failed: " + path + " - " + e.getMessage());
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

    /**
     * Get total number of cached images
     */
    public int getImageCount() {
        return imageCache.size();
    }

    /**
     * Clear all cached images (for memory management if needed)
     */
    public void clearCache() {
        imageCache.clear();
        System.out.println("[ImageManager] Cache cleared");
    }
}