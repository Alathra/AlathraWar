package me.ShermansWorld.AlathraWar.listeners;

import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.items.WarItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class PlayerInteractListener implements Listener {

    //map of broken doors
    public Map<Location, Long> brokenDoors = new HashMap<Location, Long>();

    /**
     * @param event event
     * @author DunnoConz
     * @author AubriTheHuman
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block clicked = event.getClickedBlock();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (action == Action.LEFT_CLICK_BLOCK) {
            Bukkit.getLogger().info("LEFT CLICK BLOCK");
            if (clicked != null) {
                Bukkit.getLogger().info("NOT NULL");
                if (clicked.getType().toString().contains("DOOR")) { // left click + any door types
                    Bukkit.getLogger().info("DOOR");
                    Door door = (Door) clicked.getBlockData();
                    Bukkit.getLogger().info("DOOR IS STATE");
                    // lock door in the opposite position
                    if (item.equals(WarItems.getOrNull("ram"))) {
                        Bukkit.getLogger().info("IS RAM");
                        boolean inSiegeOrRaid = false;
                        //siege check
                        for (Siege s : SiegeData.getSieges()) {
                            for (TownBlock townBlock : s.getTown().getTownBlocks()) {
                                if (WorldCoord.parseWorldCoord(clicked).equals(townBlock.getWorldCoord())) {
                                    // if we find one, just end no need to continue
                                    inSiegeOrRaid = true;
                                    break;
                                }
                            }
                        }

                        Bukkit.getLogger().info("FOUND FROM SIEGE? : " + inSiegeOrRaid);
                        //if it wasnt in a siege, then
                        if (!inSiegeOrRaid) {
                            for (Raid r : RaidData.getRaids()) {
                                for (TownBlock townBlock : r.getRaidedTown().getTownBlocks()) {
                                    if (WorldCoord.parseWorldCoord(clicked).equals(townBlock.getWorldCoord())) {
                                        // if we find one, just end no need to continue
                                        inSiegeOrRaid = true;
                                        break;
                                    }
                                }
                            }
                        }

                        Bukkit.getLogger().info("FOUND FROM RAID? : " + inSiegeOrRaid);
                        if (inSiegeOrRaid) {
                            Bukkit.getLogger().info("ARE IN ONE");
                            if (doorBroken(clicked, door)) {
                                Bukkit.getLogger().info("DOOR BROKEN");
                                player.sendMessage(Helper.chatLabel() + Helper.color("&cThe door is already broken!"));
                                event.setCancelled(true);
                                return;
                            }
                            Bukkit.getLogger().info("BREAKING DOOR");
                            player.sendMessage(Helper.chatLabel() + Helper.color("&eBreak it down alright!"));
                            brokenDoors.put(getDoorPos(clicked, door), System.currentTimeMillis() + (1000L * Main.getInstance().getConfig().getInt("batteringRamEffectiveness")));
                            Bukkit.getLogger().info("SAVED STATE");
                            door.setOpen(!door.isOpen());
                            clicked.setBlockData(door);
                            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                            event.setCancelled(true);
                        } else {
                            Bukkit.getLogger().info("NOT EITHER SIEG OR RAIDE");
                            player.sendMessage(Helper.chatLabel() + Helper.color("&cThis item can only be used in a siege or raid!"));
                            event.setCancelled(true);
                        }
                        return;
                    }
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRClick(final PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item.equals(WarItems.getOrNull("ram"))) {
            event.setCancelled(true);
        }
    }


    /**
     * @param event event
     * @author DunnoConz
     * @author AubriTheHuman
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRClick(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block clicked = event.getClickedBlock();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Bukkit.getLogger().info("RIGHT CLICK BLOCK");
            if (clicked != null) {
                Bukkit.getLogger().info("NOT NULL");
                if (clicked.getType().toString().contains("DOOR")) {
                    Bukkit.getLogger().info("DOOR FOUND");
                    Door door = (Door) clicked.getBlockData();
                    if (doorBroken(clicked, door)) {
                        Bukkit.getLogger().info("DOOR BROKEN");
                        player.sendMessage(Helper.chatLabel() + Helper.color("Door is broken! " + String.valueOf(System.currentTimeMillis()) + " " + getDoorPos(clicked, door).toString()));
                        door.setOpen(!door.isOpen());
                        clicked.setBlockData(door);
                        event.setCancelled(false);

                        return;
                    }
                }
            }

            if (item.equals(WarItems.getOrNull("ram"))) {
                if (clicked != null) {
                    if (clicked.getType().name().equals("GRASS_BLOCK")
                            || clicked.getType().name().equals("DIRT")
                            || clicked.getType().name().equals("COARSE_DIRT")
                            || clicked.getType().name().equals("ROOTED_DIRT")
                            || clicked.getType().name().equals("DIRT_PATH")) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    private Location getDoorPos(Block clicked, Door door) {
        if(door.getHalf() == Bisected.Half.BOTTOM) {
            return clicked.getLocation().clone();
        } else if(door.getHalf() == Bisected.Half.TOP) {
            return clicked.getLocation().subtract(0.0, 1.0, 0.0).clone();
        }
        return clicked.getLocation().clone();
    }

    private boolean doorBroken(@Nonnull Block clicked, Door door) {
        if(door.getHalf() == Bisected.Half.BOTTOM) {
            if(brokenDoors.get(clicked.getLocation()) != null) {
                return brokenDoors.get(clicked.getLocation()) > System.currentTimeMillis();
            }
        } else if(door.getHalf() == Bisected.Half.TOP) {
            if(brokenDoors.get(clicked.getLocation().subtract(0.0, 1.0D, 0.0)) != null) {
                return brokenDoors.get(clicked.getLocation().subtract(0.0, 1.0D, 0.0)) > System.currentTimeMillis();
            }
        }
        return false;
    }
}
