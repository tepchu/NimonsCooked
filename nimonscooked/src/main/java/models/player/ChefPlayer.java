package models.player;


import models.core.Direction;
import models.core.Position;
import models.item.Item;
import models.station.Station;

public class ChefPlayer {

    private final String id;
    private final String name;

    private Position position;
    private Direction direction;

    private Item inventory;          // bisa null
    private CurrentAction currentAction;

    private boolean busy;
    private Thread busyThread;

    private long lastDashTime = 0;
    private static final long DASH_COOLDOWN_MS = 3000; // 3 second cooldown
    private static final int DASH_DISTANCE = 3; // Move 3 tiles at once

    private long busyStartTime = 0;
    private int busyDurationSec = 0;

    public ChefPlayer(String id, String name, Position startPos) {
        this.id = id;
        this.name = name;
        this.position = startPos;
        this.direction = Direction.DOWN;
        this.currentAction = CurrentAction.IDLE;
        this.busy = false;
    }

    // ===================== MOVEMENT =====================
    public void move(Direction dir) {
        if (busy) return;

        this.direction = dir;
        switch (dir) {
            case UP -> position.setY(position.getY() - 1);
            case DOWN -> position.setY(position.getY() + 1);
            case LEFT -> position.setX(position.getX() - 1);
            case RIGHT -> position.setX(position.getX() + 1);
        }
        currentAction = CurrentAction.MOVING;
    }

    // ===================== INVENTORY =====================
    public boolean hasItem() {
        return inventory != null;
    }

    public Item getInventory() {
        return inventory;
    }

    public boolean pickUp(Item item) {
        if (inventory != null) return false;
        this.inventory = item;
        return true;
    }

    public Item drop() {
        Item tmp = inventory;
        inventory = null;
        return tmp;
    }

    // ===================== BUSY STATE =====================
    public boolean isBusy() {
        return busy;
    }

    public CurrentAction getCurrentAction() {
        return currentAction;
    }

    public void startBusy(CurrentAction action, int durationSec, Runnable onFinish) {
        if (busy) return;

        busy = true;
        currentAction = action;

        busyStartTime = System.currentTimeMillis();
        busyDurationSec = durationSec;

        busyThread = new Thread(() -> {
            try {
                Thread.sleep(durationSec * 1000L);
            } catch (InterruptedException ignored) {
                // kalau mau: simpan progress di station,
                // di sini kita cuma berhenti.
            } finally {
                busy = false;
                currentAction = CurrentAction.IDLE;
                if (onFinish != null) {
                    onFinish.run();
                }
            }
        });
        busyThread.start();
    }

    public double getBusyProgress() {
        if (!busy || busyDurationSec <= 0) {
            return 0.0;
        }
        long elapsed = System.currentTimeMillis() - busyStartTime;
        double progress = (double) elapsed / (busyDurationSec * 1000L);
        return Math.min(1.0, Math.max(0.0, progress));
    }

    public int getBusyTimeRemaining() {
        if (!busy || busyDurationSec <= 0) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - busyStartTime;
        long remaining = (busyDurationSec * 1000L) - elapsed;
        return (int) Math.max(0, (remaining + 999) / 1000);
    }

    public int getBusyDuration() {
        return busyDurationSec;
    }

    // ===================== INTERACTION =====================
    public void interact(Station station) {
        station.interact(this);
    }

    // ===================== GETTERS =====================
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction dir) {
        this.direction = dir;
    }

    public boolean canDash() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastDashTime) >= DASH_COOLDOWN_MS;
    }

    public void recordDash() {
        lastDashTime = System.currentTimeMillis();
    }

    public long getDashCooldownRemaining() {
        long elapsed = System.currentTimeMillis() - lastDashTime;
        long remaining = DASH_COOLDOWN_MS - elapsed;
        return Math.max(0, remaining);
    }
}
