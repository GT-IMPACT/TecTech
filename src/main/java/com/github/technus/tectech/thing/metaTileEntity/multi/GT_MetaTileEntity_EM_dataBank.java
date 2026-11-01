package com.github.technus.tectech.thing.metaTileEntity.multi;

import gregtech.api.util.GTStructureChannels;
import com.github.technus.tectech.Reference;
import com.github.technus.tectech.mechanics.dataTransport.InventoryDataPacket;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputDataItems;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OutputDataItems;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_Container_MultiMachineEM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_GUIContainer_MultiMachineEM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.render.TT_RenderedExtendedFacingTexture;
import com.github.technus.tectech.util.CommonValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_DataAccess;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import java.util.ArrayList;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;

import static com.github.technus.tectech.recipe.TT_recipeAdder.nullItem;
import static com.github.technus.tectech.util.CommonValues.V;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.StatCollector.translateToLocal;
import static space.impact.structures.structure.StructureUtility.transpose;
import static space.impact.structures.structure.StructureUtility.withHint;

public class GT_MetaTileEntity_EM_dataBank extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_dataBank> {
    public final static ResourceLocation activitySound = new ResourceLocation(Reference.MODID + ":fx_hi_freq");
    //region structure
    //endregion
    private static final IStructureDefinition<GT_MetaTileEntity_EM_dataBank> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_dataBank>builder()
                    .addChannel(GTStructureChannels.HATCH, 1, 1)
                    .addShape("main", transpose(new String[][]{
                            {"BCCCB", "BDDDB", "BDDDB"},
                            {"BC~CB", "BAAAB", "BDDDB"},
                            {"BCCCB", "BDDDB", "BDDDB"}
                    }))
                    .addElement('A', TecTechCasings.COMPUTER.asElement())
                    .addElement('B', TecTechCasings.COMPUTER_HEAT_VENT.asElement())
                    .addElement('C', withHint(buildHatchAdder(GT_MetaTileEntity_EM_dataBank.class).allow(TecTechHatchElement.Classic).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.GREEN))
                    .addElement('D', withHint(buildHatchAdder(GT_MetaTileEntity_EM_dataBank.class).allow(TecTechHatchElement.DataBank).buildAndChain(StructureRequirements.builder(TecTechCasings.COMPUTER, 0).build()), HintColor.PURPLE))
                    .build();
    //region variables
    private final ArrayList<GT_MetaTileEntity_Hatch_OutputDataItems> eStacksDataOutputs = new ArrayList<>();
    //endregion
    private final ArrayList<GT_MetaTileEntity_Hatch_DataAccess> eDataAccessHatches = new ArrayList<>();

    public GT_MetaTileEntity_EM_dataBank(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_dataBank(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_dataBank(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        eDataAccessHatches.clear();
        eStacksDataOutputs.clear();
        return checkPiece("main", 2, 1, 0);
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        if (eDataAccessHatches.size() > 0 && eStacksDataOutputs.size() > 0) {
            mEUt                = -(int) V[4];
            eAmpereFlow         = 1 + eStacksDataOutputs.size() * eDataAccessHatches.size();
            mMaxProgresstime    = 20;
            mEfficiencyIncrease = 10000;
            return true;
        }
        return false;
    }

    @Override
    public void outputAfterRecipe_EM() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (GT_MetaTileEntity_Hatch_DataAccess dataAccess : eDataAccessHatches) {
            int count = dataAccess.getSizeInventory();
            for (int i = 0; i < count; i++) {
                ItemStack stack = dataAccess.getStackInSlot(i);
                if (stack != null) {
                    stacks.add(stack);
                }
            }
        }
        if (stacks.size() > 0) {
            ItemStack[] arr = stacks.toArray(nullItem);
            for (GT_MetaTileEntity_Hatch_OutputDataItems hatch : eStacksDataOutputs) {
                hatch.q = new InventoryDataPacket(arr);
            }
        } else {
            for (GT_MetaTileEntity_Hatch_OutputDataItems hatch : eStacksDataOutputs) {
                hatch.q = null;
            }
        }
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder();
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.databank.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.databank.desc.1"));
        tooltip.beginStructureBlock(5, 3, 3, false);
        tooltip.addCasingInfoRange(TecTechCasings.COMPUTER, 3, 18, false);
        tooltip.addCasingInfoExactly(TecTechCasings.COMPUTER_HEAT_VENT, 18, false);
        tooltip.addCasingInfoRange(TecTechCasings.HIGH_POWER, 0, 8, false);
        tooltip.addStructureInfo(translateToLocal("tt.structure.casing_ranges"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_hatches"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_data"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.databank.hint.0"), HintColor.GREEN);
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.databank.hint.1"), HintColor.PURPLE);
        return tooltip.addStructureChannels(getStructureDefinition());
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
            return new ITexture[]{TecTechCasings.COMPUTER.getTexture(), new TT_RenderedExtendedFacingTexture(aActive ? GT_MetaTileEntity_MultiblockBase_EM.ScreenON : GT_MetaTileEntity_MultiblockBase_EM.ScreenOFF)};
        }
        return new ITexture[]{TecTechCasings.COMPUTER.getTexture()};
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected ResourceLocation getActivitySound() {
        return activitySound;
    }

    public final boolean addDataBankHatchToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) {
            return false;
        }
        IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
        if (aMetaTileEntity == null) {
            return false;
        }
        if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_OutputDataItems) {
            ((GT_MetaTileEntity_Hatch) aMetaTileEntity).updateTexture(aBaseCasingIndex);
            return eStacksDataOutputs.add((GT_MetaTileEntity_Hatch_OutputDataItems) aMetaTileEntity);
        } else if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_DataAccess && !(aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_InputDataItems)) {
            ((GT_MetaTileEntity_Hatch) aMetaTileEntity).updateTexture(aBaseCasingIndex);
            return eDataAccessHatches.add((GT_MetaTileEntity_Hatch_DataAccess) aMetaTileEntity);
        }
        return false;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece("main", stackSize, hintsOnly, 2, 1, 0);
    }
    @Override
    public int survivalConstruct(ItemStack stackSize, int budget, ISurvivalBuildEnvironment environment) {
        return survivalBuildPiece("main", stackSize, 2, 1, 0, budget, environment);
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_EM_dataBank> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    public int getDataBankCount() {
        return eStacksDataOutputs.size();
    }

}
