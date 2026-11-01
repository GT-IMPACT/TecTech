package com.github.technus.tectech.thing.metaTileEntity.multi;

import gregtech.api.util.GTStructureChannels;
import com.github.technus.tectech.Reference;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_Container_MultiMachineEM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_GUIContainer_MultiMachineEM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.INameFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.IStatusFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.Parameters;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechAutoHatch;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.render.TT_RenderedExtendedFacingTexture;
import com.github.technus.tectech.util.CommonValues;
import gregtech.api.enums.Casings;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import java.util.ArrayList;
import java.util.HashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;
import space.impact.structures.util.Vec3Impl;

import static com.github.technus.tectech.loader.MainLoader.microwaving;
import static com.github.technus.tectech.recipe.TT_recipeAdder.nullItem;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_OK;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_TOO_HIGH;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_TOO_LOW;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_WRONG;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.fromLimitsInclusiveOuterBoundary;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.AxisAlignedBB.getBoundingBox;
import static net.minecraft.util.StatCollector.translateToLocal;
import static space.impact.structures.structure.StructureUtility.transpose;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_TM_microwave extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_TM_microwave> {
    //region structure
    //use multi A energy inputs, use less power the longer it runs
    //endregion
    private static final IStructureDefinition<GT_MetaTileEntity_TM_microwave> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_TM_microwave>builder()
                    .addChannel(GTStructureChannels.HATCH, 1, 1)
                    .addShape("main", transpose(new String[][]{
                            {"AAAAA", "A---A", "A---A", "A---A", "AAAAA"},
                            {"AAAAA", "A---A", "A---A", "A---A", "AAAAA"},
                            {"AA~AA", "A---A", "A---A", "A---A", "AAAAA"},
                            {"ABBBA", "BAAAB", "BAAAB", "BAAAB", "ABBBA"}
                    }))
                    .addElement('A', Casings.CLEAN_STAINLESS_STEEL.asElement())
                    .addElement('B', withHint(buildHatchAdder(GT_MetaTileEntity_TM_microwave.class).allow(TecTechHatchElement.Classic).autoPlace(TecTechAutoHatch.Maintenance).autoPlace(TecTechAutoHatch.PowerInput, TecTechAutoHatch.ItemOutput).buildAndChain(StructureRequirements.builder(Casings.CLEAN_STAINLESS_STEEL, 0).build()), HintColor.GREEN))
                    .build();
    private static final INameFunction<GT_MetaTileEntity_TM_microwave> POWER_SETTING_NAME = (base, p) -> translateToLocal("gt.blockmachines.multimachine.tm.microwave.cfgi.0");//Power setting
    //endregion
    private static final INameFunction<GT_MetaTileEntity_TM_microwave> TIMER_SETTING_NAME = (base, p) -> translateToLocal("gt.blockmachines.multimachine.tm.microwave.cfgi.1");//Timer setting
    private static final INameFunction<GT_MetaTileEntity_TM_microwave> TIMER_VALUE_NAME = (base, p) -> translateToLocal("gt.blockmachines.multimachine.tm.microwave.cfgo.0");//Timer value
    private static final INameFunction<GT_MetaTileEntity_TM_microwave> TIMER_REMAINING_NAME = (base, p) -> translateToLocal("gt.blockmachines.multimachine.tm.microwave.cfgo.1");//Timer remaining
    private static final IStatusFunction<GT_MetaTileEntity_TM_microwave> POWER_STATUS =
            (base, p) -> LedStatus.fromLimitsInclusiveOuterBoundary(p.get(), 300, 1000, 1000, Double.POSITIVE_INFINITY);
    private static final IStatusFunction<GT_MetaTileEntity_TM_microwave> TIMER_STATUS = (base, p) -> {
        double value = p.get();
        if (Double.isNaN(value)) return STATUS_WRONG;
        value = (int) value;
        if (value <= 0) return STATUS_TOO_LOW;
        if (value > 3000) return STATUS_TOO_HIGH;
        return STATUS_OK;
    };
    //region parameters
    protected Parameters.Group.ParameterIn powerSetting, timerSetting;
    protected Parameters.Group.ParameterOut timerValue, remainingTime;
    //region variables
    private boolean hasBeenPausedThisCycle = false;
    //endregion

    public GT_MetaTileEntity_TM_microwave(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_TM_microwave(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_TM_microwave(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return checkPiece("main", 2, 2, 0);
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        hasBeenPausedThisCycle = false;
        if ((int) powerSetting.get() < 300 || timerSetting.get() <= 0 || timerSetting.get() > 3000) {
            return false;
        }
        if (remainingTime.get() <= 0) {
            remainingTime.set(timerSetting.get());
            timerValue.set(0);
        }
        mEUt                = -((int) powerSetting.get() >> 1);
        eAmpereFlow         = 1;
        mMaxProgresstime    = 20;
        mEfficiencyIncrease = 10000;
        return true;
    }

    @Override
    public void outputAfterRecipe_EM() {
        if (hasBeenPausedThisCycle) {
            return;//skip timer and actions if paused
        }
        timerValue.set(timerValue.get() + 1);
        remainingTime.set(timerSetting.get() - timerValue.get());
        IGregTechTileEntity mte = getBaseMetaTileEntity();
        Vec3Impl xyzOffsets = getExtendedFacing().getWorldOffset(new Vec3Impl(0, -1, 2));
        double xPos = mte.getXCoord() + 0.5f + xyzOffsets.get0();
        double yPos = mte.getYCoord() + 0.5f + xyzOffsets.get1();
        double zPos = mte.getZCoord() + 0.5f + xyzOffsets.get2();
        AxisAlignedBB aabb = getBoundingBox(-2, -2, -2, 2, 2, 2)
                .offset(xPos, yPos, zPos);
        xyzOffsets = getExtendedFacing().getWorldOffset(new Vec3Impl(0, -4, 0));
        Vec3Impl xyzExpansion = getExtendedFacing().getWorldOffset(new Vec3Impl(1, 0, 1)).abs();
        int power = (int) powerSetting.get();
        int damagingFactor =
                Math.min(power >> 6, 8) +
                        Math.min(power >> 8, 24) +
                        Math.min(power >> 12, 48) +
                        (power >> 18);

        ArrayList<ItemStack> itemsToOutput = new ArrayList<>();
        HashSet<Entity> tickedStuff = new HashSet<>();

        boolean inside = true;
        do {
            for (Object entity : mte.getWorld().getEntitiesWithinAABBExcludingEntity(null, aabb)) {
                if (entity instanceof Entity) {
                    if (tickedStuff.add((Entity) entity)) {
                        if (inside && entity instanceof EntityItem) {
                            GT_Recipe tRecipe = GT_Recipe.GT_Recipe_Map.sMicrowaveRecipes.findRecipe(
                                    mte, null, true, 128, null, null, new ItemStack[]{((EntityItem) entity).getEntityItem()});
                            if (tRecipe == null || tRecipe.mInputs[0].stackSize != 1) {
                                itemsToOutput.add(((EntityItem) entity).getEntityItem());
                            } else {
                                ItemStack newStuff = tRecipe.getOutput(0).copy();
                                newStuff.stackSize = ((EntityItem) entity).getEntityItem().stackSize;
                                itemsToOutput.add(newStuff);
                            }
                            ((EntityItem) entity).delayBeforeCanPickup = 2;
                            ((EntityItem) entity).setDead();
                        } else if (entity instanceof EntityLivingBase) {
                            if (!GT_Utility.isWearingFullElectroHazmat((EntityLivingBase) entity)) {
                                ((EntityLivingBase) entity).attackEntityFrom(microwaving, damagingFactor);
                            }
                        }
                    }
                }
            }
            aabb.offset(xyzOffsets.get0(), xyzOffsets.get1(), xyzOffsets.get2());
            aabb   = aabb.expand(xyzExpansion.get0() * 1.5, xyzExpansion.get1() * 1.5, xyzExpansion.get2() * 1.5);
            inside = false;
            damagingFactor >>= 1;
        } while (damagingFactor > 0);

        mOutputItems = itemsToOutput.toArray(nullItem);

        if (remainingTime.get() <= 0) {
            mte.getWorld().playSoundEffect(xPos, yPos, zPos, Reference.MODID + ":microwave_ding", 1, 1);
            stopMachine();
        }
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder(mName);
        tooltip.addInfo(CommonValues.BASS_MARK);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.tm.microwave.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.tm.microwave.desc.1"));
        tooltip.addInfo(EnumChatFormatting.BLUE + translateToLocal("gt.blockmachines.multimachine.tm.microwave.desc.2"));
        tooltip.beginStructureBlock(5, 4, 5, false);
        tooltip.addCasingInfoRange(Casings.CLEAN_STAINLESS_STEEL, 60, 72, false);
        tooltip.addStructureInfo(translateToLocal("tt.structure.casing_ranges"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_hatches"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_data"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureInfo(translateToLocal("gt.blockmachines.multimachine.tm.microwave.hint.1"));
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.tm.microwave.hint.0"), HintColor.GREEN);
        tooltip.addStructureHint("gt.blockmachines.multimachine.tm.microwave.hint.1");
        return tooltip.addSubChannel(GTStructureChannels.HATCH, "Places one energy input and one item output bus at green positions. Put items to cook inside the chamber. Also places one Maintenance Hatch.");
    }

    @Override
    public Object getServerGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_Container_MultiMachineEM(aPlayerInventory, aBaseMetaTileEntity, true, false, true);
    }

    @Override
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_GUIContainer_MultiMachineEM(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "EMDisplay.png", true, false, true);//todo texture
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            return new ITexture[]{Casings.CLEAN_STAINLESS_STEEL.getTexture(), new TT_RenderedExtendedFacingTexture(aActive ? Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE : Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE)};
        } else if (aSide == GT_Utility.getOppositeSide(aFacing)) {
            return new ITexture[]{Casings.CLEAN_STAINLESS_STEEL.getTexture(), aActive ? Casings.FUSION_GLASS_YELLOW.getTexture() : Casings.FUSION_GLASS.getTexture()};
        }
        return new ITexture[]{Casings.CLEAN_STAINLESS_STEEL.getTexture()};
    }

    @Override
    protected void parametersInstantiation_EM() {
        Parameters.Group hatch_0 = parametrization.getGroup(0, true);
        powerSetting = hatch_0.makeInParameter(0, 1000, POWER_SETTING_NAME, POWER_STATUS);
        timerSetting = hatch_0.makeInParameter(1, 360, TIMER_SETTING_NAME, TIMER_STATUS);

        timerValue    = hatch_0.makeOutParameter(0, 0, TIMER_VALUE_NAME, TIMER_STATUS);
        remainingTime = hatch_0.makeOutParameter(1, 360, TIMER_REMAINING_NAME, TIMER_STATUS);
    }

    @Override
    public void stopMachine() {
        super.stopMachine();
        remainingTime.set(timerSetting.get());
        timerValue.set(0);
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        if (eSafeVoid) {
            hasBeenPausedThisCycle = true;
        }
        return hasBeenPausedThisCycle || super.onRunningTick(aStack);//consume eu and other resources if not paused
    }

    //TODO Why is the basetype 1??
    @Override
    public byte getTileEntityBaseType() {
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

    @Override
    public IStructureDefinition<GT_MetaTileEntity_TM_microwave> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
