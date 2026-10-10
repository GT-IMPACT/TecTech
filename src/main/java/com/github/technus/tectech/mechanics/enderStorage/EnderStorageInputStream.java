package com.github.technus.tectech.mechanics.enderStorage;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.UUID;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/** Reads the pre-refactor Java serialization without changing the current save format. */
final class EnderStorageInputStream extends ObjectInputStream {

    EnderStorageInputStream(InputStream input) throws IOException {
        super(input);
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass descriptor = super.readClassDescriptor();
        if (descriptor.getName().equals(EnderLinkTag.class.getName())
                && descriptor.getSerialVersionUID() == 7963418567441731842L) {
            return legacyDescriptor(descriptor, LegacyTag.class);
        }
        if (descriptor.getName().equals(EnderFluidContainer.class.getName())
                && descriptor.getSerialVersionUID() == 272378482731905850L) {
            return legacyDescriptor(descriptor, LegacyFluid.class);
        }
        return descriptor;
    }

    private static ObjectStreamClass legacyDescriptor(ObjectStreamClass source, Class<?> legacy) throws IOException {
        ObjectStreamClass target = ObjectStreamClass.lookup(legacy);
        ObjectStreamField[] sourceFields = source.getFields();
        ObjectStreamField[] targetFields = target.getFields();
        if (sourceFields.length != targetFields.length) throw new InvalidClassException(source.getName(), "Unknown legacy fields");
        for (int i = 0; i < sourceFields.length; i++) {
            if (!sourceFields[i].getName().equals(targetFields[i].getName())
                    || sourceFields[i].getTypeCode() != targetFields[i].getTypeCode()
                    || !java.util.Objects.equals(sourceFields[i].getTypeString(), targetFields[i].getTypeString())) {
                throw new InvalidClassException(source.getName(), "Unknown legacy fields");
            }
        }
        return target;
    }

    private static final class LegacyTag implements Serializable {
        private static final long serialVersionUID = 1L;
        private Color color;
        private UUID player;

        private Object readResolve() throws InvalidObjectException {
            if (color == null) throw new InvalidObjectException("Missing legacy ender channel color");
            return new EnderLinkTag(String.format("#%08X", color.getRGB()), player);
        }
    }

    private static final class LegacyFluid implements Serializable {
        private static final long serialVersionUID = 1L;
        private int fluidID;
        private int fluidQuantity;

        private Object readResolve() throws InvalidObjectException {
            EnderFluidContainer container = new EnderFluidContainer();
            if (fluidID >= 0 && fluidQuantity > 0) {
                if (FluidRegistry.getFluid(fluidID) == null || fluidQuantity > 64000) {
                    throw new InvalidObjectException("Invalid legacy ender fluid: " + fluidID + " / " + fluidQuantity);
                }
                container.fill(ForgeDirection.UNKNOWN, new FluidStack(fluidID, fluidQuantity), true);
            }
            return container;
        }
    }
}
