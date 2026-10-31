package com.github.technus.tectech.thing.metaTileEntity.multi;

import com.github.technus.tectech.TecTech;
import com.github.technus.tectech.compatibility.thaumcraft.elementalMatter.definitions.dComplexAspectDefinition;
import com.github.technus.tectech.compatibility.thaumcraft.elementalMatter.definitions.ePrimalAspectDefinition;
import com.github.technus.tectech.mechanics.elementalMatter.core.cElementalDecayResult;
import com.github.technus.tectech.mechanics.elementalMatter.core.cElementalInstanceStackMap;
import com.github.technus.tectech.mechanics.elementalMatter.core.cElementalMutableDefinitionStackMap;
import com.github.technus.tectech.mechanics.elementalMatter.core.stacks.cElementalInstanceStack;
import com.github.technus.tectech.mechanics.elementalMatter.core.templates.cElementalPrimitive;
import com.github.technus.tectech.mechanics.elementalMatter.definitions.complex.dAtomDefinition;
import com.github.technus.tectech.mechanics.elementalMatter.definitions.complex.dHadronDefinition;
import com.github.technus.tectech.mechanics.elementalMatter.definitions.primitive.eQuarkDefinition;
import com.github.technus.tectech.thing.block.QuantumGlassBlock;
import com.github.technus.tectech.thing.casing.TT_Container_Casings;
import com.github.technus.tectech.thing.casing.TecTechCasings;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_InputElemental;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.INameFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.IStatusFunction;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.Parameters;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.TecTechHatchElement;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.render.TT_RenderedExtendedFacingTexture;
import com.github.technus.tectech.util.CommonValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StructureRequirements;
import java.util.HashMap;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.ISurvivalBuildEnvironment;
import space.impact.structures.structure.StructureDefinition;

