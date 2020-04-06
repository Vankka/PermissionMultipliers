package dev.vankka.permissionmultipliers.multiplier;

public class Multiplier {

    private final double multiplier;
    private final String permission;

    public Multiplier(final double multiplier, final String permission) {
        this.multiplier = multiplier;
        this.permission = permission;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public String getPermission() {
        return permission;
    }
}
