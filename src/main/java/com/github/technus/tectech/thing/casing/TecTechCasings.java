package com.github.technus.tectech.thing.casing;

import gregtech.api.interfaces.ICasing;
import net.minecraft.block.Block;

public enum TecTechCasings implements ICasing {
    HIGH_POWER(BlockSet.ELEMENTAL, 0),
    COMPUTER(BlockSet.ELEMENTAL, 1),
    COMPUTER_HEAT_VENT(BlockSet.ELEMENTAL, 2),
    ADVANCED_COMPUTER(BlockSet.ELEMENTAL, 3),
    MOLECULAR(BlockSet.ELEMENTAL, 4),
    ADVANCED_MOLECULAR(BlockSet.ELEMENTAL, 5),
    CONTAINMENT_FIELD(BlockSet.ELEMENTAL, 6),
    MOLECULAR_COIL(BlockSet.ELEMENTAL, 7),
    HOLLOW(BlockSet.ELEMENTAL, 8),
    SPACETIME(BlockSet.ELEMENTAL, 9),
    TELEPORTATION(BlockSet.ELEMENTAL, 10),
    DIMENSIONAL_BRIDGE(BlockSet.ELEMENTAL, 11),
    ULTIMATE_MOLECULAR(BlockSet.ELEMENTAL, 12),
    ULTIMATE_ADVANCED_MOLECULAR(BlockSet.ELEMENTAL, 13),
    ULTIMATE_CONTAINMENT_FIELD(BlockSet.ELEMENTAL, 14),
    DEBUG_SIDES(BlockSet.ELEMENTAL, 15),
    TESLA_PRIMARY_REDSTONE(BlockSet.TESLA, 0),
    TESLA_PRIMARY_MV(BlockSet.TESLA, 1),
    TESLA_PRIMARY_HV(BlockSet.TESLA, 2),
    TESLA_PRIMARY_EV(BlockSet.TESLA, 3),
    TESLA_PRIMARY_IV(BlockSet.TESLA, 4),
    TESLA_PRIMARY_LUV(BlockSet.TESLA, 5),
    TESLA_BASE(BlockSet.TESLA, 6),
    TESLA_TOROID(BlockSet.TESLA, 7),
    TESLA_SECONDARY(BlockSet.TESLA, 8),
    TESLA_PRIMARY_ZPM(BlockSet.TESLA, 9);

    private final BlockSet blockSet;
    private final int metadata;

    TecTechCasings(BlockSet blockSet, int metadata) {
        this.blockSet = blockSet;
        this.metadata = metadata;
    }

    @Override
    public Block getBlock() {
        return blockSet == BlockSet.ELEMENTAL ? TT_Container_Casings.sBlockCasingsTT : TT_Container_Casings.sBlockCasingsBA0;
    }

    @Override
    public int getMetadata() {
        return metadata;
    }

    @Override
    public int getTextureId() {
        return (blockSet == BlockSet.ELEMENTAL ? GT_Block_CasingsTT.textureOffset : GT_Block_CasingsBA0.textureOffset) + metadata;
    }

    public static TecTechCasings teslaWinding(int metadata) {
        switch (metadata) {
            case 0:
                return TESLA_PRIMARY_REDSTONE;
            case 1:
                return TESLA_PRIMARY_MV;
            case 2:
                return TESLA_PRIMARY_HV;
            case 3:
                return TESLA_PRIMARY_EV;
            case 4:
                return TESLA_PRIMARY_IV;
            case 5:
                return TESLA_PRIMARY_LUV;
            case 9:
                return TESLA_PRIMARY_ZPM;
            default:
                return null;
        }
    }

    private enum BlockSet {
        ELEMENTAL,
        TESLA
    }
}
