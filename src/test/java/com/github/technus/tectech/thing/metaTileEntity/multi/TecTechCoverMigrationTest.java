package com.github.technus.tectech.thing.metaTileEntity.multi;

import static com.github.technus.tectech.mechanics.enderStorage.EnderWorldSavedData.bindEnderLinkTag;
import static com.github.technus.tectech.mechanics.enderStorage.EnderWorldSavedData.getEnderFluidContainer;
import static com.github.technus.tectech.mechanics.enderStorage.EnderWorldSavedData.getEnderLinkTag;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.github.technus.tectech.loader.thing.CoverLoader;
import com.github.technus.tectech.mechanics.enderStorage.EnderLinkTag;
import com.github.technus.tectech.mechanics.enderStorage.EnderWorldSavedData;
import com.github.technus.tectech.mechanics.enderStorage.ForgeTestRunner;
import com.github.technus.tectech.thing.cover.GT_Cover_TM_EnderFluidLink;
import com.github.technus.tectech.thing.cover.GT_Cover_TM_PowerPassUpgrade;
import com.github.technus.tectech.thing.cover.GT_Cover_TM_TeslaCoil;
import com.github.technus.tectech.thing.cover.GT_Cover_TM_TeslaCoil_Ultimate;
import com.github.technus.tectech.thing.item.EnderFluidLinkCover;
import com.github.technus.tectech.thing.item.PowerPassUpgradeCover;
import com.github.technus.tectech.thing.item.TeslaCoilCover;
import com.github.technus.tectech.thing.metaTileEntity.multi.StructureTestWorld.TestPlayer;
import com.github.technus.tectech.thing.metaTileEntity.multi.StructureTestWorld.TestTile;
import com.github.technus.tectech.thing.metaTileEntity.multi.StructureTestWorld.TestWorld;
import com.google.common.io.ByteStreams;
import gregtech.api.covers.CoverRegistry;
import gregtech.api.modularui2.CoverGuiData;
import gregtech.common.covers.Cover;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ForgeTestRunner.class)
public class TecTechCoverMigrationTest {

    private TestWorld world;
    private TankTile tile;

    @BeforeClass
    public static void initialize() throws Exception {
        TecTechStructureMigrationTest.initialize();
        TeslaCoilCover.INSTANCE = registerItem(TeslaCoilCover.class, 7000);
        EnderFluidLinkCover.INSTANCE = registerItem(EnderFluidLinkCover.class, 7001);
        PowerPassUpgradeCover.INSTANCE = registerItem(PowerPassUpgradeCover.class, 7002);
        new CoverLoader().run();
    }

