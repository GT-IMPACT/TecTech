package com.github.technus.tectech.thing.metaTileEntity.multi;

import com.github.technus.tectech.TecTech;
import com.github.technus.tectech.mechanics.dataTransport.QuantumDataPacket;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputData;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OutputData;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_Rack;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.INameFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.IStatusFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.Parameters;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.render.TT_RenderedExtendedFacingTexture;
import com.github.technus.tectech.util.CommonValues;
import com.github.technus.tectech.util.Util;
import com.github.technus.tectech.util.Vec3Impl;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import java.util.ArrayList;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;

import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_NEUTRAL;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_OK;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_TOO_LOW;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.fromLimitsInclusiveOuterBoundary;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.getStatus;
import static com.github.technus.tectech.util.CommonValues.MULTI_CHECK_AT;
import static com.github.technus.tectech.util.CommonValues.V;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.StatCollector.translateToLocal;
import static space.impact.structures.structure.StructureUtility.transpose;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_computer extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_computer> {
    //region structure
    private static final IStructureDefinition<GT_MetaTileEntity_EM_computer> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_computer>builder()
                    .addShape("front", transpose(new String[][]{
                            {" AA"},
                            {" AA"},
                            {" ~A"},
                            {" AA"}
                    }))
                    .addShape("cap", transpose(new String[][]{
                            {"-CB"},
                            {" DD"},
                            {" DD"},
                            {"-CB"}
                    }))
                    .addShape("slice", transpose(new String[][]{
                            {"-CB"},
                            {" ED"},
                            {" ED"},
                            {"-CB"}
                    }))
                    .addShape("back", transpose(new String[][]{
                            {" AA"},
                            {" AA"},
                            {" AA"},
                            {" AA"}
                    }))
                    .addElement('A', withHint(buildHatchAdder(GT_MetaTileEntity_EM_computer.class).allow(TecTechHatchElement.All).atLeast(TecTechHatchElement.Uncertainty).buildAndChain(TecTechCasings.COMPUTER), HintColor.WHITE))
                    .addElement('B', TecTechCasings.COMPUTER.asElement())
                    .addElement('C', TecTechCasings.COMPUTER_HEAT_VENT.asElement())
                    .addElement('D', TecTechCasings.ADVANCED_COMPUTER.asElement())
                    .addElement('E', withHint(buildHatchAdder(GT_MetaTileEntity_EM_computer.class).allow(TecTechHatchElement.Rack).buildAndChain(StructureRequirements.builder(TecTechCasings.ADVANCED_COMPUTER, 0).build()), HintColor.ORANGE))
                    .build();
    private static final INameFunction<GT_MetaTileEntity_EM_computer> OC_NAME = (base, p) -> translateToLocal("gt.blockmachines.multimachine.em.computer.cfgi.0");//Overclock ratio
    //endregion
    private static final INameFunction<GT_MetaTileEntity_EM_computer> OV_NAME = (base, p) -> translateToLocal("gt.blockmachines.multimachine.em.computer.cfgi.1");//Overvoltage ratio
    private static final INameFunction<GT_MetaTileEntity_EM_computer> MAX_TEMP_NAME = (base, p) -> translateToLocal("gt.blockmachines.multimachine.em.computer.cfgo.0");//Current max. heat
    //endregion
    private static final INameFunction<GT_MetaTileEntity_EM_computer> COMPUTE_NAME = (base, p) -> translateToLocal("gt.blockmachines.multimachine.em.computer.cfgo.1");//Produced computation
    private static final IStatusFunction<GT_MetaTileEntity_EM_computer> OC_STATUS =
            (base, p) -> LedStatus.fromLimitsInclusiveOuterBoundary(p.get(), 0, 1, 1, 3);
    private static final IStatusFunction<GT_MetaTileEntity_EM_computer> OV_STATUS =
            (base, p) -> LedStatus.fromLimitsInclusiveOuterBoundary(p.get(), .7, .8, 1.2, 2);
    private static final IStatusFunction<GT_MetaTileEntity_EM_computer> MAX_TEMP_STATUS =
            (base, p) -> LedStatus.fromLimitsInclusiveOuterBoundary(p.get(), -10000, 0, 0, 5000);
    private static final IStatusFunction<GT_MetaTileEntity_EM_computer> COMPUTE_STATUS = (base, p) -> {
        if (base.eAvailableData < 0) {
            return STATUS_TOO_LOW;
        }
        if (base.eAvailableData == 0) {
            return STATUS_NEUTRAL;
        }
        return STATUS_OK;
    };
    private static Textures.BlockIcons.CustomIcon ScreenOFF;
    private static Textures.BlockIcons.CustomIcon ScreenON;
    //region variables
    private final ArrayList<GT_MetaTileEntity_Hatch_Rack> eRacks = new ArrayList<>();
    //region parameters
    protected Parameters.Group.ParameterIn overclock, overvolt;
    protected Parameters.Group.ParameterOut maxCurrentTemp, availableData;
    //endregion

    public GT_MetaTileEntity_EM_computer(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        eCertainMode   = 5;
        eCertainStatus = -128;//no-brain value
    }

    public GT_MetaTileEntity_EM_computer(String aName) {
        super(aName);
        eCertainMode   = 5;
        eCertainStatus = -128;//no-brain value
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_computer(mName);
    }

    @Override
    protected void parametersInstantiation_EM() {
        Parameters.Group hatch_0 = parametrization.getGroup(0);
        overclock      = hatch_0.makeInParameter(0, 1, OC_NAME, OC_STATUS);
        overvolt       = hatch_0.makeInParameter(1, 1, OV_NAME, OV_STATUS);
        maxCurrentTemp = hatch_0.makeOutParameter(0, 0, MAX_TEMP_NAME, MAX_TEMP_STATUS);
        availableData  = hatch_0.makeOutParameter(1, 0, COMPUTE_NAME, COMPUTE_STATUS);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        for (GT_MetaTileEntity_Hatch_Rack rack : eRacks) {
            if (GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(rack)) {
                rack.getBaseMetaTileEntity().setActive(false);
            }
        }
        eRacks.clear();
        if (!checkPiece("front", 1, 2, 0)) {
            return false;
        }
        if (!checkPiece("cap", 1, 2, -1)) {
            return false;
        }
        byte offset = -2, totalLen = 4;
        while (offset > -16) {
            if (!checkPiece("slice", 1, 2, offset)) {
                break;
            }
            totalLen++;
            offset--;
        }
        if (totalLen > 17) {
            return false;
        }
        if (!checkPiece("cap", 1, 2, ++offset)) {
            return false;
        }
        if (!checkPiece("back", 1, 2, --offset)) {
            return false;
        }
        eCertainMode = (byte) Math.min(totalLen / 3, 5);
        for (GT_MetaTileEntity_Hatch_Rack rack : eRacks) {
            if (GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(rack)) {
                rack.getBaseMetaTileEntity().setActive(iGregTechTileEntity.isActive());
            }
        }
        return eUncertainHatches.size() == 1;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        if (aBaseMetaTileEntity.isServerSide() && mMachine && !aBaseMetaTileEntity.isActive() && aTick % 20 == MULTI_CHECK_AT) {
            double maxTemp = 0;
            for (GT_MetaTileEntity_Hatch_Rack rack : eRacks) {
                if (!GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(rack)) {
                    continue;
                }
                if (rack.heat > maxTemp) {
                    maxTemp = rack.heat;
                }
            }
            maxCurrentTemp.set(maxTemp);
        }
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        parametrization.setToDefaults(false, true);
        eAvailableData = 0;
        double maxTemp = 0;
        double overClockRatio = overclock.get();
        double overVoltageRatio = overvolt.get();
        if (Double.isNaN(overClockRatio) || Double.isNaN(overVoltageRatio)) {
            return false;
        }
        if (overclock.getStatus(true).isOk && overvolt.getStatus(true).isOk) {
            float eut = V[8] * (float) overVoltageRatio * (float) overClockRatio;
            if (eut < Integer.MAX_VALUE - 7) {
                mEUt = -(int) eut;
            } else {
                mEUt = -(int) V[8];
                return false;
            }
            short thingsActive = 0;
            int rackComputation;

            for (GT_MetaTileEntity_Hatch_Rack rack : eRacks) {
                if (!GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(rack)) {
                    continue;
                }
                if (rack.heat > maxTemp) {
                    maxTemp = rack.heat;
                }
                rackComputation = rack.tickComponents((float) overClockRatio, (float) overVoltageRatio);
                if (rackComputation > 0) {
                    eAvailableData += rackComputation;
                    thingsActive += 4;
                }
                rack.getBaseMetaTileEntity().setActive(true);
            }

            for (GT_MetaTileEntity_Hatch_InputData di : eInputData) {
                if (di.q != null)//ok for power losses
                {
                    thingsActive++;
                }
            }

            if (thingsActive > 0 && eCertainStatus == 0) {
                thingsActive += eOutputData.size();
                eAmpereFlow         = 1 + (thingsActive >> 2);
                mMaxProgresstime    = 20;
                mEfficiencyIncrease = 10000;
                maxCurrentTemp.set(maxTemp);
                availableData.set(eAvailableData);
                return true;
            } else {
                eAvailableData      = 0;
                mEUt                = -(int) V[8];
                eAmpereFlow         = 1;
                mMaxProgresstime    = 20;
                mEfficiencyIncrease = 10000;
                maxCurrentTemp.set(maxTemp);
                availableData.set(eAvailableData);
                return true;
            }
        }
        return false;
    }

    @Override
    public void outputAfterRecipe_EM() {
        if (!eOutputData.isEmpty()) {
            Vec3Impl pos = new Vec3Impl(
                    getBaseMetaTileEntity().getXCoord(),
                    getBaseMetaTileEntity().getYCoord(),
                    getBaseMetaTileEntity().getZCoord()
            );

            QuantumDataPacket pack = new QuantumDataPacket(eAvailableData / eOutputData.size()).unifyTraceWith(pos);
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

            for (GT_MetaTileEntity_Hatch_OutputData o : eOutputData) {
                o.q = pack;
            }
        }
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder();
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(Util.intBitsToString(TecTech.RANDOM.nextInt()));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.computer.desc"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.computer.hint.0", 1);
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.computer.hint.1", 2);
        return tooltip;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister aBlockIconRegister) {
        ScreenOFF = new Textures.BlockIcons.CustomIcon("iconsets/EM_COMPUTER");
        ScreenON  = new Textures.BlockIcons.CustomIcon("iconsets/EM_COMPUTER_ACTIVE");
        super.registerIcons(aBlockIconRegister);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            return new ITexture[]{TecTechCasings.ADVANCED_COMPUTER.getTexture(), new TT_RenderedExtendedFacingTexture(aActive ? ScreenON : ScreenOFF)};
        }
        return new ITexture[]{TecTechCasings.ADVANCED_COMPUTER.getTexture()};
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected ResourceLocation getActivitySound() {
        return GT_MetaTileEntity_EM_switch.activitySound;
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        for (GT_MetaTileEntity_Hatch_Rack rack : eRacks) {
            if (GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(rack)) {
                rack.getBaseMetaTileEntity().setActive(false);
            }
        }
    }

    @Override
    protected void extraExplosions_EM() {
        for (MetaTileEntity tTileEntity : eRacks) {
            tTileEntity.getBaseMetaTileEntity().doExplosion(V[9]);
        }
    }

    @Override
    protected long getAvailableData_EM() {
        return eAvailableData;
    }

    @Override
    public void stopMachine() {
        super.stopMachine();
        eAvailableData = 0;
        for (GT_MetaTileEntity_Hatch_Rack rack : eRacks) {
            if (GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(rack)) {
                rack.getBaseMetaTileEntity().setActive(false);
            }
        }
    }

    @Override
    protected void afterRecipeCheckFailed() {
        super.afterRecipeCheckFailed();
        for (GT_MetaTileEntity_Hatch_Rack rack : eRacks) {
            if (GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(rack)) {
                rack.getBaseMetaTileEntity().setActive(false);
            }
        }
    }

    public final boolean addRackToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) {
            return false;
        }
        IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
        if (aMetaTileEntity == null) {
            return false;
        }
        if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_Rack) {
            ((GT_MetaTileEntity_Hatch) aMetaTileEntity).updateTexture(aBaseCasingIndex);
            return eRacks.add((GT_MetaTileEntity_Hatch_Rack) aMetaTileEntity);
        }
        return false;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece("front", stackSize, hintsOnly, 1, 2, 0);
        buildPiece("cap", stackSize, hintsOnly, 1, 2, -1);

        byte offset = -2;
        for (int rackSlices = Math.max(1, Math.min(stackSize.stackSize, 12)); rackSlices > 0; rackSlices--) {
            buildPiece("slice", stackSize, hintsOnly, 1, 2, offset--);
        }

        buildPiece("cap", stackSize, hintsOnly, 1, 2, offset--);
        buildPiece("back", stackSize, hintsOnly, 1, 2, offset);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int budget, ISurvivalBuildEnvironment environment) {
        if (mMachine) {
            return -1;
        }
        int slices = Math.max(1, Math.min(stackSize.stackSize, 12));
        int built = 0;
        for (int depth = 0; depth < slices + 4 && built < budget; depth++) {
            String piece = depth == 0 ? "front" : depth == slices + 3 ? "back" : depth == 1 || depth == slices + 2 ? "cap" : "slice";
            int result = survivalBuildPiece(piece, stackSize, 1, 2, -depth, budget - built, environment);
            if (result > 0) {
                built += result;
            }
            if (mMachine) {
                return built == 0 ? -1 : built;
            }
        }
        return built;
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_EM_computer> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    public int getRackCount() {
        return eRacks.size();
    }

}
