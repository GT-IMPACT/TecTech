package com.github.technus.tectech.thing.metaTileEntity.multi;

import gregtech.api.util.GTStructureChannels;
import com.github.technus.tectech.Reference;
import com.github.technus.tectech.TecTech;
import com.github.technus.tectech.mechanics.elementalMatter.core.cElementalInstanceStackMap;
import com.github.technus.tectech.mechanics.elementalMatter.core.stacks.cElementalInstanceStack;
import com.github.technus.tectech.mechanics.elementalMatter.core.stacks.iHasElementalDefinition;
import com.github.technus.tectech.mechanics.elementalMatter.core.transformations.aFluidQuantizationInfo;
import com.github.technus.tectech.mechanics.elementalMatter.core.transformations.aItemQuantizationInfo;
import com.github.technus.tectech.mechanics.elementalMatter.core.transformations.aOredictQuantizationInfo;
import com.github.technus.tectech.mechanics.elementalMatter.core.transformations.bTransformationInfo;
import com.github.technus.tectech.thing.block.QuantumGlassBlock;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechAutoHatch;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.util.CommonValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTech_API;
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

import static com.github.technus.tectech.loader.TecTechConfig.DEBUG_MODE;
import static com.github.technus.tectech.mechanics.elementalMatter.core.templates.iElementalDefinition.DEFAULT_ENERGY_LEVEL;
import static com.github.technus.tectech.mechanics.elementalMatter.core.templates.iElementalDefinition.STABLE_RAW_LIFE_TIME;
import static com.github.technus.tectech.mechanics.elementalMatter.definitions.complex.dAtomDefinition.refMass;
import static com.github.technus.tectech.mechanics.elementalMatter.definitions.complex.dAtomDefinition.refUnstableMass;
import static com.github.technus.tectech.recipe.TT_recipeAdder.nullFluid;
import static com.github.technus.tectech.recipe.TT_recipeAdder.nullItem;
import static com.github.technus.tectech.util.CommonValues.V;
import static com.github.technus.tectech.util.Util.isInputEqual;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.StatCollector.translateToLocal;
import static space.impact.structures.structure.StructureUtility.ofBlock;
import static space.impact.structures.structure.StructureUtility.transpose;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_quantizer extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_quantizer> {
    public final static ResourceLocation activitySound = new ResourceLocation(Reference.MODID + ":fx_mid_freq");
    //region structure
    //use multi A energy inputs, use less power the longer it runs
    private static final IStructureDefinition<GT_MetaTileEntity_EM_quantizer> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_quantizer>builder()
                    .addChannel(GTStructureChannels.HATCH, 1, 1)
                    .addShape("main", transpose(new String[][]{
                            {"CCC", "BAB", "EEE", "DBD"},
                            {"C~C", "ABA", "EBE", "BFB"},
                            {"CCC", "BAB", "EEE", "DBD"}
                    }))
                    .addElement('A', TecTechCasings.HIGH_POWER.asElement())
                    .addElement('B', TecTechCasings.MOLECULAR.asElement())
                    .addElement('C', withHint(buildHatchAdder(GT_MetaTileEntity_EM_quantizer.class).allow(TecTechHatchElement.Classic).autoPlace(TecTechAutoHatch.Maintenance).autoPlace(TecTechAutoHatch.PowerInput, TecTechAutoHatch.ItemInput, TecTechAutoHatch.FluidInput).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.GREEN))
                    .addElement('D', ofBlock(QuantumGlassBlock.INSTANCE, 0))
                    .addElement('E', withHint(buildHatchAdder(GT_MetaTileEntity_EM_quantizer.class).allow(TecTechHatchElement.ElementalOverflow).autoPlace(TecTechHatchElement.ElementalOverflow).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.LIME))
                    .addElement('F', buildHatchAdder(GT_MetaTileEntity_EM_quantizer.class).hint(HintColor.ORANGE).build(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).atLeast(TecTechHatchElement.ElementalOutput).build()))
                    .build();
    //endregion

    public GT_MetaTileEntity_EM_quantizer(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_quantizer(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_quantizer(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return checkPiece("main", 1, 1, 0);
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {//TODO implement instance quantization
        if (GregTech_API.sPostloadFinished) {
            ArrayList<ItemStack> storedInputs = getStoredInputs();
            ItemStack[] inI = storedInputs.toArray(nullItem);
            if (inI.length > 0) {
                for (ItemStack is : inI) {
                    //ITEM STACK quantization
                    aItemQuantizationInfo aIQI = bTransformationInfo.itemQuantization.get(new aItemQuantizationInfo(is, false, null));
                    if (aIQI == null) {
                        aIQI = bTransformationInfo.itemQuantization.get(new aItemQuantizationInfo(is, true, null));//todo check if works?
                    }
                    if (aIQI == null) {
                        //ORE DICT quantization //todo fix for uranium?
                        int[] oreIDs = OreDictionary.getOreIDs(is);
                        for (int ID : oreIDs) {
                            if (DEBUG_MODE) {
                                TecTech.LOGGER.info("Quantifier-Ore-recipe " + is.getItem().getUnlocalizedName() + '.' + is.getItemDamage() + ' ' + OreDictionary.getOreName(ID));
                            }
                            aOredictQuantizationInfo aOQI = bTransformationInfo.oredictQuantization.get(ID);
                            if (aOQI == null) {
                                continue;
                            }
                            iHasElementalDefinition into = aOQI.output();
                            if (into != null && isInputEqual(true, false, nullFluid, new ItemStack[]{new ItemStack(is.getItem(), aOQI.amount, is.getItemDamage())}, null, inI)) {
                                startRecipe(into);
                                return true;
                            }
                        }
                    } else {
                        //Do ITEM STACK quantization
                        if (DEBUG_MODE) {
                            TecTech.LOGGER.info("Quantifier-Item-recipe " + is.getItem().getUnlocalizedName() + '.' + is.getItemDamage());
                        }
                        iHasElementalDefinition into = aIQI.output();
                        if (into != null && isInputEqual(true, false, nullFluid, new ItemStack[]{new ItemStack(is.getItem(), aIQI.input().stackSize, is.getItemDamage())}, null, inI)) {
                            startRecipe(into);
                            return true;
                        }
                    }
                }
            }
            ArrayList<FluidStack> storedFluids = getStoredFluids();
            FluidStack[] inF = storedFluids.toArray(nullFluid);
            if (inF.length > 0) {
                for (FluidStack fs : inF) {
                    aFluidQuantizationInfo aFQI = bTransformationInfo.fluidQuantization.get(fs.getFluid().getID());
                    if (aFQI == null) {
                        continue;
                    }
                    iHasElementalDefinition into = aFQI.output();
                    if (into != null && fs.amount >= aFQI.input().amount && isInputEqual(true, false,
                            new FluidStack[]{aFQI.input()}, nullItem, inF, (ItemStack[]) null
                    )) {
                        startRecipe(into);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void startRecipe(iHasElementalDefinition into) {
        mMaxProgresstime = 20;
        mEfficiencyIncrease = 10000;
        double mass = into.getMass();
        double euMult = Math.abs(mass / refMass);
        eAmpereFlow = (int) Math.ceil(Math.sqrt(Math.sqrt(euMult)));
        if (mass > refUnstableMass || into.getDefinition().getRawTimeSpan(DEFAULT_ENERGY_LEVEL) < STABLE_RAW_LIFE_TIME) {
            mEUt = (int) -V[8];
        } else {
            mEUt = (int) -V[6];
        }
        outputEM = new cElementalInstanceStackMap[]{
                into instanceof cElementalInstanceStack ?
                        new cElementalInstanceStackMap((cElementalInstanceStack) into) :
                        new cElementalInstanceStackMap(new cElementalInstanceStack(into.getDefinition(), into.getAmount()))
        };
    }

    @Override
    public void outputAfterRecipe_EM() {
        if (eOutputHatches.size() < 1) {
            stopMachine();
            return;
        }
        eOutputHatches.get(0).getContainerHandler().putUnifyAll(outputEM[0]);
        outputEM = null;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder(mName);
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.mattertoem.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.mattertoem.desc.1"));
        tooltip.beginStructureBlock(3, 3, 4, false);
        tooltip.addCasingInfoRange(TecTechCasings.HIGH_POWER, 4, 12, false);
        tooltip.addCasingInfoRange(TecTechCasings.MOLECULAR, 10, 18, false);
        tooltip.addOtherStructurePart(4, QuantumGlassBlock.INSTANCE.getLocalizedName(), translateToLocal("tt.structure.hologram"));
        tooltip.addOtherStructurePart(1, TecTechHatchElement.ElementalOutput.getDescription(), translateToLocal("tt.structure.required_position"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.casing_ranges"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_hatches"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_data"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.mattertoem.hint.0"), HintColor.GREEN);
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.mattertoem.hint.1"), HintColor.ORANGE);
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.mattertoem.hint.2"), HintColor.LIME);
        return tooltip.addSubChannel(GTStructureChannels.HATCH, "Places one elemental output, one overflow hatch, one energy input, one item input bus and one fluid input hatch at their marked positions. Also places one Maintenance Hatch.");
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected ResourceLocation getActivitySound() {
        return activitySound;
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
    public IStructureDefinition<GT_MetaTileEntity_EM_quantizer> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
