package com.github.technus.tectech.compatibility.thaumcraft.thing.metaTileEntity.multi;

import com.github.technus.tectech.compatibility.thaumcraft.elementalMatter.definitions.ePrimalAspectDefinition;
import com.github.technus.tectech.mechanics.elementalMatter.core.cElementalInstanceStackMap;
import com.github.technus.tectech.mechanics.elementalMatter.core.stacks.cElementalInstanceStack;
import com.github.technus.tectech.thing.block.QuantumGlassBlock;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.multi.GT_MetaTileEntity_EM_quantizer;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.util.CommonValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;

import static com.github.technus.tectech.compatibility.thaumcraft.thing.metaTileEntity.multi.EssentiaCompat.essentiaContainerCompat;
import static com.github.technus.tectech.util.CommonValues.V;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.StatCollector.translateToLocal;
import static space.impact.structures.structure.StructureUtility.ofBlock;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_essentiaDequantizer extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_essentiaDequantizer> {
    //region structure
    //use multi A energy inputs, use less power the longer it runs
    private static final IStructureDefinition<GT_MetaTileEntity_EM_essentiaDequantizer> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_essentiaDequantizer>builder()
                    .addShape("main", new String[][] {
                            {"CCC", "C~C", "CCC"},
                            {"0 0", " F ", "0 0"},
                            {"121", "232", "121"},
                            {"EEE", "E2E", "EEE"},
                            {"202", "0D0", "202"},
                    })
                    .addElement('0', ofBlock(QuantumGlassBlock.INSTANCE, 0))
                    .addElement('1', TecTechCasings.HIGH_POWER.asElement())
                    .addElement('2', TecTechCasings.MOLECULAR.asElement())
                    .addElement('3', TecTechCasings.HOLLOW.asElement())
                    .addElement('C', withHint(buildHatchAdder(GT_MetaTileEntity_EM_essentiaDequantizer.class).allow(TecTechHatchElement.Classic).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.GREEN))
                    .addElement('D', withHint(buildHatchAdder(GT_MetaTileEntity_EM_essentiaDequantizer.class).allow(TecTechHatchElement.ElementalInput).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.BLUE))
                    .addElement('E', withHint(buildHatchAdder(GT_MetaTileEntity_EM_essentiaDequantizer.class).allow(TecTechHatchElement.ElementalOverflow).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.LIME))
                    .addElement('F', new EssentiaContainerElement<>())
                    .build();
    //endregion

    public GT_MetaTileEntity_EM_essentiaDequantizer(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_essentiaDequantizer(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_essentiaDequantizer(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return checkPiece("main", 1, 1, 0);
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        TileEntity container = essentiaContainerCompat.getContainer(this);
        if (eInputHatches.size() < 1 || container == null) {
            stopMachine();
            return false;
        }
        cElementalInstanceStackMap inputHatchContainer = eInputHatches.get(0).getContainerHandler();
        if (inputHatchContainer.hasStacks()) {
            cElementalInstanceStack stack = inputHatchContainer.getFirst();
            inputHatchContainer.removeAmount(false, new cElementalInstanceStack(stack.definition, 1));
            if (!essentiaContainerCompat.putElementalInstanceStack(container, stack)) {
                cleanStackEM_EM(stack);
            }
            mMaxProgresstime = 20;
            mEfficiencyIncrease = 10000;
            eAmpereFlow = 1;
            if (stack.definition instanceof ePrimalAspectDefinition) {
                mEUt = (int) -V[8];
            } else {
                mEUt = (int) -V[10];
            }
            return true;
        }
        return false;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder();
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.emtoessentia.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.emtoessentia.desc.1"));
        tooltip.beginStructureBlock(3, 3, 5, false);
        tooltip.addCasingInfoRange(TecTechCasings.HIGH_POWER, 4, 12, false);
        tooltip.addCasingInfoRange(TecTechCasings.MOLECULAR, 9, 18, false);
        tooltip.addCasingInfoExactly(TecTechCasings.HOLLOW, 1, false);
        tooltip.addOtherStructurePart(8, QuantumGlassBlock.INSTANCE.getLocalizedName(), translateToLocal("tt.structure.hologram"));
        tooltip.addOtherStructurePart(1, translateToLocal("tt.structure.essentia_storage"), translateToLocal("tt.structure.required_position"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.casing_ranges"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_hatches"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_data"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.emtoessentia.hint.0"), HintColor.GREEN);
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.emtoessentia.hint.1"), HintColor.BLUE);
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.emtoessentia.hint.2"), HintColor.LIME);
        tooltip.addStructureInfo(translateToLocal("gt.blockmachines.multimachine.em.emtoessentia.hint.3"));
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.emtoessentia.hint.3");
        return tooltip;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected ResourceLocation getActivitySound() {
        return GT_MetaTileEntity_EM_quantizer.activitySound;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece("main", stackSize, hintsOnly, 1, 1, 0);
    }
    @Override
    public int survivalConstruct(ItemStack stackSize, int budget, ISurvivalBuildEnvironment environment) {
        return survivalBuildPiece("main", stackSize, 1, 1, 0, budget, environment);
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_EM_essentiaDequantizer> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
