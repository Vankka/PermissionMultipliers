/*
 * PermissionMultipliers Bukkit plugin for giving players multipliers based on permissions
 * Copyright (C) 2020  Vankka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
