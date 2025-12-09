package models.enums;

public enum StationType {
    CUTTING,
    COOKING,
    WASHING,
    ASSEMBLY,
    INGREDIENT_STORAGE,
    PLATE_STORAGE,
    SERVING_COUNTER,
    TRASH;

    public static StationType fromString(String s) {
        if (s == null) return null;
        try {
            return StationType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
