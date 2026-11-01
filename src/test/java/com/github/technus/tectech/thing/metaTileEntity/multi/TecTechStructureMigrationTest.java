package com.github.technus.tectech.thing.metaTileEntity.multi;

import com.github.technus.tectech.TecTech;
import com.github.technus.tectech.loader.TecTechConfig;
import com.github.technus.tectech.mechanics.enderStorage.ForgeTestRunner;
import com.github.technus.tectech.thing.block.QuantumGlassBlock;
import com.github.technus.tectech.thing.casing.TT_Container_Casings;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_Holder;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputElemental;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OutputElemental;
import com.github.technus.tectech.thing.metaTileEntity.multi.StructureTestWorld.HintRecorder;
import com.github.technus.tectech.thing.metaTileEntity.multi.StructureTestWorld.Source;
import com.github.technus.tectech.thing.metaTileEntity.multi.StructureTestWorld.TestPlayer;
import com.github.technus.tectech.thing.metaTileEntity.multi.StructureTestWorld.TestTile;
import com.github.technus.tectech.thing.metaTileEntity.multi.StructureTestWorld.TestWorld;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Casings;
import gregtech.api.interfaces.ICasing;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_EnhancedMultiBlockBase;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_TooltipMultiBlockBase;
import gregtech.api.util.GT_Config;
import ic2.core.init.MainConfig;
import ic2.core.util.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import space.impact.structures.Main;
import space.impact.structures.hint.HintColor;
import space.impact.structures.alignment.constructable.ISurvivalConstructable;
import space.impact.structures.alignment.constructable.ChannelDataAccessor;
import space.impact.structures.structure.IChannelDefinition;
import gregtech.api.util.GTStructureChannels;
import space.impact.structures.alignment.enumerable.ExtendedFacing;
import space.impact.structures.structure.IStructureElement;
import space.impact.structures.structure.ISurvivalBuildEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(ForgeTestRunner.class)
public class TecTechStructureMigrationTest {
    @BeforeClass
    public static void initialize() throws Exception {
        Field side = FMLRelaunchLog.class.getDeclaredField("side");
        side.setAccessible(true);
        side.set(null, Side.SERVER);
        Launch.classLoader = (LaunchClassLoader) TecTechStructureMigrationTest.class.getClassLoader();
        Launch.blackboard = new HashMap<>();
        Launch.blackboard.put("fml.deobfuscatedEnvironment", false);
        Bootstrap.func_151354_b();
        TecTech.configTecTech = new TecTechConfig(new File("build/test-config"), "impact", "tectech");
        Config config = new Config("test");
        config.set("balance/energy/generator/nuclear", 1.0f);
        Field ic2Config = MainConfig.class.getDeclaredField("config");
        ic2Config.setAccessible(true);
        ic2Config.set(null, config);
        GregTech_API.sMachineFile = new GT_Config(new Configuration() {
            @Override
            public void load() {
            }

            @Override
            public void save() {
            }
        });
        for (TecTechCasings casing : TecTechCasings.values()) {
            assertNull(casing.getBlock());
            assertTrue(casing.getMetadata() >= 0 && casing.getMetadata() < 16);
        }
        TT_Container_Casings.sBlockCasingsTT = Blocks.wool;
        TT_Container_Casings.sBlockCasingsBA0 = Blocks.stained_hardened_clay;
        TT_Container_Casings.sHintCasingsTT = Blocks.stone;
        GregTech_API.sBlockCasings1 = Blocks.wool;
        GregTech_API.sBlockCasings4 = Blocks.wool;
        GregTech_API.sBlockMachines = Blocks.iron_block;
        QuantumGlassBlock.INSTANCE = new QuantumGlassBlock();
    }

    @Test
    public void descriptorsResolveRegisteredBlocksAndTeslaMetadata() {
        assertSame(Blocks.wool, TecTechCasings.MOLECULAR.getBlock());
        assertEquals(4, TecTechCasings.MOLECULAR.getMetadata());
        assertEquals(16, TecTechCasings.TESLA_PRIMARY_REDSTONE.getTextureId() - TecTechCasings.HIGH_POWER.getTextureId());
        assertSame(Blocks.stained_hardened_clay, TecTechCasings.TESLA_BASE.getBlock());
        assertEquals(9, TecTechCasings.TESLA_PRIMARY_ZPM.getMetadata());
        assertSame(TecTechCasings.TESLA_PRIMARY_ZPM, TecTechCasings.teslaWinding(9));
        assertNull(TecTechCasings.teslaWinding(6));
        assertNull(TecTechCasings.teslaWinding(7));
        assertNull(TecTechCasings.teslaWinding(8));
        assertNull(TecTechCasings.teslaWinding(10));
    }

