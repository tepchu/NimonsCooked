package models. level;

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

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public Level(int id, String name, MapType mapType, int timeLimit,
                 int targetScore, int maxFailedOrders, Difficulty difficulty,
                 int orderSpawnInterval, int maxActiveOrders, int orderTimeout) {
        this. id = id;
        this.name = name;
        this.mapType = mapType;
        this.timeLimit = timeLimit;
        this.targetScore = targetScore;
        this.maxFailedOrders = maxFailedOrders;
        this.difficulty = difficulty;
        this. orderSpawnInterval = orderSpawnInterval;
        this.maxActiveOrders = maxActiveOrders;
        this.orderTimeout = orderTimeout;
        this.completed = false;
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
}