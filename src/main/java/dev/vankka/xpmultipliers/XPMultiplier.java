package dev.vankka.xpmultipliers;

public class XPMultiplier {

    private final double multiplier;
    private final String permission;
    private final int priority;

    public XPMultiplier(final double multiplier, final String permission, final int priority) {
        this.multiplier = multiplier;
        this.permission = permission;
        this.priority = priority;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public String getPermission() {
        return permission;
    }

    public int getPriority() {
        return priority;
    }
}
