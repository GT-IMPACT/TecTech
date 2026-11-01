package com.github.technus.tectech.thing.metaTileEntity.multi;

import gregtech.api.util.GTStructureChannels;
import com.github.technus.tectech.TecTech;
import com.github.technus.tectech.mechanics.elementalMatter.core.cElementalInstanceStackMap;
import com.github.technus.tectech.mechanics.elementalMatter.core.stacks.cElementalDefinitionStack;
import com.github.technus.tectech.mechanics.elementalMatter.core.stacks.cElementalInstanceStack;
import com.github.technus.tectech.mechanics.elementalMatter.core.tElementalException;
import com.github.technus.tectech.recipe.TT_recipe;
import com.github.technus.tectech.thing.CustomItemList;
import com.github.technus.tectech.thing.block.QuantumGlassBlock;
import com.github.technus.tectech.thing.block.QuantumStuffBlock;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.item.ElementalDefinitionScanStorage_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.INameFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.IStatusFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.Parameters;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechAutoHatch;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.util.CommonValues;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Energy;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_EnergyMulti;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.util.GT_LanguageManager;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.reflect.FieldUtils;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.IStructureElement;
import space.impact.structures.structure.AutoPlaceEnvironment;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;

import static com.github.technus.tectech.loader.TecTechConfig.DEBUG_MODE;
import static com.github.technus.tectech.mechanics.elementalMatter.definitions.primitive.cPrimitiveDefinition.nbtE__;
import static com.github.technus.tectech.recipe.TT_recipe.E_RECIPE_ID;
import static com.github.technus.tectech.thing.metaTileEntity.multi.GT_MetaTileEntity_EM_crafting.crafter;
import static com.github.technus.tectech.thing.metaTileEntity.multi.em_machine.GT_MetaTileEntity_EM_machine.machine;
import static com.github.technus.tectech.util.CommonValues.V;
import static com.github.technus.tectech.util.CommonValues.VN;
import static com.github.technus.tectech.util.Util.areBitsSet;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.StatCollector.translateToLocal;
import static net.minecraft.util.StatCollector.translateToLocalFormatted;
import static space.impact.structures.structure.StructureUtility.ofBlock;
import static space.impact.structures.structure.StructureUtility.isAir;
import static space.impact.structures.structure.StructureUtility.transpose;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_scanner extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_scanner> {
    //region variables
    public static final int SCAN_DO_NOTHING = 0,
            SCAN_GET_NOMENCLATURE = 1, SCAN_GET_DEPTH_LEVEL = 2, SCAN_GET_AMOUNT = 4, SCAN_GET_CHARGE = 8,
            SCAN_GET_MASS = 16, SCAN_GET_ENERGY_LEVEL = 32, SCAN_GET_TIMESPAN_INFO = 64, SCAN_GET_ENERGY_STATES = 128,
            SCAN_GET_COLOR = 256, SCAN_GET_AGE = 512, SCAN_GET_TIMESPAN_MULT = 1024, SCAN_GET_CLASS_TYPE = 2048;
    //region structure
    private static final IStructureDefinition<GT_MetaTileEntity_EM_scanner> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_scanner>builder()
                    .addChannel(GTStructureChannels.HATCH, 1, 1)
                    .addShape("main", transpose(new String[][]{
                            {"CCCCC", "BBBBB", "BBDBB", "BDDDB", "BDDDB", "BDDDB", "BBDBB", "EEEEE"},
                            {"CAAAC", "BBBBB", "BDDDB", "D---D", "D---D", "D---D", "BDDDB", "EBBBE"},
                            {"CA~AC", "BBBBB", "DDDDD", "D---D", "D-Q-D", "D---D", "DDGDD", "EBFBE"},
                            {"CAAAC", "BBBBB", "BDDDB", "D---D", "D---D", "D---D", "BDDDB", "EBBBE"},
                            {"CCCCC", "BBBBB", "BBDBB", "BDDDB", "BDDDB", "BDDDB", "BBDBB", "EEEEE"}
                    }))
                    .addElement('Q', quantumSpace())
                    .addElement('A', TecTechCasings.HIGH_POWER.asElement())
                    .addElement('B', TecTechCasings.MOLECULAR.asElement())
                    .addElement('C', withHint(buildHatchAdder(GT_MetaTileEntity_EM_scanner.class).allow(TecTechHatchElement.Classic).autoPlace(TecTechAutoHatch.Maintenance).autoPlace(TecTechAutoHatch.PowerInput, TecTechAutoHatch.DataInput).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.GREEN))
                    .addElement('D', ofBlock(QuantumGlassBlock.INSTANCE, 0))
                    .addElement('E', withHint(buildHatchAdder(GT_MetaTileEntity_EM_scanner.class).allow(TecTechHatchElement.ElementalOverflow).autoPlace(TecTechHatchElement.ElementalOverflow).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.LIME))
                    .addElement('F', buildHatchAdder(GT_MetaTileEntity_EM_scanner.class).hint(HintColor.BLUE).build(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).atLeast(TecTechHatchElement.ElementalInput).build()))
                    .addElement('G', buildHatchAdder(GT_MetaTileEntity_EM_scanner.class).hatchFacing((machine, tile) -> machine.getExtendedFacing().getDirection()).hint(HintColor.ORANGE).build(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).atLeast(TecTechHatchElement.ElementalOutput).build()))
                    .build();
    //region parameters
    private static final INameFunction<GT_MetaTileEntity_EM_scanner> CONFIG_NAME =
            (base, p) -> "Config at Depth: " + (p.hatchId() * 2 + p.parameterId());
    private static final IStatusFunction<GT_MetaTileEntity_EM_scanner> CONFIG_STATUS =
            (base, p) -> {
                double v = p.get();
                if (Double.isNaN(v)) {
                    return LedStatus.STATUS_WRONG;
                }
                v = (int) v;
                if (v == 0) return LedStatus.STATUS_NEUTRAL;
                if (v >= SCAN_GET_CLASS_TYPE) return LedStatus.STATUS_TOO_HIGH;
                if (v < 0) return LedStatus.STATUS_TOO_LOW;
                return LedStatus.STATUS_OK;
            };
    protected Parameters.Group.ParameterIn[] scanConfiguration;
    private TT_recipe.TT_EMRecipe.TT_EMRecipe eRecipe;
    private cElementalDefinitionStack objectResearched;
    //endregion
    private cElementalInstanceStackMap objectsScanned;
    private String machineType;
    //endregion
    private long computationRemaining, computationRequired;
    private int[] scanComplexity;
    private String clientLocale = "en_US";
    //endregion

    public GT_MetaTileEntity_EM_scanner(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        eDismantleBoom = true;
    }

    public GT_MetaTileEntity_EM_scanner(String aName) {
        super(aName);
        eDismantleBoom = true;
    }

    /** The scanner renders its own effect here; all other chamber positions still require air. */
    private static IStructureElement<GT_MetaTileEntity_EM_scanner> quantumSpace() {
        return new IStructureElement<GT_MetaTileEntity_EM_scanner>() {
            @Override
            public boolean check(GT_MetaTileEntity_EM_scanner machine, World world, int x, int y, int z) {
                return world.isAirBlock(x, y, z) || world.getBlock(x, y, z) == QuantumStuffBlock.INSTANCE;
            }

            @Override
            public boolean couldBeValid(GT_MetaTileEntity_EM_scanner machine, World world,
                    int x, int y, int z, ItemStack trigger) {
                return check(machine, world, x, y, z);
            }

            @Override
            public boolean spawnHint(GT_MetaTileEntity_EM_scanner machine, World world,
                    int x, int y, int z, ItemStack trigger) {
                return world.getBlock(x, y, z) == QuantumStuffBlock.INSTANCE
                        || isAir().spawnHint(machine, world, x, y, z, trigger);
            }

            @Override
            public boolean placeBlock(GT_MetaTileEntity_EM_scanner machine, World world,
                    int x, int y, int z, ItemStack trigger) {
                return check(machine, world, x, y, z);
            }

            @Override
            public PlaceResult survivalPlaceBlock(GT_MetaTileEntity_EM_scanner machine, World world,
                    int x, int y, int z, ItemStack trigger, AutoPlaceEnvironment environment) {
                return check(machine, world, x, y, z) ? PlaceResult.SKIP : PlaceResult.REJECT;
            }
        };
    }

    private void quantumStuff(boolean shouldExist) {
        IGregTechTileEntity base = getBaseMetaTileEntity();
        if (base != null && base.getWorld() != null) {
            int[] offset = new int[3];
            getExtendedFacing().getWorldOffset(new int[]{0, 0, 4}, offset);
            int xDir = base.getXCoord() + offset[0];
            int yDir = base.getYCoord() + offset[1];
            int zDir = base.getZCoord() + offset[2];
            Block block = base.getWorld().getBlock(xDir, yDir, zDir);
            if (shouldExist) {
                if (block != null && block.getMaterial() == Material.air) {
                    base.getWorld().setBlock(xDir, yDir, zDir, QuantumStuffBlock.INSTANCE, 0, 2);
                }
            } else {
                if (block instanceof QuantumStuffBlock) {
                    base.getWorld().setBlock(xDir, yDir, zDir, Blocks.air, 0, 2);
                }
            }
        }
    }

    private void addComputationRequirements(int depthPlus, int capabilities) {
        if (areBitsSet(SCAN_GET_NOMENCLATURE, capabilities)) {
            computationRequired += depthPlus * 5L;
            eRequiredData += depthPlus;
        }
        if (areBitsSet(SCAN_GET_DEPTH_LEVEL, capabilities)) {
            computationRequired += depthPlus * 10L;
            eRequiredData += depthPlus;

        }
        if (areBitsSet(SCAN_GET_AMOUNT, capabilities)) {
            computationRequired += depthPlus * 64L;
            eRequiredData += depthPlus * 8L;

        }
        if (areBitsSet(SCAN_GET_CHARGE, capabilities)) {
            computationRequired += depthPlus * 128L;
            eRequiredData += depthPlus * 4L;

        }
        if (areBitsSet(SCAN_GET_MASS, capabilities)) {
            computationRequired += depthPlus * 256L;
            eRequiredData += depthPlus * 4L;

        }
        if (areBitsSet(SCAN_GET_ENERGY_LEVEL, capabilities)) {
            computationRequired += depthPlus * 512L;
            eRequiredData += depthPlus * 16L;

        }
        if (areBitsSet(SCAN_GET_TIMESPAN_INFO, capabilities)) {
            computationRequired += depthPlus * 1024L;
            eRequiredData += depthPlus * 32L;

        }
        if (areBitsSet(SCAN_GET_ENERGY_STATES, capabilities)) {
            computationRequired += depthPlus * 2048L;
            eRequiredData += depthPlus * 32L;

        }
        if (areBitsSet(SCAN_GET_COLOR, capabilities)) {
            computationRequired += depthPlus * 1024L;
            eRequiredData += depthPlus * 48L;

        }
        if (areBitsSet(SCAN_GET_AGE, capabilities)) {
            computationRequired += depthPlus * 2048L;
            eRequiredData += depthPlus * 64L;

        }
        if (areBitsSet(SCAN_GET_TIMESPAN_MULT, capabilities)) {
            computationRequired += depthPlus * 2048L;
            eRequiredData += depthPlus * 64L;

        }
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_scanner(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        if (!checkPiece("main", 2, 2, 0)) {
            return false;
        }
        return eInputHatches.size() == 1 && eOutputHatches.size() == 1 && eOutputHatches.get(0).getBaseMetaTileEntity().getFrontFacing() == iGregTechTileEntity.getFrontFacing();
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        eRecipe = null;
        if (!eInputHatches.isEmpty() && eInputHatches.get(0).getContainerHandler().hasStacks() && !eOutputHatches.isEmpty()) {
            cElementalInstanceStackMap researchEM = eInputHatches.get(0).getContainerHandler();
            if (ItemList.Tool_DataOrb.isStackEqual(itemStack, false, true)) {
                GT_Recipe scannerRecipe = null;
                for (cElementalInstanceStack stackEM : researchEM.values()) {
                    objectsScanned = null;
                    eRecipe        = TT_recipe.TT_Recipe_Map_EM.sMachineRecipesEM.findRecipe(stackEM.definition);
                    if (eRecipe != null) {
                        scannerRecipe    = eRecipe.scannerRecipe;
                        machineType      = machine;
                        objectResearched = new cElementalDefinitionStack(stackEM.definition, 1);
                        //cleanMassEM_EM(objectResearched.getMass());
                        researchEM.remove(objectResearched.definition);
                        break;
                    }
                    eRecipe = TT_recipe.TT_Recipe_Map_EM.sCrafterRecipesEM.findRecipe(stackEM.definition);
                    if (eRecipe != null) {
                        scannerRecipe    = eRecipe.scannerRecipe;
                        machineType      = crafter;
                        objectResearched = new cElementalDefinitionStack(stackEM.definition, 1);
                        //cleanMassEM_EM(objectResearched.getMass());
                        researchEM.remove(objectResearched.definition);
                        break;
                    }
                    cleanStackEM_EM(stackEM);
                    researchEM.remove(stackEM.definition);
                }
                if (eRecipe != null && scannerRecipe != null) {//todo make sure it werks
                    computationRequired = computationRemaining = scannerRecipe.mDuration * 20L;
                    mMaxProgresstime    = 20;//const
                    mEfficiencyIncrease = 10000;
                    eRequiredData       = (short) (scannerRecipe.mSpecialValue >>> 16);
                    eAmpereFlow         = (short) (scannerRecipe.mSpecialValue & 0xFFFF);
                    mEUt                = scannerRecipe.mEUt;
                    quantumStuff(true);
                    return true;
                }
            } else if (CustomItemList.scanContainer.isStackEqual(itemStack, false, true)) {
                eRecipe = null;
                if (researchEM.hasStacks()) {
                    objectsScanned = researchEM.takeAllToNewMap();
                    cleanMassEM_EM(objectsScanned.getMass());

                    computationRequired = 0;
                    eRequiredData       = 0;
                    eAmpereFlow         = objectsScanned.size() + TecTech.RANDOM.next(objectsScanned.size());
                    mEUt                = -(int) V[8];

                    //get depth scan complexity array
                    {
                        int[] scanComplexityTemp = new int[20];
                        for (int i = 0; i < 20; i++) {
                            scanComplexityTemp[i] = (int) scanConfiguration[i].get();
                        }
                        int maxDepth = 0;
                        for (int i = 0; i < 20; i++) {
                            if (scanComplexityTemp[i] != SCAN_DO_NOTHING) {
                                maxDepth = i;
                                if (!DEBUG_MODE) {
                                    scanComplexityTemp[i] &= ~SCAN_GET_CLASS_TYPE;
                                }
                                addComputationRequirements(i + 1, scanComplexityTemp[i]);
                            }
                        }
                        maxDepth += 1;//from index to len
                        scanComplexity = new int[maxDepth];
                        System.arraycopy(scanComplexityTemp, 0, scanComplexity, 0, maxDepth);
                    }

                    computationRemaining = computationRequired *= 20;
                    mMaxProgresstime     = 20;//const
                    mEfficiencyIncrease  = 10000;
                    quantumStuff(true);
                    return true;
                }
            }
        }
        quantumStuff(false);
        objectResearched     = null;
        computationRemaining = 0;
        return false;
    }

    @Override
    public void outputAfterRecipe_EM() {
        if (eRecipe != null && ItemList.Tool_DataOrb.isStackEqual(mInventory[1], false, true)) {

            mInventory[1].setStackDisplayName(GT_LanguageManager.getTranslation(eRecipe.mOutputs[0].getDisplayName()) + ' ' + machineType + " Construction Data");
            NBTTagCompound tNBT = mInventory[1].getTagCompound();//code above makes it not null

            tNBT.setString("eMachineType", machineType);
            tNBT.setTag(E_RECIPE_ID, objectResearched.toNBT());
            tNBT.setString("author", EnumChatFormatting.BLUE + "Tec" + EnumChatFormatting.DARK_BLUE + "Tech" + EnumChatFormatting.WHITE + ' ' + machineType + " EM Recipe Generator");
        } else if (objectsScanned != null && CustomItemList.scanContainer.isStackEqual(mInventory[1], false, true)) {
            ElementalDefinitionScanStorage_EM.setContent(mInventory[1], objectsScanned, scanComplexity);
        }
        objectResearched     = null;
        computationRemaining = 0;
        quantumStuff(false);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder(mName);
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.scanner.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.scanner.desc.1"));
        tooltip.beginStructureBlock(5, 5, 8, false);
        tooltip.addCasingInfoRange(TecTechCasings.HIGH_POWER, 8, 24, false);
        tooltip.addCasingInfoRange(TecTechCasings.MOLECULAR, 69, 85, false);
        tooltip.addOtherStructurePart(61, QuantumGlassBlock.INSTANCE.getLocalizedName(), translateToLocal("tt.structure.hologram"));
        tooltip.addOtherStructurePart(1, TecTechHatchElement.ElementalInput.getDescription(), translateToLocal("tt.structure.required_position"));
        tooltip.addOtherStructurePart(1, TecTechHatchElement.ElementalOutput.getDescription(), translateToLocal("tt.structure.required_position"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.casing_ranges"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.scanner.output"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_hatches"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_data"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.scanner.hint.0"), HintColor.GREEN);
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.scanner.hint.1"), HintColor.BLUE);
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.scanner.hint.2"), HintColor.ORANGE);
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.scanner.hint.3"), HintColor.LIME);
        return tooltip.addSubChannel(GTStructureChannels.HATCH, "Places the elemental input and front-facing elemental output at blue and orange positions, one overflow hatch at a lime position, and energy and computation data inputs at green positions. Also places one Maintenance Hatch.");
    }

    @Override
    public String[] getInfoData() {
        long storedEnergy = 0;
        long maxEnergy = 0;
        for (GT_MetaTileEntity_Hatch_Energy tHatch : mEnergyHatches) {
            if (GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(tHatch)) {
                storedEnergy += tHatch.getBaseMetaTileEntity().getStoredEU();
                maxEnergy += tHatch.getBaseMetaTileEntity().getEUCapacity();
            }
        }
        for (GT_MetaTileEntity_Hatch_EnergyMulti tHatch : mEnergyHatchesMulti) {
            if (GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(tHatch)) {
                storedEnergy += tHatch.getBaseMetaTileEntity().getStoredEU();
                maxEnergy += tHatch.getBaseMetaTileEntity().getEUCapacity();
            }
        }

        return new String[]{
                translateToLocalFormatted("tt.keyphrase.Energy_Hatches", clientLocale) + ":",
                EnumChatFormatting.GREEN + Long.toString(storedEnergy) + EnumChatFormatting.RESET + " EU / " +
                        EnumChatFormatting.YELLOW + maxEnergy + EnumChatFormatting.RESET + " EU",
                (mEUt <= 0 ? translateToLocalFormatted("tt.keyphrase.Probably_uses", clientLocale) + ": " : translateToLocalFormatted("tt.keyphrase.Probably_makes", clientLocale) + ": ") +
                        EnumChatFormatting.RED + Math.abs(mEUt) + EnumChatFormatting.RESET + " EU/t " + translateToLocalFormatted("tt.keyword.at", clientLocale) + " " +
                        EnumChatFormatting.RED + eAmpereFlow + EnumChatFormatting.RESET + " A",
                translateToLocalFormatted("tt.keyphrase.Tier_Rating", clientLocale) + ": " + EnumChatFormatting.YELLOW + VN[getMaxEnergyInputTier_EM()] + EnumChatFormatting.RESET + " / " + EnumChatFormatting.GREEN + VN[getMinEnergyInputTier_EM()] + EnumChatFormatting.RESET +
                        " " + translateToLocalFormatted("tt.keyphrase.Amp_Rating", clientLocale) + ": " + EnumChatFormatting.GREEN + eMaxAmpereFlow + EnumChatFormatting.RESET + " A",
                translateToLocalFormatted("tt.keyword.Problems", clientLocale) + ": " + EnumChatFormatting.RED + (getIdealStatus() - getRepairStatus()) + EnumChatFormatting.RESET +
                        " " + translateToLocalFormatted("tt.keyword.Efficiency", clientLocale) + ": " + EnumChatFormatting.YELLOW + mEfficiency / 100.0F + EnumChatFormatting.RESET + " %",
                translateToLocalFormatted("tt.keyword.PowerPass", clientLocale) + ": " + EnumChatFormatting.BLUE + ePowerPass + EnumChatFormatting.RESET +
                        " " + translateToLocalFormatted("tt.keyword.SafeVoid", clientLocale) + ": " + EnumChatFormatting.BLUE + eSafeVoid,
                translateToLocalFormatted("tt.keyphrase.Computation_Available", clientLocale) + ": " + EnumChatFormatting.GREEN + eAvailableData + EnumChatFormatting.RESET + " / " + EnumChatFormatting.YELLOW + eRequiredData + EnumChatFormatting.RESET,
                translateToLocalFormatted("tt.keyphrase.Computation_Remaining", clientLocale) + ":",
                EnumChatFormatting.GREEN + Long.toString(computationRemaining / 20L) + EnumChatFormatting.RESET + " / " +
                        EnumChatFormatting.YELLOW + computationRequired / 20L
        };
    }

    @Override
    public void onRemoval() {
        quantumStuff(false);
        super.onRemoval();
    }

    @Override
    protected void parametersInstantiation_EM() {
        scanConfiguration = new Parameters.Group.ParameterIn[20];
        for (int i = 0; i < 10; i++) {
            Parameters.Group hatch = parametrization.getGroup(i);
            scanConfiguration[i * 2]     = hatch.makeInParameter(0, 0, CONFIG_NAME, CONFIG_STATUS);
            scanConfiguration[i * 2 + 1] = hatch.makeInParameter(1, 0, CONFIG_NAME, CONFIG_STATUS);
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setLong("eComputationRemaining", computationRemaining);
        aNBT.setLong("eComputationRequired", computationRequired);
        if (objectResearched != null) {
            aNBT.setTag("eObject", objectResearched.toNBT());
        } else {
            aNBT.removeTag("eObject");
        }
        if (scanComplexity != null) {
            aNBT.setIntArray("eScanComplexity", scanComplexity);
        } else {
            aNBT.removeTag("eScanComplexity");
        }
        if (objectsScanned != null) {
            aNBT.setTag("eScanObjects", objectsScanned.toNBT());
        } else {
            aNBT.removeTag("eScanObjects");
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        computationRemaining = aNBT.getLong("eComputationRemaining");
        computationRequired  = aNBT.getLong("eComputationRequired");
        if (aNBT.hasKey("eObject")) {
            objectResearched = cElementalDefinitionStack.fromNBT(aNBT.getCompoundTag("eObject"));
            if (objectResearched.definition == nbtE__) {
                objectResearched = null;
            }
        } else {
            objectResearched = null;
        }
        if (aNBT.hasKey("eScanComplexity")) {
            scanComplexity = aNBT.getIntArray("eScanComplexity");
        } else {
            scanComplexity = null;
        }
        try {
            if (aNBT.hasKey("eScanObjects")) {
                objectsScanned = cElementalInstanceStackMap.fromNBT(aNBT.getCompoundTag("eScanObjects"));
            }
        } catch (tElementalException e) {
            objectsScanned = new cElementalInstanceStackMap();
        }
    }

    @Override
    public void stopMachine() {
        quantumStuff(false);
        super.stopMachine();
        computationRequired = computationRemaining = 0;
        objectResearched    = null;

    }

    @Override
    public void onFirstTick_EM(IGregTechTileEntity aBaseMetaTileEntity) {
        if (aBaseMetaTileEntity.isServerSide()) {
            if (computationRemaining > 0 && objectResearched != null) {
                eRecipe = null;
                if (ItemList.Tool_DataOrb.isStackEqual(mInventory[1], false, true)) {
                    eRecipe = TT_recipe.TT_Recipe_Map_EM.sMachineRecipesEM.findRecipe(objectResearched.definition);
                    if (eRecipe != null) {
                        machineType = machine;
                    } else {
                        eRecipe = TT_recipe.TT_Recipe_Map_EM.sCrafterRecipesEM.findRecipe(objectResearched.definition);
                        if (eRecipe != null) {
                            machineType = crafter;
                        }
                    }
                }
                if (eRecipe == null) {
                    quantumStuff(false);
                    objectResearched    = null;
                    eRequiredData       = 0;
                    computationRequired = computationRemaining = 0;
                    mMaxProgresstime    = 0;
                    mEfficiencyIncrease = 0;
                } else {
                    quantumStuff(true);
                }
            }
        }
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        if (computationRemaining <= 0) {
            computationRemaining = 0;
            mProgresstime        = mMaxProgresstime;
            return true;
        } else {
            computationRemaining -= eAvailableData;
            mProgresstime = 1;
            return super.onRunningTick(aStack);
        }
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        super.onRightclick(aBaseMetaTileEntity, aPlayer);

        if (!aBaseMetaTileEntity.isClientSide() && aPlayer instanceof EntityPlayerMP) {
            try {
                EntityPlayerMP player = (EntityPlayerMP) aPlayer;
                clientLocale = (String) FieldUtils.readField(player, "translator", true);
            } catch (Exception e) {
                clientLocale = "en_US";
            }
        } else {
            return true;
        }
        return true;
    }

    @Override
    public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aBaseMetaTileEntity.isActive() && (aTick & 0x2) == 0 && aBaseMetaTileEntity.isClientSide()) {
            int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX * 4 + aBaseMetaTileEntity.getXCoord();
            int yDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetY * 4 + aBaseMetaTileEntity.getYCoord();
            int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ * 4 + aBaseMetaTileEntity.getZCoord();
            aBaseMetaTileEntity.getWorld().markBlockRangeForRenderUpdate(xDir, yDir, zDir, xDir, yDir, zDir);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece("main", stackSize, hintsOnly, 2, 2, 0);
    }
    @Override
    public int survivalConstruct(ItemStack stackSize, int budget, ISurvivalBuildEnvironment environment) {
        return survivalBuildPiece("main", stackSize, 2, 2, 0, budget, environment);
    }

    public IStructureDefinition<GT_MetaTileEntity_EM_scanner> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
