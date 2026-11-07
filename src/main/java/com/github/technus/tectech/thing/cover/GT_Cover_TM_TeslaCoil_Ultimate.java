package com.github.technus.tectech.thing.cover;

import gregtech.api.covers.CoverContext;
import net.minecraftforge.fluids.Fluid;

public class GT_Cover_TM_TeslaCoil_Ultimate extends GT_Cover_TM_TeslaCoil {

    public GT_Cover_TM_TeslaCoil_Ultimate(CoverContext context) {
        super(context);
    }

    @Override
    public boolean letsEnergyOut() {
        return true;
    }

    @Override
    public boolean letsItemsIn(int slot) {
        return true;
    }

    @Override
    public boolean letsItemsOut(int slot) {
        return true;
    }

    @Override
    public boolean letsFluidIn(Fluid fluid) {
        return true;
    }

    @Override
    public boolean letsFluidOut(Fluid fluid) {
        return true;
    }

    @Override
    public byte getTeslaReceptionCapability() {
        return 1;
    }
}
