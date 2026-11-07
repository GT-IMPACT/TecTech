package com.github.technus.tectech.loader.thing;

import com.github.technus.tectech.TecTech;
import com.github.technus.tectech.thing.cover.GT_Cover_TM_EnderFluidLink;
import com.github.technus.tectech.thing.cover.GT_Cover_TM_PowerPassUpgrade;
import com.github.technus.tectech.thing.cover.GT_Cover_TM_TeslaCoil;
import com.github.technus.tectech.thing.cover.GT_Cover_TM_TeslaCoil_Ultimate;
import com.github.technus.tectech.thing.item.EnderFluidLinkCover;
import com.github.technus.tectech.thing.item.PowerPassUpgradeCover;
import com.github.technus.tectech.thing.item.TeslaCoilCover;
import gregtech.api.covers.CoverPlacer;
import gregtech.api.covers.CoverRegistry;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.objects.GT_RenderedTexture;
import net.minecraft.item.ItemStack;


public class CoverLoader implements Runnable {

    @Override
    public void run() {
        final IIconContainer TESLA_OVERLAY = new Textures.BlockIcons.CustomIcon("iconsets/TESLA_OVERLAY");
        final IIconContainer TESLA_OVERLAY_ULTIMATE = new Textures.BlockIcons.CustomIcon("iconsets/TESLA_OVERLAY_ULTIMATE");
        final IIconContainer ENDERFLUIDLINK_OVERLAY = new Textures.BlockIcons.CustomIcon("iconsets/ENDERFLUIDLINK_OVERLAY");
        final IIconContainer POWERPASSUPGRADE_OVERLAY = new Textures.BlockIcons.CustomIcon("iconsets/POWERPASSUPGRADE_OVERLAY");

        CoverRegistry.registerCover(
            new ItemStack(TeslaCoilCover.INSTANCE, 1, 0),
            new GT_RenderedTexture(TESLA_OVERLAY),
            GT_Cover_TM_TeslaCoil::new
        );
        CoverRegistry.registerCover(
            new ItemStack(TeslaCoilCover.INSTANCE, 1, 1),
            new GT_RenderedTexture(TESLA_OVERLAY_ULTIMATE),
            GT_Cover_TM_TeslaCoil_Ultimate::new
        );
        CoverRegistry.registerCover(
            new ItemStack(EnderFluidLinkCover.INSTANCE, 1, 0),
            new GT_RenderedTexture(ENDERFLUIDLINK_OVERLAY),
            GT_Cover_TM_EnderFluidLink::new
        );
        CoverRegistry.registerCover(
            new ItemStack(PowerPassUpgradeCover.INSTANCE, 1, 0),
            new GT_RenderedTexture(POWERPASSUPGRADE_OVERLAY),
            GT_Cover_TM_PowerPassUpgrade::new,
            CoverPlacer.builder()
                .onlyPlaceIf((side, stack, tile) -> GT_Cover_TM_PowerPassUpgrade.isCoverPlaceable(tile))
                .build()
        );
        TecTech.LOGGER.info("Cover functionality registered");
    }
}
