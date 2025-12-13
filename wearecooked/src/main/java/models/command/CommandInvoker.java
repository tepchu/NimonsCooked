package models.command;

import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

/**
 * Command Pattern - Invoker
 * Manages command execution, history, and undo/redo functionality
 * <p>
 * Features:
 * - Command execution with validation
 * - Command history tracking (with size limit)
 * - Undo/Redo support
 * - Command listeners for event notification
 * - Statistics tracking
 *
 * @author Nimonscooked Team
 * @version 1.0
 */
public class CommandInvoker {
    private Stack<ChefCommand> commandHistory;
    private Stack<ChefCommand> undoneCommands;
    private int maxHistorySize;
    private List<CommandListener> listeners;

    // Statistics
    private int totalExecuted;
    private int totalFailed;
    private int totalUndone;
    private int totalRedone;

    /**
     * Default constructor with max history of 50 commands
     */
    public CommandInvoker() {
        this(50);
    }

    /**
     * Constructor with custom max history size
     *
     * @param maxHistorySize Maximum number of commands to keep in history
     */
    public CommandInvoker(int maxHistorySize) {
        this.commandHistory = new Stack<>();
        this.undoneCommands = new Stack<>();
        this.maxHistorySize = Math.max(10, maxHistorySize); // Minimum 10
        this.listeners = new ArrayList<>();

        // Initialize statistics
        this.totalExecuted = 0;
        this.totalFailed = 0;
        this.totalUndone = 0;
        this.totalRedone = 0;
    }

    /**
     * Execute a command and add to history
     *
     * @param command The command to execute
     * @return true if command executed successfully, false otherwise
     */
    public boolean executeCommand(ChefCommand command) {
        if (command == null) {
            System.err.println("[INVOKER] Cannot execute null command");
            return false;
        }

        // Execute the command
        boolean success = command.execute();

        if (success) {
            // Add to history
            commandHistory.push(command);

            // Clear redo stack after new command
            undoneCommands.clear();

            // Maintain max history size
            if (commandHistory.size() > maxHistorySize) {
                commandHistory.remove(0); // Remove oldest command
            }

            // Update statistics
            totalExecuted++;

            // Notify listeners
            notifyCommandExecuted(command);

            System.out.println("[INVOKER] ✓ Executed: " + command.getDescription());
        } else {
            // Update statistics
            totalFailed++;

            System.out.println("[INVOKER] ✗ Failed: " + command.getDescription());
        }

        return success;
    }

