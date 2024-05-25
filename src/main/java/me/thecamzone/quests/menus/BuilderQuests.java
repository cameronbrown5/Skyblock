package me.thecamzone.quests.menus;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.thecamzone.Skyblock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BuilderQuests implements QuestMenu {

    @Override
    public Gui getGui() {
        ChestGui gui = new ChestGui(6, "Builder Quests");
        StaticPane control = new StaticPane(0, 5, 9, 1);
        StaticPane questPane = new StaticPane(0, 0, 9, 5);

        control.addItem(new GuiItem(new ItemStack(Material.BARRIER)), 4, 0);
        Skyblock.getInstance().getQuestsPlugin().getLoadedQuests().forEach(quest -> {
            ItemStack item = quest.getGUIDisplay();

            questPane.addItem(new GuiItem(item, event -> {
                Bukkit.broadcastMessage("You clicked on " + quest.getName() + " quest.");
            }), 0, 0);
        });

        gui.addPane(control);
        gui.addPane(questPane);
        return gui;
    }

}
