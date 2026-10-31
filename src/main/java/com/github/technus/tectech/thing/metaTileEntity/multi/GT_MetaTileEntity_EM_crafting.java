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
public class GT_MetaTileEntity_EM_crafting extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_crafting> {
    public static final String crafter = "EM Crafting";
    //region structure
    private static final IStructureDefinition<GT_MetaTileEntity_EM_crafting> STRUCTURE_DEFINITION = StructureDefinition
            .<GT_MetaTileEntity_EM_crafting>builder()
            .addShape("main", transpose(new String[][]{
                    {" AAA ", "AAAAA", "A   A", "BBBBB", "BGCGB", "BGGGB", "BGCGB", "BBBBB", "A   A", "AAAAA", " AAA "},
                    {"AHHHA", "AAAAA", " FFF ", "BBBBB", "GGGGG", "GGGGG", "GGGGG", "BBBBB", " FFF ", "AAAAA", "AHHHA"},
                    {"AH~HA", "AAAAA", " FEF ", "BBEBB", "CGEGC", "GGDGG", "CGEGC", "BBEBB", " FEF ", "AAAAA", "AHHHA"},
                    {"AHHHA", "AAAAA", " FFF ", "BBBBB", "GGGGG", "GGGGG", "GGGGG", "BBBBB", " FFF ", "AAAAA", "AHHHA"},
                    {" AAA ", "AAAAA", "A   A", "BBBBB", "BGCGB", "BGGGB", "BGCGB", "BBBBB", "A   A", "AAAAA", " AAA "}
            }))
            .addElement('A', TecTechCasings.MOLECULAR.asElement())
            .addElement('B', TecTechCasings.ADVANCED_MOLECULAR.asElement())
            .addElement('C', TecTechCasings.CONTAINMENT_FIELD.asElement())
            .addElement('D', TecTechCasings.SPACETIME.asElement())
            .addElement('E', TecTechCasings.TELEPORTATION.asElement())
            .addElement('F', withHint(buildHatchAdder(GT_MetaTileEntity_EM_crafting.class).allow(TecTechHatchElement.Elemental).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.ORANGE))
            .addElement('G', ofBlock(QuantumGlassBlock.INSTANCE, 0))
            .addElement('H', withHint(buildHatchAdder(GT_MetaTileEntity_EM_crafting.class).allow(TecTechHatchElement.Classic).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.WHITE))
            .build();
    //endregion
    //region variables
    private static Textures.BlockIcons.CustomIcon ScreenOFF;
    private static Textures.BlockIcons.CustomIcon ScreenON;
    //endregion

    public GT_MetaTileEntity_EM_crafting(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_crafting(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_crafting(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return checkPiece("main", 2, 2, 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister aBlockIconRegister) {
        ScreenOFF = new Textures.BlockIcons.CustomIcon("iconsets/EM_CRAFTING");
        ScreenON  = new Textures.BlockIcons.CustomIcon("iconsets/EM_CRAFTING_ACTIVE");
        super.registerIcons(aBlockIconRegister);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder();
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.crafter.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.crafter.desc.1"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.crafter.hint.0", 1);
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.crafter.hint.1", 2);
        return tooltip;
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
        buildPiece("main", stackSize, hintsOnly, 2, 2, 0);
    }
    @Override
    public int survivalConstruct(ItemStack stackSize, int budget, ISurvivalBuildEnvironment environment) {
        return survivalBuildPiece("main", stackSize, 2, 2, 0, budget, environment);
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_EM_crafting> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

}
