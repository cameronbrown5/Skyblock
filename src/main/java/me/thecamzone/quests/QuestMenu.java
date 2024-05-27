package me.thecamzone.quests;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.BukkitObjective;
import me.pikamug.quests.quests.components.Rewards;
import me.pikamug.quests.quests.components.Stage;
import me.thecamzone.Skyblock;
import me.thecamzone.utility.StringUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class QuestMenu {

    HashMap<String, List<String>> categories = new HashMap<>();

    public QuestMenu() {
        load();
    }

    public Gui getGui(Player player, String category, String title) {
        Quester quester = Skyblock.getInstance().getQuestsPlugin().getQuester(player.getUniqueId());

        ChestGui gui = new ChestGui(6, title);
        StaticPane control = new StaticPane(0, 5, 9, 1);
        PaginatedPane questPane = new PaginatedPane(0, 0, 9, 5);

        control.addItem(new GuiItem(new ItemStack(Material.BARRIER)), 4, 0);
        control.fillWith(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));

        Skyblock.getInstance().getQuestsPlugin().getLoadedQuests().forEach(quest -> {
            if(!categories.get(category.toLowerCase()).contains(quest.getName())) {
                return;
            }

            QuestState state = getState(quester, quest);

            ItemStack item = quest.getGUIDisplay();

            if(state == QuestState.COMPLETED) {
                item.setType(Material.LIME_STAINED_GLASS_PANE);
            }

            ItemMeta meta = item.getItemMeta();
            meta.removeEnchantments();
            meta.setDisplayName(getColor(quester, quest) + quest.getName());

            List<String> lore = new ArrayList<>();
            lore.add(StringUtil.color("&7" + quest.getDescription()));
            lore.add("");
            lore.add(StringUtil.color("&fRequirements:"));
            lore.addAll(getRequirementsLore(quest));
            lore.add("");
            lore.add(StringUtil.color("&fRewards:"));
            lore.addAll(getRewardsLore(quest));
            if(!getStatusLore(state, quest).isEmpty()) {
                lore.add("");
                lore.addAll(getStatusLore(getState(quester, quest), quest));
            }
            meta.setLore(lore);

            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

            item.setItemMeta(meta);

            switch(state) {
                case ACTIVE:
                    ItemMeta activeMeta = item.getItemMeta();
                    activeMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                    item.setItemMeta(activeMeta);
                case AVAILABLE:
                    questPane.populateWithGuiItems(List.of(new GuiItem(item, event -> {
                        if(quester.getCurrentQuests().containsKey(quest)) {
                            player.sendMessage(StringUtil.color("&cYou are already on this quest."));
                            return;
                        }
                        quester.takeQuest(quest, false);
                        getGui(player, category, title).show(player);
                    })));
                    break;
                case LOCKED:
                    questPane.populateWithGuiItems(List.of(new GuiItem(item, event -> {
                        player.sendMessage(StringUtil.color("&cYou cannot start this quest yet."));
                    })));
                    break;
                default:
                    ItemMeta noEnchantMeta = item.getItemMeta();
                    noEnchantMeta.removeEnchantments();
                    item.setItemMeta(noEnchantMeta);
                    questPane.populateWithGuiItems(List.of(new GuiItem(item)));
                    break;
            }
        });

        gui.addPane(control);
        gui.addPane(questPane);
        return gui;
    }

    private List<String> getStatusLore(QuestState state, Quest quest) {
        List<String> lore = new ArrayList<>();

        switch(state) {
            case ACTIVE:
                lore.add(StringUtil.color("&a&lACTIVE"));
                break;
            case COMPLETED:
                lore.add(StringUtil.color("&a&lCOMPLETED"));
                break;
            case AVAILABLE:
                lore.add(StringUtil.color("&fLeft Click to Start Quest"));
                break;
            case LOCKED:
                lore.add(StringUtil.color("&cLocked"));
                lore.add(StringUtil.color("&7Quests Required:"));
                lore.addAll(getPrerequisitesLore(quest));
                break;
        }

        return lore;
    }

    private List<String> getPrerequisitesLore(Quest quest) {
        List<String> lore = new ArrayList<>();

        for (String prereq : quest.getRequirements().getNeededQuestIds()) {
            Quest requiredQuest = Skyblock.getInstance().getQuestsPlugin().getQuestById(prereq);

            lore.add(StringUtil.color("&f- " + requiredQuest.getName()));
        }

        return lore;
    }

    private List<String> getRewardsLore(Quest quest) {
        List<String> rewardsStrings = new ArrayList<>();

        Rewards rewards = quest.getRewards();

        // Get the additional requirements if detailsOverride is empty
        int money = rewards.getMoney();
        if (money > 0) {
            rewardsStrings.add(StringUtil.color("&f- &7$" + money));
        }

        int questPoints = rewards.getQuestPoints();
        if (questPoints > 0) {
            rewardsStrings.add(StringUtil.color("&f- &7" + questPoints + " quest points"));
        }

        int exp = rewards.getExp();
        if (exp > 0) {
            rewardsStrings.add(StringUtil.color("&f- &7" + exp + " experience points"));
        }

        for (ItemStack item : (LinkedList<ItemStack>) rewards.getItems()) {
            rewardsStrings.add(StringUtil.color("&f- &7" + item.getAmount() + "x " + StringUtil.renameMinecraftId(item.getType().name())));
        }

        return rewardsStrings;
    }

    private List<String> getRequirementsLore(Quest quest) {
        List<String> lore = new ArrayList<>();

        for (Stage stage : quest.getStages()) {
            for (ItemStack blockToBreak : (LinkedList<ItemStack>) stage.getBlocksToBreak()) {
                lore.add(StringUtil.color("&f- &7Break " + blockToBreak.getAmount() + "x " + StringUtil.renameMinecraftId(blockToBreak.getType().name())));
            }
            for (ItemStack blockToDamage : (LinkedList<ItemStack>) stage.getBlocksToDamage()) {
                lore.add(StringUtil.color("&f- &7Damage " + blockToDamage.getAmount() + "x " + StringUtil.renameMinecraftId(blockToDamage.getType().name())));
            }
            for (ItemStack blockToPlace : (LinkedList<ItemStack>) stage.getBlocksToPlace()) {
                lore.add(StringUtil.color("&f- &7Place " + blockToPlace.getAmount() + "x " + StringUtil.renameMinecraftId(blockToPlace.getType().name())));
            }
            for (ItemStack blockToUse : (LinkedList<ItemStack>) stage.getBlocksToUse()) {
                lore.add(StringUtil.color("&f- &7Use " + blockToUse.getAmount() + "x " + StringUtil.renameMinecraftId(blockToUse.getType().name())));
            }
            for (ItemStack blockToCut : (LinkedList<ItemStack>) stage.getBlocksToCut()) {
                lore.add(StringUtil.color("&f- &7Cut " + blockToCut.getAmount() + "x " + StringUtil.renameMinecraftId(blockToCut.getType().name())));
            }
            for (ItemStack itemToCraft : (LinkedList<ItemStack>) stage.getItemsToCraft()) {
                lore.add(StringUtil.color("&f- &7Craft " + itemToCraft.getAmount() + "x " + StringUtil.renameMinecraftId(itemToCraft.getType().name())));
            }
            for (ItemStack itemToSmelt : (LinkedList<ItemStack>) stage.getItemsToSmelt()) {
                lore.add(StringUtil.color("&f- &7Smelt " + itemToSmelt.getAmount() + "x " + StringUtil.renameMinecraftId(itemToSmelt.getType().name())));
            }
            for (ItemStack itemToEnchant : (LinkedList<ItemStack>) stage.getItemsToEnchant()) {
                lore.add(StringUtil.color("&f- &7Enchant " + itemToEnchant.getAmount() + "x " + StringUtil.renameMinecraftId(itemToEnchant.getType().name())));
            }
            for (ItemStack itemToBrew : (LinkedList<ItemStack>) stage.getItemsToBrew()) {
                lore.add(StringUtil.color("&f- &7Brew " + itemToBrew.getAmount() + "x " + StringUtil.renameMinecraftId(itemToBrew.getType().name())));
            }
            for (ItemStack itemToConsume : (LinkedList<ItemStack>) stage.getItemsToConsume()) {
                lore.add(StringUtil.color("&f- &7Consume " + itemToConsume.getAmount() + "x " + StringUtil.renameMinecraftId(itemToConsume.getType().name())));
            }
            for (ItemStack itemToDeliver : (LinkedList<ItemStack>) stage.getItemsToDeliver()) {
                lore.add(StringUtil.color("&f- &7Deliver " + itemToDeliver.getAmount() + "x " + StringUtil.renameMinecraftId(itemToDeliver.getType().name())));
            }
            for (int npcNum : (LinkedList<Integer>) stage.getNpcNumToKill()) {
                lore.add(StringUtil.color("&f- &7Kill NPC " + npcNum + " times"));
            }
            for (EntityType mob : (LinkedList<EntityType>) stage.getMobsToKill()) {
                lore.add(StringUtil.color("&f- &7Kill " + StringUtil.renameMinecraftId(mob.name())));
            }
            for (int mobNum : (LinkedList<Integer>) stage.getMobNumToKill()) {
                lore.add(StringUtil.color("&f- &7Kill " + mobNum + " mobs"));
            }
            for (Location location : (LinkedList<Location>) stage.getLocationsToKillWithin()) {
                lore.add(StringUtil.color("&f- &7Kill within location " + StringUtil.locationToStringReadable(location)));
            }
            for (int radius : (LinkedList<Integer>) stage.getRadiiToKillWithin()) {
                lore.add(StringUtil.color("&f- &7Within radius " + radius));
            }
            for (String killName : (LinkedList<String>) stage.getKillNames()) {
                lore.add(StringUtil.color("&f- &7Kill named " + killName));
            }
            for (EntityType mob : (LinkedList<EntityType>) stage.getMobsToTame()) {
                lore.add(StringUtil.color("&f- &7Tame " + StringUtil.renameMinecraftId(mob.name())));
            }
            for (int mobNum : (LinkedList<Integer>) stage.getMobNumToTame()) {
                lore.add(StringUtil.color("&f- &7Tame " + mobNum + " mobs"));
            }
            if (stage.getFishToCatch() != null) {
                lore.add(StringUtil.color("&f- &7Catch " + stage.getFishToCatch() + " fish"));
            }
            if (stage.getCowsToMilk() != null) {
                lore.add(StringUtil.color("&f- &7Milk " + stage.getCowsToMilk() + " cows"));
            }
            for (DyeColor sheepColor : (LinkedList<DyeColor>) stage.getSheepToShear()) {
                lore.add(StringUtil.color("&f- &7Shear " + StringUtil.renameMinecraftId(sheepColor.name()) + " sheep"));
            }
            for (int sheepNum : (LinkedList<Integer>) stage.getSheepNumToShear()) {
                lore.add(StringUtil.color("&f- &7Shear " + sheepNum + " sheep"));
            }
            if (stage.getPlayersToKill() != null) {
                lore.add(StringUtil.color("&f- &7Kill " + stage.getPlayersToKill() + " players"));
            }
            for (Location location : (LinkedList<Location>) stage.getLocationsToReach()) {
                lore.add(StringUtil.color("&f- &7Reach location " + StringUtil.locationToStringReadable(location)));
            }
            for (int radius : (LinkedList<Integer>) stage.getRadiiToReachWithin()) {
                lore.add(StringUtil.color("&f- &7Reach within radius " + radius));
            }
            for (World world : (LinkedList<World>) stage.getWorldsToReachWithin()) {
                lore.add(StringUtil.color("&f- &7Reach within world " + world.getName()));
            }
            for (String locationName : (LinkedList<String>) stage.getLocationNames()) {
                lore.add(StringUtil.color("&f- &7Reach location named " + locationName));
            }
        }

        return lore;
    }

    private QuestState getState(Quester quester, Quest quest) {
        if (quester.getCurrentQuests().containsKey(quest)) {
            return QuestState.ACTIVE;
        } else if (quester.getCompletedQuests().contains(quest)) {
            return QuestState.COMPLETED;
        } else if (quester.canAcceptOffer(quest, false)) {
            return QuestState.AVAILABLE;
        } else {
            return QuestState.LOCKED;
        }
    }

    private String getColor(Quester quester, Quest quest) {
        switch(getState(quester, quest)) {
            case ACTIVE:
                return StringUtil.color("&a");
            case COMPLETED:
                return StringUtil.color("&a&l");
            case AVAILABLE:
                return StringUtil.color("&f");
            case LOCKED:
                return StringUtil.color("&c");
            default:
                return StringUtil.color("&7");
        }
    }

    private void load() {
        FileConfiguration config = Skyblock.getInstance().getCategoriesFile().getConfig();

        ConfigurationSection categories = config.getConfigurationSection("categories");
        if(categories == null) {
            Skyblock.getInstance().getLogger().severe("No categories found in categories.yml.");
            Skyblock.getInstance().getServer().shutdown();
            return;
        }

        for (String category : categories.getKeys(false)) {
            List<String> quests = categories.getStringList(category.toLowerCase());
            this.categories.put(category.toLowerCase(), quests);
        }

        Skyblock.getInstance().getLogger().info("Loaded " + this.categories.size() + " categories.");
    }
}
