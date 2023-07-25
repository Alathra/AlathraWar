package com.github.alathra.AlathranWars.listeners;

import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BlockBreakListener implements Listener {
    private final static Set<Material> allowedBlocks = Set.of(
        Material.TNT,
        // Mortar & Cannons
        Material.IRON_BLOCK,
        Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
        Material.TORCH,
        Material.STONE_BUTTON,
        Material.BLACK_WOOL
    );

    @EventHandler
    public void onBlockBreak(final @NotNull BlockBreakEvent e) {
        final @NotNull Block block = e.getBlock();
        /*if (SiegeData.getSieges().isEmpty() *//*&& RaidData.getRaids().isEmpty()*//*) {
            return;
        }*/

        if (allowedBlocks.contains(block.getType())) {
            return;
        }

        if (e.getPlayer().hasPermission("AlathranWars.break")) {
            return;
        }

        /*for (final Siege siege : WarManager.getInstance().getSieges()) {
            if (siege.beaconLocations.contains(block.getLocation())) {
                e.setCancelled(true);
                return;
            }
        }*/

        if (WorldCoord.parseWorldCoord(block).getTownOrNull() != null) {
            /*for (final Siege siege : WarManager.getInstance().getSieges()) {
                if (siege.getTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
                    e.getPlayer().sendMessage(ColorParser.of("<red>You can not break blocks during sieges").parseLegacy().build());
                    e.setCancelled(true);
                    return;
                }
            }*/

            /*for (final OldRaid oldRaid : RaidData.getRaids()) {
                if (oldRaid.getRaidedTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
                    p.sendMessage(Helper.color("<red>You can not break blocks during raids"));
                    event.setCancelled(true);
                    return;
                }
            }*/
        }
    }

    @EventHandler
    public void onBlockPlace(final @NotNull BlockPlaceEvent event) {
        final @NotNull Block block = event.getBlock();
        /*if (SiegeData.getSieges().isEmpty() && RaidData.getRaids().isEmpty()) {
            return;
        }*/
        if (allowedBlocks.contains(block.getType())) {
            return;
        }

        @NotNull Player p = event.getPlayer();
        if (p.hasPermission("AlathranWars.place")) {
            return;
        }

        if (WorldCoord.parseWorldCoord(block).getTownOrNull() != null) {
            /*for (final Siege siege : WarManager.getInstance().getSieges()) {
                if (siege.getTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
                    p.sendMessage(ColorParser.of("<red>You can not place blocks during sieges").parseLegacy().build());
                    event.setCancelled(true);
                    return;
                }
            }*/

            /*for (final OldRaid oldRaid : RaidData.getRaids()) {
                if (oldRaid.getRaidedTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
                    p.sendMessage(Helper.color("<red>You can not place blocks during raids"));
                    event.setCancelled(true);
                    return;
                }
            }*/
        }
    }
}
