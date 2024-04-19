package me.thecamzone.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.thecamzone.Skyblock;
import me.thecamzone.island.Island;
import me.thecamzone.island.IslandManager;
import me.thecamzone.utility.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("island|is")
@Description("Main island command")
public class IslandCommand extends BaseCommand {

    @Subcommand("help")
    @Default
    public void onHelp(Player player) {
        player.sendMessage(StringUtil.color("&aIsland Commands:"));
        player.sendMessage(StringUtil.color("&f- &a/is help &7- Shows this screen."));
        player.sendMessage(StringUtil.color("&f- &a/is test - Scans chunks to find chests."));
    }

    @Subcommand("unknown")
    @Private
    @CatchUnknown
    public void catchUnknown(CommandSender sender, String[] args) {
        sender.sendMessage(StringUtil.color("&cUnknown subcommand. Type '/is help' for help."));
    }

    @Subcommand("create")
    public void onCreate(Player player, String[] args) {
        String argMessage = String.join(" ", args);

        if(Skyblock.getInstance().getIslandManager().getOwnedIsland(player) != null) {
            player.sendMessage(StringUtil.color("&cYou already own an island."));
            return;
        }

        if (argMessage.isEmpty()) {
            player.sendMessage(StringUtil.color("&cPlease provide a name for your island."));
            return;
        }

        Skyblock.getInstance().getIslandManager().createIsland(player, argMessage);
        player.sendMessage(StringUtil.color("&aIsland " + argMessage + " created!"));
    }

    @Subcommand("invite")
    public void onInvite(Player sender, OnlinePlayer otherPlayer, String islandName) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        List<Island> islands = islandManager.getIslandsPlayerBelongsTo(sender);

        if(islands == null || islands.isEmpty()) {
            sender.sendMessage(StringUtil.color("&cYou are not apart of an island."));
            return;
        }

        Island island = null;
        if(islandName.isEmpty()) {
            island = islandManager.getOwnedIsland(sender);
        } else {
            island = islandManager.getIsland(islandName);
        }

        if(island == null) {
            sender.sendMessage(StringUtil.color("&cIsland not found."));
            return;
        }

        if(!island.getPlayers().contains(sender.getUniqueId())) {
            sender.sendMessage(StringUtil.color("&cYou are not a member of this island."));
            return;
        }

        if(island.getPlayers().contains(otherPlayer.getPlayer().getUniqueId())) {
            sender.sendMessage(StringUtil.color("&c" + otherPlayer.getPlayer().getName() + " is already a member of this island."));
            return;
        }

        island.addPlayerInvite(otherPlayer.getPlayer().getUniqueId());
        sender.sendMessage(StringUtil.color("&aInvited " + otherPlayer.getPlayer().getName() + " to your island."));
        otherPlayer.getPlayer().sendMessage(StringUtil.color("&aYou have been invited to join " + sender.getName() + "'s island. Type /is accept to join."));
    }

    @Subcommand("accept")
    public void accept(Player player, String islandName) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        Island island = islandManager.getIsland(islandName);

        if(island == null) {
            player.sendMessage(StringUtil.color("&cIsland not found."));
            return;
        }

        if(!island.getInvitedPlayers().contains(player.getUniqueId())) {
            player.sendMessage(StringUtil.color("&cYou have not been invited to this island."));
            return;
        }

        island.addPlayer(player.getUniqueId());
        player.sendMessage(StringUtil.color("&aYou have joined " + island.getName() + "'s island."));

        islandManager.saveIsland(island);
    }

}
