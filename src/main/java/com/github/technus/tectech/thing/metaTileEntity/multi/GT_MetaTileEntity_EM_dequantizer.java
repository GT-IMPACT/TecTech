package com.github.technus.tectech.thing.metaTileEntity.multi;

import com.github.technus.tectech.mechanics.constructable.IConstructable;
import com.github.technus.tectech.mechanics.elementalMatter.core.cElementalInstanceStackMap;
import com.github.technus.tectech.mechanics.elementalMatter.core.stacks.cElementalInstanceStack;
import com.github.technus.tectech.mechanics.elementalMatter.core.stacks.iHasElementalDefinition;
import com.github.technus.tectech.mechanics.elementalMatter.core.transformations.aFluidDequantizationInfo;
import com.github.technus.tectech.mechanics.elementalMatter.core.transformations.aItemDequantizationInfo;
import com.github.technus.tectech.mechanics.elementalMatter.core.transformations.aOredictDequantizationInfo;
import com.github.technus.tectech.mechanics.structure.Structure;
import com.github.technus.tectech.mechanics.structure.adders.IHatchAdder;
import com.github.technus.tectech.thing.block.QuantumGlassBlock;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputElemental;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.util.CommonValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;

import static com.github.technus.tectech.mechanics.elementalMatter.core.templates.iElementalDefinition.STABLE_RAW_LIFE_TIME;
import static com.github.technus.tectech.mechanics.elementalMatter.definitions.complex.dAtomDefinition.refMass;
import static com.github.technus.tectech.mechanics.elementalMatter.definitions.complex.dAtomDefinition.refUnstableMass;
import static com.github.technus.tectech.mechanics.structure.Structure.adders;
import static com.github.technus.tectech.thing.casing.GT_Block_CasingsTT.textureOffset;
import static com.github.technus.tectech.thing.casing.TT_Container_Casings.sBlockCasingsTT;
import static com.github.technus.tectech.util.CommonValues.V;
import static net.minecraft.util.StatCollector.translateToLocal;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_dequantizer extends GT_MetaTileEntity_MultiblockBase_EM implements IConstructable {
    //region structure
    //use multi A energy inputs, use less power the longer it runs
    private static final String[][] shape = new String[][]{
            {"   ", " . ", "   ",},
            {"010", "111", "010",},
            {"\"\"\"", "\"1\"", "\"\"\"",},
            {"121", "2!2", "121",},
    };
    private static final Block[] blockType = new Block[]{sBlockCasingsTT, sBlockCasingsTT, QuantumGlassBlock.INSTANCE};
    private static final byte[] blockMeta = new byte[]{0, 4, 0};
    private static final IHatchAdder<GT_MetaTileEntity_EM_dequantizer>[] addingMethods = adders(
            GT_MetaTileEntity_EM_dequantizer::addClassicToMachineList,
            GT_MetaTileEntity_EM_dequantizer::addElementalInputToMachineList,
            GT_MetaTileEntity_EM_dequantizer::addElementalMufflerToMachineList);
    private static final short[] casingTextures = new short[]{textureOffset, textureOffset + 4, textureOffset + 4};
    private static final Block[] blockTypeFallback = new Block[]{sBlockCasingsTT, sBlockCasingsTT, sBlockCasingsTT};
    private static final byte[] blockMetaFallback = new byte[]{0, 4, 4};
    private static final String[] description = new String[]{
            EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":",
            translateToLocal("gt.blockmachines.multimachine.em.emtomatter.hint.0"),//1 - Classic Hatches or High Power Casing"
            translateToLocal("gt.blockmachines.multimachine.em.emtomatter.hint.1"),//2 - Elemental Input Hatch
            translateToLocal("gt.blockmachines.multimachine.em.emtomatter.hint.2"),//3 - Elemental Overflow Hatches or Molecular Casing
    };
    //endregion

    public GT_MetaTileEntity_EM_dequantizer(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_dequantizer(String aName) {
        super(aName);
    }

    private void startRecipe(iHasElementalDefinition from, long energy) {
        mMaxProgresstime = 20;
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
        return structureCheck_EM(shape, blockType, blockMeta, addingMethods, casingTextures, blockTypeFallback, blockMetaFallback, 1, 1, 0);
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
    public String[] getDescription() {
        return new String[]{
                CommonValues.TEC_MARK_EM,
                translateToLocal("gt.blockmachines.multimachine.em.emtomatter.desc.0"),//Transform quantum form back to...
                EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.emtomatter.desc.1")//regular one, but why?
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected ResourceLocation getActivitySound() {
        return GT_MetaTileEntity_EM_quantizer.activitySound;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        Structure.builder(shape, blockType, blockMeta, 1, 1, 0, getBaseMetaTileEntity(), getExtendedFacing(), hintsOnly);
    }

    @Override
    public String[] getStructureDescription(ItemStack stackSize) {
        return description;
    }
}