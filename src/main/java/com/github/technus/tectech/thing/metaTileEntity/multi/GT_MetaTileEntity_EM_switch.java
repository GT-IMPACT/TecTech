package com.github.technus.tectech.thing.metaTileEntity.multi;

import gregtech.api.util.GTStructureChannels;
import com.github.technus.tectech.Reference;
import com.github.technus.tectech.mechanics.dataTransport.QuantumDataPacket;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputData;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OutputData;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.INameFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.IStatusFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.Parameters;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechAutoHatch;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.render.TT_RenderedExtendedFacingTexture;
import com.github.technus.tectech.util.CommonValues;
import com.github.technus.tectech.util.Vec3Impl;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;

import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_HIGH;
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
import static space.impact.structures.structure.StructureUtility.transpose;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_switch extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_switch> {
    public final static ResourceLocation activitySound = new ResourceLocation(Reference.MODID + ":fx_hi_freq");
    //region structure
    //endregion
    private static final IStructureDefinition<GT_MetaTileEntity_EM_switch> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_switch>builder()
                    .addChannel(GTStructureChannels.HATCH, 1, 1)
                    .addShape("main", transpose(new String[][]{
                            {"BBB", "BBB", "BBB"},
                            {"B~B", "BAB", "BBB"},
                            {"BBB", "BBB", "BBB"}
                    }))
                    .addElement('A', TecTechCasings.ADVANCED_COMPUTER.asElement())
                    .addElement('B', withHint(buildHatchAdder(GT_MetaTileEntity_EM_switch.class).allow(TecTechHatchElement.Classic).autoPlace(TecTechAutoHatch.Maintenance).autoPlace(TecTechAutoHatch.PowerInput, TecTechAutoHatch.DataInput, TecTechAutoHatch.DataOutput).buildAndChain(StructureRequirements.builder(TecTechCasings.COMPUTER, 0).build()), HintColor.GREEN))
                    .build();
    //region parameters
    private static final INameFunction<GT_MetaTileEntity_EM_switch> ROUTE_NAME =
            (base, p) -> (p.parameterId() == 0 ? translateToLocal("tt.keyword.Destination") + " " : translateToLocal("tt.keyword.Weight") + " ") + p.hatchId();
    private static final IStatusFunction<GT_MetaTileEntity_EM_switch> WEI_STATUS =
            (base, p) -> {
                double v = p.get();
                if (Double.isNaN(v)) return STATUS_WRONG;
                if (v < 0) return STATUS_TOO_LOW;
                if (v == 0) return STATUS_LOW;
                if (Double.isInfinite(v)) return STATUS_HIGH;
                return STATUS_OK;
            };
    private static final IStatusFunction<GT_MetaTileEntity_EM_switch> DST_STATUS =
            (base, p) -> {
                if (base.weight[p.hatchId()].getStatus(false).isOk) {
                    double v = p.get();
                    if (Double.isNaN(v)) return STATUS_WRONG;
                    v = (int) v;
                    if (v <= 0) return STATUS_TOO_LOW;
                    if (v >= base.eOutputHatches.size()) return STATUS_TOO_HIGH;
                    return STATUS_OK;
                }
                return STATUS_NEUTRAL;
            };
    protected Parameters.Group.ParameterIn[] dst;
    //endregion
    protected Parameters.Group.ParameterIn[] weight;

    public GT_MetaTileEntity_EM_switch(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_switch(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_switch(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return checkPiece("main", 1, 1, 0);
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        short thingsActive = 0;
        for (GT_MetaTileEntity_Hatch_InputData di : eInputData) {
            if (di.q != null) {
                thingsActive++;
            }
        }

        if (thingsActive > 0) {
            thingsActive += eOutputData.size();
            mEUt                = -(int) V[7];
            eAmpereFlow         = 1 + (thingsActive >> 2);
            mMaxProgresstime    = 20;
            mEfficiencyIncrease = 10000;
            return true;
        }
        return false;
    }

    @Override
    public void outputAfterRecipe_EM() {
        if (!eOutputData.isEmpty()) {
            double total = 0;
            double weight;
            for (int i = 0; i < 10; i++) {//each param pair
                weight = this.weight[i].get();
                if (weight > 0 && dst[i].get() >= 0) {
                    total += weight;//Total weighted div
                }
            }

            Vec3Impl pos = new Vec3Impl(
                    getBaseMetaTileEntity().getXCoord(),
                    getBaseMetaTileEntity().getYCoord(),
                    getBaseMetaTileEntity().getZCoord()
            );

            QuantumDataPacket pack = new QuantumDataPacket(0L).unifyTraceWith(pos);
            if (pack == null) {
                return;
            }
            for (GT_MetaTileEntity_Hatch_InputData hatch : eInputData) {
                if (hatch.q == null || hatch.q.contains(pos)) {
                    continue;
                }
                pack = pack.unifyPacketWith(hatch.q);
                if (pack == null) {
                    return;
                }
            }

            long remaining = pack.getContent();

            double dest;
            for (int i = 0; i < 10; i++) {
                dest   = dst[i].get();
                weight = this.weight[i].get();
                if (weight > 0 && dest >= 0) {
                    int outIndex = (int) dest - 1;
                    if (outIndex < 0 || outIndex >= eOutputData.size()) {
                        continue;
                    }
                    GT_MetaTileEntity_Hatch_OutputData out = eOutputData.get(outIndex);
                    if (Double.isInfinite(total)) {
                        if (Double.isInfinite(weight)) {
                            out.q = new QuantumDataPacket(remaining).unifyTraceWith(pack);
                            break;
                        }
                    } else {
                        long part = (long) Math.floor(pack.getContent() * weight / total);
                        if (part > 0) {
                            remaining -= part;
                            if (remaining > 0) {
                                out.q = new QuantumDataPacket(part).unifyTraceWith(pack);
                            } else if (part + remaining > 0) {
                                out.q = new QuantumDataPacket(part + remaining).unifyTraceWith(pack);
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder(mName);
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.switch.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.switch.desc.1"));
        tooltip.beginStructureBlock(3, 3, 3, false);
        tooltip.addCasingInfoExactly(TecTechCasings.ADVANCED_COMPUTER, 1, false);
        tooltip.addCasingInfoRange(TecTechCasings.COMPUTER, 0, 25, false);
        tooltip.addStructureInfo(translateToLocal("tt.structure.casing_ranges"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_hatches"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_data"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.computer.hint.0"), HintColor.GREEN);
        return tooltip.addSubChannel(GTStructureChannels.HATCH, "Places one computation data input, one computation data output and one energy input at green positions. Set the data routes in the controller. Also places one Maintenance Hatch.");
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

    @Override
    protected void parametersInstantiation_EM() {
        dst    = new Parameters.Group.ParameterIn[10];
        weight = new Parameters.Group.ParameterIn[10];
        for (int i = 0; i < 10; i++) {
            Parameters.Group hatch = parametrization.getGroup(i);
            dst[i]    = hatch.makeInParameter(0, i, ROUTE_NAME, DST_STATUS);
            weight[i] = hatch.makeInParameter(1, 0, ROUTE_NAME, WEI_STATUS);
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

    @Override
    public IStructureDefinition<GT_MetaTileEntity_EM_switch> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
