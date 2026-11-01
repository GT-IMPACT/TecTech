package com.github.technus.tectech.thing.metaTileEntity.multi;

import com.github.technus.tectech.thing.block.QuantumGlassBlock;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.render.TT_RenderedExtendedFacingTexture;
import com.github.technus.tectech.util.CommonValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import net.minecraft.client.renderer.texture.IIconRegister;
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
public class GT_MetaTileEntity_EM_wormhole extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_wormhole> {
    //region structure
    private static final IStructureDefinition<GT_MetaTileEntity_EM_wormhole> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_wormhole>builder()
                    .addShape("main", transpose(new String[][]{
                            {"         ", "         ", "         ", "         ", "         ", "    D    ", "  DDDDD  ", "  DGGGD  ", "  DGGGD  ", "  DGGGD  ", "  DDDDD  "},
                            {"         ", "         ", "         ", "    D    ", "    D    ", "    D    ", " DDAAADD ", " EABBBAE ", " EABBBAE ", " EABBBAE ", " DDAAADD "},
                            {"         ", "    D    ", "    D    ", "    D    ", "         ", "         ", "DDA   ADD", "DABFFFBAD", "DABCCCBAD", "DABFFFBAD", "DDA   ADD"},
                            {"   EEE   ", "   DDD   ", "   F F   ", "         ", "         ", "         ", "DA     AD", "GBF   FBG", "GBC   CBG", "GBF   FBG", "DA     AD"},
                            {"   E~E   ", "  DDBDD  ", "  D   D  ", " DD   DD ", " D     D ", "DD     DD", "DA     AD", "GBF   FBG", "GBC   CBG", "GBF   FBG", "DA     AD"},
                            {"   EEE   ", "   DDD   ", "   F F   ", "         ", "         ", "         ", "DA     AD", "GBF   FBG", "GBC   CBG", "GBF   FBG", "DA     AD"},
                            {"         ", "    D    ", "    D    ", "    D    ", "         ", "         ", "DDA   ADD", "DABFFFBAD", "DABCCCBAD", "DABFFFBAD", "DDA   ADD"},
                            {"         ", "         ", "         ", "    D    ", "    D    ", "    D    ", " DDAAADD ", " EABBBAE ", " EABBBAE ", " EABBBAE ", " DDAAADD "},
                            {"         ", "         ", "         ", "         ", "         ", "    D    ", "  DDDDD  ", "  DGGGD  ", "  DGGGD  ", "  DGGGD  ", "  DDDDD  "}
                    }))
                    .addElement('A', TecTechCasings.ADVANCED_MOLECULAR.asElement())
                    .addElement('B', TecTechCasings.TELEPORTATION.asElement())
                    .addElement('C', TecTechCasings.DIMENSIONAL_BRIDGE.asElement())
                    .addElement('D', TecTechCasings.ULTIMATE_MOLECULAR.asElement())
                    .addElement('E', withHint(buildHatchAdder(GT_MetaTileEntity_EM_wormhole.class).allow(TecTechHatchElement.Classic).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.GREEN))
                    .addElement('F', ofBlock(QuantumGlassBlock.INSTANCE, 0))
                    .addElement('G', withHint(buildHatchAdder(GT_MetaTileEntity_EM_wormhole.class).allow(TecTechHatchElement.Elemental).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.LIME))
                    .build();
    //endregion
    //region variables
    private static Textures.BlockIcons.CustomIcon ScreenOFF;
    private static Textures.BlockIcons.CustomIcon ScreenON;
    //endregion

    public GT_MetaTileEntity_EM_wormhole(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_wormhole(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_wormhole(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return checkPiece("main", 4, 4, 0);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder();
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.wormhole.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.wormhole.desc.1"));
        tooltip.beginStructureBlock(9, 9, 11, false);
        tooltip.addCasingInfoExactly(TecTechCasings.ADVANCED_MOLECULAR, 56, false);
        tooltip.addCasingInfoExactly(TecTechCasings.TELEPORTATION, 49, false);
        tooltip.addCasingInfoExactly(TecTechCasings.DIMENSIONAL_BRIDGE, 12, false);
        tooltip.addCasingInfoExactly(TecTechCasings.ULTIMATE_MOLECULAR, 124, false);
        tooltip.addCasingInfoRange(TecTechCasings.HIGH_POWER, 0, 20, false);
        tooltip.addCasingInfoRange(TecTechCasings.MOLECULAR, 0, 36, false);
        tooltip.addOtherStructurePart(28, QuantumGlassBlock.INSTANCE.getLocalizedName(), translateToLocal("tt.structure.hologram"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.casing_ranges"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_hatches"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_data"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.elemental_hatches"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.wormhole.hint.0"), HintColor.GREEN);
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.wormhole.hint.1"), HintColor.LIME);
        return tooltip;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister aBlockIconRegister) {
        ScreenOFF = new Textures.BlockIcons.CustomIcon("iconsets/EM_WH");
        ScreenON  = new Textures.BlockIcons.CustomIcon("iconsets/EM_WH_ACTIVE");
        super.registerIcons(aBlockIconRegister);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            return new ITexture[]{TecTechCasings.MOLECULAR.getTexture(), new TT_RenderedExtendedFacingTexture(aActive ? ScreenON : ScreenOFF)};
        }
        return new ITexture[]{TecTechCasings.MOLECULAR.getTexture()};
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece("main", stackSize, hintsOnly, 4, 4, 0);
    }
    @Override
    public int survivalConstruct(ItemStack stackSize, int budget, ISurvivalBuildEnvironment environment) {
        return survivalBuildPiece("main", stackSize, 4, 4, 0, budget, environment);
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_EM_wormhole> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
