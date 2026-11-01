package com.github.technus.tectech.thing.metaTileEntity.multi;

import gregtech.api.util.GTStructureChannels;
import com.github.technus.tectech.Reference;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_Container_MultiMachineEM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_GUIContainer_MultiMachineEM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechAutoHatch;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.render.TT_RenderedExtendedFacingTexture;
import com.github.technus.tectech.util.CommonValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.Casings;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;

import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.StatCollector.translateToLocal;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_transformer extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_transformer> {
    public final static ResourceLocation activitySound = new ResourceLocation(Reference.MODID + ":fx_noise");
    //region structure
    private static final StructureRequirements STRUCTURE_REQUIREMENTS = StructureRequirements.builder(TecTechCasings.HIGH_POWER, 5).build();
    private static final IStructureDefinition<GT_MetaTileEntity_EM_transformer> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_transformer>builder()
                    .addChannel(GTStructureChannels.HATCH, 1, 1)
                    .addShape("main", new String[][]{
                            {"111", "1~1", "111",},
                            {"111", "101", "111",},
                            {"111", "111", "111",},
                    })
                    .addElement('0', Casings.SUPERCONDUCTING_COIL.asElement())
                    .addElement('1', withHint(buildHatchAdder(GT_MetaTileEntity_EM_transformer.class).allow(TecTechHatchElement.EnergyIO).autoPlace(TecTechAutoHatch.PowerInput, TecTechAutoHatch.PowerOutput).buildAndChain(STRUCTURE_REQUIREMENTS, t -> t.casingCount++), HintColor.YELLOW))
                    .build();
    private int casingCount = 0;
    //endregion

    public GT_MetaTileEntity_EM_transformer(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        mWrench        = true;
        mScrewdriver   = true;
        mSoftHammer    = true;
        mHardHammer    = true;
        mSolderingTool = true;
        mCrowbar       = true;
        eDismantleBoom = true;
    }

    public GT_MetaTileEntity_EM_transformer(String aName) {
        super(aName);
        mWrench        = true;
        mScrewdriver   = true;
        mSoftHammer    = true;
        mHardHammer    = true;
        mSolderingTool = true;
        mCrowbar       = true;
        eDismantleBoom = true;
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_EM_transformer> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_transformer(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        casingCount = 0;
        return checkPiece("main", 1, 1, 0) && STRUCTURE_REQUIREMENTS.isSatisfied(this, casingCount);
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        if (ePowerPass) {
            mEfficiencyIncrease = 10000;
            mMaxProgresstime    = 20;
        } else {
            mEfficiencyIncrease = 0;
            mMaxProgresstime    = 0;
        }
        eAmpereFlow = 0;
        mEUt        = 0;
        return ePowerPass;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder(mName);
        tooltip.addInfo(CommonValues.TEC_MARK_GENERAL);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.transformer.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.transformer.desc.1"));
        tooltip.addInfo(EnumChatFormatting.BLUE + translateToLocal("gt.blockmachines.multimachine.em.transformer.desc.2"));
        tooltip.beginStructureBlock(3, 3, 3, false);
        tooltip.addCasingInfoMin(TecTechCasings.HIGH_POWER, STRUCTURE_REQUIREMENTS.getMinimumCasings(), false);
        tooltip.addCasingInfoExactly(Casings.SUPERCONDUCTING_COIL, 1, false);
        tooltip.addStructureInfo(translateToLocal("tt.structure.transformer.io"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.transformer.hint"), HintColor.YELLOW);
        return tooltip.addSubChannel(GTStructureChannels.HATCH, "Places one energy input and one energy output hatch at yellow positions while retaining the required casings.");
    }

    @Override
    public Object getServerGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_Container_MultiMachineEM(aPlayerInventory, aBaseMetaTileEntity, true, false, false);
    }

    @Override
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_GUIContainer_MultiMachineEM(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "EMDisplay.png", true, false, false);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            return new ITexture[]{TecTechCasings.HIGH_POWER.getTexture(), new TT_RenderedExtendedFacingTexture(aActive ? GT_MetaTileEntity_MultiblockBase_EM.ScreenON : GT_MetaTileEntity_MultiblockBase_EM.ScreenOFF)};
        }
        return new ITexture[]{TecTechCasings.HIGH_POWER.getTexture()};
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected ResourceLocation getActivitySound() {
        return activitySound;
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        return true;
    }

    @Override
    public boolean doRandomMaintenanceDamage() {
        return true;
    }

    @Override
    public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if ((aTick & 31) == 31) {
            ePowerPass = aBaseMetaTileEntity.isAllowedToWork();
        }
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece("main", stackSize, hintsOnly, 1, 1, 0);
    }
    @Override
    public int survivalConstruct(ItemStack stackSize, int budget, ISurvivalBuildEnvironment environment) {
        return survivalBuildPiece("main", stackSize, 1, 1, 0, budget, environment);
    }

}
