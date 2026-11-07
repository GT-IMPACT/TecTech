package com.github.technus.tectech.thing.cover;

import static com.github.technus.tectech.mechanics.enderStorage.EnderWorldSavedData.bindEnderLinkTag;
import static com.github.technus.tectech.mechanics.enderStorage.EnderWorldSavedData.getEnderFluidContainer;
import static com.github.technus.tectech.mechanics.enderStorage.EnderWorldSavedData.getEnderLinkTag;

import java.util.UUID;

import com.github.technus.tectech.mechanics.enderStorage.EnderLinkTag;
import com.github.technus.tectech.thing.cover.gui.EnderFluidLinkCoverGui;
import com.google.common.io.ByteArrayDataInput;
import eu.usrv.yamcore.auxiliary.PlayerChatHelper;
import gregtech.api.covers.CoverContext;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.common.covers.Cover;
import gregtech.common.gui.modularui.cover.base.CoverBaseGui;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class GT_Cover_TM_EnderFluidLink extends Cover {

    private static final int L_PER_TICK = 8000;
    private static final int IMPORT_EXPORT_MASK = 0b0001;
    private static final int PUBLIC_PRIVATE_MASK = 0b0010;
    private int coverData;

    public GT_Cover_TM_EnderFluidLink(CoverContext context) {
        super(context, null);
    }

    private void transferFluid(IFluidHandler source, ForgeDirection sourceSide,
            IFluidHandler target, ForgeDirection targetSide) {
        FluidStack fluid = source.drain(sourceSide, L_PER_TICK, false);
        if (fluid != null) {
            int transferred = target.fill(targetSide, fluid, true);
            source.drain(sourceSide, transferred, true);
        }
    }

    @Override
    public void doCoverThings(byte inputRedstone, long timer) {
        ICoverable tile = getTile();
        if (!(tile instanceof IFluidHandler)) {
            return;
        }
        IFluidHandler tank = (IFluidHandler) tile;
        IFluidHandler enderTank = getEnderFluidContainer(getEnderLinkTag(tank));
        if (enderTank == null) {
            return;
        }
        if (isExporting()) {
            transferFluid(enderTank, ForgeDirection.UNKNOWN, tank, coverSide);
        } else {
            transferFluid(tank, coverSide, enderTank, ForgeDirection.UNKNOWN);
        }
    }

    // Import/export is named from the ender channel's perspective, as in the original GUI.
    public boolean isExporting() {
        return (coverData & IMPORT_EXPORT_MASK) != 0;
    }

    public void setExporting(boolean exporting) {
        if (isValid()) {
            coverData = exporting ? coverData | IMPORT_EXPORT_MASK : coverData & ~IMPORT_EXPORT_MASK;
            markGuiChanged();
        }
    }

    public boolean isPrivateChannel() {
        return (coverData & PUBLIC_PRIVATE_MASK) != 0;
    }

    public String getFrequency() {
        EnderLinkTag tag = getChannel();
        return tag == null ? "" : tag.getFrequency();
    }

    private EnderLinkTag getChannel() {
        ICoverable tile = getTile();
        if (tile instanceof IFluidHandler && tile.isServerSide()) {
            return getEnderLinkTag((IFluidHandler) tile);
        }
        return null;
    }

    public void setFrequency(String frequency) {
        if (!isValid() || frequency == null || frequency.length() > 9) {
            return;
        }
        EnderLinkTag tag = getChannel();
        if (tag != null) {
            updateChannel(new EnderLinkTag(frequency, tag.getUUID()));
        }
    }

    public void setPrivateChannel(boolean privateChannel, EntityPlayer player) {
        if (!isValid() || player == null) {
            return;
        }
        EnderLinkTag tag = getChannel();
        UUID owner = privateChannel ? player.getUniqueID() : null;
        if (tag != null && updateChannel(new EnderLinkTag(tag.getFrequency(), owner))) {
            coverData = privateChannel ? coverData | PUBLIC_PRIVATE_MASK : coverData & ~PUBLIC_PRIVATE_MASK;
            markGuiChanged();
        }
    }

    private boolean updateChannel(EnderLinkTag tag) {
        ICoverable tile = getTile();
        if (!(tile instanceof IFluidHandler) || !tile.isServerSide()) {
            return false;
        }
        if (tag.getUUID() != null && (!(tile instanceof BaseMetaTileEntity)
                || !tag.getUUID().equals(((BaseMetaTileEntity) tile).getOwnerUuid()))) {
            return false;
        }
        bindEnderLinkTag((IFluidHandler) tile, tag);
        markGuiChanged();
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
    public void onCoverScrewdriverClick(EntityPlayer player, float x, float y, float z) {
        boolean exporting = isExporting();
        setExporting(!exporting);
        PlayerChatHelper.SendInfo(player, exporting ? "Ender Suction Engaged!" : "Ender Filling Engaged!");
    }

    @Override
    public int getMinimumTickRate() {
        return 1;
    }

    @Override
    public boolean hasCoverGUI() {
        return getTile() instanceof IFluidHandler;
    }

    @Override
    protected CoverBaseGui<?> getCoverGui() {
        return new EnderFluidLinkCoverGui(this);
    }

    @Override
    public int getLegacyData() {
        return coverData;
    }

    @Override
    public void setLegacyData(int value) {
        coverData = value;
    }

    @Override
    protected NBTBase saveDataToNbt() {
        return new NBTTagInt(coverData);
    }

    @Override
    protected void readDataFromNbt(NBTBase nbt) {
        coverData = nbt instanceof NBTTagInt ? ((NBTTagInt) nbt).func_150287_d() : 0;
    }

    @Override
    protected void writeDataToByteBuf(ByteBuf buffer) {
        buffer.writeInt(coverData);
    }

    @Override
    protected void readDataFromPacket(ByteArrayDataInput input) {
        coverData = input.readInt();
    }
}
