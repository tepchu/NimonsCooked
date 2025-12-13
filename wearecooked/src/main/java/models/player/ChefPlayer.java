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

    private Item inventory;
    private CurrentAction currentAction;

    private double visualX;
    private double visualY;
    private boolean isMoving;
    private int targetX;
    private int targetY;
    private static final double MOVE_SPEED = 0.15;
    private static final double DASH_SPEED = 0.5;
    private boolean isDashing;

    private boolean busy;
    private Thread busyThread;
    private boolean interrupted; // Track if action was interrupted

    private long lastDashTime = 0;
    private static final long DASH_COOLDOWN_MS = 3000;
    private static final int DASH_DISTANCE = 3;

    private long busyStartTime = 0;
    private int busyDurationSec = 0;

    public ChefPlayer(String id, String name, Position startPos) {
        this.id = id;
        this.name = name;
        this.position = startPos;
        this.visualX = startPos.getX();
        this.visualY = startPos.getY();
        this.targetX = startPos.getX();
        this.targetY = startPos.getY();
        this.direction = Direction.DOWN;
        this.currentAction = CurrentAction.IDLE;
        this.busy = false;
        this.interrupted = false;
    }

    // ===================== SMOOTH MOVEMENT =====================

    public void startMove(int newX, int newY, boolean dash) {
        if (isMoving || isBusy()) return;

        this.targetX = newX;
        this.targetY = newY;
        this.isMoving = true;
        this.isDashing = dash;
        this.currentAction = CurrentAction.MOVING;
    }

    public void updateMovement() {
        if (!isMoving) return;

        double speed = isDashing ? DASH_SPEED : MOVE_SPEED;
        double dx = targetX - visualX;
        double dy = targetY - visualY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.05) {
            visualX = targetX;
            visualY = targetY;
            position.setX(targetX);
            position.setY(targetY);
            isMoving = false;
            isDashing = false;
            currentAction = CurrentAction.IDLE;
        } else {
            double moveDistance = Math.min(speed, distance);
            visualX += (dx / distance) * moveDistance;
            visualY += (dy / distance) * moveDistance;
        }
    }

    public void teleportTo(int x, int y) {
        this.position.setX(x);
        this.position.setY(y);
        this.visualX = x;
        this.visualY = y;
        this.targetX = x;
        this.targetY = y;
        this.isMoving = false;
        this.isDashing = false;
    }

    // ===================== MOVEMENT =====================
    public void move(Direction dir) {
        if (isMoving) return;

        this.direction = dir;
        int newX = position.getX();
        int newY = position.getY();

        switch (dir) {
            case UP -> newY--;
            case DOWN -> newY++;
            case LEFT -> newX--;
            case RIGHT -> newX++;
        }

        startMove(newX, newY, false);
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
        interrupted = false; // Reset interrupted flag
        currentAction = action;

        busyStartTime = System.currentTimeMillis();
        busyDurationSec = durationSec;

        busyThread = new Thread(() -> {
            try {
                Thread.sleep(durationSec * 1000L);

                // Only run callback if NOT interrupted
                if (!interrupted && onFinish != null) {
                    onFinish.run();
                }
            } catch (InterruptedException e) {
                // Thread was interrupted - DON'T run callback
                interrupted = true;
            } finally {
                // Only clear state if not interrupted
                // If interrupted, state should be preserved for progress saving
                if (!interrupted) {
                    busy = false;
                    currentAction = CurrentAction.IDLE;
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

    /**
     * Interrupt busy action and save elapsed time
     * Returns elapsed time in seconds
     */
    public int interruptBusy() {
        if (!busy || busyThread == null) return 0;

        interrupted = true; // Set flag BEFORE interrupting
        busyThread.interrupt();

        long elapsed = System.currentTimeMillis() - busyStartTime;
        int elapsedSec = (int) (elapsed / 1000);

        // Clear busy state after calculating elapsed time
        busy = false;
        currentAction = CurrentAction.IDLE;

        System.out.println("[CHEF] Interrupted - elapsed: " + elapsedSec + "s");
        return elapsedSec;
    }

    /**
     * Get elapsed busy time without interrupting
     */
    public int getBusyElapsedTime() {
        if (!busy) return 0;
        long elapsed = System.currentTimeMillis() - busyStartTime;
        return (int) (elapsed / 1000);
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

    public double getVisualX() {
        return visualX;
    }

    public double getVisualY() {
        return visualY;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public boolean isDashing() {
        return isDashing;
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