package com.github.technus.tectech.thing.metaTileEntity.multi;

import com.mojang.authlib.GameProfile;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraftforge.common.util.ForgeDirection;
import space.impact.structures.CommonProxy;
import space.impact.structures.hint.HintColor;
import space.impact.structures.hint.HintMarkerKind;
import space.impact.structures.structure.IItemSource;

import static org.junit.Assert.assertTrue;

final class StructureTestWorld {
    public static class TestTile extends BaseMetaTileEntity {
        private byte facing;

        TestTile(World world, MetaTileEntity machine, ForgeDirection direction) {
            worldObj = world;
            facing = (byte) direction.ordinal();
            machine.setBaseMetaTileEntity(this);
        }

        @Override
        public byte getFrontFacing() {
            return facing;
        }

        @Override
        public byte getBackFacing() {
            return (byte) ForgeDirection.getOrientation(facing).getOpposite().ordinal();
        }

        @Override
        public void setFrontFacing(byte side) {
            if (isValidFacing(side)) {
                facing = side;
                ((MetaTileEntity) getMetaTileEntity()).onFacingChange();
            }
        }

        @Override
        public void issueTextureUpdate() {
        }

        @Override
        public void markDirty() {
        }
    }

    public static class TestWorld extends World {
        final Set<String> unloadedPositions = new HashSet<>();
        final Map<String, Block> blocks = new HashMap<>();
        final Map<String, Integer> metadata = new HashMap<>();
        final Map<String, TestTile> tiles = new HashMap<>();
        int changes;

        TestWorld() {
            super(new SaveHandlerMP(), "structures", new WorldSettings(0, WorldSettings.GameType.SURVIVAL,
                    false, false, WorldType.DEFAULT), new WorldProviderSurface(), new Profiler());
        }

        static String key(int x, int y, int z) {
            return x + ":" + y + ":" + z;
        }

        @Override
        public Chunk getChunkFromChunkCoords(int x, int z) {
            return new Chunk(this, x, z);
        }

        @Override
        protected IChunkProvider createChunkProvider() {
            return null;
        }

        @Override
        protected int func_152379_p() {
            return 0;
        }

        @Override
        public Entity getEntityByID(int id) {
            return null;
        }

        @Override
        public boolean blockExists(int x, int y, int z) {
            return !unloadedPositions.contains(key(x, y, z));
        }

        @Override
        public Block getBlock(int x, int y, int z) {
            return blocks.getOrDefault(key(x, y, z), Blocks.air);
        }

        @Override
        public int getBlockMetadata(int x, int y, int z) {
            return metadata.getOrDefault(key(x, y, z), 0);
        }

        @Override
        public TileEntity getTileEntity(int x, int y, int z) {
            return tiles.get(key(x, y, z));
        }

        @Override
        public boolean setBlock(int x, int y, int z, Block block, int meta, int flags) {
            changes++;
            String key = key(x, y, z);
            tiles.remove(key);
            if (block == Blocks.air) {
                blocks.remove(key);
                metadata.remove(key);
            } else {
                blocks.put(key, block);
                metadata.put(key, meta);
            }
            return true;
        }

        void putTile(int x, int y, int z, TestTile tile) {
            setBlock(x, y, z, Blocks.iron_block, 0, 0);
            tile.xCoord = x;
            tile.yCoord = y;
            tile.zCoord = z;
            tiles.put(key(x, y, z), tile);
        }

        @Override
        public boolean canPlaceEntityOnSide(Block block, int x, int y, int z, boolean replace,
                int side, Entity entity, ItemStack stack) {
            return isAirBlock(x, y, z) || getBlock(x, y, z).isReplaceable(this, x, y, z);
        }

        @Override
        public void playSoundEffect(double x, double y, double z, String sound, float volume, float pitch) {
        }
    }

    static class TestPlayer extends EntityPlayer {
        final List<IChatComponent> messages = new ArrayList<>();

        TestPlayer(World world) {
            super(world, new GameProfile(UUID.randomUUID(), "Builder"));
        }

        @Override
        public void addChatMessage(IChatComponent message) {
            messages.add(message);
        }

        @Override
        public void addChatComponentMessage(IChatComponent message) {
            messages.add(message);
        }

        @Override
        public boolean canCommandSenderUseCommand(int level, String command) {
            return true;
        }

        @Override
        public ChunkCoordinates getPlayerCoordinates() {
            return new ChunkCoordinates(0, 64, 0);
        }

        @Override
        public void addStat(StatBase stat, int amount) {
        }

        @Override
        public boolean canPlayerEdit(int x, int y, int z, int side, ItemStack stack) {
            return true;
        }
    }

    static class Source implements IItemSource {
        final List<ItemStack> stacks;
        int consumed;
        int queries;

        Source(ItemStack... stacks) {
            this.stacks = Arrays.asList(stacks);
        }

        @Override
        public Map<ItemStack, Integer> take(Predicate<ItemStack> predicate, boolean simulate, int count) {
            queries++;
            Map<ItemStack, Integer> result = new LinkedHashMap<>();
            for (ItemStack stack : stacks) {
                if (stack.stackSize <= 0 || !predicate.test(stack))
                    continue;
                int taken = Math.min(count, stack.stackSize);
                ItemStack copy = stack.copy();
                copy.stackSize = taken;
                result.put(copy, taken);
                if (!simulate) {
                    stack.stackSize -= taken;
                    consumed += taken;
                }
                count -= taken;
                if (count == 0)
                    break;
            }
            return result;
        }
    }

    static class HintRecorder extends CommonProxy {
        final Set<String> positions = new HashSet<>();
        final Set<String> tintedPositions = new HashSet<>();
        final Map<String, Block> blocks = new HashMap<>();
        final Map<String, Integer> metadata = new HashMap<>();
        final Map<String, HintColor> frames = new HashMap<>();

        void clear() {
            positions.clear();
            tintedPositions.clear();
            blocks.clear();
            metadata.clear();
            frames.clear();
        }

        @Override
        public void hintParticle(World world, int x, int y, int z, Block block, int meta) {
            String position = TestWorld.key(x, y, z);
            positions.add(position);
            blocks.put(position, block);
            metadata.put(position, meta);
        }

        @Override
        public void hintMarker(World world, int x, int y, int z, HintMarkerKind kind) {
            positions.add(TestWorld.key(x, y, z));
        }

        @Override
        public void hintParticleTinted(World world, int x, int y, int z, IIcon[] icons, short[] color,
                ItemStack displayStack) {
            positions.add(TestWorld.key(x, y, z));
            tintedPositions.add(TestWorld.key(x, y, z));
        }

        @Override
        public void hintFrame(World world, int x, int y, int z, HintColor color) {
            String position = TestWorld.key(x, y, z);
            assertTrue("The frame must decorate an existing block hint", blocks.containsKey(position) || tintedPositions.contains(position));
            frames.put(position, color);
        }
    }
}
