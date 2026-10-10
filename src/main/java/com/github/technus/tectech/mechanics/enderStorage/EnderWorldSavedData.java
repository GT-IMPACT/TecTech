package com.github.technus.tectech.mechanics.enderStorage;

import static com.github.technus.tectech.Reference.MODID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.IFluidHandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;

public class EnderWorldSavedData extends WorldSavedData {

    private static EnderWorldSavedData INSTANCE;

    private static final String DATA_NAME = MODID + "_EnderWorldSavedData";
    private static final String ENDER_LIQUID_TAG_LINK = DATA_NAME + "_EnderLiquidTagLink";
    private static final String ENDER_LIQUID_TANK_LINK = DATA_NAME + "_EnderLiquidTankLink";
    private static final EnderLinkTag DEFAULT_LINK_TAG = new EnderLinkTag("", null);

    private Map<EnderLinkTag, EnderFluidContainer> EnderLiquidTagLink = new HashMap<>();
    private Map<EnderLinkTank, EnderLinkTag> EnderLiquidTankLink = new HashMap<>();
    private NBTTagCompound failedLoadData;

    public EnderWorldSavedData() {
        super(DATA_NAME);
    }

    public EnderWorldSavedData(String s) {
        super(s);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        try {
            if (nbtTagCompound.hasKey(ENDER_LIQUID_TAG_LINK) != nbtTagCompound.hasKey(ENDER_LIQUID_TANK_LINK)) {
                throw new InvalidObjectException("Incomplete ender fluid link data");
            }
            Map<EnderLinkTag, EnderFluidContainer> fluids = readMap(nbtTagCompound, ENDER_LIQUID_TAG_LINK,
                    EnderLinkTag.class, EnderFluidContainer.class);
            Map<EnderLinkTank, EnderLinkTag> tanks = readMap(nbtTagCompound, ENDER_LIQUID_TANK_LINK,
                    EnderLinkTank.class, EnderLinkTag.class);
            EnderLiquidTagLink = fluids;
            EnderLiquidTankLink = tanks;
            failedLoadData = null;
        } catch (IOException | ClassNotFoundException | RuntimeException error) {
            failedLoadData = (NBTTagCompound) nbtTagCompound.copy();
            LogManager.getLogger("TecTech").error("Cannot load ender fluid links; transfers disabled and original data preserved", error);
        }
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> readMap(NBTTagCompound nbt, String key, Class<K> keyType, Class<V> valueType)
            throws IOException, ClassNotFoundException {
        if (!nbt.hasKey(key)) return new HashMap<>();
        try (ObjectInputStream input = new EnderStorageInputStream(new ByteArrayInputStream(nbt.getByteArray(key)))) {
            Object data = input.readObject();
            if (!(data instanceof Map)) throw new InvalidObjectException("Invalid ender fluid link map: " + key);
            Map<?, ?> map = (Map<?, ?>) data;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!keyType.isInstance(entry.getKey()) || !valueType.isInstance(entry.getValue())) {
                    throw new InvalidObjectException("Invalid ender fluid link entry: " + key);
                }
            }
            return (Map<K, V>) map;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        if (failedLoadData != null) {
            if (failedLoadData.hasKey(ENDER_LIQUID_TAG_LINK)) {
                nbtTagCompound.setByteArray(ENDER_LIQUID_TAG_LINK, failedLoadData.getByteArray(ENDER_LIQUID_TAG_LINK));
            }
            if (failedLoadData.hasKey(ENDER_LIQUID_TANK_LINK)) {
                nbtTagCompound.setByteArray(ENDER_LIQUID_TANK_LINK, failedLoadData.getByteArray(ENDER_LIQUID_TANK_LINK));
            }
            return;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(EnderLiquidTagLink);
            oos.flush();
            byte[] data = bos.toByteArray();
            nbtTagCompound.setByteArray(ENDER_LIQUID_TAG_LINK, data);
        } catch (IOException ignored) {
            System.out.println("ENDER_LIQUID_TAG_LINK SAVE FAILED");
            System.out.println(ignored);
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(EnderLiquidTankLink);
            oos.flush();
            byte[] data = bos.toByteArray();
            nbtTagCompound.setByteArray(ENDER_LIQUID_TANK_LINK, data);
        } catch (IOException ignored) {
            System.out.println("ENDER_LIQUID_TANK_LINK SAVE FAILED");
            System.out.println(ignored);
        }
    }

    private static void loadInstance() {
        if (INSTANCE == null) {
            MapStorage storage = DimensionManager.getWorld(0).mapStorage;
            INSTANCE = (EnderWorldSavedData) storage.loadData(EnderWorldSavedData.class, DATA_NAME);
            if (INSTANCE == null) {
                INSTANCE = new EnderWorldSavedData();
                storage.setData(DATA_NAME, INSTANCE);
            }
        }
        if (INSTANCE.failedLoadData == null) INSTANCE.markDirty();
    }

    private static Map<EnderLinkTag, EnderFluidContainer> getEnderLiquidLink() {
        loadInstance();
        return INSTANCE.failedLoadData == null ? INSTANCE.EnderLiquidTagLink : null;
    }

    private static Map<EnderLinkTank, EnderLinkTag> getEnderLiquidTankLink() {
        loadInstance();
        return INSTANCE.failedLoadData == null ? INSTANCE.EnderLiquidTankLink : null;
    }

    public static EnderFluidContainer getEnderFluidContainer(EnderLinkTag tag) {
        Map<EnderLinkTag, EnderFluidContainer> fluids = getEnderLiquidLink();
        if (fluids == null || tag == null) return null;
        if (!fluids.containsKey(tag)) {
            fluids.put(tag, new EnderFluidContainer());
        }
        return fluids.get(tag);
    }

    public static EnderLinkTag getEnderLinkTag(IFluidHandler handler) {
        Map<EnderLinkTank, EnderLinkTag> tanks = getEnderLiquidTankLink();
        if (tanks == null) return null;
        EnderLinkTank tank = new EnderLinkTank(handler);
        if (!tanks.containsKey(tank)) {
            tanks.put(tank, DEFAULT_LINK_TAG);
        }
        return tanks.get(tank);
    }

    public static void bindEnderLinkTag(IFluidHandler handler, EnderLinkTag tag) {
        Map<EnderLinkTank, EnderLinkTag> tanks = getEnderLiquidTankLink();
        if (tanks == null || tag == null) return;
        EnderLinkTank tank = new EnderLinkTank(handler);
        tanks.put(tank, tag);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world.provider.dimensionId == 0) {
            INSTANCE = null;
        }
    }
}
