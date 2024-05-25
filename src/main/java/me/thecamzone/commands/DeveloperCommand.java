package me.thecamzone.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import me.thecamzone.quests.menus.BuilderQuests;
import org.bukkit.entity.Player;

@CommandAlias("skydev")
public class DeveloperCommand extends BaseCommand {
    @Subcommand("testOpenGUI")
    public void testOpenGUI(Player player) {
        BuilderQuests builderQuests = new BuilderQuests();
        builderQuests.getGui().show(player);
    }
}
