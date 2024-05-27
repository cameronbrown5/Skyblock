package me.thecamzone.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.google.common.collect.ImmutableList;
import me.thecamzone.CamsLootTables;
import me.thecamzone.Skyblock;
import me.thecamzone.chunks.ChunkCoordinates;
import me.thecamzone.chunks.ChunkSearch;
import me.thecamzone.island.Island;
import me.thecamzone.island.IslandManager;
import me.thecamzone.player.PlayerConfig;
import me.thecamzone.player.PlayerConfigManager;
import me.thecamzone.utility.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@CommandAlias("island|is")
@Description("Main island command")
public class IslandCommand extends BaseCommand {

    private PaperCommandManager manager;

    public IslandCommand(PaperCommandManager manager) {
        this.manager = manager;

//        manager.getCommandCompletions().registerCompletion("questCategories", c -> {
//            return ImmutableList.of("BUILDER");
//        });
    }

    @Subcommand("help")
    @Default
    public void onHelp(Player player) {
        player.sendMessage(StringUtil.color("&aIsland Commands:"));
        player.sendMessage(StringUtil.color("&f- &a/is help &7- Shows this screen."));
        player.sendMessage(StringUtil.color("&f- &a/is create <name> &7- Create an island."));
        player.sendMessage(StringUtil.color("&f- &a/is invite <player> [island] &7- Invite a player to your island."));
        player.sendMessage(StringUtil.color("&f- &a/is accept <island> &7- Accept an island invite."));
        player.sendMessage(StringUtil.color("&f- &a/is kick <player> &7- Kick a player from your island."));
        player.sendMessage(StringUtil.color("&f- &a/is members &7- List members of an island."));
        player.sendMessage(StringUtil.color("&f- &a/is delete <name> &7- Delete your island."));
        player.sendMessage(StringUtil.color("&f- &a/is nearby &7- Toggle nearby islands."));
        player.sendMessage(StringUtil.color("&f- &a/is setspawn &7- Set your island spawn."));
        player.sendMessage(StringUtil.color("&f- &a/is spawn &7- Teleport to your island spawn."));
    }

    @Subcommand("unknown")
    @Private
    @CatchUnknown
    public void catchUnknown(CommandSender sender, String[] args) {
        sender.sendMessage(StringUtil.color("&cUnknown subcommand. Type '/is help' for help."));
    }

    @Subcommand("create")
    public void onCreate(Player player, String[] args) {
        PlayerConfig playerConfig = PlayerConfigManager.getInstance().getPlayerConfig(player.getUniqueId());

        if(PlayerConfigManager.getInstance().getPlayerConfig(player.getUniqueId()).isCreatingIsland()) {
            player.sendMessage(StringUtil.color("&cPlease wait before using this command again."));
            return;
        }

        long startTime = System.nanoTime();

        String argMessage = String.join(" ", args);

        if(Skyblock.getInstance().getIslandManager().getOwnedIsland(player) != null) {
            player.sendMessage(StringUtil.color("&cYou already own an island."));
            return;
        }

        if (argMessage.isEmpty()) {
            player.sendMessage(StringUtil.color("&cPlease provide a name for your island."));
            return;
        }

        playerConfig.setCreatingIsland(true);

        player.sendMessage(StringUtil.color("&7Creating island..."));

        Bukkit.getScheduler().runTaskAsynchronously(Skyblock.getInstance(), () -> {

            ChunkSearch chunkSearch = new ChunkSearch(Bukkit.getWorld("skyblock"));
            Chunk chunk = null;
            try {
                ChunkCoordinates chunkCoordinates = chunkSearch.findFreeChunk(Skyblock.getInstance().getDatabase().getConnection(), 100);
                chunk = chunkCoordinates.getChunk();
            } catch (SQLException e) {
                player.sendMessage(StringUtil.color("&cFailed to find a free chunk. Internal server error."));
                e.printStackTrace();
                return;
            }

            Island island = Skyblock.getInstance().getIslandManager().createIsland(player, chunk, argMessage);
            island.addClaimedChunk(chunk);
            playerConfig.setOwnedIsland(UUID.randomUUID());
            Skyblock.getInstance().getIslandManager().saveIsland(island);

            Chunk finalChunk = chunk;
            Bukkit.getScheduler().runTask(Skyblock.getInstance(), () -> {
                Skyblock.getInstance().getCamsLootTables().getChunkHandler().fillChestsInChunk(finalChunk);
                player.teleport(island.getSpawn());

                long endTime = System.nanoTime();
                long durationInNanoseconds = (endTime - startTime);
                double durationInMilliseconds = durationInNanoseconds / 1_000_000.0;
                String formattedDuration = String.format("%.2f", durationInMilliseconds);

                player.sendMessage(StringUtil.color("&aIsland " + argMessage + " created in " + formattedDuration + "ms."));
                playerConfig.setCreatingIsland(false);
            });
            playerConfig.save();
        });
    }

