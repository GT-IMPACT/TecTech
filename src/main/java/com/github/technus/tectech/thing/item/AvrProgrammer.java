package com.github.technus.tectech.thing.item;

import com.github.technus.avrClone.AvrCore;
import com.github.technus.avrClone.instructions.InstructionRegistry;
import com.github.technus.avrClone.memory.program.ProgramMemory;
import com.github.technus.tectech.TecTech;
import com.github.technus.tectech.loader.gui.ModGuiHandler;
import com.github.technus.tectech.thing.metaTileEntity.single.GT_MetaTileEntity_MicroController;

import cpw.mods.fml.common.Optional;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import java.util.List;

import static com.github.technus.tectech.Reference.MODID;
import static com.github.technus.tectech.loader.gui.CreativeTabTecTech.creativeTabTecTech;
import static net.minecraft.util.StatCollector.translateToLocal;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.fs.FileSystem", modid = "OpenComputers")})
public class AvrProgrammer extends Item {

    public static AvrProgrammer INSTANCE = new AvrProgrammer();

    private AvrProgrammer() {
        setMaxStackSize(1);
        setHasSubtypes(true);
        setUnlocalizedName("em.programmer");
        setTextureName(MODID + ":itemProgrammer");
        setCreativeTab(creativeTabTecTech);
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ, int side, float hitX, float hitY, float hitZ) {
        TileEntity tTileEntity = aWorld.getTileEntity(aX, aY, aZ);
        if (tTileEntity == null || aPlayer instanceof FakePlayer) {
            return aPlayer instanceof EntityPlayerMP;
        }
        if (aPlayer instanceof EntityPlayerMP) {
            if (tTileEntity instanceof IGregTechTileEntity) {
                IMetaTileEntity metaTE = ((IGregTechTileEntity) tTileEntity).getMetaTileEntity();
                if (metaTE instanceof GT_MetaTileEntity_MicroController) {
                    if (aPlayer.isSneaking()) {
                        if (stack.stackTagCompound.hasKey("pgm")) {
                            NBTTagCompound pgm = stack.stackTagCompound.getCompoundTag("pgm");
                            if (pgm.hasKey("instructions")) {
                                AvrCore core = ((GT_MetaTileEntity_MicroController) metaTE).core;
                                InstructionRegistry registry = InstructionRegistry.REGISTRIES.
                                        get(pgm.getString("instructionRegistry"));
                                if (registry != null) {
                                    core.setProgramMemory(new ProgramMemory(
                                            registry,
                                            pgm.getBoolean("immersive"),
                                            pgm.getIntArray("instructions"),
                                            pgm.getIntArray("param0"),
                                            pgm.getIntArray("param1")));
                                }
                            }
                        }
                    } else {
                        NBTTagCompound tag = new NBTTagCompound();
                        metaTE.saveNBTData(tag);
                        stack.stackTagCompound.setTag("avr", tag.getCompoundTag("avr"));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void writeToProgrammer(ItemStack stack, InstructionRegistry registry, boolean immersive, List<String> strings) throws Exception {
        writeToProgrammer(stack, new ProgramMemory(registry, immersive, strings));
    }

    public void writeToProgrammer(ItemStack stack, InstructionRegistry registry, boolean immersive, String... strings) throws Exception {
        writeToProgrammer(stack, new ProgramMemory(registry, immersive, strings));
    }

    public void writeToProgrammer(ItemStack stack, ProgramMemory programMemory) {
        NBTTagCompound pgm = new NBTTagCompound();
        pgm.setIntArray("instructions", programMemory.instructions);
        pgm.setIntArray("param0", programMemory.param0);
        pgm.setIntArray("param1", programMemory.param1);
        pgm.setBoolean("immersive", programMemory.immersiveOperands);
        pgm.setString("instructionRegistry", programMemory.registry.toString());
        stack.stackTagCompound.setTag("pgm", pgm);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(TecTech.instance, ModGuiHandler.PROGRAMMER_DISPLAY_SCREEN_ID, world, 0, 0, 0);
        }
        return itemStack;
    }

    @Override
    public void addInformation(ItemStack aStack, EntityPlayer ep, List aList, boolean boo) {
        if (aStack.stackTagCompound.hasKey("avr")) {
            NBTTagCompound avr = aStack.stackTagCompound.getCompoundTag("avr");
            aList.add(translateToLocal("item.em.programmer.desc.0") + ": " + avr.getInteger("programCounter"));//Current PC
            aList.add(translateToLocal("item.em.programmer.desc.1") + ": " + avr.getBoolean("awoken"));//Awoken
            aList.add(translateToLocal("item.em.programmer.desc.2") + ": " + avr.getBoolean("active"));//Active
            aList.add(translateToLocal("item.em.programmer.desc.3") + ": " + avr.getBoolean("debugRun"));//Debug
            aList.add(translateToLocal("item.em.programmer.desc.4") + ": " + avr.getBoolean("delay"));//Delay
        }
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
        ItemStack stack = new ItemStack(item, 1, 0);
        stack.setTagCompound(new NBTTagCompound());
        list.add(stack);
    }
}