import static com.github.technus.tectech.loader.TecTechConfig.DEBUG_MODE;
import static com.github.technus.tectech.mechanics.elementalMatter.core.transformations.bTransformationInfo.AVOGADRO_CONSTANT;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_OK;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_TOO_HIGH;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_TOO_LOW;
import static com.github.technus.tectech.thing.metaTileEntity.multi.base.LedStatus.STATUS_WRONG;
import static com.github.technus.tectech.util.DoubleCount.add;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static net.minecraft.util.StatCollector.translateToLocal;
import static space.impact.structures.structure.StructureUtility.defer;
import static space.impact.structures.structure.StructureUtility.error;
import static space.impact.structures.structure.StructureUtility.ofBlock;
import static space.impact.structures.structure.StructureUtility.transpose;
import static space.impact.structures.structure.StructureUtility.withHint;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_collider extends GT_MetaTileEntity_MultiblockBase_EM<GT_MetaTileEntity_EM_collider> {
    //region collision handlers
    public static final HashMap<Integer, IColliderHandler> FUSE_HANDLERS = new HashMap<>();
    public static final HashMap<String, IPrimitiveColliderHandler> PRIMITIVE_FUSE_HANDLERS = new HashMap<>();
    protected static final byte FUSE_MODE = 0, COLLIDE_MODE = 1;
    private static final IStatusFunction<GT_MetaTileEntity_EM_collider> MODE_STATUS = (base_EM, p) -> {
        if (base_EM.isMaster()) {
            double mode = p.get();
            if (mode == FUSE_MODE || mode == COLLIDE_MODE) {
                return STATUS_OK;
            } else if (mode > 1) {
                return STATUS_TOO_HIGH;
            } else if (mode < 0) {
                return STATUS_TOO_LOW;
            }
            return STATUS_WRONG;
        }
        return STATUS_OK;
    };
    private static final INameFunction<GT_MetaTileEntity_EM_collider> MODE_NAME = (base_EM, p) -> {
        if (base_EM.isMaster()) {
            double mode = p.get();
            if (mode == FUSE_MODE) {
                return translateToLocal("gt.blockmachines.multimachine.em.collider.mode.0");//Mode: Fuse
            } else if (mode == COLLIDE_MODE) {
                return translateToLocal("gt.blockmachines.multimachine.em.collider.mode.1");//Mode: Collide
            }
            return translateToLocal("gt.blockmachines.multimachine.em.collider.mode.2");//Mode: Undefined
        }
        return translateToLocal("gt.blockmachines.multimachine.em.collider.mode.3");//Currently Slaves...
    };
    //region structure
    //use multi A energy inputs, use less power the longer it runs
    private static final IStructureDefinition<GT_MetaTileEntity_EM_collider> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_EM_collider>builder()
                    .addShape("main", transpose(new String[][]{
                            {"         A A A         ", "        AAAAAAA        ", "      BBBBIIIBBBB      ", "     BAAAAAAAAAAAB     ", "    BAAAA     AAAAB    ", "   BAAA         AAAB   ", "  BAAA           AAAB  ", "  BAA             AAB  ", " ABAA             AABA ", "AABA               ABAA", " AIA               AIA ", "AAIA               AIAA", " AIA               AIA ", "AABA               ABAA", " ABAA             AABA ", "  BAA             AAB  ", "  BAAA           AAAB  ", "   BAAA         AAAB   ", "    BAAAABJJJBAAAAB    ", "     BAHHBBBBBHHAB     ", "      BBBBGFGBBBB      "},
                            {"         AAAAA         ", "       AADDDDDAA       ", "      CDDEEEEEDDC      ", "     CDEEDDDDDEEDC     ", "    CDEDD     DDEDC    ", "   CDED         DEDC   ", "  CDED           DEDC  ", " ADED             DEDA ", " ADED             DEDA ", "ADED               DEDA", "ADED               DEDA", "ADED               DEDA", "ADED               DEDA", "ADED               DEDA", " ADED             DEDA ", " ADED             DEDA ", "  CDED           DEDC  ", "   CDED         DEDC   ", "    CDEDDBJ~JBDDEDC    ", "     CDEEDDDDDEEDC     ", "      CDDEEEEEDDC      "},
                            {"         A A A         ", "        AAAAAAA        ", "      BBBBIIIBBBB      ", "     BAAAAAAAAAAAB     ", "    BAAAA     AAAAB    ", "   BAAA         AAAB   ", "  BAAA           AAAB  ", "  BAA             AAB  ", " ABAA             AABA ", "AABA               ABAA", " AIA               AIA ", "AAIA               AIAA", " AIA               AIA ", "AABA               ABAA", " ABAA             AABA ", "  BAA             AAB  ", "  BAAA           AAAB  ", "   BAAA         AAAB   ", "    BAAAABJJJBAAAAB    ", "     BAHHBBBBBHHAB     ", "      BBBBGFGBBBB      "}
                    }))
                    .addElement('A', TecTechCasings.MOLECULAR.asElement())
                    .addElement('B', defer(t -> (int) t.eTier, (t, item) -> 2 - (item.stackSize & 1), error(), TecTechCasings.MOLECULAR.asElement(), TecTechCasings.ADVANCED_MOLECULAR.asElement()))
                    .addElement('C', defer(t -> (int) t.eTier, (t, item) -> 2 - (item.stackSize & 1), error(), TecTechCasings.MOLECULAR.asElement(), TecTechCasings.CONTAINMENT_FIELD.asElement()))
                    .addElement('D', TecTechCasings.MOLECULAR_COIL.asElement())
                    .addElement('E', defer(t -> (int) t.eTier, (t, item) -> 2 - (item.stackSize & 1), error(), TecTechCasings.HOLLOW.asElement(), TecTechCasings.SPACETIME.asElement()))
                    .addElement('F', withHint(buildHatchAdder(GT_MetaTileEntity_EM_collider.class).allow(TecTechHatchElement.ElementalOutput).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.MAGENTA))
                    .addElement('G', withHint(buildHatchAdder(GT_MetaTileEntity_EM_collider.class).allow(TecTechHatchElement.ElementalOverflow).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.LIGHT_BLUE))
                    .addElement('H', withHint(buildHatchAdder(GT_MetaTileEntity_EM_collider.class).allow(TecTechHatchElement.ElementalInput).buildAndChain(StructureRequirements.builder(TecTechCasings.MOLECULAR, 0).build()), HintColor.ORANGE))
                    .addElement('I', ofBlock(QuantumGlassBlock.INSTANCE, 0))
                    .addElement('J', withHint(buildHatchAdder(GT_MetaTileEntity_EM_collider.class).allow(TecTechHatchElement.Classic).buildAndChain(StructureRequirements.builder(TecTechCasings.HIGH_POWER, 0).build()), HintColor.WHITE))
                    .build();
    //region variables
    private static Textures.BlockIcons.CustomIcon ScreenOFF;
    private static Textures.BlockIcons.CustomIcon ScreenON;
    private static Textures.BlockIcons.CustomIcon ScreenON_Slave;
    private static Textures.BlockIcons.CustomIcon ScreenOFF_Slave;
    //endregion
    private static double MASS_TO_EU_INSTANT;
    private static int STARTUP_COST, KEEPUP_COST;

    static {
        FUSE_HANDLERS.put((dAtomDefinition.getClassTypeStatic() << 16) | dAtomDefinition.getClassTypeStatic(), new IColliderHandler() {
            @Override
            public void collide(cElementalInstanceStack in1, cElementalInstanceStack in2, cElementalInstanceStackMap out) {
                try {
                    cElementalMutableDefinitionStackMap defs = new cElementalMutableDefinitionStackMap();
                    defs.putUnifyAll(in1.definition.getSubParticles());
                    defs.putUnifyAll(in2.definition.getSubParticles());
                    dAtomDefinition atom = new dAtomDefinition(defs.toImmutable_optimized_unsafeLeavesExposedElementalTree());
                    out.putUnify(new cElementalInstanceStack(atom, Math.min(in1.amount, in2.amount)));
                } catch (Exception e) {
                    out.putUnifyAll(in1, in2);
                    return;
                }
                if (in1.amount > in2.amount) {
                    out.putUnify(new cElementalInstanceStack(in1.definition, in1.amount - in2.amount));
                } else if (in2.amount > in1.amount) {
                    out.putUnify(new cElementalInstanceStack(in2.definition, in2.amount - in1.amount));
                }
            }

            @Override
            public byte getRequiredTier() {
                return 1;
            }
        });
        registerSimpleAtomFuse(dHadronDefinition.getClassTypeStatic());
        registerSimpleAtomFuse(dComplexAspectDefinition.getClassTypeStatic());
        registerSimpleAtomFuse(cElementalPrimitive.getClassTypeStatic());

        FUSE_HANDLERS.put((dHadronDefinition.getClassTypeStatic() << 16) | dHadronDefinition.getClassTypeStatic(), new IColliderHandler() {
            @Override
            public void collide(cElementalInstanceStack in1, cElementalInstanceStack in2, cElementalInstanceStackMap out) {
                try {
                    cElementalMutableDefinitionStackMap defs = new cElementalMutableDefinitionStackMap();
                    defs.putUnifyAll(in1.definition.getSubParticles());
                    defs.putUnifyAll(in2.definition.getSubParticles());
                    dHadronDefinition hadron = new dHadronDefinition(defs.toImmutable_optimized_unsafeLeavesExposedElementalTree());
                    out.putUnify(new cElementalInstanceStack(hadron, Math.min(in1.amount, in2.amount)));
                } catch (Exception e) {
                    out.putUnifyAll(in1, in2);
                    return;
                }
                if (in1.amount > in2.amount) {
                    out.putUnify(new cElementalInstanceStack(in1.definition, in1.amount - in2.amount));
                } else if (in2.amount > in1.amount) {
                    out.putUnify(new cElementalInstanceStack(in2.definition, in2.amount - in1.amount));
                }
            }

            @Override
            public byte getRequiredTier() {
                return 2;
            }
        });
        FUSE_HANDLERS.put((dHadronDefinition.getClassTypeStatic() << 16) | cElementalPrimitive.getClassTypeStatic(), new IColliderHandler() {
            @Override
            public void collide(cElementalInstanceStack in1, cElementalInstanceStack in2, cElementalInstanceStackMap out) {
                try {
                    cElementalMutableDefinitionStackMap defs = new cElementalMutableDefinitionStackMap();
                    defs.putUnifyAll(in1.definition.getSubParticles());
                    defs.putUnify(in2.definition.getStackForm(1));
                    dHadronDefinition hadron = new dHadronDefinition(defs.toImmutable_optimized_unsafeLeavesExposedElementalTree());
                    out.putUnify(new cElementalInstanceStack(hadron, Math.min(in1.amount, in2.amount)));
                } catch (Exception e) {
                    out.putUnifyAll(in1, in2);
                    return;
                }
                if (in1.amount > in2.amount) {
                    out.putUnify(new cElementalInstanceStack(in1.definition, in1.amount - in2.amount));
                } else if (in2.amount > in1.amount) {
                    out.putUnify(new cElementalInstanceStack(in2.definition, in2.amount - in1.amount));
                }
            }

            @Override
            public byte getRequiredTier() {
                return 2;
            }
        });

        registerSimpleAspectFuse(dComplexAspectDefinition.getClassTypeStatic());
        registerSimpleAspectFuse(cElementalPrimitive.getClassTypeStatic());

        FUSE_HANDLERS.put((cElementalPrimitive.getClassTypeStatic() << 16) | cElementalPrimitive.getClassTypeStatic(), new IColliderHandler() {
            @Override
            public void collide(cElementalInstanceStack in1, cElementalInstanceStack in2, cElementalInstanceStackMap out) {
                IPrimitiveColliderHandler collisionHandler = PRIMITIVE_FUSE_HANDLERS.get(in1.definition.getClass().getName() + '\0' + in2.definition.getClass().getName());
                if (collisionHandler != null) {
                    collisionHandler.collide(in2, in1, out);
                } else {
                    out.putUnifyAll(in1, in2);
                }
            }

            @Override
            public byte getRequiredTier() {
                return 2;
            }
        });

        PRIMITIVE_FUSE_HANDLERS.put(eQuarkDefinition.class.getName() + '\0' + eQuarkDefinition.class.getName(), (in1, in2, out) -> {
            try {
                cElementalMutableDefinitionStackMap defs = new cElementalMutableDefinitionStackMap();
                defs.putUnify(in1.definition.getStackForm(1));
                defs.putUnify(in2.definition.getStackForm(1));
                dHadronDefinition hadron = new dHadronDefinition(defs.toImmutable_optimized_unsafeLeavesExposedElementalTree());
                out.putUnify(new cElementalInstanceStack(hadron, Math.min(in1.amount, in2.amount)));
            } catch (Exception e) {
                out.putUnifyAll(in1, in2);
                return;
            }
            if (in1.amount > in2.amount) {
                out.putUnify(new cElementalInstanceStack(in1.definition, in1.amount - in2.amount));
            } else if (in2.amount > in1.amount) {
                out.putUnify(new cElementalInstanceStack(in2.definition, in2.amount - in1.amount));
            }
        });
        PRIMITIVE_FUSE_HANDLERS.put(ePrimalAspectDefinition.class.getName() + '\0' + ePrimalAspectDefinition.class.getName(), (in1, in2, out) -> {
            if (fuseAspects(in1, in2, out)) return;
            if (in1.amount > in2.amount) {
                out.putUnify(new cElementalInstanceStack(in1.definition, in1.amount - in2.amount));
            } else if (in2.amount > in1.amount) {
                out.putUnify(new cElementalInstanceStack(in2.definition, in2.amount - in1.amount));
            }
        });
    }

    protected byte eTier = 0;
    protected cElementalInstanceStack stack;
    protected boolean started = false;
    //region parameters
    protected Parameters.Group.ParameterIn mode;
    private long plasmaEnergy;
    //endregion

    public GT_MetaTileEntity_EM_collider(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_collider(String aName) {
        super(aName);
    }

    private static boolean fuseAspects(cElementalInstanceStack in1, cElementalInstanceStack in2, cElementalInstanceStackMap out) {
        try {
            cElementalMutableDefinitionStackMap defs = new cElementalMutableDefinitionStackMap();
            defs.putUnify(in1.definition.getStackForm(1));
            defs.putUnify(in2.definition.getStackForm(1));
            dComplexAspectDefinition aspect = new dComplexAspectDefinition(defs.toImmutable_optimized_unsafeLeavesExposedElementalTree());
            out.putUnify(new cElementalInstanceStack(aspect, Math.min(in1.amount, in2.amount)));
        } catch (Exception e) {
            out.putUnifyAll(in1, in2);
            return true;
        }
        return false;
    }
    //endregion

    private static void registerSimpleAspectFuse(byte classTypeStatic) {
        FUSE_HANDLERS.put((dComplexAspectDefinition.getClassTypeStatic() << 16) | classTypeStatic, new IColliderHandler() {
            @Override
            public void collide(cElementalInstanceStack in1, cElementalInstanceStack in2, cElementalInstanceStackMap out) {
                if (fuseAspects(in1, in2, out)) return;
                if (in1.amount > in2.amount) {
                    out.putUnify(new cElementalInstanceStack(in1.definition, in1.amount - in2.amount));
                } else if (in2.amount > in1.amount) {
                    out.putUnify(new cElementalInstanceStack(in2.definition, in2.amount - in1.amount));
                }
            }

            @Override
            public byte getRequiredTier() {
                return 1;
            }
        });
    }

    private static void registerSimpleAtomFuse(byte classTypeStatic) {
        FUSE_HANDLERS.put((dAtomDefinition.getClassTypeStatic() << 16) | classTypeStatic, new IColliderHandler() {
            @Override
            public void collide(cElementalInstanceStack in1, cElementalInstanceStack in2, cElementalInstanceStackMap out) {
                try {
                    cElementalMutableDefinitionStackMap defs = new cElementalMutableDefinitionStackMap();
                    defs.putUnifyAll(in1.definition.getSubParticles());
                    defs.putUnify(in2.definition.getStackForm(1));
                    dAtomDefinition atom = new dAtomDefinition(defs.toImmutable_optimized_unsafeLeavesExposedElementalTree());
                    out.putUnify(new cElementalInstanceStack(atom, Math.min(in1.amount, in2.amount)));
                } catch (Exception e) {
                    out.putUnifyAll(in1, in2);
                    return;
                }
                if (in1.amount > in2.amount) {
                    out.putUnify(new cElementalInstanceStack(in1.definition, in1.amount - in2.amount));
                } else if (in2.amount > in1.amount) {
                    out.putUnify(new cElementalInstanceStack(in2.definition, in2.amount - in1.amount));
                }
            }

            @Override
            public byte getRequiredTier() {
                return 1;
            }
        });
    }

    public static void setValues(int heliumPlasmaValue) {
        double MASS_TO_EU_PARTIAL = heliumPlasmaValue / (1.75893000478707E07 * AVOGADRO_CONSTANT);//mass diff
        MASS_TO_EU_INSTANT = MASS_TO_EU_PARTIAL * 20;
        STARTUP_COST       = -heliumPlasmaValue * 10000;
        KEEPUP_COST        = -heliumPlasmaValue;
    }

    //endregion

    @Override
    public IStructureDefinition<GT_MetaTileEntity_EM_collider> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    protected double fuse(GT_MetaTileEntity_EM_collider partner) {///CAN MAKE EU
        if (partner.stack != null && stack != null) {//todo add single event mode as an option
            boolean check = stack.definition.fusionMakesEnergy(stack.getEnergy()) &&
                    partner.stack.definition.fusionMakesEnergy(partner.stack.getEnergy());

            cElementalInstanceStack stack2 = partner.stack;
            double preMass = add(stack2.getMass(), stack.getMass());
            //System.out.println("preMass = " + preMass);

            cElementalInstanceStackMap map = new cElementalInstanceStackMap();
            IColliderHandler colliderHandler;
            if (stack2.definition.getClassType() > stack.definition.getClassType()) {//always bigger first
                colliderHandler = FUSE_HANDLERS.get((stack2.definition.getClassType() << 16) | stack.definition.getClassType());
                if (handleRecipe(stack2, map, colliderHandler)) return 0;
            } else {
                colliderHandler = FUSE_HANDLERS.get((stack.definition.getClassType() << 16) | stack2.definition.getClassType());
                if (handleRecipe(stack2, map, colliderHandler)) return 0;
            }
            for (cElementalInstanceStack newStack : map.values()) {
                check &= newStack.definition.fusionMakesEnergy(newStack.getEnergy());
            }
            //System.out.println("outputEM[0].getMass() = " + outputEM[0].getMass());
            outputEM = new cElementalInstanceStackMap[]{map};

            partner.stack = stack = null;
            //System.out.println("check = " + check);
            //System.out.println("preMass-map.getMass() = " + (preMass - map.getMass()));
            return check ? preMass - map.getMass() : Math.min(preMass - map.getMass(), 0);
        }
        return 0;
    }

    protected double collide(GT_MetaTileEntity_EM_collider partner) {//DOES NOT MAKE EU!
        if (partner.stack != null && stack != null) {//todo add single event mode as an option
            cElementalInstanceStack stack2 = partner.stack;
            double preMass = stack2.getMass() + stack.getMass();
            //System.out.println("preMass = " + preMass);

            cElementalInstanceStackMap map = new cElementalInstanceStackMap();
            IColliderHandler colliderHandler;
            if (stack2.definition.getClassType() > stack.definition.getClassType()) {//always bigger first
                colliderHandler = FUSE_HANDLERS.get((stack2.definition.getClassType() << 16) | stack.definition.getClassType());
                if (handleRecipe(stack2, map, colliderHandler)) return 0;
            } else {
                colliderHandler = FUSE_HANDLERS.get((stack.definition.getClassType() << 16) | stack2.definition.getClassType());
                if (handleRecipe(stack2, map, colliderHandler)) return 0;
            }
            //System.out.println("outputEM[0].getMass() = " + outputEM[0].getMass());
            outputEM = new cElementalInstanceStackMap[]{map};

            partner.stack = stack = null;
            //System.out.println("check = " + check);
            //System.out.println("preMass-map.getMass() = " + (preMass - map.getMass()));
            return Math.min(preMass - map.getMass(), 0);
        }
        return 0;
    }

    private boolean handleRecipe(cElementalInstanceStack stack2, cElementalInstanceStackMap map, IColliderHandler colliderHandler) {
        if (colliderHandler != null && eTier >= colliderHandler.getRequiredTier()) {
            colliderHandler.collide(stack2, stack, map);
        } else {
            map.putUnifyAll(stack, stack2);
            outputEM = new cElementalInstanceStackMap[]{map};
            return true;
        }
        return false;
    }

    protected GT_MetaTileEntity_EM_collider getPartner() {
        IGregTechTileEntity iGregTechTileEntity = getBaseMetaTileEntity();
        int xDir = ForgeDirection.getOrientation(iGregTechTileEntity.getBackFacing()).offsetX * 4;
        int yDir = ForgeDirection.getOrientation(iGregTechTileEntity.getBackFacing()).offsetY * 4;
        int zDir = ForgeDirection.getOrientation(iGregTechTileEntity.getBackFacing()).offsetZ * 4;
        IGregTechTileEntity gregTechBaseTileEntity = iGregTechTileEntity.getIGregTechTileEntityOffset(xDir, yDir, zDir);
        if (gregTechBaseTileEntity != null) {
            IMetaTileEntity gregTechMetaTileEntity = gregTechBaseTileEntity.getMetaTileEntity();
            return gregTechMetaTileEntity instanceof GT_MetaTileEntity_EM_collider &&
                    ((GT_MetaTileEntity_EM_collider) gregTechMetaTileEntity).mMachine &&
                    gregTechBaseTileEntity.getBackFacing() == iGregTechTileEntity.getFrontFacing() ?
                    (GT_MetaTileEntity_EM_collider) gregTechMetaTileEntity : null;
        }
        return null;
    }

    protected final boolean isMaster() {
        return getBaseMetaTileEntity().getFrontFacing() % 2 == 0;
    }

    private void makeEU(double massDiff) {
        plasmaEnergy += massDiff * MASS_TO_EU_INSTANT;
        if (DEBUG_MODE) {
            System.out.println("plasmaEnergy = " + plasmaEnergy);
        }
    }

    private cElementalInstanceStackMap tickStack() {
        if (stack == null) {
            return null;
        }
        cElementalDecayResult newInstances = stack.decay(1, stack.age += 1, 0);
        if (newInstances == null) {
            stack.nextColor();
            return null;
        } else {
            stack = newInstances.getOutput().remove(newInstances.getOutput().getLast().definition);
            return newInstances.getOutput();
        }
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_collider(mName);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        int xDir = ForgeDirection.getOrientation(iGregTechTileEntity.getBackFacing()).offsetX * 2;
        int yDir = ForgeDirection.getOrientation(iGregTechTileEntity.getBackFacing()).offsetY * 2;
        int zDir = ForgeDirection.getOrientation(iGregTechTileEntity.getBackFacing()).offsetZ * 2;
        if (iGregTechTileEntity.getBlockOffset(xDir, yDir, zDir) != TecTechCasings.HOLLOW.getBlock()) {
            eTier = 0;
            return false;
        }

        if (iGregTechTileEntity.getMetaIDOffset(xDir, yDir, zDir) == TecTechCasings.HOLLOW.getMetadata()) {
            eTier = 1;
        } else if (iGregTechTileEntity.getMetaIDOffset(xDir, yDir, zDir) == TecTechCasings.SPACETIME.getMetadata()) {
            eTier = 2;
        } else {
            eTier = 0;
            return false;
        }
        if (checkPiece("main", 11, 1, 18)) {
            return true;
        }
        eTier = 0;
        return false;
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        GT_MetaTileEntity_EM_collider partner = getPartner();
        if (partner == null) {
            return false;
        }
        mEfficiencyIncrease = 10000;
        if (started) {
            if (stack == null) {
                for (GT_MetaTileEntity_Hatch_InputElemental inputElemental : eInputHatches) {
                    cElementalInstanceStackMap container = inputElemental.getContainerHandler();
                    if (container.isEmpty()) {
                        continue;
                    }
                    stack = container.remove(container.getFirst().definition);
                    long eut = KEEPUP_COST + (long) (KEEPUP_COST * Math.abs(stack.getMass() / dAtomDefinition.getSomethingHeavy().getMass())) / 2;
                    if (eut < Integer.MIN_VALUE + 7) {
                        return false;
                    }
                    mMaxProgresstime = 20;
                    mEUt             = (int) eut;
                    eAmpereFlow      = 5;
                    return true;
                }
                mMaxProgresstime = 20;
                mEUt             = KEEPUP_COST;
                eAmpereFlow      = 1;
                return true;
            }
            mMaxProgresstime = 20;
            mEUt             = KEEPUP_COST;
            eAmpereFlow      = 2;
        } else {
            started          = true;
            mMaxProgresstime = 20;
            mEUt             = STARTUP_COST;
            eAmpereFlow      = 10;
        }
        return true;
    }

    @Override
    public void outputAfterRecipe_EM() {
        GT_MetaTileEntity_EM_collider partner = getPartner();
        if (partner == null) {
            if (stack != null) {
                cleanMassEM_EM(stack.getMass());
                stack = null;
            }
            return;
        }
        if (isMaster()) {
            switch ((int) mode.get()) {
                case FUSE_MODE:
                    makeEU(fuse(partner));
                    break;
                case COLLIDE_MODE:
                    collide(partner);//todo
                    break;
                default: {
                    outputEM    = new cElementalInstanceStackMap[2];
                    outputEM[1] = tickStack();
                    if (outputEM[1] == null) {
                        outputEM[1] = partner.tickStack();
                    } else {
                        cElementalInstanceStackMap map = partner.tickStack();
                        if (map != null) {
                            outputEM[1].putUnifyAll(map);
                        }
                    }
                }
            }
            if (outputEM != null) {
                for (int i = 0, lim = Math.min(outputEM.length, eOutputHatches.size()); i < lim; i++) {
                    if (outputEM[i] != null) {
                        eOutputHatches.get(i).getContainerHandler().putUnifyAll(outputEM[i]);
                        outputEM[i] = null;
                    }
                }
            }
        }
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltip = new MultiblockTooltipBuilder();
        tooltip.addInfo(CommonValues.TEC_MARK_EM);
        tooltip.addInfo(translateToLocal("gt.blockmachines.multimachine.em.collider.desc.0"));
        tooltip.addInfo(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + translateToLocal("gt.blockmachines.multimachine.em.collider.desc.1"));
        tooltip.addStructureInfo(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureInfo(translateToLocal("gt.blockmachines.multimachine.em.collider.hint.4"));
        tooltip.addStructureHint(EnumChatFormatting.AQUA + translateToLocal("tt.keyphrase.Hint_Details") + ":");
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.collider.hint.0", 1);
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.collider.hint.1", 2);
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.collider.hint.2", 3);
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.collider.hint.3", 4);
        tooltip.addStructureHint("gt.blockmachines.multimachine.em.collider.hint.4");
        return tooltip;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister aBlockIconRegister) {
        ScreenOFF       = new Textures.BlockIcons.CustomIcon("iconsets/EM_COLLIDER");
        ScreenON        = new Textures.BlockIcons.CustomIcon("iconsets/EM_COLLIDER_ACTIVE");
        ScreenOFF_Slave = new Textures.BlockIcons.CustomIcon("iconsets/EM_COLLIDER_SLAVE");
        ScreenON_Slave  = new Textures.BlockIcons.CustomIcon("iconsets/EM_COLLIDER_ACTIVE_SLAVE");
        super.registerIcons(aBlockIconRegister);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            if (aFacing % 2 == 0) {
                return new ITexture[]{TecTechCasings.MOLECULAR.getTexture(), new TT_RenderedExtendedFacingTexture(aActive ? ScreenON : ScreenOFF)};
            } else {
                return new ITexture[]{TecTechCasings.MOLECULAR.getTexture(), new TT_RenderedExtendedFacingTexture(aActive ? ScreenON_Slave : ScreenOFF_Slave)};
            }
        }
        return new ITexture[]{TecTechCasings.MOLECULAR.getTexture()};
    }

    @Override
    protected void parametersInstantiation_EM() {
        Parameters.Group hatch_0 = parametrization.getGroup(0);
        mode = hatch_0.makeInParameter(0, FUSE_MODE, MODE_NAME, MODE_STATUS);
    }

    @Override
    public void parametersStatusesWrite_EM(boolean machineBusy) {
        if (isMaster()) {
            super.parametersStatusesWrite_EM(machineBusy);
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setByte("eTier", eTier);//collider tier
        aNBT.setBoolean("eStarted", started);
        if (stack != null) {
            aNBT.setTag("eStack", stack.toNBT());
        }
        aNBT.setLong("ePlasmaEnergy", plasmaEnergy);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        eTier   = aNBT.getByte("eTier");//collider tier
        started = aNBT.getBoolean("eStarted");
        if (aNBT.hasKey("eStack")) {
            stack = cElementalInstanceStack.fromNBT(aNBT.getCompoundTag("eStack"));
        }
        plasmaEnergy = aNBT.getLong("ePlasmaEnergy");
    }

    @Override
    public void stopMachine() {
        started = false;
        if (stack != null) {
            cleanMassEM_EM(stack.getMass());
            stack = null;
        }
        super.stopMachine();
    }

    @Override
    protected void afterRecipeCheckFailed() {
        started = false;
        if (stack != null) {
            cleanMassEM_EM(stack.getMass());
            stack = null;
        }
        getBaseMetaTileEntity().disableWorking();
        super.afterRecipeCheckFailed();
    }

    @Override
    public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (!aBaseMetaTileEntity.isAllowedToWork()) {
            started = false;
        }
        super.onPreTick(aBaseMetaTileEntity, aTick);
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        IGregTechTileEntity iGregTechTileEntity = getBaseMetaTileEntity();
        int xDir = ForgeDirection.getOrientation(iGregTechTileEntity.getBackFacing()).offsetX * 4;
        int yDir = ForgeDirection.getOrientation(iGregTechTileEntity.getBackFacing()).offsetY * 4;
        int zDir = ForgeDirection.getOrientation(iGregTechTileEntity.getBackFacing()).offsetZ * 4;
        if (hintsOnly) {
            TecTech.proxy.hint_particle(iGregTechTileEntity.getWorld(),
                    iGregTechTileEntity.getXCoord() + xDir,
                    iGregTechTileEntity.getYCoord() + yDir,
                    iGregTechTileEntity.getZCoord() + zDir,
                    TT_Container_Casings.sHintCasingsTT, 12
            );
        }
        buildPiece("main", stackSize, hintsOnly, 11, 1, 18);
    }
    @Override
    public int survivalConstruct(ItemStack stackSize, int budget, ISurvivalBuildEnvironment environment) {
        return survivalBuildPiece("main", stackSize, 11, 1, 18, budget, environment);
    }

    public interface IPrimitiveColliderHandler {
        void collide(cElementalInstanceStack in1, cElementalInstanceStack in2, cElementalInstanceStackMap out);
    }

    public interface IColliderHandler extends IPrimitiveColliderHandler {
        byte getRequiredTier();
    }
}
