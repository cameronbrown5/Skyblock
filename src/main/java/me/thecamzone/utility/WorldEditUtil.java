package me.thecamzone.utility;

import com.fastasyncworldedit.core.function.pattern.MaskedPattern;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.StateApplyingPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTTileEntity;
import me.thecamzone.Skyblock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldEditUtil {

    private static final Gson GSON = new Gson();

    public static Clipboard load(File schematic) {
        Clipboard clipboard;

        ClipboardFormat format = ClipboardFormats.findByFile(schematic);
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematic))) {
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return clipboard;
    }

    public static void paste(Clipboard clipboard, Location location) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {

            StateApplyingPattern persistentPattern = new StateApplyingPattern(clipboard, Map.of("persistent", "false"));
            BlockTypeMask blockTypeMask = new BlockTypeMask(clipboard, BlockTypes.OAK_LEAVES, BlockTypes.BIRCH_LEAVES, BlockTypes.SPRUCE_LEAVES, BlockTypes.JUNGLE_LEAVES, BlockTypes.ACACIA_LEAVES, BlockTypes.DARK_OAK_LEAVES, BlockTypes.AZALEA_LEAVES, BlockTypes.FLOWERING_AZALEA_LEAVES, BlockTypes.MANGROVE_LEAVES, BlockTypes.CHERRY_LEAVES);
            clipboard.getRegion().forEach(vector -> {
                if(blockTypeMask.test(vector)) {
                    clipboard.setBlock(vector, persistentPattern.applyBlock(vector));
                }
            });

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                    .ignoreAirBlocks(true)
                    .copyEntities(true)
                    .build();
            Operations.complete(operation);
        }
    }

}
