package com.github.technus.tectech.thing.metaTileEntity.multi;

import com.github.technus.tectech.TecTech;
import com.github.technus.tectech.compatibility.thaumcraft.thing.metaTileEntity.multi.EssentiaCompat;
import com.github.technus.tectech.compatibility.thaumcraft.thing.metaTileEntity.multi.EssentiaCompatEnabled;
import com.github.technus.tectech.mechanics.enderStorage.ForgeTestRunner;
import com.github.technus.tectech.thing.block.QuantumGlassBlock;
import com.github.technus.tectech.thing.block.QuantumStuffBlock;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OverflowElemental;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaPipeEntity_Frame;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Maintenance;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GTStructureChannels;
import gregtech.common.blocks.GT_Item_Machines;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import space.impact.structures.alignment.constructable.ChannelDataAccessor;
import space.impact.structures.alignment.enumerable.ExtendedFacing;
import space.impact.structures.structure.AutoPlaceEnvironment;
import space.impact.structures.structure.IChannelDefinition;
import space.impact.structures.structure.IStructureElement;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;
import thaumcraft.common.tiles.TileJarFillable;

import static com.github.technus.tectech.thing.metaTileEntity.multi.StructureTestWorld.*;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechAutoHatch.*;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement.ElementalInput;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement.ElementalOutput;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement.ElementalOverflow;
import static org.junit.Assert.*;

/** Real hatch classes, frame metatiles and Thaumcraft jars; no preinstalled required ports. */
@RunWith(ForgeTestRunner.class)
public class TecTechAutomaticConstructionTest {
    private static final int HATCH_ID = 31800;
    private static final List<Supplier<MetaTileEntity>> FACTORIES = new ArrayList<>();
    private static Item hatchItem;
    private static Item frameItem;
    private static Block jarBlock;
    private static HintRecorder hints;

