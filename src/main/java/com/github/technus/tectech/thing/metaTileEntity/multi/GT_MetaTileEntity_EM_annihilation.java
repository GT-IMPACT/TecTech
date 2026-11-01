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
public class GT_MetaTileEntity_EM_annihilation extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_annihilation> {
    //region structure
    private static final IStructureDefinition<GT_MetaTileEntity_EM_annihilation> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_annihilation>builder()
                    .addShape("main", transpose(new String[][]{
                            {"           ", "   AB BA   ", "   AB BA   ", "    BBB    ", " A   A   A ", "AA   F   AA", " A   A   A ", "    BBB    ", "   AB BA   ", "   AB BA   ", "           "},
                            {"           ", "   AB BA   ", " EECEEECEE ", " EGGEEEGGE ", "ACGGGEGGGCA", "AEGGGDGGGEA", "ACGGGEGGGCA", " EGGEEEGGE ", " EECEEECEE ", "   AB BA   ", "           "},
                            {"           ", "    B B    ", " EGGEGEGGE ", " G       G ", " G       G ", " G       G ", " G       G ", " G       G ", " EGGEGEGGE ", "    B B    ", "           "},
                            {"    AAA    ", "AA  BAB  AA", "ACGGEGEGGCA", " G       G ", " G       G ", " G       G ", " G       G ", " G       G ", "ACGGEGEGGCA", "AA  BAB  AA", "    AAA    "},
                            {"   AHHHA   ", "BBBBBBBBBBB", "BEEEDEDEEEB", "BE       EB", " G       G ", " G       G ", " G       G ", "BE       EB", "BEEEDEDEEEB", "BBBBBBBBBBB", "   AHHHA   "},
                            {"   AH~HA   ", "   ABBBA   ", " EGGEEEGGE ", "BE       EB", "AE       EA", "FD       DF", "AE       EA", "BE       EB", " EGGEEEGGE ", "   ABBBA   ", "   AHHHA   "},
                            {"   AHHHA   ", "BBBBBBBBBBB", "BEEEDEDEEEB", "BE       EB", " G       G ", " G       G ", " G       G ", "BE       EB", "BEEEDEDEEEB", "BBBBBBBBBBB", "   AHHHA   "},
                            {"    AAA    ", "AA  BAB  AA", "ACGGEGEGGCA", " G       G ", " G       G ", " G       G ", " G       G ", " G       G ", "ACGGEGEGGCA", "AA  BAB  AA", "    AAA    "},
                            {"           ", "    B B    ", " EGGEGEGGE ", " G       G ", " G       G ", " G       G ", " G       G ", " G       G ", " EGGEGEGGE ", "    B B    ", "           "},
                            {"           ", "   AB BA   ", " EECEEECEE ", " EGGEEEGGE ", "ACGGGEGGGCA", "AEGGGDGGGEA", "ACGGGEGGGCA", " EGGEEEGGE ", " EECEEECEE ", "   AB BA   ", "           "},
                            {"           ", "   AB BA   ", "   AB BA   ", "    BBB    ", " A   A   A ", "AA   F   AA", " A   A   A ", "    BBB    ", "   AB BA   ", "   AB BA   ", "           "}
                    }))
                    .addElement('A', TecTechCasings.MOLECULAR.asElement())
                    .addElement('B', TecTechCasings.ADVANCED_MOLECULAR.asElement())
                    .addElement('C', TecTechCasings.CONTAINMENT_FIELD.asElement())
                    .addElement('D', TecTechCasings.TELEPORTATION.asElement())
                    .addElement('E', TecTechCasings.ULTIMATE_MOLECULAR.asElement())
                    .addElement('F', withHint(buildHatchAdder(GT_MetaTileEntity_EM_annihilation.class).allow(TecTechHatchElement.Elemental).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.LIME))
                    .addElement('G', ofBlock(QuantumGlassBlock.INSTANCE, 0))
                    .addElement('H', withHint(buildHatchAdder(GT_MetaTileEntity_EM_annihilation.class).allow(TecTechHatchElement.Classic).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.GREEN))
                    .build();
    //region variables
    private static Textures.BlockIcons.CustomIcon ScreenOFF;
    //endregion
    private static Textures.BlockIcons.CustomIcon ScreenON;
    //endregion

    public GT_MetaTileEntity_EM_annihilation(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_annihilation(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_annihilation(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return checkPiece("main", 5, 5, 0);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder();
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.annihilation.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.annihilation.desc.1"));
        tooltip.beginStructureBlock(11, 11, 11, false);
        tooltip.addCasingInfoRange(TecTechCasings.MOLECULAR, 116, 120, false);
        tooltip.addCasingInfoExactly(TecTechCasings.ADVANCED_MOLECULAR, 122, false);
        tooltip.addCasingInfoExactly(TecTechCasings.CONTAINMENT_FIELD, 24, false);
        tooltip.addCasingInfoExactly(TecTechCasings.TELEPORTATION, 12, false);
        tooltip.addCasingInfoExactly(TecTechCasings.ULTIMATE_MOLECULAR, 134, false);
        tooltip.addCasingInfoRange(TecTechCasings.HIGH_POWER, 0, 17, false);
        tooltip.addOtherStructurePart(152, QuantumGlassBlock.INSTANCE.getLocalizedName(), translateToLocal("tt.structure.hologram"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.casing_ranges"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_hatches"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.classic_data"));
        tooltip.addStructureInfo(translateToLocal("tt.structure.elemental_hatches"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.annihilation.hint.0"), HintColor.GREEN);
        tooltip.addStructureHint(translateToLocal("gt.blockmachines.multimachine.em.annihilation.hint.1"), HintColor.LIME);
        return tooltip;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister aBlockIconRegister) {
        ScreenOFF = new Textures.BlockIcons.CustomIcon("iconsets/EM_ANNIHILATION");
        ScreenON  = new Textures.BlockIcons.CustomIcon("iconsets/EM_ANNIHILATION_ACTIVE");
        super.registerIcons(aBlockIconRegister);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            return new ITexture[]{TecTechCasings.ULTIMATE_MOLECULAR.getTexture(), new TT_RenderedExtendedFacingTexture(aActive ? ScreenON : ScreenOFF)};
        }
        return new ITexture[]{TecTechCasings.ULTIMATE_MOLECULAR.getTexture()};
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece("main", stackSize, hintsOnly, 5, 5, 0);
    }
    @Override
    public int survivalConstruct(ItemStack stackSize, int budget, ISurvivalBuildEnvironment environment) {
        return survivalBuildPiece("main", stackSize, 5, 5, 0, budget, environment);
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_EM_annihilation> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