    private static <T extends Item> T registerItem(Class<T> type, int id) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        T item = constructor.newInstance();
        Item.itemRegistry.addObject(id, "tectech:test_cover_" + id, item);
        return item;
    }

    @Before
    public void createTankAndStorage() throws Exception {
        Field instance = EnderWorldSavedData.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, new EnderWorldSavedData());
        world = new TestWorld();
        tile = new TankTile(world);
        world.putTile(0, 64, 0, tile);
    }

    private GT_Cover_TM_EnderFluidLink attachEnderCover() {
        Cover cover = CoverRegistry.buildCover(new ItemStack(EnderFluidLinkCover.INSTANCE), ForgeDirection.NORTH, tile);
        tile.attachCover(cover);
        return (GT_Cover_TM_EnderFluidLink) cover;
    }

    @Test
    public void registrationsBuildDistinctNativeObjectsForEverySide() {
        ItemStack[] stacks = {
            new ItemStack(TeslaCoilCover.INSTANCE, 1, 0),
            new ItemStack(TeslaCoilCover.INSTANCE, 1, 1),
            new ItemStack(EnderFluidLinkCover.INSTANCE),
            new ItemStack(PowerPassUpgradeCover.INSTANCE)
        };
        Class<?>[] types = {
            GT_Cover_TM_TeslaCoil.class,
            GT_Cover_TM_TeslaCoil_Ultimate.class,
            GT_Cover_TM_EnderFluidLink.class,
            GT_Cover_TM_PowerPassUpgrade.class
        };
        for (int i = 0; i < stacks.length; i++) {
            Cover first = CoverRegistry.buildCover(stacks[i], ForgeDirection.NORTH, tile);
            Cover second = CoverRegistry.buildCover(stacks[i], ForgeDirection.SOUTH, tile);
            assertEquals(types[i], first.getClass());
            assertNotSame(first, second);
            assertTrue(first.useMui2());
            assertSame(tile, first.getTile());
            assertSame(ForgeDirection.NORTH, first.getSide());
            assertNotNull(CoverRegistry.getCoverTexture(stacks[i]));
        }
    }

    @Test
    public void legacyModesSurviveNbtAndPacketRoundTrips() {
        for (int mode = 0; mode < 4; mode++) {
            GT_Cover_TM_EnderFluidLink cover = attachEnderCover();
            tile.loadLegacyCovers(cover.getCoverID(), mode);
            cover = (GT_Cover_TM_EnderFluidLink) tile.getCoverAtSide(ForgeDirection.NORTH);
            assertEquals(mode, cover.getLegacyData());
            NBTTagCompound legacy = new NBTTagCompound();
            legacy.setTag("d", new NBTTagInt(mode));
            cover.readFromNbt(legacy);
            assertEquals(mode, cover.getLegacyData());
            assertEquals((mode & 1) != 0, cover.isExporting());
            assertEquals((mode & 2) != 0, cover.isPrivateChannel());
            cover.setTickRateAddition(5);

            NBTTagCompound saved = new NBTTagCompound();
            CoverRegistry.writeCoverToNbt(cover, saved);
            Cover restored = CoverRegistry.buildCoverFromNbt(saved, ForgeDirection.SOUTH, tile);
            assertEquals(mode, restored.getLegacyData());
            assertEquals(6, restored.getTickRate());

            ByteBuf buffer = Unpooled.buffer();
            try {
                cover.writeToByteBuf(buffer);
                byte[] bytes = new byte[buffer.readableBytes()];
                buffer.readBytes(bytes);
                restored.readFromPacket(ByteStreams.newDataInput(bytes));
                assertEquals(mode, restored.getLegacyData());
                assertEquals(6, restored.getTickRate());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    public void transfersEightBucketsPerTickAndOnlyDrainsWhatTheTargetAccepts() {
        GT_Cover_TM_EnderFluidLink cover = attachEnderCover();
        tile.tank.fill(new FluidStack(FluidRegistry.WATER, 20000), true);
        cover.doCoverThings((byte) 0, 1);
        IFluidHandler enderTank = getEnderFluidContainer(getEnderLinkTag(tile));
        assertEquals(12000, tile.tank.getFluidAmount());
        assertEquals(8000, enderTank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.amount);
        assertSame(ForgeDirection.NORTH, tile.lastDrainSide);

        tile.tank.setFluid(new FluidStack(FluidRegistry.WATER, 63000));
        cover.setExporting(true);
        cover.doCoverThings((byte) 0, 2);
        assertEquals(64000, tile.tank.getFluidAmount());
        assertEquals(7000, enderTank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.amount);
        assertSame(ForgeDirection.NORTH, tile.lastFillSide);

        tile.tank.setFluid(new FluidStack(FluidRegistry.LAVA, 1000));
        cover.doCoverThings((byte) 0, 3);
        assertEquals(1000, tile.tank.getFluidAmount());
        assertEquals(7000, enderTank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.amount);
    }

    @Test
    public void channelChangesPreserveOwnerAndRejectUnauthorizedPrivateLinks() {
        GT_Cover_TM_EnderFluidLink cover = attachEnderCover();
        TestPlayer owner = new TestPlayer(world);
        TestPlayer visitor = new TestPlayer(world);
        tile.setOwnerUuid(owner.getUniqueID());
        bindEnderLinkTag(tile, new EnderLinkTag("old", null));
        cover.setFrequency("#FFFF0000");
        cover.setPrivateChannel(true, visitor);
        assertFalse(cover.isPrivateChannel());
        assertEquals(new EnderLinkTag("#FFFF0000", null), getEnderLinkTag(tile));

        cover.setPrivateChannel(true, owner);
        cover.setFrequency("new");
        assertTrue(cover.isPrivateChannel());
        assertEquals(new EnderLinkTag("new", owner.getUniqueID()), getEnderLinkTag(tile));
        cover.setPrivateChannel(false, owner);
        assertEquals(new EnderLinkTag("new", null), getEnderLinkTag(tile));
    }

    @Test
    public void standaloneAndPopupUseTheGtTemplateAndTheSameSyncControls() {
        GT_Cover_TM_EnderFluidLink cover = attachEnderCover();
        TestPlayer player = new TestPlayer(world);
        tile.setOwnerUuid(player.getUniqueID());
        for (boolean popup : new boolean[] { false, true }) {
            ModularSyncManager manager = new ModularSyncManager(false);
            PanelSyncManager sync = new PanelSyncManager(manager, true);
            UISettings settings = new UISettings();
            CoverGuiData data = new CoverGuiData(
                player,
                cover.getCoverID(),
                0,
                64,
                0,
                ForgeDirection.NORTH,
                popup
            );
            ModularPanel panel;
            if (popup) {
                panel = cover.buildPopUpUI(data, "test", sync, settings, new ButtonWidget<>());
            } else {
                panel = cover.buildUI(data, sync, settings);
            }
            WidgetTree.collectSyncValues(sync, panel);
            ModularContainer container = new ModularContainer();
            container.construct(player, manager, settings, panel.getName(), data);
            assertTrue(container.isInitialized());
            assertEquals(36, container.inventorySlots.size());
            sync.findSyncHandler("frequency", StringSyncValue.class).setValue("gui", true, false);
            sync.findSyncHandler("private_channel", BooleanSyncValue.class).setValue(true, true, false);
            sync.findSyncHandler("exporting", BooleanSyncValue.class).setValue(true, true, false);
            assertEquals(new EnderLinkTag("gui", player.getUniqueID()), getEnderLinkTag(tile));
            assertTrue(cover.isExporting());
        }
        tile.detachCover(ForgeDirection.NORTH);
        cover.setFrequency("stale");
        cover.setPrivateChannel(false, player);
        cover.setExporting(false);
        assertEquals(new EnderLinkTag("gui", player.getUniqueID()), getEnderLinkTag(tile));
        assertTrue(cover.isExporting());
    }

    @Test
    public void powerPassPlacementAndRemovalUpdateTheActualController() {
        ItemStack stack = new ItemStack(PowerPassUpgradeCover.INSTANCE);
        GT_MetaTileEntity_EM_transformer controller = (GT_MetaTileEntity_EM_transformer) tile.getMetaTileEntity();
        assertTrue(CoverRegistry.getCoverPlacer(stack).isCoverPlaceable(ForgeDirection.NORTH, stack, tile));
        CoverRegistry.getCoverPlacer(stack).placeCover(null, stack, tile, ForgeDirection.NORTH);
        assertTrue(controller.ePowerPassCover);
        assertTrue(controller.ePowerPass);
        assertFalse(CoverRegistry.getCoverPlacer(stack).isCoverPlaceable(ForgeDirection.SOUTH, stack, tile));
        tile.detachCover(ForgeDirection.NORTH);
        assertFalse(controller.ePowerPassCover);
        assertFalse(controller.ePowerPass);
        assertTrue(CoverRegistry.getCoverPlacer(stack).isCoverPlaceable(ForgeDirection.SOUTH, stack, tile));
    }

    private static class TankTile extends TestTile {

        private final FluidTank tank = new FluidTank(64000);
        private ForgeDirection lastDrainSide;
        private ForgeDirection lastFillSide;

        private TankTile(TestWorld world) {
            super(world, new GT_MetaTileEntity_EM_transformer("cover-test"), ForgeDirection.NORTH);
        }

        private void loadLegacyCovers(int coverId, int mode) {
            int[] ids = new int[6];
            int[] modes = new int[6];
            ids[ForgeDirection.NORTH.ordinal()] = coverId;
            modes[ForgeDirection.NORTH.ordinal()] = mode;
            readCoverNBT(new NBTTagCompound(), ids, modes);
        }

        @Override
        public int fill(ForgeDirection side, FluidStack fluid, boolean doFill) {
            lastFillSide = side;
            return tank.fill(fluid, doFill);
        }

        @Override
        public FluidStack drain(ForgeDirection side, int amount, boolean doDrain) {
            lastDrainSide = side;
            return tank.drain(amount, doDrain);
        }
    }
}
