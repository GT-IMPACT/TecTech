package com.github.technus.tectech.thing.metaTileEntity.multi;

import cofh.api.energy.IEnergyContainerItem;
import com.github.technus.tectech.Reference;
import com.github.technus.tectech.TecTech;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_Container_MultiMachineEM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_GUIContainer_MultiMachineEM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.util.CommonValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;

import static com.github.technus.tectech.loader.TecTechConfig.DEBUG_MODE;
import static gregtech.api.GregTech_API.mEUtoRF;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.StatCollector.translateToLocal;
import static space.impact.structures.structure.StructureUtility.transpose;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_infuser extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_infuser> {
    public final static ResourceLocation activitySound = new ResourceLocation(Reference.MODID + ":fx_whooum");
    //region structure
    //endregion
    private static final IStructureDefinition<GT_MetaTileEntity_EM_infuser> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_infuser>builder()
                    .addShape("main", transpose(new String[][]{
                            {"CCC", "CCC", "CCC"},
                            {"BBB", "BAB", "BBB"},
                            {"A~A", "AAA", "AAA"},
                            {"BBB", "BAB", "BBB"},
                            {"CCC", "CCC", "CCC"}
                    }))
                    .addElement('A', TecTechCasings.MOLECULAR.asElement())
                    .addElement('B', TecTechCasings.MOLECULAR_COIL.asElement())
                    .addElement('C', withHint(buildHatchAdder(GT_MetaTileEntity_EM_infuser.class).allow(TecTechHatchElement.Classic).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.GREEN))
                    .build();

    public GT_MetaTileEntity_EM_infuser(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        minRepairStatus = (byte) getIdealStatus();
        eDismantleBoom  = true;
    }

    public GT_MetaTileEntity_EM_infuser(String aName) {
        super(aName);
        minRepairStatus = (byte) getIdealStatus();
        eDismantleBoom  = true;
    }

    private long doChargeItemStack(IElectricItem item, ItemStack stack) {
        try {
            double euDiff = item.getMaxCharge(stack) - ElectricItem.manager.getCharge(stack);
            if (euDiff > 0) {
                setEUVar(getEUVar() - (getEUVar() >> 5));
            }
            long remove = (long) Math.ceil(
                    ElectricItem.manager.charge(stack,
                            Math.min(euDiff, getEUVar())
                            , item.getTier(stack), true, false
                    ));
            setEUVar(getEUVar() - remove);
            if (getEUVar() < 0) {
                setEUVar(0);
            }
            return remove;
        } catch (Exception e) {
            if (DEBUG_MODE) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private long doChargeItemStackRF(IEnergyContainerItem item, ItemStack stack) {
        try {
            long RF = Math.min(item.getMaxEnergyStored(stack) - item.getEnergyStored(stack), getEUVar() * mEUtoRF / 100L);
            //if(RF>0)this.setEUVar(this.getEUVar()-this.getEUVar()>>10);
            RF = item.receiveEnergy(stack, RF > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) RF, false);
            RF = RF * 100L / mEUtoRF;
            setEUVar(getEUVar() - RF);
            if (getEUVar() < 0) {
                setEUVar(0);
            }
            return RF;
        } catch (Exception e) {
            if (DEBUG_MODE) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_infuser(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return checkPiece("main", 1, 2, 0);
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        if (itemStack != null && itemStack.stackSize == 1) {
            Item ofThis = itemStack.getItem();
            if (ofThis instanceof IElectricItem) {
                mEfficiencyIncrease = 10000;
                mMaxProgresstime    = 20;
                return true;
            } else if (TecTech.hasCOFH && ofThis instanceof IEnergyContainerItem) {
                mEfficiencyIncrease = 10000;
                mMaxProgresstime    = 20;
                return true;
            }
        }
        return false;
    }

    @Override
    public void outputAfterRecipe_EM() {
        ItemStack itemStack = mInventory[1];
        if (itemStack != null && itemStack.stackSize == 1) {
            Item ofThis = itemStack.getItem();
            if (ofThis instanceof IElectricItem) {
                if (doChargeItemStack((IElectricItem) ofThis, itemStack) == 0) {
                    getBaseMetaTileEntity().disableWorking();
                }
                return;
            } else if (TecTech.hasCOFH && ofThis instanceof IEnergyContainerItem) {
                if (doChargeItemStackRF((IEnergyContainerItem) ofThis, itemStack) == 0) {
                    getBaseMetaTileEntity().disableWorking();
                }
                return;
            }
        }
        getBaseMetaTileEntity().disableWorking();
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder();
        tooltip.addInfo(CommonValues.TEC_MARK_GENERAL);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.infuser.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.infuser.desc.1"));
        tooltip.addInfo(EnumChatFormatting.BLUE + translateToLocal("gt.blockmachines.multimachine.em.infuser.desc.2"));
        tooltip.addInfo(EnumChatFormatting.BLUE + translateToLocal("gt.blockmachines.multimachine.em.infuser.desc.3"));
        tooltip.beginStructureBlock(3, 5, 3, false);
        tooltip.addCasingInfoExactly(TecTechCasings.MOLECULAR, 10, false);
        tooltip.addCasingInfoExactly(TecTechCasings.MOLECULAR_COIL, 16, false);
        tooltip.addCasingInfoRange(TecTechCasings.HIGH_POWER, 0, 18, false);
        tooltip.addStructureInfo(translateToLocal("tt.structure.casing_ranges"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_hatches"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_data"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.infuser.hint"), HintColor.GREEN);
        return tooltip;
    }

    @Override
    public Object getServerGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_Container_MultiMachineEM(aPlayerInventory, aBaseMetaTileEntity, true, false, true);
    }

    @Override
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_GUIContainer_MultiMachineEM(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "EMDisplay.png", true, false, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected ResourceLocation getActivitySound() {
        return activitySound;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece("main", stackSize, hintsOnly, 1, 2, 0);
    }
    @Override
    public int survivalConstruct(ItemStack stackSize, int budget, ISurvivalBuildEnvironment environment) {
        return survivalBuildPiece("main", stackSize, 1, 2, 0, budget, environment);
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_EM_infuser> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