    @BeforeClass
    public static void initialize() throws Exception {
        TecTechStructureMigrationTest.initialize();
        TecTech.proxy = new com.github.technus.tectech.proxy.CommonProxy();
        QuantumStuffBlock.INSTANCE = new QuantumStuffBlock();
        GregTech_API.sBlockCasings1 = Blocks.stained_glass;
        GregTech_API.sBlockCasings4 = Blocks.carpet;
        Block.blockRegistry.addObject(3011, "auto_test_glass", QuantumGlassBlock.INSTANCE);
        Item.itemRegistry.addObject(3011, "auto_test_glass", new ItemBlock(QuantumGlassBlock.INSTANCE));
        hints = new HintRecorder();
        Field proxy = space.impact.structures.Main.class.getDeclaredField("proxy");
        proxy.setAccessible(true);
        proxy.set(null, hints);
        registerJar();
        registerFrames();

        // Maintenance appears first so it cannot accidentally satisfy a requested power or data role.
        FACTORIES.add(() -> new GT_MetaTileEntity_Hatch_Maintenance("test", 1, "", null, false));
        for (String type : new String[]{"Energy", "Dynamo", "Input", "Output", "InputBus", "OutputBus", "Muffler", "DataAccess"}) {
            registerFactory("gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_" + type);
        }
        for (String type : new String[]{"InputElemental", "OutputElemental", "InputData", "OutputData", "Holder",
                "Uncertainty", "Rack", "Capacitor", "InputDataItems", "OutputDataItems"}) {
            registerFactory("com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_" + type);
        }
        FACTORIES.add(() -> new GT_MetaTileEntity_Hatch_OverflowElemental("test", 8, 1000000D, "", null));
        for (int index = 0; index < FACTORIES.size(); index++) {
            GregTech_API.METATILEENTITIES[HATCH_ID + index] = FACTORIES.get(index).get();
        }
        Block block = new Block(Material.iron) {};
        Block.blockRegistry.addObject(3012, "auto_test_hatches", block);
        hatchItem = new GT_Item_Machines(block) {
            @Override
            public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                    int side, float hitX, float hitY, float hitZ) {
                MetaTileEntity hatch = FACTORIES.get(stack.getItemDamage() - HATCH_ID).get();
                ((TestWorld) world).putTile(x, y, z, new TestTile(world, hatch, ForgeDirection.NORTH));
                stack.stackSize--;
                return true;
            }
        };
        Item.itemRegistry.addObject(3012, "auto_test_hatches", hatchItem);
    }

    private static void registerFactory(String name) throws Exception {
        Constructor<?> constructor = Class.forName(name).getConstructor(String.class, int.class, String.class, ITexture[][][].class);
        FACTORIES.add(() -> {
            try {
                return (MetaTileEntity) constructor.newInstance("test", 8, "", null);
            } catch (Exception failure) {
                throw new AssertionError(failure);
            }
        });
    }

    private static void registerJar() throws Exception {
        jarBlock = new Block(Material.glass) {};
        // A headless Forge test has no active mod container; retain Thaumcraft's exact registry key.
        Class<?> gameData = cpw.mods.fml.common.registry.GameData.class;
        Method main = gameData.getDeclaredMethod("getMain");
        main.setAccessible(true);
        Object data = main.invoke(null);
        Method registerBlock = gameData.getDeclaredMethod("registerBlock", Block.class, String.class, int.class);
        registerBlock.setAccessible(true);
        registerBlock.invoke(data, jarBlock, "Thaumcraft:blockJar", 3015);
        Method registerItem = gameData.getDeclaredMethod("registerItem", Item.class, String.class, int.class);
        registerItem.setAccessible(true);
        registerItem.invoke(data, new ItemBlock(jarBlock), "Thaumcraft:blockJar", 3015);
        assertSame(jarBlock, cpw.mods.fml.common.registry.GameRegistry.findBlock("Thaumcraft", "blockJar"));
        EssentiaCompat.essentiaContainerCompat = new EssentiaCompatEnabled();
    }

    private static void registerFrames() {
        Block block = new Block(Material.iron) {};
        Block.blockRegistry.addObject(3016, "auto_test_frame", block);
        frameItem = new GT_Item_Machines(block) {
            @Override
            public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                    int side, float hitX, float hitY, float hitZ, int meta) {
                ((ConstructionWorld) world).putFrame(x, y, z);
                return true;
            }

            @Override
            public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                    int side, float hitX, float hitY, float hitZ) {
                placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, stack.getItemDamage());
                stack.stackSize--;
                return true;
            }
        };
        Item.itemRegistry.addObject(3016, "auto_test_frame", frameItem);
        GT_OreDictUnificator.set(OrePrefixes.frameGt, Materials.Titanium, new ItemStack(frameItem), true, true);
    }

    @Test
    public void all22ControllersBuildTheirFunctionalRolesAndRespectEmptyInventory() throws Exception {
        int controllers = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/structures/controllers.list")))) {
            String name;
            while ((name = reader.readLine()) != null) {
                for (int selection : new int[]{1, 64}) {
                    Fixture fixture = new Fixture(name, ExtendedFacing.of(ForgeDirection.NORTH));
                    ItemStack trigger = trigger(fixture.machine, selection);
                    Source empty = new Source();
                    int changes = fixture.world.changes;
                    assertEquals(name, 0, fixture.build(trigger, 17, empty));
                    assertEquals(name, changes, fixture.world.changes);
                    assertFalse(fixture.formed());
                    Source source = inventory();
                    complete(fixture, trigger, source, selection == 1 ? 17 : 64);
                    for (Map.Entry<IHatchElement, Integer> role : expectedRoles(name).entrySet()) {
                        assertEquals(name + ": " + role.getKey(), (int) role.getValue(), role.getKey().count(fixture.machine));
                    }
                    assertEquals("An ordinary muffler does not replace useful ports", 64, source.stacks.get(7).stackSize);
                    int consumed = source.consumed;
                    assertEquals(-1, fixture.build(trigger, 64, source));
                    assertEquals(consumed, source.consumed);
                    System.out.println("AUTO_FORMED " + name + " selection=" + selection + " roles=" + expectedRoles(name));
                }
                controllers++;
            }
        }
        assertEquals(22, controllers);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void unrelatedAcceptedTypesCannotSatisfyAutomaticRoleRequests() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/structures/controllers.list")))) {
            String name;
            while ((name = reader.readLine()) != null) {
                Fixture fixture = new Fixture(name, ExtendedFacing.of(ForgeDirection.NORTH));
                ItemStack trigger = trigger(fixture.machine, 1);
                Map<Character, IStructureElement> elements = ((StructureDefinition) fixture.machine.getStructureDefinition()).getElements();
                for (Map.Entry<Character, IStructureElement> entry : elements.entrySet()) {
                    if (entry.getValue().isNavigating()) continue;
                    Source unrelated = new Source(new ItemStack(hatchItem, 1, HATCH_ID + 7));
                    TestPlayer player = new TestPlayer(fixture.world);
                    entry.getValue().survivalPlaceBlock(fixture.machine, fixture.world, 40, 90, 40, trigger,
                            AutoPlaceEnvironment.fromLegacy(unrelated, player, player::addChatMessage));
                    assertEquals(name + " element " + entry.getKey(), 0, unrelated.consumed);
                }
            }
        }
    }

    @Test
    public void scannerOwnEffectPreservesStructureInEveryAlignmentButForeignBlocksDoNot() throws Exception {
        String name = GT_MetaTileEntity_EM_scanner.class.getName();
        Method effect = GT_MetaTileEntity_EM_scanner.class.getDeclaredMethod("quantumStuff", boolean.class);
        effect.setAccessible(true);
        for (ExtendedFacing facing : ExtendedFacing.VALUES) {
            Fixture fixture = new Fixture(name, facing);
            ItemStack trigger = trigger(fixture.machine, 1);
            complete(fixture, trigger, inventory(), 64);
            int[] center = fixture.position(0, 0, 4);
            effect.invoke(fixture.machine, true);
            assertSame(QuantumStuffBlock.INSTANCE, fixture.world.getBlock(center[0], center[1], center[2]));
            assertTrue("Own effect is valid: " + facing, fixture.formed());
            hints.clear();
            fixture.world.isRemote = true;
            fixture.machine.construct(trigger, true);
            fixture.world.isRemote = false;
            assertFalse("No air marker over the running effect", hints.positions.contains(TestWorld.key(center[0], center[1], center[2])));
            fixture.world.setBlock(center[0], center[1], center[2], Blocks.stone, 0, 0);
            assertFalse("Foreign block still invalid", fixture.formed());
            fixture.world.setBlock(center[0], center[1], center[2], QuantumStuffBlock.INSTANCE, 0, 0);
            int[] adjacent = fixture.position(1, 0, 4);
            fixture.world.setBlock(adjacent[0], adjacent[1], adjacent[2], Blocks.stone, 0, 0);
            assertFalse("Rest of the chamber must remain clear", fixture.formed());
            fixture.world.setBlock(adjacent[0], adjacent[1], adjacent[2], Blocks.air, 0, 0);
            effect.invoke(fixture.machine, false);
            assertTrue(fixture.formed());
        }
    }

    @Test
    public void disabledHatchChannelLeavesEveryHatchInInventory() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/structures/controllers.list")))) {
            String name;
            while ((name = reader.readLine()) != null) {
                Fixture fixture = new Fixture(name, ExtendedFacing.of(ForgeDirection.NORTH));
                Source source = inventory();
                for (int attempt = 0; attempt < 1000; attempt++) {
                    int consumed = source.consumed;
                    fixture.build(new ItemStack(Items.stick), 64, source);
                    if (consumed == source.consumed) break;
                }
                for (int index = 0; index < FACTORIES.size(); index++) {
                    assertEquals(name + " must honor disabled hatch channel", 64, source.stacks.get(index).stackSize);
                }
            }
        }
    }

    @Test
    public void essentiaJarHintAndPlayerLegendUseTheSameColor() throws Exception {
        String prefix = "com.github.technus.tectech.compatibility.thaumcraft.thing.metaTileEntity.multi.GT_MetaTileEntity_EM_";
        String color = gregtech.api.util.GTStructureUtility.formatHintText(space.impact.structures.hint.HintColor.LIGHT_BLUE, "\u25a0") + " - ";
        for (String name : new String[]{"essentiaQuantizer", "essentiaDequantizer"}) {
            Fixture fixture = new Fixture(prefix + name, ExtendedFacing.of(ForgeDirection.NORTH));
            hints.clear();
            fixture.world.isRemote = true;
            fixture.machine.construct(new ItemStack(Items.stick), true);
            fixture.world.isRemote = false;
            assertTrue(hints.frames.containsValue(space.impact.structures.hint.HintColor.LIGHT_BLUE));
            assertTrue(String.join("\n", fixture.machine.getStructureDescription(null)).contains(color));
        }
    }

    private static void complete(Fixture fixture, ItemStack trigger, Source source, int budget) {
        for (int attempt = 0; attempt < 1000; attempt++) {
            int before = source.consumed;
            int built = fixture.build(trigger, budget, source);
            assertTrue("Build respects requested budget", built <= budget);
            if (built < 0 || source.consumed == before) break;
        }
        assertTrue("Full survival must form " + fixture.machine.getClass().getSimpleName(), fixture.formed());
    }

    private static Source inventory() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int index = 0; index < FACTORIES.size(); index++) stacks.add(new ItemStack(hatchItem, 64, HATCH_ID + index));
        for (Block block : new Block[]{Blocks.wool, Blocks.stained_hardened_clay, Blocks.stained_glass, Blocks.carpet, QuantumGlassBlock.INSTANCE}) {
            for (int meta = 0; meta < (block == QuantumGlassBlock.INSTANCE ? 1 : 16); meta++) stacks.add(new ItemStack(block, 10000, meta));
        }
        stacks.add(new ItemStack(frameItem, 64));
        stacks.add(new ItemStack(jarBlock, 64));
        return new Source(stacks.toArray(new ItemStack[0]));
    }

    private static ItemStack trigger(GT_MetaTileEntity_MultiblockBase_EM<?> machine, int selection) {
        ItemStack trigger = new ItemStack(Items.stick, selection);
        for (IChannelDefinition channel : machine.getStructureDefinition().getChannelDefinitions()) {
            ChannelDataAccessor.setChannelData(trigger, channel.getChannel(), channel.getChannel().equals(GTStructureChannels.HATCH) ? 1 : selection);
        }
        return trigger;
    }

    private static Map<IHatchElement, Integer> expectedRoles(String name) {
        List<IHatchElement> roles = new ArrayList<>();
        String shortName = name.substring(name.lastIndexOf("GT_MetaTileEntity_") + "GT_MetaTileEntity_".length());
        switch (shortName) {
            case "EM_annihilation": case "EM_bhg":
                roles.addAll(Arrays.asList(PowerInput, PowerOutput, ElementalInput, ElementalOutput, ElementalOverflow)); break;
            case "EM_crafting": case "EM_stabilizer": case "EM_wormhole": case "EM_junction": case "EM_machine":
                roles.addAll(Arrays.asList(PowerInput, ElementalInput, ElementalOutput, ElementalOverflow)); break;
            case "EM_collider": case "EM_decay":
                roles.addAll(Arrays.asList(PowerInput, PowerOutput, ElementalInput, ElementalOutput, ElementalOutput, ElementalOverflow)); break;
            case "EM_dataBank": roles.addAll(Arrays.asList(PowerInput, DataAccess, RecipeDataOutput)); break;
            case "EM_infuser": roles.add(PowerInput); break;
            case "EM_switch": roles.addAll(Arrays.asList(PowerInput, DataInput, DataOutput)); break;
            case "EM_transformer": roles.addAll(Arrays.asList(PowerInput, PowerOutput)); break;
            case "TM_microwave": roles.addAll(Arrays.asList(PowerInput, ItemOutput)); break;
            case "TM_teslaCoil": roles.addAll(Arrays.asList(PowerInput, Capacitor)); break;
            case "EM_computer": roles.addAll(Arrays.asList(PowerInput, DataOutput, TecTechHatchElement.Uncertainty, TecTechHatchElement.Rack)); break;
            case "EM_research": roles.addAll(Arrays.asList(PowerInput, DataInput, TecTechHatchElement.Holder)); break;
            case "EM_scanner": roles.addAll(Arrays.asList(PowerInput, DataInput, ElementalInput, ElementalOutput, ElementalOverflow)); break;
            case "EM_quantizer": roles.addAll(Arrays.asList(PowerInput, ItemInput, FluidInput, ElementalOutput, ElementalOverflow)); break;
            case "EM_dequantizer": roles.addAll(Arrays.asList(PowerInput, ItemOutput, FluidOutput, ElementalInput, ElementalOverflow)); break;
            case "EM_essentiaQuantizer": roles.addAll(Arrays.asList(PowerInput, ElementalOutput, ElementalOverflow)); break;
            case "EM_essentiaDequantizer": roles.addAll(Arrays.asList(PowerInput, ElementalInput, ElementalOverflow)); break;
            default: throw new AssertionError("Controller missing from role contract: " + name);
        }
        if (!shortName.equals("EM_transformer")) roles.add(Maintenance);
        Map<IHatchElement, Integer> expected = new LinkedHashMap<>();
        for (IHatchElement role : roles) expected.merge(role, 1, Integer::sum);
        return expected;
    }

    private static final class Fixture {
        final ConstructionWorld world = new ConstructionWorld();
        final GT_MetaTileEntity_MultiblockBase_EM<?> machine;
        final TestTile controller;

        Fixture(String name, ExtendedFacing facing) throws Exception {
            machine = (GT_MetaTileEntity_MultiblockBase_EM<?>) Class.forName(name).getConstructor(String.class).newInstance(name);
            controller = new TestTile(world, machine, facing.getDirection());
            world.putTile(0, 64, 0, controller);
            machine.setExtendedFacing(facing);
        }

        int build(ItemStack trigger, int budget, Source source) {
            return machine.survivalConstruct(trigger, budget, ISurvivalBuildEnvironment.create(source, new TestPlayer(world)));
        }

        boolean formed() {
            return machine.checkMachine(controller, null);
        }

        int[] position(int a, int b, int c) {
            int[] result = new int[3];
            machine.getExtendedFacing().getWorldOffset(new int[]{a, b, c}, result);
            result[1] += 64;
            return result;
        }
    }

    private static final class ConstructionWorld extends TestWorld {
        final Map<String, TileEntity> components = new HashMap<>();

        @Override
        public TileEntity getTileEntity(int x, int y, int z) {
            TileEntity component = components.get(key(x, y, z));
            return component == null ? super.getTileEntity(x, y, z) : component;
        }

        @Override
        public boolean setBlock(int x, int y, int z, Block block, int meta, int flags) {
            components.remove(key(x, y, z));
            boolean placed = super.setBlock(x, y, z, block, meta, flags);
            if (block == jarBlock) {
                TileJarFillable jar = new TileJarFillable();
                jar.setWorldObj(this);
                jar.xCoord = x; jar.yCoord = y; jar.zCoord = z;
                components.put(key(x, y, z), jar);
            }
            return placed;
        }

        void putFrame(int x, int y, int z) {
            GT_MetaPipeEntity_Frame frame = new GT_MetaPipeEntity_Frame("titanium", Materials.Titanium);
            setBlock(x, y, z, Blocks.iron_block, frame.getTileEntityBaseType(), 0);
            BaseMetaPipeEntity tile = new BaseMetaPipeEntity() {
                @Override public void markDirty() {}
            };
            tile.setWorldObj(this);
            tile.xCoord = x; tile.yCoord = y; tile.zCoord = z;
            frame.setBaseMetaTileEntity(tile);
            components.put(key(x, y, z), tile);
        }
    }
}
