package com.github.technus.tectech.mechanics.tesla;

import com.github.technus.tectech.util.Vec3Impl;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class TeslaCoverConnection implements ITeslaConnectableSimple {
    private final IGregTechTileEntity IGT;
    private final byte teslaReceptionCapability;

    public TeslaCoverConnection(IGregTechTileEntity IGT, byte teslaReceptionCapability) {
        this.IGT = IGT;
        this.teslaReceptionCapability = teslaReceptionCapability;
    }

    @Override
    public byte getTeslaReceptionCapability() {
        return teslaReceptionCapability;
    }

    @Override
    public float getTeslaReceptionCoefficient() {
        return 1;
    }

    @Override
    public boolean isTeslaReadyToReceive() {
        return true;
    }

    @Override
    public long getTeslaStoredEnergy() {
        return IGT.getStoredEU();
    }

    @Override
    public Vec3Impl getTeslaPosition() {
        return new Vec3Impl(IGT);
    }

    @Override
    public Integer getTeslaDimension() {
        return IGT.getWorld().provider.dimensionId;
    }

    @Override
    public boolean teslaInjectEnergy(long teslaVoltageInjected) {
        //Same as in the microwave transmitters, this does not account for amp limits
        return IGT.injectEnergyUnits((byte) 1, teslaVoltageInjected, 1L) > 0L;
    }
}