    /**
     * Undo the last command
     *
     * @return true if undo was successful, false otherwise
     */
    public boolean undo() {
        if (commandHistory.isEmpty()) {
            System.out.println("[INVOKER] Nothing to undo");
            return false;
        }

        ChefCommand command = commandHistory.pop();

        try {
            command.undo();
            undoneCommands.push(command);

            // Update statistics
            totalUndone++;

            System.out.println("[INVOKER] ↶ Undone: " + command.getDescription());
            return true;
        } catch (Exception e) {
            // If undo fails, restore to history
            commandHistory.push(command);
            System.err.println("[INVOKER] Undo failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Redo the last undone command
     *
     * @return true if redo was successful, false otherwise
     */
    public boolean redo() {
        if (undoneCommands.isEmpty()) {
            System.out.println("[INVOKER] Nothing to redo");
            return false;
        }

        ChefCommand command = undoneCommands.pop();

        // Re-execute the command
        if (command.execute()) {
            commandHistory.push(command);

            // Update statistics
            totalRedone++;

            System.out.println("[INVOKER] ↷ Redone: " + command.getDescription());
            return true;
        } else {
            // If redo fails, restore to undone stack
            undoneCommands.push(command);
            System.out.println("[INVOKER] Redo failed: " + command.getDescription());
            return false;
        }
    }

    /**
     * Undo multiple commands
     *
     * @param count Number of commands to undo
     * @return Number of commands actually undone
     */
    public int undoMultiple(int count) {
        int undoneCount = 0;
        for (int i = 0; i < count && !commandHistory.isEmpty(); i++) {
            if (undo()) {
                undoneCount++;
            } else {
                break;
            }
        }
        return undoneCount;
    }

    /**
     * Clear all command history and redo stack
     */
    public void clearHistory() {
        commandHistory.clear();
        undoneCommands.clear();
        System.out.println("[INVOKER] History cleared");
    }

    /**
     * Get command history size
     *
     * @return Number of commands in history
     */
    public int getHistorySize() {
        return commandHistory.size();
    }

    /**
     * Get redo stack size
     *
     * @return Number of commands that can be redone
     */
    public int getRedoSize() {
        return undoneCommands.size();
    }

    /**
     * Check if undo is available
     *
     * @return true if there are commands to undo
     */
    public boolean canUndo() {
        return !commandHistory.isEmpty();
    }

    /**
     * Check if redo is available
     *
     * @return true if there are commands to redo
     */
    public boolean canRedo() {
        return !undoneCommands.isEmpty();
    }

    /**
     * Get last executed command description
     *
     * @return Description of last command, or "None" if history is empty
     */
    public String getLastCommandDescription() {
        if (commandHistory.isEmpty()) {
            return "None";
        }
        return commandHistory.peek().getDescription();
    }

    /**
     * Get command history as list (newest first)
     *
     * @param limit Maximum number of commands to return
     * @return List of command descriptions
     */
    public List<String> getRecentHistory(int limit) {
        List<String> history = new ArrayList<>();
        int count = Math.min(limit, commandHistory.size());

        for (int i = commandHistory.size() - 1; i >= commandHistory.size() - count; i--) {
            history.add(commandHistory.get(i).getDescription());
        }

        return history;
    }

    /**
     * Get all command types executed in history
     *
     * @return List of command type names
     */
    public List<String> getCommandTypes() {
        List<String> types = new ArrayList<>();
        for (ChefCommand cmd : commandHistory) {
            String type = cmd.getType();
            if (!types.contains(type)) {
                types.add(type);
            }
        }
        return types;
    }

    /**
     * Add a command listener
     *
     * @param listener The listener to add
     */
    public void addListener(CommandListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a command listener
     *
     * @param listener The listener to remove
     */
    public void removeListener(CommandListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners about command execution
     */
    private void notifyCommandExecuted(ChefCommand command) {
        for (CommandListener listener : listeners) {
            try {
                listener.onCommandExecuted(command);
            } catch (Exception e) {
                System.err.println("[INVOKER] Listener error: " + e.getMessage());
            }
        }
    }

    /**
     * Get execution statistics
     *
     * @return Statistics summary string
     */
    public String getStatistics() {
        return String.format(
                "Commands Executed: %d | Failed: %d | Undone: %d | Redone: %d | In History: %d",
                totalExecuted, totalFailed, totalUndone, totalRedone, commandHistory.size()
        );
    }

    /**
     * Get total commands executed
     */
    public int getTotalExecuted() {
        return totalExecuted;
    }

    /**
     * Get total commands failed
     */
    public int getTotalFailed() {
        return totalFailed;
    }

    /**
     * Get total undos performed
     */
    public int getTotalUndone() {
        return totalUndone;
    }

    /**
     * Get total redos performed
     */
    public int getTotalRedone() {
        return totalRedone;
    }

    /**
     * Reset all statistics
     */
    public void resetStatistics() {
        totalExecuted = 0;
        totalFailed = 0;
        totalUndone = 0;
        totalRedone = 0;
    }

    /**
     * Set maximum history size
     *
     * @param size New maximum size (minimum 10)
     */
    public void setMaxHistorySize(int size) {
        this.maxHistorySize = Math.max(10, size);

        // Trim history if needed
        while (commandHistory.size() > maxHistorySize) {
            commandHistory.remove(0);
        }
    }

    /**
     * Get maximum history size
     */
    public int getMaxHistorySize() {
        return maxHistorySize;
    }

    /**
     * Print command history to console (for debugging)
     */
    public void printHistory() {
        System.out.println("\n========== COMMAND HISTORY ==========");
        System.out.println("Total Commands: " + commandHistory.size());
        System.out.println("Can Undo: " + canUndo());
        System.out.println("Can Redo: " + canRedo());
        System.out.println("\nRecent Commands (newest first):");

        List<String> recent = getRecentHistory(10);
        for (int i = 0; i < recent.size(); i++) {
            System.out.println((i + 1) + ". " + recent.get(i));
        }

        System.out.println("\n" + getStatistics());
        System.out.println("=====================================\n");
    }

    /**
     * Listener interface for command execution events
     */
    public interface CommandListener {
        /**
         * Called when a command is executed
         *
         * @param command The command that was executed
         */
        void onCommandExecuted(ChefCommand command);
    }
}