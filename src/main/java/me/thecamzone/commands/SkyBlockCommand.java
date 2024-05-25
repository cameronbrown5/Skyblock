package me.thecamzone.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import me.thecamzone.Skyblock;
import me.thecamzone.island.Island;
import me.thecamzone.utility.StringUtil;
import me.thecamzone.utility.WorldEditUtil;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.joml.Random;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

@CommandAlias("skyblock|sb")
@Description("Main SkyBlock command")
@CommandPermission("skyblock.admin")
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

    @Subcommand("reload")
    public void onReload(Player player) {
        Skyblock.getInstance().reload();
        player.sendMessage(StringUtil.color("&aConfiguration reloaded."));
    }

    @Subcommand("islands list")
    public void onIslandsList(Player player, String[] args) {
        Set<String> islandNames = Skyblock.getInstance().getIslandManager().getIslands().stream().map(Island::getName).collect(Collectors.toSet());
        player.sendMessage(StringUtil.color("&aIslands List:"));
        for (String islandName : islandNames) {
            player.sendMessage(StringUtil.color("&f- &a" + islandName));
        }

    }

    @Subcommand("pasteSchematic")
    public void onPaste(Player player, String schematicName) {
        Chunk chunk = player.getChunk();
        Clipboard schematic = WorldEditUtil.load(new File(Skyblock.getInstance().getDataFolder(), "schematics/" + schematicName + ".schem"));

        if(schematic == null) {
            player.sendMessage(StringUtil.color("&cSchematic not found."));
            return;
        }

        Location location = chunk.getBlock(8, 0, 8).getLocation().add(0, 2, 0);
        WorldEditUtil.paste(schematic, location);
    }

}
