package com.github.alathra.AlathranWars.commands;

import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.holder.WarManager;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static com.github.alathra.AlathranWars.enums.CommandArgsSiege.ALL_SIEGES;
import static com.github.alathra.AlathranWars.enums.CommandArgsSiege.IN_SIEGE;
import static com.github.alathra.AlathranWars.enums.CommandArgsWar.ALL_WARS;
import static com.github.alathra.AlathranWars.enums.CommandArgsWar.IN_WAR;

public class SiegeCommands {
    public SiegeCommands() {
        new CommandAPICommand("siege")
            .withSubcommands(
                commandStart(false),
                commandStop(false),
                commandAbandon(false),
                commandSurrender(false),
                commandList()
            )
            .executesPlayer((sender, args) -> {
                if (args.count() == 0)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "Invalid Arguments. /siege help").build());
            })
            .register();
    }

    public static CommandAPICommand commandStart(boolean asAdmin) {
        return new CommandAPICommand("start")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument(
                    "war",
                    asAdmin,
                    asAdmin ? ALL_WARS : IN_WAR,
                    ""
                ),
                CommandUtil.customSiegeAttackableTownArgument(
                    "town",
                    "war",
                    false,
                    true
                ),
                new PlayerArgument("leader")
                    .setOptional(!asAdmin)
                    .withPermission("AlathranWars.admin")/*,
                new BooleanArgument("minutemen")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin")*/
            )
            .executesPlayer((Player p, CommandArguments args) -> siegeStart(p, args, false));
    }

    public static CommandAPICommand commandStop(boolean asAdmin) {
        return new CommandAPICommand("stop")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.siegeSiegeArgument("siege", asAdmin, asAdmin ? ALL_SIEGES : IN_SIEGE, "player")
            )
            .executesPlayer((Player p, CommandArguments args) -> siegeStop(p, args, asAdmin));
    }

    public static CommandAPICommand commandAbandon(boolean asAdmin) {
        return new CommandAPICommand("abandon")
            .withArguments(
                CommandUtil.siegeSiegeArgument("siege", asAdmin, asAdmin ? ALL_SIEGES : IN_SIEGE, "player")
            )
            .executesPlayer((Player p, CommandArguments args) -> siegeAbandon(p, args, asAdmin));
    }

    public static CommandAPICommand commandSurrender(boolean asAdmin) {
        return new CommandAPICommand("surrender")
            .withArguments(
                CommandUtil.siegeSiegeArgument("siege", asAdmin, asAdmin ? ALL_SIEGES : IN_SIEGE, "player")
            )
            .executesPlayer((Player p, CommandArguments args) -> siegeSurrender(p, args, asAdmin));
    }

    public static CommandAPICommand commandList() {
        return new CommandAPICommand("list")
            .executesPlayer(SiegeCommands::siegeList);
    }

    protected static void siegeStart(@NotNull Player sender, @NotNull CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        if (!(args.get("war") instanceof final @NotNull War war))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a war.").build());

        if (!(args.get("town") instanceof final @NotNull Town town))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a town.").build());

        final @NotNull Player siegeLeader = (Player) args.getOptional("leader").orElse(sender);

        if (siegeLeader == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("You need to pick a valid leader.").build());

        // Player participance check
//        @Nullable Town leaderTown = TownyAPI.getInstance().getResident(siegeLeader).getTownOrNull();
//        @Nullable Side side = war.getTownSide(leaderTown);
        @Nullable Side side = war.getPlayerSide(siegeLeader);
        if (side == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("You are not in this war.").build());

        if (side.isPlayerSurrendered(siegeLeader))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("You have surrendered and cannot participate in this war.").build());

        // Attacking own side
        if (side.isTownOnSide(town)) {
            if (admin)
                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "You cannot attack your own side.").build());
            else
                siegeLeader.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You cannot attack your own towns.").build());
            return;
        }

        Siege siege = new Siege(war, town, siegeLeader);

        war.addSiege(siege);

        Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + siege.getTown() + " has been put to siege by " + siege.getAttackerSide().getName() + "!").build());

        siege.start();
    }

    private static void siegeStop(@NotNull Player sender, @NotNull CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        if (!(args.get("siege") instanceof final @NotNull Siege siege))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid siege.").build());

        siege.noWinner();
    }

    private static void siegeAbandon(Player p, @NotNull CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        if (!(args.get("siege") instanceof final @NotNull Siege siege))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid siege.").build());

        @NotNull OfflinePlayer siegeLeader = siege.getSiegeLeader();
        if (siegeLeader != p && !admin)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>You are not the leader of this siege.").build());

        Bukkit.broadcast(
            ColorParser.of("<prefix>The siege at <town> has been abandoned.")
                .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                .parseMinimessagePlaceholder("town", siege.getTown().getName())
            .build()
        );
        siege.defendersWin();
    }

    private static void siegeSurrender(Player p, @NotNull CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        if (!(args.get("siege") instanceof final @NotNull Siege siege))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid siege.").build());

        Town town = siege.getTown();

        @Nullable Resident res = TownyAPI.getInstance().getResident(p);
        if (res == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Resident invalid.").build());

        final boolean canKingSurrender = (res.hasNation() && town.hasNation() && res.getNationOrNull().equals(town.getNationOrNull()) && res.isKing());
        final boolean canMayorSurrender = (res.hasTown() && res.getTownOrNull().equals(town) && res.isMayor());

        if (!admin && (!canKingSurrender || !canMayorSurrender))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>You cannot surrender this town.").build());

        Bukkit.broadcast(
            ColorParser.of("<prefix>The town of <town> has surrendered.")
                .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                .parseMinimessagePlaceholder("town", siege.getTown().getName())
                .build()
        );
        siege.attackersWin();
    }

    private static void siegeList(@NotNull Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        Set<Siege> sieges = WarManager.getInstance().getSieges();

        if (sieges.isEmpty()) {
            sender.sendMessage(ColorParser.of("<red>There are no sieges at the moment.").build());
            return;
        }

        final StringBuilder msg = new StringBuilder();

        for (Siege siege : sieges) {
            msg.append("\n<white><bold>%s<reset>".formatted(siege.getName()));
            msg.append("\n <grey>Progress: <white>%.0f%%".formatted(siege.getSiegeProgressPercentage() * 100));
            msg.append("\n <grey>Remaining: <white>%s minutes".formatted(String.valueOf(Duration.between(Instant.now(), siege.getEndTime()).toMinutesPart())));
            msg.append("\n");
        }

        sender.sendMessage(
            ColorParser.of(
                msg.toString()
            ).build()
        );
    }
}
