package com.github.alathra.AlathranWars.hooks;

import com.github.alathra.AlathranWars.conflict.war.War;
import com.github.alathra.AlathranWars.conflict.war.WarController;
import com.github.alathra.AlathranWars.utility.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NameColorHandler {
    private static NameColorHandler instance;
    private Map<Player, String> playerColor = new HashMap<>();
    private Map<Player, String> playerInitial = new HashMap<>();

    private NameColorHandler() {
        if (instance != null)
            Logger.get().warn("Tried to re-initialize singleton");
    }

    @NotNull
    public static NameColorHandler getInstance() {
        if (instance == null)
            instance = new NameColorHandler();

        return instance;
    }

    public void calculatePlayerColors(Player p) {
        if (!WarController.getInstance().isPlayerInAnyWars(p)) {
            playerColor.remove(p);
            playerInitial.remove(p);
            return;
        }

        Optional<War> war = WarController.getInstance().getPlayerWars(p).stream().findFirst();

        if (war.isEmpty())
            return;

        final boolean isPlayerSurrendered = war.get().getSide1().isPlayerSurrendered(p) || war.get().getSide2().isPlayerSurrendered(p);

        if (isPlayerSurrendered)
            return;

        final boolean isSideOne = war.get().getSide1().isPlayerOnSide(p);

        if (isSideOne) {
            playerColor.put(p, "<red>");
        } else {
            playerColor.put(p, "<blue>");
        }
        playerInitial.put(p, war.get().getPlayerSide(p).getName().substring(0, 1).toUpperCase());
    }

    public boolean isPlayerUsingModifiedName(Player p) {
        calculatePlayerColors(p);
        return WarController.getInstance().isPlayerInAnyWars(p);
    }

    public String getPlayerTabNameColor(Player p) {
        return "%s[%s] ".formatted(playerColor.get(p), playerInitial.get(p));
    }

    public String getPlayerNameColor(Player p) {
        return "%s".formatted(playerColor.get(p));
    }
}
