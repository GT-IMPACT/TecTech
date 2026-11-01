package com.github.technus.tectech.thing.metaTileEntity.multi;

import gregtech.api.util.GTStructureChannels;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputElemental;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OutputElemental;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.INameFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.IStatusFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.Parameters;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechAutoHatch;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.util.CommonValues;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;

import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_LOW;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_NEUTRAL;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_OK;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_TOO_HIGH;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_TOO_LOW;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_WRONG;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.getStatus;
import static com.github.technus.tectech.util.CommonValues.V;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.StatCollector.translateToLocal;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_junction extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_junction> {
    //region structure
    //use multi A energy inputs, use less power the longer it runs

    private static final IStructureDefinition<GT_MetaTileEntity_EM_junction> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_junction>builder()
                    .addChannel(GTStructureChannels.VARIANT, 1, 2)
                    .addChannel(GTStructureChannels.HATCH, 1, 1)
                    .addShape("main", new String[][]{
                            {"CCC", "C~C", "CCC"},
                            {"AAA", "AAA", "AAA"},
                            {"DDD", "DAD", "DDD"},
                            {"DDD", "DDD", "DDD"}
                    })
                    .addShape("mainBig", new String[][]{
                            {"     ", " CCC ", " C~C ", " CCC ", "     "},
                            {" DDD ", "DAAAD", "DABAD", "DAAAD", " DDD "},
                            {"DDDDD", "DAAAD", "DAAAD", "DAAAD", "DDDDD"},
                            {" DDD ", "DAAAD", "DAAAD", "DAAAD", " DDD "},
                            {" DDD ", "DDDDD", "DDDDD", "DDDDD", " DDD "}
                    })
                    .addElement('A', TecTechCasings.MOLECULAR.asElement())
                    .addElement('B', TecTechCasings.ADVANCED_MOLECULAR.asElement())
                    .addElement('C', withHint(buildHatchAdder(GT_MetaTileEntity_EM_junction.class).allow(TecTechHatchElement.Classic).autoPlace(TecTechAutoHatch.Maintenance).autoPlace(TecTechAutoHatch.PowerInput).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.GREEN))
                    .addElement('D', withHint(buildHatchAdder(GT_MetaTileEntity_EM_junction.class).allow(TecTechHatchElement.Elemental).autoPlace(TecTechHatchElement.ElementalInput, TecTechHatchElement.ElementalOutput, TecTechHatchElement.ElementalOverflow).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.LIME))
                    .build();
    //endregion

    //region parameters
    private static final INameFunction<GT_MetaTileEntity_EM_junction> ROUTE_NAME =
            (base, p) -> (p.parameterId() == 0 ? translateToLocal("tt.keyword.Source") + " " : translateToLocal("tt.keyword.Destination") + " ") + p.hatchId();
    private static final IStatusFunction<GT_MetaTileEntity_EM_junction> SRC_STATUS =
            (base, p) -> {
                double v = p.get();
                if (Double.isNaN(v)) return STATUS_WRONG;
                v = (int) v;
                if (v < 0) return STATUS_TOO_LOW;
                if (v == 0) return STATUS_NEUTRAL;
                if (v >= base.eInputHatches.size()) return STATUS_TOO_HIGH;
                return STATUS_OK;
            };
    private static final IStatusFunction<GT_MetaTileEntity_EM_junction> DST_STATUS =
            (base, p) -> {
                if (base.src[p.hatchId()].getStatus(false) == STATUS_OK) {
                    double v = p.get();
                    if (Double.isNaN(v)) return STATUS_WRONG;
                    v = (int) v;
                    if (v < 0) return STATUS_TOO_LOW;
                    if (v == 0) return STATUS_LOW;
                    if (v >= base.eInputHatches.size()) return STATUS_TOO_HIGH;
                    return STATUS_OK;
                }
                return STATUS_NEUTRAL;
            };
    protected Parameters.Group.ParameterIn[] src;
    protected Parameters.Group.ParameterIn[] dst;
    //endregion

    public GT_MetaTileEntity_EM_junction(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_junction(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_junction(mName);
    }

    private int constructionVariant;

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        int meta = iGregTechTileEntity.getMetaIDAtSide(GT_Utility.getOppositeSide(iGregTechTileEntity.getFrontFacing()));
        if (meta == 4) {
            return checkPiece("main", 1, 1, 0);
        } else if (meta == 5) {
            return checkPiece("mainBig", 2, 2, 0);
        }
        if (constructionVariant != 0) {
            // Discover existing ports before the size-identifying center is installed.
            boolean small = constructionVariant == 1;
            checkPiece(small ? "main" : "mainBig", small ? 1 : 2, small ? 1 : 2, 0);
        }
        return false;
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        for (GT_MetaTileEntity_Hatch_InputElemental in : eInputHatches) {
            if (in.getContainerHandler().hasStacks()) {
                mEUt                = -(int) V[8];
                eAmpereFlow         = 1 + (eInputHatches.size() + eOutputHatches.size() >> 1);
                mMaxProgresstime    = 20;
                mEfficiencyIncrease = 10000;
                return true;
            }
        }
        return false;
    }

    @Override
    public void outputAfterRecipe_EM() {
        double src, dst;
        for (int i = 0; i < 10; i++) {
            src = this.src[i].get();
            dst = this.dst[i].get();
            if (Double.isNaN(src) || Double.isNaN(dst)) {
                continue;
            }
            int inIndex = (int) src - 1;
            if (inIndex < 0 || inIndex >= eInputHatches.size()) {
                continue;
            }
            int outIndex = (int) dst - 1;
            GT_MetaTileEntity_Hatch_InputElemental in = eInputHatches.get(inIndex);
            if (outIndex == -1) {//param==0 -> null the content
                cleanHatchContentEM_EM(in);
            } else {
                if (outIndex < 0 || outIndex >= eOutputHatches.size()) {
                    continue;
                }
                GT_MetaTileEntity_Hatch_OutputElemental out = eOutputHatches.get(outIndex);
                out.getContainerHandler().putUnifyAll(in.getContainerHandler());
                in.getContainerHandler().clear();
            }
        }
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder(mName);
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.junction.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.junction.desc.1"));
        tooltip.addSubChannel(GTStructureChannels.VARIANT, "Value 1: small junction; value 2: large junction. If unset, an odd projector stack size selects the small junction; an even stack size selects the large one.");
        tooltip.addStructureInfo(translateToLocal("tt.structure.junction.small"));
        tooltip.beginStructureBlock(3, 3, 4, false);
        tooltip.addCasingInfoRange(TecTechCasings.MOLECULAR, 10, 27, false);
        tooltip.addCasingInfoRange(TecTechCasings.HIGH_POWER, 0, 8, false);
        tooltip.addStructureInfo(translateToLocal("tt.structure.junction.large"));
        tooltip.beginStructureBlock(5, 5, 5, false);
        tooltip.addCasingInfoRange(TecTechCasings.MOLECULAR, 26, 87, false);
        tooltip.addCasingInfoExactly(TecTechCasings.ADVANCED_MOLECULAR, 1, false);
        tooltip.addCasingInfoRange(TecTechCasings.HIGH_POWER, 0, 8, false);
        tooltip.addStructureInfo(translateToLocal("tt.structure.casing_ranges"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_hatches"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_data"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.elemental_hatches"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.junction.hint.0"), HintColor.GREEN);
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.junction.hint.1"), HintColor.LIME);
        return tooltip.addSubChannel(GTStructureChannels.HATCH, "Places one elemental input, output and overflow hatch at lime positions and one energy input at a green position. Set the routes in the controller. Also places one Maintenance Hatch.");
    }

    @Override
    protected void parametersInstantiation_EM() {
        src = new Parameters.Group.ParameterIn[10];
        dst = new Parameters.Group.ParameterIn[10];
        for (int i = 0; i < 10; i++) {
            Parameters.Group hatch = parametrization.getGroup(i);
            src[i] = hatch.makeInParameter(0, i, ROUTE_NAME, SRC_STATUS);
            dst[i] = hatch.makeInParameter(1, i, ROUTE_NAME, DST_STATUS);
        }
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        if ((GTStructureChannels.getValue(stackSize, GTStructureChannels.VARIANT) & 1) == 1) {
            buildPiece("main", stackSize, hintsOnly, 1, 1, 0);
        } else {
            buildPiece("mainBig", stackSize, hintsOnly, 2, 2, 0);
        }
    }
    @Override
    public int survivalConstruct(ItemStack stackSize, int budget, ISurvivalBuildEnvironment environment) {
        boolean small = (GTStructureChannels.getValue(stackSize, GTStructureChannels.VARIANT) & 1) == 1;
        constructionVariant = small ? 1 : 2;
        try {
            return survivalBuildPiece(small ? "main" : "mainBig", stackSize, small ? 1 : 2, small ? 1 : 2,
                    0, budget, environment);
        } finally {
            constructionVariant = 0;
        }
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_EM_junction> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
