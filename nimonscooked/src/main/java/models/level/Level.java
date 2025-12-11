package models.level;

import models.map.MapType;

public class Level {
    private final int id;
    private final String name;
    private final MapType mapType;
    private final int timeLimit;           // dalam detik
    private final int targetScore;
    private final int maxFailedOrders;
    private final Difficulty difficulty;
    private final int orderSpawnInterval;  // detik per order baru
    private final int maxActiveOrders;     // max order sekaligus
    private final int orderTimeout;

    private boolean completed;
    private int starsEarned;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public Level(int id, String name, MapType mapType, int timeLimit,
                 int targetScore, int maxFailedOrders, Difficulty difficulty,
                 int orderSpawnInterval, int maxActiveOrders, int orderTimeout) {
        this.id = id;
        this.name = name;
        this.mapType = mapType;
        this.timeLimit = timeLimit;
        this.targetScore = targetScore;
        this.maxFailedOrders = maxFailedOrders;
        this.difficulty = difficulty;
        this.orderSpawnInterval = orderSpawnInterval;
        this.maxActiveOrders = maxActiveOrders;
        this.orderTimeout = orderTimeout;
        this.completed = false;
        this.starsEarned = 0;
    }

    /**
     * Calculate stars based on score
     * 1 star = 1/3 of target score
     * 2 stars = 2/3 of target score
     * 3 stars = full target score (level cleared)
     */
    public int calculateStars(int finalScore) {
        if (finalScore >= targetScore) {
            return 3;
        } else if (finalScore >= (targetScore * 2 / 3)) {
            return 2;
        } else if (finalScore >= (targetScore / 3)) {
            return 1;
        }
        return 0;
    }

    public int getOneStarThreshold() {
        return targetScore / 3;
    }

    public int getTwoStarThreshold() {
        return (targetScore * 2) / 3;
    }

    public int getThreeStarThreshold() {
        return targetScore;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MapType getMapType() {
        return mapType;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public int getMaxFailedOrders() {
        return maxFailedOrders;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getOrderSpawnInterval() {
        return orderSpawnInterval;
    }

    public int getMaxActiveOrders() {
        return maxActiveOrders;
    }

    public int getOrderTimeout() {
        return orderTimeout;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getStarsEarned() {
        return starsEarned;
    }

    public void setStarsEarned(int stars) {
        this.starsEarned = Math.max(0, Math.min(3, stars));
        if (stars >= 3) {
            this.completed = true;
        }
    }

    public void updateStarsIfBetter(int newStars) {
        if (newStars > starsEarned) {
            setStarsEarned(newStars);
        }
    }
}