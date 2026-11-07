package com.github.technus.tectech.thing.cover;

import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import gregtech.api.covers.CoverContext;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.common.covers.Cover;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class GT_Cover_TM_PowerPassUpgrade extends Cover {

    public GT_Cover_TM_PowerPassUpgrade(CoverContext context) {
        super(context, null);
    }

    public static boolean isCoverPlaceable(ICoverable tile) {
        GT_MetaTileEntity_MultiblockBase_EM<?> controller = getController(tile);
        return controller != null && !controller.ePowerPassCover;
    }

    private static GT_MetaTileEntity_MultiblockBase_EM<?> getController(ICoverable tile) {
        if (tile instanceof IGregTechTileEntity) {
            Object controller = ((IGregTechTileEntity) tile).getMetaTileEntity();
            if (controller instanceof GT_MetaTileEntity_MultiblockBase_EM<?>) {
                return (GT_MetaTileEntity_MultiblockBase_EM<?>) controller;
            }
        }
        return null;
    }

    @Override
    public void onPlayerAttach(EntityPlayer player, ItemStack coverItem) {
        GT_MetaTileEntity_MultiblockBase_EM<?> controller = getController(getTile());
        if (controller != null) {
            controller.ePowerPassCover = true;
            controller.ePowerPass = true;
        }
    }

    @Override
    public void onCoverRemoval() {
        GT_MetaTileEntity_MultiblockBase_EM<?> controller = getController(getTile());
        if (controller != null) {
            controller.ePowerPassCover = false;
            controller.ePowerPass = false;
        }
    }
}
