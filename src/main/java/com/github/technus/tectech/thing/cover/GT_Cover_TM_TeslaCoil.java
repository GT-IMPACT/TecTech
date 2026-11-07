package com.github.technus.tectech.thing.cover;

import com.github.technus.tectech.mechanics.tesla.TeslaCoverConnection;
import gregtech.api.covers.CoverContext;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.util.GT_Utility;
import gregtech.common.covers.Cover;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

import static com.github.technus.tectech.mechanics.tesla.ITeslaConnectable.TeslaUtil.teslaSimpleNodeSetAdd;
import static ic2.api.info.Info.DMG_ELECTRIC;

public class GT_Cover_TM_TeslaCoil extends Cover {

    public GT_Cover_TM_TeslaCoil(CoverContext context) {
        super(context, null);
    }

    @Override
    public void doCoverThings(byte inputRedstone, long timer) {
        ICoverable tile = getTile();
        if (tile == null) {
            return;
        }
        if (coverSide == ForgeDirection.UP || tile.getEUCapacity() > 0) {
            //Makes sure we're on the list
            teslaSimpleNodeSetAdd(new TeslaCoverConnection(tile.getIGregTechTileEntityOffset(0, 0, 0),
                    getTeslaReceptionCapability()));
        }
    }

    @Override
    public String getDescription() {
        return "Do not attempt to use screwdriver!";//TODO Translation support
    }

    @Override
    public boolean letsEnergyIn() {
        return true;
    }

    @Override
    public void onCoverScrewdriverClick(EntityPlayer player, float x, float y, float z) {
        ICoverable tile = getTile();
        //Shock a non-hazmat player if they dare stuff a screwdriver into one of these
        if (tile != null && tile.getStoredEU() > 0 && !GT_Utility.isWearingFullElectroHazmat(player)) {
            player.attackEntityFrom(DMG_ELECTRIC, 20);
        }
    }

    @Override
    public int getMinimumTickRate() {
        //It updates once every 200 ticks, so once every 10 seconds
        return 200;
    }

    public byte getTeslaReceptionCapability() {
        return 2;
    }
}