    @Subcommand("invite")
    @Syntax("<player> [island]")
    @CommandCompletion("@players @player_islands")
    public void onInvite(Player sender, OnlinePlayer otherPlayer, @Optional String islandName) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        List<Island> islands = islandManager.getIslandsPlayerBelongsTo(sender);

        if(islands == null || islands.isEmpty()) {
            sender.sendMessage(StringUtil.color("&cYou are not apart of an island."));
            return;
        }

        Island island = null;
        if(islandName == null || islandName.isEmpty()) {
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

        islandManager.invitePlayer(otherPlayer.getPlayer(), island);
        sender.sendMessage(StringUtil.color("&aInvited " + otherPlayer.getPlayer().getName() + " to your island."));
        otherPlayer.getPlayer().sendMessage(StringUtil.color("&aYou have been invited to join " + sender.getName() + "'s island. Type /is accept to join."));
    }

    @Subcommand("accept")
    @Syntax("<island>")
    @CommandCompletion("@player_invites")
    public void accept(Player player, String islandName) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        Island island = islandManager.getIsland(islandName);

        if(island == null) {
            player.sendMessage(StringUtil.color("&cIsland not found."));
            return;
        }

        if(!islandManager.getInvitedIslands(player).contains(island)) {
            player.sendMessage(StringUtil.color("&cYou have not been invited to this island."));
            return;
        }

        islandManager.acceptInvite(player, island);
        player.sendMessage(StringUtil.color("&aYou have joined " + island.getName() + "'s island."));

