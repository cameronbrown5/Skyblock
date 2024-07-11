package me.thecamzone.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.google.common.collect.ImmutableList;
import me.thecamzone.Skyblock;
import me.thecamzone.quests.QuestMenu;
import me.thecamzone.utility.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("skydev")
@CommandPermission("skyblock.staff")
public class DeveloperCommand extends BaseCommand {

    private PaperCommandManager manager;

    public DeveloperCommand(PaperCommandManager manager) {
        this.manager = manager;

//        manager.getCommandCompletions().registerCompletion("questCategories", c -> {
//            return ImmutableList.of("BUILDER");
//        });
    }

    @Subcommand("openGUI")
    @CommandCompletion("@questCategories @players @players")
    public void openGUI(CommandSender sender, String category, @Optional OnlinePlayer target, @Optional OnlinePlayer open) {
        QuestMenu questMenu = Skyblock.getInstance().getQuestMenu();

        final String title = category + " Quests";

        if(sender instanceof Player player)
            if(target != null && open != null)
                questMenu.getGui(target.getPlayer(), category.toLowerCase(), title).show(open.getPlayer());
            else if(target != null)
                questMenu.getGui(target.getPlayer(), category.toLowerCase(), title).show(player);
            else
                questMenu.getGui(player, category.toLowerCase(), title).show(player);
        else {
            if(target != null && open != null)
                questMenu.getGui(target.getPlayer(), category.toLowerCase(), title).show(open.getPlayer());
            else if(target != null)
                questMenu.getGui(target.getPlayer(), category.toLowerCase(), title).show(target.getPlayer());
            else {
                sender.sendMessage(StringUtil.color("&cYou must specify a player to open the GUI on."));
            }

        }

    }
}
