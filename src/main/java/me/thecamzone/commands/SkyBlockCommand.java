package me.thecamzone.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.thecamzone.Skyblock;
import me.thecamzone.island.Island;
import me.thecamzone.utility.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.joml.Random;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

@CommandAlias("skyblock|sb")
@Description("Main SkyBlock command")
public class SkyBlockCommand extends BaseCommand {

    @Subcommand("help")
    @Default
    public void onHelp(Player player) {
        player.sendMessage(StringUtil.color("&aSkyblock Commands:"));
        player.sendMessage(StringUtil.color("&f- &a/dev help &7- Shows this screen."));
        player.sendMessage(StringUtil.color("&f- &a/dev test - Scans chunks to find chests."));
    }

    @Subcommand("unknown")
    @Private
    @CatchUnknown
    public void catchUnknown(CommandSender sender, String[] args) {
        sender.sendMessage(StringUtil.color("&cUnknown subcommand. Type '/dev help' for help."));
    }

    @Subcommand("islands")
    public void onIslands(Player player, String[] args) {
        Set<String> islandNames = Skyblock.getInstance().getIslandManager().getIslands().stream().map(Island::getName).collect(Collectors.toSet());
        player.sendMessage(StringUtil.color("&aIslands List:"));
        for (String islandName : islandNames) {
            player.sendMessage(StringUtil.color("&f- &a" + islandName));
        }

    }

}
