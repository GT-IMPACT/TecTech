package com.github.technus.tectech.thing.metaTileEntity.multi.base;

import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_Capacitor;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputData;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputDataItems;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OutputData;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OutputDataItems;
import com.github.technus.tectech.thing.metaTileEntity.multi.GT_MetaTileEntity_EM_dataBank;
import com.github.technus.tectech.thing.metaTileEntity.multi.GT_MetaTileEntity_TM_teslaCoil;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_DataAccess;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Dynamo;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Energy;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Input;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Maintenance;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Output;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_OutputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import net.minecraft.util.StatCollector;

/** Construction requests count individual functions, never a union of unrelated hatch types. */
public enum TecTechAutoHatch implements IHatchElement {
    Maintenance(tile -> tile instanceof GT_MetaTileEntity_Hatch_Maintenance,
            machine -> machine.mMaintenanceHatches.size(), TecTechHatchElement.Classic),
    PowerInput(tile -> tile instanceof GT_MetaTileEntity_Hatch_Energy
            || tile instanceof com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_EnergyMulti
            || tile instanceof gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_EnergyMulti,
            machine -> machine.mEnergyHatches.size() + machine.eEnergyMulti.size() + machine.gtEnergyMulti.size(),
            TecTechHatchElement.Classic),
    PowerOutput(tile -> tile instanceof GT_MetaTileEntity_Hatch_Dynamo
            || tile instanceof com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_DynamoMulti
            || tile instanceof gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_DynamoMulti,
            machine -> machine.mDynamoHatches.size() + machine.eDynamoMulti.size() + machine.gtDynamoMulti.size(),
            TecTechHatchElement.Classic),
    ItemInput(tile -> tile instanceof GT_MetaTileEntity_Hatch_InputBus,
            machine -> machine.mInputBusses.size(), TecTechHatchElement.Classic),
    ItemOutput(tile -> tile instanceof GT_MetaTileEntity_Hatch_OutputBus,
            machine -> machine.mOutputBusses.size(), TecTechHatchElement.Classic),
    FluidInput(tile -> tile instanceof GT_MetaTileEntity_Hatch_Input,
            machine -> machine.mInputHatches.size(), TecTechHatchElement.Classic),
    FluidOutput(tile -> tile instanceof GT_MetaTileEntity_Hatch_Output,
            machine -> machine.mOutputHatches.size(), TecTechHatchElement.Classic),
    DataInput(tile -> tile instanceof GT_MetaTileEntity_Hatch_InputData,
            machine -> machine.eInputData.size(), TecTechHatchElement.Classic),
    DataOutput(tile -> tile instanceof GT_MetaTileEntity_Hatch_OutputData,
            machine -> machine.eOutputData.size(), TecTechHatchElement.Classic),
    DataAccess(tile -> tile instanceof GT_MetaTileEntity_Hatch_DataAccess
            && !(tile instanceof GT_MetaTileEntity_Hatch_InputDataItems),
            machine -> ((GT_MetaTileEntity_EM_dataBank) machine).getDataAccessCount(), TecTechHatchElement.DataBank),
    RecipeDataOutput(tile -> tile instanceof GT_MetaTileEntity_Hatch_OutputDataItems,
            machine -> ((GT_MetaTileEntity_EM_dataBank) machine).getDataBankCount(), TecTechHatchElement.DataBank),
    Capacitor(tile -> tile instanceof GT_MetaTileEntity_Hatch_Capacitor,
            machine -> ((GT_MetaTileEntity_TM_teslaCoil) machine).getCapacitorHatchCount(), TecTechHatchElement.Capacitor);

    private final Predicate<IMetaTileEntity> matcher;
    private final ToIntFunction<GT_MetaTileEntity_MultiblockBase_EM<?>> counter;
    private final IHatchElement accepter;

    TecTechAutoHatch(Predicate<IMetaTileEntity> matcher,
            ToIntFunction<GT_MetaTileEntity_MultiblockBase_EM<?>> counter, IHatchElement accepter) {
        this.matcher = matcher;
        this.counter = counter;
        this.accepter = accepter;
    }

    @Override
    public boolean matches(IMetaTileEntity tile) {
        return matcher.test(tile);
    }

    @Override
    public int count(GT_MetaTileEntity_MultiBlockBase machine) {
        return counter.applyAsInt((GT_MetaTileEntity_MultiblockBase_EM<?>) machine);
    }

    @Override
    public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
        return accepter.add(machine, tile, textureIndex);
    }

    @Override
    public String getDescription() {
        return StatCollector.translateToLocal("tt.structure.auto." + name());
    }
}
