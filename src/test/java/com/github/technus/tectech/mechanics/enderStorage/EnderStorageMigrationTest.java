package com.github.technus.tectech.mechanics.enderStorage;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(ForgeTestRunner.class)
public class EnderStorageMigrationTest {
    // Fixtures were serialized by the original classes from commit 5f3b8e12.
    @BeforeClass
    public static void initializeMinecraft() {
        net.minecraft.init.Bootstrap.func_151354_b();
    }

    private static final UUID OWNER = UUID.fromString("12345678-1234-5678-1234-567812345678");
    private static final String FLUIDS = "tectech_EnderWorldSavedData_EnderLiquidTagLink";
    private static final String TANKS = "tectech_EnderWorldSavedData_EnderLiquidTankLink";

    private byte[] fixture(String name) throws Exception {
        try (java.io.InputStream input = getClass().getResourceAsStream("/enderStorage/" + name + ".base64")) {
            assertNotNull(input);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) != -1) bytes.write(buffer, 0, length);
            return Base64.getDecoder().decode(new String(bytes.toByteArray(), StandardCharsets.US_ASCII).trim());
        }
    }

    @Test
    public void migratesLegacyFluidsWithoutMergingChannels() throws Exception {
        try (ObjectInputStream input = new EnderStorageInputStream(new ByteArrayInputStream(fixture("legacy-fluids")))) {
            Map<?, ?> fluids = (Map<?, ?>) input.readObject();
            assertEquals(3, fluids.size());
            EnderFluidContainer water = (EnderFluidContainer) fluids.get(new EnderLinkTag("#FFFF0000", null));
            EnderFluidContainer lava = (EnderFluidContainer) fluids.get(new EnderLinkTag("#FF0000FF", OWNER));
            assertEquals("water", water.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.getFluid().getName());
            assertEquals(12345, water.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.amount);
            assertEquals("lava", lava.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.getFluid().getName());
            assertEquals(64000, lava.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.amount);
            assertNull(((EnderFluidContainer) fluids.get(new EnderLinkTag("#FFFFFFFF", null)))
                    .getTankInfo(ForgeDirection.UNKNOWN)[0].fluid);
        }
    }

    @Test
    public void migratesTankCoordinatesDimensionsAndOwners() throws Exception {
        try (ObjectInputStream input = new EnderStorageInputStream(new ByteArrayInputStream(fixture("legacy-tanks")))) {
            Map<?, ?> tanks = (Map<?, ?>) input.readObject();
            assertEquals(2, tanks.size());
            for (Map.Entry<?, ?> entry : tanks.entrySet()) {
                EnderLinkTag tag = (EnderLinkTag) entry.getValue();
                boolean privateChannel = OWNER.equals(tag.getUUID());
                assertEquals(privateChannel ? "#FF0000FF" : "#FFFF0000", tag.getFrequency());
                assertEquals(privateChannel ? 11 : 10, coordinate(entry.getKey(), "X"));
                assertEquals(64, coordinate(entry.getKey(), "Y"));
                assertEquals(20, coordinate(entry.getKey(), "Z"));
                assertEquals(privateChannel ? -1 : 0, coordinate(entry.getKey(), "D"));
            }
        }
    }

    private int coordinate(Object tank, String name) throws Exception {
        Field field = EnderLinkTank.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(tank);
    }

    @Test
    public void currentFluidFormatPreservesNbtAndEmptyContainers() throws Exception {
        for (boolean tagged : new boolean[] { false, true }) {
            FluidStack stack = new FluidStack(FluidRegistry.WATER, 23456);
            if (tagged) {
                stack.tag = new NBTTagCompound();
                stack.tag.setString("marker", "preserved");
            }
            EnderFluidContainer container = new EnderFluidContainer();
            container.fill(ForgeDirection.UNKNOWN, stack, true);
            FluidStack restored = ((EnderFluidContainer) roundTrip(container)).getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
            assertTrue(stack.isFluidEqual(restored));
            assertEquals(stack.amount, restored.amount);
        }
        assertNull(((EnderFluidContainer) roundTrip(new EnderFluidContainer())).getTankInfo(ForgeDirection.UNKNOWN)[0].fluid);
        assertEquals(new EnderLinkTag("current", OWNER), roundTrip(new EnderLinkTag("current", OWNER)));
    }

    private Object roundTrip(Object object) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) { output.writeObject(object); }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return input.readObject();
        }
    }

    @Test(expected = InvalidObjectException.class)
    public void missingFluidDoesNotSilentlyBecomeEmpty() throws Exception {
        EnderFluidContainer container = new EnderFluidContainer();
        container.fill(ForgeDirection.UNKNOWN, new FluidStack(FluidRegistry.WATER, 1000), true);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) { output.writeObject(container); }
        byte[] serialized = bytes.toByteArray();
        byte[] name = "water".getBytes(StandardCharsets.US_ASCII);
        boolean replaced = false;
        for (int i = 0; i <= serialized.length - name.length; i++) {
            boolean matches = true;
            for (int j = 0; j < name.length; j++) matches &= serialized[i + j] == name[j];
            if (matches) {
                System.arraycopy("other".getBytes(StandardCharsets.US_ASCII), 0, serialized, i, name.length);
                replaced = true;
                break;
            }
        }
        assertTrue(replaced);
        try (ObjectInputStream input = new EnderStorageInputStream(new ByteArrayInputStream(serialized))) {
            input.readObject();
        }
    }

    @Test
    public void incompleteSaveIsPreserved() throws Exception {
        NBTTagCompound incomplete = new NBTTagCompound();
        incomplete.setByteArray(FLUIDS, fixture("legacy-fluids"));
        EnderWorldSavedData data = new EnderWorldSavedData();
        data.readFromNBT(incomplete);
        NBTTagCompound saved = new NBTTagCompound();
        data.writeToNBT(saved);
        assertArrayEquals(incomplete.getByteArray(FLUIDS), saved.getByteArray(FLUIDS));
        assertFalse(saved.hasKey(TANKS));
    }

    private static final class UnreadableData implements Serializable {
        private static final long serialVersionUID = 1L;

        private void readObject(ObjectInputStream input) {
            throw new IllegalArgumentException("Simulated unchecked deserialization failure");
        }
    }

    @Test
    public void uncheckedReadFailureDoesNotEscapeWorldLoading() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(new UnreadableData());
        }
        NBTTagCompound broken = new NBTTagCompound();
        broken.setByteArray(FLUIDS, bytes.toByteArray());
        broken.setByteArray(TANKS, fixture("legacy-tanks"));
        EnderWorldSavedData data = new EnderWorldSavedData();
        data.readFromNBT(broken);
        NBTTagCompound saved = new NBTTagCompound();
        data.writeToNBT(saved);
        assertArrayEquals(broken.getByteArray(FLUIDS), saved.getByteArray(FLUIDS));
        assertArrayEquals(broken.getByteArray(TANKS), saved.getByteArray(TANKS));
    }

    @Test
    public void migratedWorldCanBeSavedAndLoadedAgain() throws Exception {
        NBTTagCompound legacy = new NBTTagCompound();
        legacy.setByteArray(FLUIDS, fixture("legacy-fluids"));
        legacy.setByteArray(TANKS, fixture("legacy-tanks"));
        EnderWorldSavedData data = new EnderWorldSavedData();
        data.readFromNBT(legacy);
        NBTTagCompound saved = new NBTTagCompound();
        data.writeToNBT(saved);
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(saved.getByteArray(FLUIDS)))) {
            assertEquals(3, ((Map<?, ?>) input.readObject()).size());
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(saved.getByteArray(TANKS)))) {
            assertEquals(2, ((Map<?, ?>) input.readObject()).size());
        }
    }

    @Test
    public void corruptSaveIsPreservedAndTransfersAreDisabled() throws Exception {
        NBTTagCompound broken = new NBTTagCompound();
        broken.setByteArray(FLUIDS, fixture("legacy-fluids"));
        broken.setByteArray(TANKS, new byte[] { 1, 2, 3 });
        EnderWorldSavedData data = new EnderWorldSavedData();
        data.readFromNBT(broken);
        NBTTagCompound saved = new NBTTagCompound();
        data.writeToNBT(saved);
        assertArrayEquals(broken.getByteArray(FLUIDS), saved.getByteArray(FLUIDS));
        assertArrayEquals(broken.getByteArray(TANKS), saved.getByteArray(TANKS));
        Field instance = EnderWorldSavedData.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        Object previous = instance.get(null);
        try {
            instance.set(null, data);
            assertNull(EnderWorldSavedData.getEnderFluidContainer(new EnderLinkTag("", null)));
            assertNull(EnderWorldSavedData.getEnderLinkTag(null));
            EnderWorldSavedData.bindEnderLinkTag(null, new EnderLinkTag("", null));
        } finally {
            instance.set(null, previous);
        }
    }
}
