package com.github.technus.tectech.compatibility.thaumcraft.thing.metaTileEntity.multi;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.AutoPlaceEnvironment;
import space.impact.structures.structure.IStructureElement;
import space.impact.structures.structure.StructureUtility;

import static com.github.technus.tectech.compatibility.thaumcraft.thing.metaTileEntity.multi.EssentiaCompat.essentiaContainerCompat;

/** Existing jars and reservoirs remain valid; automatic construction supplies an empty jar. */
public final class EssentiaContainerElement<T> implements IStructureElement<T> {
    @Override
    public boolean check(T machine, World world, int x, int y, int z) {
        return essentiaContainerCompat != null && essentiaContainerCompat.isContainer(world.getTileEntity(x, y, z));
    }

    @Override
    public boolean couldBeValid(T machine, World world, int x, int y, int z, ItemStack trigger) {
        return check(machine, world, x, y, z);
    }

    private IStructureElement<T> jar() {
        Block jar = GameRegistry.findBlock("Thaumcraft", "blockJar");
        return jar == null ? StructureUtility.ofHint(HintColor.LIGHT_BLUE) : StructureUtility.<T>ofBlock(jar, 0).withHint(HintColor.LIGHT_BLUE);
    }

    @Override
    public boolean spawnHint(T machine, World world, int x, int y, int z, ItemStack trigger) {
        return jar().spawnHint(machine, world, x, y, z, trigger);
    }

    @Override
    public boolean placeBlock(T machine, World world, int x, int y, int z, ItemStack trigger) {
        return jar().placeBlock(machine, world, x, y, z, trigger);
    }

    @Override
    public PlaceResult survivalPlaceBlock(T machine, World world, int x, int y, int z, ItemStack trigger, AutoPlaceEnvironment environment) {
        return couldBeValid(machine, world, x, y, z, trigger) ? PlaceResult.SKIP : jar().survivalPlaceBlock(machine, world, x, y, z, trigger, environment);
    }
}
