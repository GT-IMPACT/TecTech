package com.github.technus.tectech.thing.metaTileEntity.multi;

import com.github.technus.tectech.mechanics.enderStorage.ForgeTestRunner;
import com.github.technus.tectech.thing.metaTileEntity.multi.StructureTestWorld.TestTile;
import com.github.technus.tectech.thing.metaTileEntity.multi.StructureTestWorld.TestWorld;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregtech.api.util.GTStructureChannels;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import space.impact.structures.ImpactStructures;
import space.impact.structures.StructureEvent.StructureElementVisitedEvent;
import space.impact.structures.alignment.constructable.ChannelDataAccessor;
import space.impact.structures.structure.IStructureElement;

import static org.junit.Assert.assertSame;

@RunWith(ForgeTestRunner.class)
public class ComputerPreviewTest {

    private final Map<String, IStructureElement<?>> firstElements = new HashMap<>();
    private TestWorld world;

    @BeforeClass
    public static void initialize() throws Exception {
        TecTechStructureMigrationTest.initialize();
    }

    @SubscribeEvent
    public void onElement(Event received) {
        // The headless runner does not inject Forge's no-arg constructor into event subclasses.
        if (!(received instanceof StructureElementVisitedEvent)) {
            return;
        }
        StructureElementVisitedEvent event = (StructureElementVisitedEvent) received;
        if (event.getInstrumentIdentifier() == this && event.getWorld() == world) {
            // Blueprints retains the first visited element, including nested machine checks.
            firstElements.putIfAbsent(TestWorld.key(event.getX(), event.getY(), event.getZ()), event.getElement());
        }
    }

    @Test
    public void constructionEventsRetainTheActualElementsInEveryLayer() {
        MinecraftForge.EVENT_BUS.register(this);
        try {
            for (int slices : new int[] { 1, 12 }) {
                for (ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
                    world = new TestWorld();
                    firstElements.clear();
                    GT_MetaTileEntity_EM_computer machine = new GT_MetaTileEntity_EM_computer("preview");
                    world.putTile(0, 64, 0, new TestTile(world, machine, facing));
                    ItemStack trigger = new ItemStack(Items.stick);
                    ChannelDataAccessor.setChannelData(trigger, GTStructureChannels.LENGTH, slices);
                    ImpactStructures.enableInstrument(this);
                    try {
                        machine.construct(trigger, false);
                    } finally {
                        ImpactStructures.disableInstrument();
                    }
                    for (int depth = 0; depth < slices + 4; depth++) {
                        String piece = depth == 0 ? "front" : depth == slices + 3 ? "back"
                                : depth == 1 || depth == slices + 2 ? "cap" : "slice";
                        machine.getStructureDefinition().iterate(
                                piece,
                                world,
                                machine.getExtendedFacing(),
                                0,
                                64,
                                0,
                                1,
                                2,
                                -depth,
                                (element, visitedWorld, x, y, z, a, b, c) -> {
                                    String position = TestWorld.key(x, y, z);
                                    assertSame(piece + " at " + position, element, firstElements.get(position));
                                    return true;
                                }
                        );
                    }
                }
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }
}
