package com.github.technus.tectech.thing.metaTileEntity.multi;

import com.github.technus.tectech.mechanics.elementalMatter.core.cElementalInstanceStackMap;
import com.github.technus.tectech.mechanics.elementalMatter.core.stacks.cElementalInstanceStack;
import com.github.technus.tectech.mechanics.elementalMatter.core.stacks.iHasElementalDefinition;
import com.github.technus.tectech.mechanics.elementalMatter.core.transformations.aFluidDequantizationInfo;
import com.github.technus.tectech.mechanics.elementalMatter.core.transformations.aItemDequantizationInfo;
import com.github.technus.tectech.mechanics.elementalMatter.core.transformations.aOredictDequantizationInfo;
import com.github.technus.tectech.thing.block.QuantumGlassBlock;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputElemental;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.util.CommonValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;

import static com.github.technus.tectech.mechanics.elementalMatter.core.templates.iElementalDefinition.STABLE_RAW_LIFE_TIME;
import static com.github.technus.tectech.mechanics.elementalMatter.definitions.complex.dAtomDefinition.refMass;
import static com.github.technus.tectech.mechanics.elementalMatter.definitions.complex.dAtomDefinition.refUnstableMass;
import static com.github.technus.tectech.util.CommonValues.V;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.StatCollector.translateToLocal;
import static space.impact.structures.structure.StructureUtility.ofBlock;
import static space.impact.structures.structure.StructureUtility.transpose;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_dequantizer extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_dequantizer> {
    //region structure
    //use multi A energy inputs, use less power the longer it runs

    private static final IStructureDefinition<GT_MetaTileEntity_EM_dequantizer> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_dequantizer>builder()
                    .addShape("main", transpose(new String[][]{
                            {"CCC", "ABA", "EEE", "BDB"},
                            {"C~C", "BBB", "EBE", "DFD"},
                            {"CCC", "ABA", "EEE", "BDB"}
                    }))
                    .addElement('A', TecTechCasings.HIGH_POWER.asElement())
                    .addElement('B', TecTechCasings.MOLECULAR.asElement())
                    .addElement('C', withHint(buildHatchAdder(GT_MetaTileEntity_EM_dequantizer.class).allow(TecTechHatchElement.Classic).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.WHITE))
                    .addElement('D', ofBlock(QuantumGlassBlock.INSTANCE, 0))
                    .addElement('E', withHint(buildHatchAdder(GT_MetaTileEntity_EM_dequantizer.class).allow(TecTechHatchElement.ElementalOverflow).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.MAGENTA))
                    .addElement('F', buildHatchAdder(GT_MetaTileEntity_EM_dequantizer.class).hint(HintColor.ORANGE).build(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).atLeast(TecTechHatchElement.ElementalInput).build()))
                    .build();

    //endregion

    public GT_MetaTileEntity_EM_dequantizer(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_dequantizer(String aName) {
        super(aName);
    }

    private void startRecipe(iHasElementalDefinition from, long energy) {
        mMaxProgresstime    = 20;
        mEfficiencyIncrease = 10000;
        double mass = from.getMass();
        double euMult = Math.abs(mass / refMass);
        eAmpereFlow = (int) Math.ceil(Math.sqrt(Math.sqrt(euMult)));
        if (mass > refUnstableMass || from.getDefinition().getRawTimeSpan(energy) < STABLE_RAW_LIFE_TIME) {
            mEUt = (int) -V[8];
        } else {
            mEUt = (int) -V[6];
        }
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_dequantizer(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return checkPiece("main", 1, 1, 0);
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        for (GT_MetaTileEntity_Hatch_InputElemental in : eInputHatches) {
            cElementalInstanceStackMap map = in.getContainerHandler();
            for (cElementalInstanceStack stack : map.values()) {
                {
                    aFluidDequantizationInfo info = stack.getDefinition().someAmountIntoFluidStack();
                    if (info != null) {
                        if (map.removeAllAmounts(false, info.input())) {
                            mOutputFluids = new FluidStack[]{info.output()};
                            startRecipe(info.input(), stack.getEnergy());
                            return true;
                        }
                    }
                }
                {
                    aItemDequantizationInfo info = stack.getDefinition().someAmountIntoItemsStack();
                    if (info != null) {
                        if (map.removeAllAmounts(false, info.input())) {
                            mOutputItems = new ItemStack[]{info.output()};
                            startRecipe(info.input(), stack.getEnergy());
                            return true;
                        }
                    }
                }
                {
                    aOredictDequantizationInfo info = stack.getDefinition().someAmountIntoOredictStack();
                    if (info != null) {
                        if (map.removeAllAmounts(false, info.input())) {
                            ArrayList<ItemStack> items = OreDictionary.getOres(info.out);
                            if (items != null && !items.isEmpty()) {
                                mOutputItems = new ItemStack[]{items.get(0)};
                                startRecipe(info.input(), stack.getEnergy());
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder();
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.emtomatter.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.emtomatter.desc.1"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.emtomatter.hint.0", 1);
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.emtomatter.hint.1", 2);
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.emtomatter.hint.2", 3);
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
    public IStructureDefinition<GT_MetaTileEntity_EM_dequantizer> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