        islandManager.saveIsland(island);
    }

    @Subcommand("kick")
    @Syntax("<player>")
    @CommandCompletion("@island_players")
    public void kick(Player player, OnlinePlayer otherPlayer) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        Island island = islandManager.getOwnedIsland(player);

        if(island == null) {
            player.sendMessage(StringUtil.color("&cYou do not own an island."));
            return;
        }

        if(!island.getPlayers().contains(otherPlayer.getPlayer().getUniqueId())) {
            player.sendMessage(StringUtil.color("&c" + otherPlayer.getPlayer().getName() + " is not a member of your island."));
            return;
        }

        if(otherPlayer.getPlayer().getUniqueId() == player.getUniqueId()) {
            player.sendMessage(StringUtil.color("&cYou cannot kick yourself from your island."));
            return;
        }

        island.getPlayers().remove(otherPlayer.getPlayer().getUniqueId());
        islandManager.saveIsland(island);

        player.sendMessage(StringUtil.color("&aKicked " + otherPlayer.getPlayer().getName() + " from your island."));
        otherPlayer.getPlayer().sendMessage(StringUtil.color("&cYou have been kicked from " + player.getName() + "'s island."));
    }

    @Subcommand("members")
    @CommandCompletion("@islands")
    public void members(Player player, String island) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        Island islandObj = islandManager.getIsland(island);

        if(islandObj == null) {
            player.sendMessage(StringUtil.color("&cIsland not found."));
            return;
        }

        player.sendMessage(StringUtil.color("&aMembers of " + islandObj.getName() + ":"));
        for (UUID member : islandObj.getPlayers()) {
            player.sendMessage(StringUtil.color("&f- &a" + Bukkit.getPlayer(member).getName()));
        }
    }

    @Subcommand("delete")
    public void delete(Player player, String[] args) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        PlayerConfig playerConfig = PlayerConfigManager.getInstance().getPlayerConfig(player.getUniqueId());
        Island islandObj = islandManager.getOwnedIsland(player);

        if(islandObj == null) {
            player.sendMessage(StringUtil.color("&cYou do not own an island."));
            return;
        }

        if(!islandObj.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(StringUtil.color("&cYou do not own this island."));
            return;
        }

        if(args.length == 0) {
            player.sendMessage(StringUtil.color("&cPlease type the name of your island to confirm deletion."));
            return;
        }

        if(!args[0].equalsIgnoreCase(islandObj.getName())) {
            player.sendMessage(StringUtil.color("&cIsland name does not match."));
            return;
        }

        if(playerConfig.getOwnedIsland() == null) {
            player.sendMessage(StringUtil.color("&cYou do not own an island."));
            return;
        }

        player.sendMessage(StringUtil.color("&7Deleting island..."));

        Bukkit.getScheduler().runTask(Skyblock.getInstance(), () -> {
            islandManager.deleteIsland(islandObj);
            playerConfig.setOwnedIsland(null);
            playerConfig.save();

            player.sendMessage(StringUtil.color("&aIsland " + islandObj.getName() + " deleted."));
        });
    }

    @Subcommand("nearby")
    public void toggleNearby(Player player) {
        PlayerConfig playerConfig = PlayerConfigManager.getInstance().getPlayerConfig(player.getUniqueId());
        playerConfig.setShowNearby(!playerConfig.getShowNearby());
        playerConfig.save();
        player.sendMessage(StringUtil.color("&aNearby islands toggled " + (playerConfig.getShowNearby() ? "on" : "off") + "."));
    }

    @Subcommand("setspawn")
    public void setSpawn(Player player) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        Island island = islandManager.getOwnedIsland(player);

        if(island == null) {
            player.sendMessage(StringUtil.color("&cYou do not own an island."));
            return;
        }

        island.setSpawn(player.getLocation());
        islandManager.saveIsland(island);
        player.sendMessage(StringUtil.color("&aIsland spawn set."));
    }

    @Subcommand("spawn")
    public void spawn(Player player) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        Island island = islandManager.getOwnedIsland(player);

        if(island == null) {
            player.sendMessage(StringUtil.color("&cYou do not own an island."));
            return;
        }

        if(island.getSpawn() == null) {
            player.sendMessage(StringUtil.color("&cIsland spawn not set."));
            return;
        }

        player.teleport(island.getSpawn());
        player.sendMessage(StringUtil.color("&aTeleported to your island."));
    }

    @Subcommand("setname")
    public void setName(Player player, String name) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        Island island = islandManager.getOwnedIsland(player);

        if(island == null) {
            player.sendMessage(StringUtil.color("&cYou do not own an island."));
            return;
        }

        island.setName(name);
        islandManager.saveIsland(island);
        player.sendMessage(StringUtil.color("&aIsland name set to " + name + "."));
    }

    @Subcommand("claim")
    public void claim(Player player) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        Island island = islandManager.getOwnedIsland(player);

        if(island == null) {
            player.sendMessage(StringUtil.color("&cYou do not own an island."));
            return;
        }

        HashSet<ChunkCoordinates> claimedChunks;
        try {
            claimedChunks = island.getClaimedChunks();
        } catch (Exception e) {
            player.sendMessage(StringUtil.color("&cFailed to claim chunk."));
            e.printStackTrace();
            return;
        }

        if(claimedChunks.size() >= island.getMaxChunks()) {
            player.sendMessage(StringUtil.color("&cYou have reached the maximum amount of chunks."));
            return;
        }

        if(claimedChunks.contains(new ChunkCoordinates(player.getWorld(), player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ()))) {
            player.sendMessage(StringUtil.color("&cYour island already owns this chunk."));
            return;
        }

        island.addClaimedChunk(player.getChunk());
        islandManager.saveIsland(island);
        player.sendMessage(StringUtil.color("&aChunk claimed. You have " + (island.getMaxChunks() - claimedChunks.size()) + " chunks left."));
    }

    @Subcommand("unclaim")
    public void unclaim(Player player) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();
        Island island = islandManager.getOwnedIsland(player);

        if(island == null) {
            player.sendMessage(StringUtil.color("&cYou do not own an island."));
            return;
        }

        HashSet<ChunkCoordinates> claimedChunks;
        try {
            claimedChunks = island.getClaimedChunks();
        } catch (Exception e) {
            player.sendMessage(StringUtil.color("&cFailed to unclaim chunk."));
            e.printStackTrace();
            return;
        }

        ChunkCoordinates chunkCoordinates = new ChunkCoordinates(player.getWorld(), player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
        if(!claimedChunks.contains(chunkCoordinates)) {
            player.sendMessage(StringUtil.color("&cThis chunk is not claimed."));
            return;
        }

        island.removeClaimedChunk(player.getChunk());
        islandManager.saveIsland(island);
        player.sendMessage(StringUtil.color("&aChunk unclaimed."));
    }

}