    @Test
    public void everyPiecePreservesItsCheckedGeometry() throws Exception {
        int pieces = 0;
        try (BufferedReader reader = resource("checked-positions.tsv")) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t");
                GT_MetaTileEntity_MultiblockBase_EM<?> machine = machine(fields[0]);
                Set<String> expected = new HashSet<>(Arrays.asList(fields[2].split(";")));
                Set<String> actual = new HashSet<>();
                int x = 0, y = 0, z = 0;
                for (IStructureElement<?> element : machine.getStructureDefinition().getStructureFor(fields[1])) {
                    if (element.isNavigating()) {
                        x = (element.resetA() ? 0 : x) + element.getStepA();
                        y = (element.resetB() ? 0 : y) + element.getStepB();
                        z = (element.resetC() ? 0 : z) + element.getStepC();
                    } else {
                        assertTrue(actual.add(x + "," + y + "," + z));
                        x++;
                    }
                }
                assertEquals(fields[0] + ":" + fields[1], expected, actual);
                pieces++;
            }
        }
        assertEquals(27, pieces);
    }

    @Test
    public void allControllersHaveSurvivalAlignmentAndCachedTooltips() throws Exception {
        Method tooltip = GT_MetaTileEntity_TooltipMultiBlockBase.class.getDeclaredMethod("getTooltip");
        tooltip.setAccessible(true);
        int controllers = 0;
        try (BufferedReader reader = resource("controllers.list")) {
            String name;
            while ((name = reader.readLine()) != null) {
                GT_MetaTileEntity_MultiblockBase_EM<?> machine = machine(name);
                assertTrue(name, machine instanceof ISurvivalConstructable);
                assertNotSame(ISurvivalConstructable.class, machine.getClass().getMethod("survivalConstruct", ItemStack.class, int.class, ISurvivalBuildEnvironment.class).getDeclaringClass());
                for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                    assertTrue(name, machine.isFacingValid((byte) direction.ordinal()));
                }
                for (ExtendedFacing facing : ExtendedFacing.VALUES) {
                    assertTrue(name, machine.isNewExtendedFacingValid(facing));
                }
                Object cached = tooltip.invoke(machine);
                assertNotNull(name, cached);
                assertSame(cached, tooltip.invoke(machine));
                assertTrue(machine.getDescription().length > 1);
                assertTrue(machine.getSecondaryDescription().length > 0);
                for (IChannelDefinition channel : machine.getStructureDefinition().getChannelDefinitions()) {
                    assertTrue(name + " missing " + channel.getChannel(),
                            String.join("\n", machine.getSecondaryDescription()).contains("(" + channel.getChannel() + ")"));
                    assertTrue(name + " missing channel hint",
                            String.join("\n", machine.getStructureDescription(null)).contains("(" + channel.getChannel() + ")"));
                    String description = plain(machine.getSecondaryDescription());
                    String header = net.minecraft.util.StatCollector.translateToLocal("gt.tooltip.channels");
                    assertTrue(name + " channel outside section", description.indexOf("(" + channel.getChannel() + ")") > description.indexOf(header));
                    assertFalse(name + " generic channel description", description.contains(GTStructureChannels.getDescription(channel.getChannel())));
                    assertFalse(name + " channels in primary tooltip", plain(machine.getDescription()).contains(header));
                }
                assertTrue(machine.getStructureDescription(null).length > 0);
                GT_MetaTileEntity_TooltipMultiBlockBase.invalidateTooltipCache();
                assertNotSame(cached, tooltip.invoke(machine));
                controllers++;
            }
        }
        assertEquals(22, controllers);
    }

    @Test
    public void allTooltipsKeepOriginalTextAndListMaterialsAfterLanguageReload() throws Exception {
        List<String[]> expected = new ArrayList<>();
        List<GT_MetaTileEntity_MultiblockBase_EM<?>> machines = new ArrayList<>();
        try (BufferedReader reader = resource("tooltip-content.tsv")) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\t", -1);
                expected.add(fields);
                machines.add(machine(fields[0]));
            }
        }
        assertEquals(22, machines.size());
        Map<Map<String, String>, Map<String, String>> originals = translationMaps();
        try {
            for (String locale : new String[] { "en_US", "ru_RU" }) {
                Map<String, String> language = language("en_US");
                language.putAll(language(locale));
                for (Map<String, String> target : originals.keySet()) {
                    target.putAll(language);
                }
                GT_MetaTileEntity_TooltipMultiBlockBase.invalidateTooltipCache();
                for (int index = 0; index < machines.size(); index++) {
                    GT_MetaTileEntity_MultiblockBase_EM<?> machine = machines.get(index);
                    String[] fields = expected.get(index);
                    String primary = plain(machine.getDescription());
                    String structure = plain(machine.getSecondaryDescription());
                    String hints = plain(machine.getStructureDescription(null));
                    String channelHeader = language.get("gt.tooltip.channels");
                    int channelStart = structure.indexOf(channelHeader);
                    assertTrue(fields[0] + " missing channel section", channelStart > 0);
                    assertTrue(hints.contains(channelHeader));
                    assertFalse(primary.contains(channelHeader));
                    for (IChannelDefinition channel : machine.getStructureDefinition().getChannelDefinitions()) {
                        String label = GTStructureChannels.getLabel(channel.getChannel());
                        assertTrue(fields[0], structure.indexOf(label + " (" + channel.getChannel() + ")") > channelStart);
                        assertTrue(fields[0], String.join("\n", machine.getSecondaryDescription())
                                .contains(EnumChatFormatting.AQUA + label + EnumChatFormatting.DARK_GRAY));
                    }
                    for (String key : fields[1].split(";")) {
                        if (!key.isEmpty()) {
                            assertTrue(fields[0] + " lost description " + key, primary.contains(language.get(key)));
                        }
                    }
                    for (String key : fields[2].split(";")) {
                        if (!key.isEmpty()) {
                            String translated = language.get(key);
                            assertTrue(fields[0] + " lost Shift explanation " + key, structure.contains(translated));
                            assertTrue(fields[0] + " lost projector explanation " + key, hints.contains(translated));
                        }
                    }
                    assertFalse(fields[0], structure.contains("gt.blockmachines."));
                    assertFalse(fields[0], structure.contains("tt.structure."));
                    for (String specification : fields[3].split(";")) {
                        if (specification.isEmpty()) {
                            continue;
                        }
                        String[] parts = specification.split(":");
                        String[] casingName = parts[0].split("\\.");
                        ICasing casing = casingName[0].equals("TecTechCasings")
                                ? TecTechCasings.valueOf(casingName[1]) : Casings.valueOf(casingName[1]);
                        String material = parts[1] + " " + casing.getDisplayName();
                        assertTrue(fields[0] + " omits " + material, structure.contains(material));
                    }
                    if (!fields[0].endsWith("EM_transformer")) {
                        assertTrue(fields[0], structure.contains(language.get("tt.structure.casing_ranges")));
                    }
                    if (fields[0].endsWith("EM_computer")) {
                        assertTrue(structure.contains(language.get("TileEntity_DESCRIPTION_multimachine.em.computer_Channel_Index_0")));
                        assertTrue(structure.contains("1 " + language.get("tt.structure.hatch.Uncertainty")));
                        assertTrue(structure.contains(language.get("tt.structure.computer.uncertainty")));
                    }
                    if (fields[0].endsWith("EM_scanner")) {
                        assertTrue(structure.contains(language.get("tt.structure.scanner.output")));
                    }
                }
            }
        } finally {
            originals.forEach((target, original) -> {
                target.clear();
                target.putAll(original);
            });
            GT_MetaTileEntity_TooltipMultiBlockBase.invalidateTooltipCache();
        }
    }

    private static String plain(String[] lines) {
        return EnumChatFormatting.getTextWithoutFormattingCodes(String.join("\\n", lines));
    }

    private Map<String, String> language(String locale) throws Exception {
        Map<String, String> result = new HashMap<>();
        for (String mod : new String[] { "gregtech", "tectech" }) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream("/assets/" + mod + "/lang/" + locale + ".lang"), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int separator = line.indexOf('=');
                    if (!line.startsWith("#") && separator > 0) {
                        result.put(line.substring(0, separator), line.substring(separator + 1));
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<Map<String, String>, Map<String, String>> translationMaps() throws Exception {
        Map<Map<String, String>, Map<String, String>> originals = new IdentityHashMap<>();
        for (Field field : StatCollector.class.getDeclaredFields()) {
            if (field.getType() != StringTranslate.class) {
                continue;
            }
            field.setAccessible(true);
            Object translator = field.get(null);
            for (Field translations : StringTranslate.class.getDeclaredFields()) {
                if (translations.getType() == Map.class) {
                    translations.setAccessible(true);
                    Map<String, String> entries = (Map<String, String>) translations.get(translator);
                    originals.put(entries, new HashMap<>(entries));
                }
            }
        }
        assertFalse("The server-safe translation map must be available", originals.isEmpty());
        return originals;
    }

    @Test
    public void hatchMatchingDoesNotTreatEveryHatchAsClassic() {
        GT_MetaTileEntity_Hatch_InputElemental input = new GT_MetaTileEntity_Hatch_InputElemental("input", 8, "", null);
        GT_MetaTileEntity_Hatch_OutputElemental output = new GT_MetaTileEntity_Hatch_OutputElemental("output", 8, "", null);
        GT_MetaTileEntity_Hatch_Holder holder = new GT_MetaTileEntity_Hatch_Holder("holder", 8, "", null);
        assertFalse(TecTechHatchElement.Classic.matches(input));
        assertFalse(TecTechHatchElement.Classic.matches(output));
        assertFalse(TecTechHatchElement.Classic.matches(holder));
        assertTrue(TecTechHatchElement.Elemental.matches(input));
        assertTrue(TecTechHatchElement.Elemental.matches(output));
        assertFalse(TecTechHatchElement.Elemental.matches(holder));
        assertTrue(TecTechHatchElement.Holder.matches(holder));
        assertFalse(TecTechHatchElement.ElementalInput.matches(output));
    }

    @Test
    public void allHologramsArePureAndCombineCasingAndHatchHints() throws Exception {
        HintRecorder hints = installHints();
        TecTech.proxy = new com.github.technus.tectech.proxy.CommonProxy();
        try (BufferedReader reader = resource("controllers.list")) {
            String name;
            while ((name = reader.readLine()) != null) {
                for (int size : new int[] { 1, 2, 3, 64 }) {
                    TestWorld world = new TestWorld();
                    GT_MetaTileEntity_MultiblockBase_EM<?> machine = machine(name);
                    world.putTile(0, 64, 0, new TestTile(world, machine, ForgeDirection.NORTH));
                    world.isRemote = true;
                    int changes = world.changes;
                    hints.clear();
                    machine.construct(new ItemStack(Items.stick, size), true);
                    assertEquals(name, changes, world.changes);
                    assertFalse(name, hints.positions.isEmpty());
                    assertFalse(name, hints.frames.isEmpty());
                    assertFalse("White frames hide on TecTech casings: " + name,
                            hints.frames.containsValue(HintColor.WHITE));
                    if (machine instanceof GT_MetaTileEntity_TM_teslaCoil) {
                        assertEquals("All titanium frames retain their material hologram", 16, hints.tintedPositions.size());
                        for (String position : hints.tintedPositions) {
                            assertFalse("Fixed titanium frames are not hatch positions: " + position,
                                    hints.frames.containsKey(position));
                        }
                    }
                    Set<String> first = new HashSet<>(hints.positions);
                    Map<String, Block> firstBlocks = new HashMap<>(hints.blocks);
                    Map<String, Integer> firstMetadata = new HashMap<>(hints.metadata);
                    hints.clear();
                    ItemStack named = new ItemStack(Items.stick, size == 1 ? 64 : 1);
                    for (IChannelDefinition channel : machine.getStructureDefinition().getChannelDefinitions()) {
                        if (!GTStructureChannels.HATCH.equals(channel.getChannel())) {
                            ChannelDataAccessor.setChannelData(named, channel.getChannel(), size);
                        }
                    }
                    machine.construct(named, true);
                    assertEquals(name, first, hints.positions);
                    assertEquals(name, firstBlocks, hints.blocks);
                    assertEquals(name, firstMetadata, hints.metadata);
                    assertEquals(name, changes, world.changes);
                }
            }
        }
    }

    @Test
    public void transformerSurvivalRespectsBudgetResourcesAndObstacles() throws Exception {
        installHints();
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TestWorld world = new TestWorld();
            GT_MetaTileEntity_EM_transformer machine = new GT_MetaTileEntity_EM_transformer("survival");
            TestTile tile = new TestTile(world, machine, direction);
            world.putTile(0, 64, 0, tile);
            Source source = new Source(new ItemStack(Blocks.wool, 100, 0), new ItemStack(Blocks.wool, 10, 15));
            ISurvivalBuildEnvironment environment = ISurvivalBuildEnvironment.create(source, new TestPlayer(world));
            ItemStack trigger = new ItemStack(Items.stick);
            int[] obstacle = new int[3];
            machine.getExtendedFacing().getWorldOffset(new int[] { -1, -1, 0 }, obstacle);
            obstacle[1] += 64;
            world.setBlock(obstacle[0], obstacle[1], obstacle[2], Blocks.bedrock, 0, 0);
            assertEquals(0, machine.survivalConstruct(trigger, 7, environment));
            assertEquals(0, source.consumed);
            assertSame(Blocks.bedrock, world.getBlock(obstacle[0], obstacle[1], obstacle[2]));
            world.setBlock(obstacle[0], obstacle[1], obstacle[2], Blocks.air, 0, 0);
            for (int attempt = 0; attempt < 10; attempt++) {
                int previous = source.consumed;
                int built = machine.survivalConstruct(trigger, 7, environment);
                if (built == -1) {
                    break;
                }
                assertTrue(built >= 0 && built <= 7);
                assertEquals(built, source.consumed - previous);
            }
            assertEquals(26, source.consumed);
            assertTrue(machine.checkMachine(tile, null));
            assertTrue(machine.checkMachine(tile, null));
            world.setBlock(obstacle[0], obstacle[1], obstacle[2], Blocks.air, 0, 0);
            assertFalse(machine.checkMachine(tile, null));
        }
    }

    @Test
    public void computerSurvivalSharesOneBudgetAcrossAllPieces() throws Exception {
        installHints();
        for (int size : new int[] { 1, 2, 12, 64 }) {
            TestWorld world = new TestWorld();
            GT_MetaTileEntity_EM_computer machine = new GT_MetaTileEntity_EM_computer("computer");
            TestTile tile = new TestTile(world, machine, ForgeDirection.NORTH);
            world.putTile(0, 64, 0, tile);
            Source source = new Source(new ItemStack(Blocks.wool, 500, 1), new ItemStack(Blocks.wool, 500, 2), new ItemStack(Blocks.wool, 500, 3));
            ISurvivalBuildEnvironment environment = ISurvivalBuildEnvironment.create(source, new TestPlayer(world));
            ItemStack trigger = new ItemStack(Items.stick, size == 1 ? 64 : 1);
            ChannelDataAccessor.setChannelData(trigger, GTStructureChannels.LENGTH, size);
            for (int attempt = 0; attempt < 100; attempt++) {
                int previous = source.consumed;
                int built = machine.survivalConstruct(trigger, 7, environment);
                assertTrue(built >= 0 && built <= 7);
                assertEquals(built, source.consumed - previous);
                if (built == 0) {
                    break;
                }
            }
            TestWorld creative = new TestWorld();
            GT_MetaTileEntity_EM_computer expected = new GT_MetaTileEntity_EM_computer("creative");
            creative.putTile(0, 64, 0, new TestTile(creative, expected, ForgeDirection.NORTH));
            expected.construct(new ItemStack(Items.stick, size), false);
            assertEquals(creative.blocks, world.blocks);
            assertEquals(creative.metadata, world.metadata);
            assertFalse("An uncertainty resolver is mandatory", machine.checkMachine(tile, null));
        }
    }

    @Test
    public void savedAlignmentAndEmStateRoundTripInEveryOrientation() throws Exception {
        Field alignment = GT_MetaTileEntity_EnhancedMultiBlockBase.class.getDeclaredField("extendedFacing");
        alignment.setAccessible(true);
        for (ExtendedFacing facing : ExtendedFacing.VALUES) {
            TestWorld world = new TestWorld();
            GT_MetaTileEntity_EM_transformer saved = new GT_MetaTileEntity_EM_transformer("saved");
            new TestTile(world, saved, facing.getDirection());
            alignment.set(saved, facing);
            saved.eAmpereFlow = 128;
            saved.ePowerPass = true;
            saved.eSafeVoid = true;
            NBTTagCompound tag = new NBTTagCompound();
            saved.saveNBTData(tag);
            assertEquals(facing.getRotation().getIndex(), tag.getByte("eRotation"));
            assertEquals(facing.getFlip().getIndex(), tag.getByte("eFlip"));
            GT_MetaTileEntity_EM_transformer loaded = new GT_MetaTileEntity_EM_transformer("loaded");
            new TestTile(world, loaded, facing.getDirection());
            loaded.loadNBTData(tag);
            assertEquals(facing, loaded.getExtendedFacing());
            assertEquals(128, loaded.eAmpereFlow);
            assertTrue(loaded.ePowerPass);
            assertTrue(loaded.eSafeVoid);
            assertEquals(2, loaded.mInventory.length);
        }
    }

    @Test
    public void controllerClassesLoadWithoutClientClasses() throws Exception {
        try (URLClassLoader server = new URLClassLoader(((LaunchClassLoader) getClass().getClassLoader()).getURLs(), null) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("net.minecraft.client.") || name.startsWith("org.lwjgl.")) {
                    throw new ClassNotFoundException("Client dependency on server: " + name);
                }
                return super.loadClass(name, resolve);
            }
        }; BufferedReader reader = resource("controllers.list")) {
            Class<?> base = server.loadClass(GT_MetaTileEntity_EnhancedMultiBlockBase.class.getName());
            String name;
            while ((name = reader.readLine()) != null) {
                assertTrue(name, base.isAssignableFrom(Class.forName(name, false, server)));
            }
        }
    }

    @Test
    public void colliderBothTiersBuildAndValidateInEveryDirection() throws Exception {
        installHints();
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            for (int tier : new int[] { 1, 2 }) {
                TestWorld world = new TestWorld();
                GT_MetaTileEntity_EM_collider machine = new GT_MetaTileEntity_EM_collider("collider");
                TestTile tile = new TestTile(world, machine, direction);
                world.putTile(0, 64, 0, tile);
                machine.construct(new ItemStack(Items.stick, tier), false);
                assertTrue(direction + " tier " + tier, machine.checkMachine(tile, null));
                assertTrue(machine.checkMachine(tile, null));
                assertEquals(tier, machine.eTier);
                int[] core = new int[3];
                machine.getExtendedFacing().getWorldOffset(new int[] { 0, 0, 2 }, core);
                world.setBlock(core[0], 64 + core[1], core[2], Blocks.air, 0, 0);
                assertFalse(machine.checkMachine(tile, null));
            }
        }
    }

    private HintRecorder installHints() throws Exception {
        HintRecorder hints = new HintRecorder();
        Field proxy = Main.class.getDeclaredField("proxy");
        proxy.setAccessible(true);
        proxy.set(null, hints);
        Field block = Main.class.getDeclaredField("blockHint");
        block.setAccessible(true);
        block.set(null, new Block(Material.rock) {
        });
        return hints;
    }

    private static GT_MetaTileEntity_MultiblockBase_EM<?> machine(String name) throws Exception {
        String machineName = "migration-test";
        if (name.endsWith("GT_MetaTileEntity_EM_computer")) machineName = "multimachine.em.computer";
        if (name.endsWith("GT_MetaTileEntity_EM_collider")) machineName = "multimachine.em.collider";
        if (name.endsWith("GT_MetaTileEntity_EM_bhg")) machineName = "multimachine.em.blackholegenerator";
        if (name.endsWith("GT_MetaTileEntity_EM_junction")) machineName = "multimachine.em.junction";
        if (name.endsWith("GT_MetaTileEntity_TM_teslaCoil")) machineName = "multimachine.tm.teslaCoil";
        return (GT_MetaTileEntity_MultiblockBase_EM<?>) Class.forName(name).getConstructor(String.class).newInstance(machineName);
    }

    private BufferedReader resource(String name) {
        return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/structures/" + name), StandardCharsets.UTF_8));
    }
}
