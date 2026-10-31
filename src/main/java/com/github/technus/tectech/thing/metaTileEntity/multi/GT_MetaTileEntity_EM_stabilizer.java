package com.github.technus.tectech.thing.metaTileEntity.multi;

import com.github.technus.tectech.thing.block.QuantumGlassBlock;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.util.CommonValues;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;

import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.StatCollector.translateToLocal;
import static space.impact.structures.structure.StructureUtility.ofBlock;
import static space.impact.structures.structure.StructureUtility.transpose;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_stabilizer extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_stabilizer> {
    //region structure

    private static final IStructureDefinition<GT_MetaTileEntity_EM_stabilizer> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_stabilizer>builder()
                    .addShape("main", transpose(new String[][]{
                            {" AFA ", "BCBCB", "FBGBF", "BCBCB", " AFA "},
                            {"AEEEA", "CBBBC", "BBDBB", "CBBBC", "AEEEA"},
                            {"FE~EF", "BBBBB", "GDDDG", "BBBBB", "FEEEF"},
                            {"AEEEA", "CBBBC", "BBDBB", "CBBBC", "AEEEA"},
                            {" AFA ", "BCBCB", "FBGBF", "BCBCB", " AFA "}
                    }))
                    .addElement('A', TecTechCasings.MOLECULAR.asElement())
                    .addElement('B', TecTechCasings.ADVANCED_MOLECULAR.asElement())
                    .addElement('C', TecTechCasings.CONTAINMENT_FIELD.asElement())
                    .addElement('D', TecTechCasings.SPACETIME.asElement())
                    .addElement('E', withHint(buildHatchAdder(GT_MetaTileEntity_EM_stabilizer.class).allow(TecTechHatchElement.Classic).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.WHITE))
                    .addElement('F', ofBlock(QuantumGlassBlock.INSTANCE, 0))
                    .addElement('G', withHint(buildHatchAdder(GT_MetaTileEntity_EM_stabilizer.class).allow(TecTechHatchElement.Elemental).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.ORANGE))
                    .build();
    //endregion

    public GT_MetaTileEntity_EM_stabilizer(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_stabilizer(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_stabilizer(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return checkPiece("main", 2, 2, 0);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder();
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.stabilizer.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.stabilizer.desc.1"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.stabilizer.hint.0", 1);
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.stabilizer.hint.1", 2);
        return tooltip;
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
    public IStructureDefinition<GT_MetaTileEntity_EM_stabilizer> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
