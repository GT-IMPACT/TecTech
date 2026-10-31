package com.github.technus.tectech.thing.metaTileEntity.multi.base;

import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_Capacitor;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_DynamoMulti;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_EnergyMulti;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_Holder;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputData;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputDataItems;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputElemental;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OutputData;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OutputDataItems;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OutputElemental;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_OverflowElemental;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_Param;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_Rack;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_Uncertainty;
import com.github.technus.tectech.thing.metaTileEntity.multi.GT_MetaTileEntity_EM_computer;
import com.github.technus.tectech.thing.metaTileEntity.multi.GT_MetaTileEntity_EM_dataBank;
import com.github.technus.tectech.thing.metaTileEntity.multi.GT_MetaTileEntity_EM_research;
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
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Muffler;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Output;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_OutputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import net.minecraft.util.StatCollector;

public enum TecTechHatchElement implements IHatchElement {
    Classic {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_Input
                    || tile instanceof GT_MetaTileEntity_Hatch_InputBus
                    || tile instanceof GT_MetaTileEntity_Hatch_Output
                    || tile instanceof GT_MetaTileEntity_Hatch_OutputBus
                    || tile instanceof GT_MetaTileEntity_Hatch_Energy
                    || tile instanceof GT_MetaTileEntity_Hatch_Dynamo
                    || tile instanceof GT_MetaTileEntity_Hatch_Maintenance
                    || tile instanceof GT_MetaTileEntity_Hatch_Muffler
                    || tile instanceof GT_MetaTileEntity_Hatch_Param
                    || tile instanceof GT_MetaTileEntity_Hatch_Uncertainty
                    || tile instanceof GT_MetaTileEntity_Hatch_EnergyMulti
                    || tile instanceof gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_EnergyMulti
                    || tile instanceof GT_MetaTileEntity_Hatch_DynamoMulti
                    || tile instanceof gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_DynamoMulti
                    || tile instanceof GT_MetaTileEntity_Hatch_InputData
                    || tile instanceof GT_MetaTileEntity_Hatch_OutputData;
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.mInputHatches.size()
                    + em.mInputBusses.size()
                    + em.mOutputHatches.size()
                    + em.mOutputBusses.size()
                    + em.mEnergyHatches.size()
                    + em.mDynamoHatches.size()
                    + em.mMaintenanceHatches.size()
                    + em.mMufflerHatches.size()
                    + em.eParamHatches.size()
                    + em.eUncertainHatches.size()
                    + em.eEnergyMulti.size()
                    + em.gtEnergyMulti.size()
                    + em.eDynamoMulti.size()
                    + em.gtDynamoMulti.size()
                    + em.eInputData.size()
                    + em.eOutputData.size();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.addClassicToMachineList(tile, textureIndex);
        }
    },
    Elemental {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_InputElemental
                    || tile instanceof GT_MetaTileEntity_Hatch_OutputElemental
                    || tile instanceof GT_MetaTileEntity_Hatch_OverflowElemental;
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.eInputHatches.size()
                    + em.eOutputHatches.size()
                    + em.eMufflerHatches.size();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.addElementalToMachineList(tile, textureIndex);
        }
    },
    ElementalInput {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_InputElemental;
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.eInputHatches.size();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.addElementalInputToMachineList(tile, textureIndex);
        }
    },
    ElementalOutput {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_OutputElemental;
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.eOutputHatches.size();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.addElementalOutputToMachineList(tile, textureIndex);
        }
    },
    ElementalOverflow {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_OverflowElemental;
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.eMufflerHatches.size();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.addElementalMufflerToMachineList(tile, textureIndex);
        }
    },
    EnergyIO {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_Energy
                    || tile instanceof GT_MetaTileEntity_Hatch_EnergyMulti
                    || tile instanceof gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_EnergyMulti
                    || tile instanceof GT_MetaTileEntity_Hatch_Dynamo
                    || tile instanceof GT_MetaTileEntity_Hatch_DynamoMulti
                    || tile instanceof gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_DynamoMulti;
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.mEnergyHatches.size()
                    + em.eEnergyMulti.size()
                    + em.gtEnergyMulti.size()
                    + em.mDynamoHatches.size()
                    + em.eDynamoMulti.size()
                    + em.gtDynamoMulti.size();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.addEnergyIOToMachineList(tile, textureIndex);
        }
    },
    All {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_Input
                    || tile instanceof GT_MetaTileEntity_Hatch_InputBus
                    || tile instanceof GT_MetaTileEntity_Hatch_Output
                    || tile instanceof GT_MetaTileEntity_Hatch_OutputBus
                    || tile instanceof GT_MetaTileEntity_Hatch_Energy
                    || tile instanceof GT_MetaTileEntity_Hatch_Dynamo
                    || tile instanceof GT_MetaTileEntity_Hatch_Maintenance
                    || tile instanceof GT_MetaTileEntity_Hatch_Muffler
                    || tile instanceof GT_MetaTileEntity_Hatch_InputElemental
                    || tile instanceof GT_MetaTileEntity_Hatch_OutputElemental
                    || tile instanceof GT_MetaTileEntity_Hatch_Param
                    || tile instanceof GT_MetaTileEntity_Hatch_Uncertainty
                    || tile instanceof GT_MetaTileEntity_Hatch_OverflowElemental
                    || tile instanceof GT_MetaTileEntity_Hatch_EnergyMulti
                    || tile instanceof gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_EnergyMulti
                    || tile instanceof GT_MetaTileEntity_Hatch_DynamoMulti
                    || tile instanceof gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_DynamoMulti
                    || tile instanceof GT_MetaTileEntity_Hatch_InputData
                    || tile instanceof GT_MetaTileEntity_Hatch_OutputData;
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.mInputHatches.size()
                    + em.mInputBusses.size()
                    + em.mOutputHatches.size()
                    + em.mOutputBusses.size()
                    + em.mEnergyHatches.size()
                    + em.mDynamoHatches.size()
                    + em.mMaintenanceHatches.size()
                    + em.mMufflerHatches.size()
                    + em.eInputHatches.size()
                    + em.eOutputHatches.size()
                    + em.eParamHatches.size()
                    + em.eUncertainHatches.size()
                    + em.eMufflerHatches.size()
                    + em.eEnergyMulti.size()
                    + em.gtEnergyMulti.size()
                    + em.eDynamoMulti.size()
                    + em.gtDynamoMulti.size()
                    + em.eInputData.size()
                    + em.eOutputData.size();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.addToMachineList(tile, textureIndex);
        }
    },
    Uncertainty {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_Uncertainty;
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.eUncertainHatches.size();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            GT_MetaTileEntity_MultiblockBase_EM<?> em = (GT_MetaTileEntity_MultiblockBase_EM<?>) machine;
            return em.addUncertainToMachineList(tile, textureIndex);
        }
    },
    Rack {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_Rack;
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            return ((GT_MetaTileEntity_EM_computer) machine).getRackCount();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            return ((GT_MetaTileEntity_EM_computer) machine).addRackToMachineList(tile, textureIndex);
        }
    },
    Holder {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_Holder;
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            return ((GT_MetaTileEntity_EM_research) machine).getHolderCount();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            return ((GT_MetaTileEntity_EM_research) machine).addHolderToMachineList(tile, textureIndex);
        }
    },
    DataBank {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_OutputDataItems
                    || (tile instanceof GT_MetaTileEntity_Hatch_DataAccess && !(tile instanceof GT_MetaTileEntity_Hatch_InputDataItems));
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            return ((GT_MetaTileEntity_EM_dataBank) machine).getDataBankCount();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            return ((GT_MetaTileEntity_EM_dataBank) machine).addDataBankHatchToMachineList(tile, textureIndex);
        }
    },
    Capacitor {
        @Override
        public boolean matches(IMetaTileEntity tile) {
            return tile instanceof GT_MetaTileEntity_Hatch_Capacitor
                    || tile instanceof GT_MetaTileEntity_Hatch_Maintenance
                    || tile instanceof GT_MetaTileEntity_Hatch_Energy
                    || tile instanceof GT_MetaTileEntity_Hatch_EnergyMulti
                    || tile instanceof GT_MetaTileEntity_Hatch_Dynamo
                    || tile instanceof GT_MetaTileEntity_Hatch_DynamoMulti
                    || tile instanceof GT_MetaTileEntity_Hatch_Param
                    || tile instanceof GT_MetaTileEntity_Hatch_Input
                    || tile instanceof GT_MetaTileEntity_Hatch_Output;
        }

        @Override
        public int count(GT_MetaTileEntity_MultiBlockBase machine) {
            return ((GT_MetaTileEntity_TM_teslaCoil) machine).getCapacitorCount();
        }

        @Override
        public boolean add(GT_MetaTileEntity_MultiBlockBase machine, IGregTechTileEntity tile, int textureIndex) {
            return ((GT_MetaTileEntity_TM_teslaCoil) machine).addCapacitorToMachineList(tile, textureIndex);
        }
    };

    @Override
    public String getDescription() {
        return StatCollector.translateToLocal("tt.structure.hatch." + name());
    }
}
