package com.github.technus.tectech;

import java.io.InputStream;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class YamCoreRuntimeCompatibilityTest {

    @Test
    public void clientPacketPlayerLookupLinksAgainstDevelopmentMinecraft() throws Exception {
        ClassNode proxy = readClass("eu/usrv/yamcore/proxy/ClientProxy");
        ClassNode minecraft = readClass("net/minecraft/client/Minecraft");
        MethodNode lookup = proxy.methods.stream()
                .filter(method -> method.name.equals("getPlayerEntity"))
                .findFirst().orElseThrow(() -> new AssertionError("Missing YAMCore player lookup"));
        int checkedMembers = 0;
        for (AbstractInsnNode instruction : lookup.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.owner.equals(minecraft.name)) {
                    assertTrue("YAMCore calls an unavailable Minecraft method: " + call.name + call.desc,
                            minecraft.methods.stream().anyMatch(method ->
                                    method.name.equals(call.name) && method.desc.equals(call.desc)));
                    checkedMembers++;
                }
            } else if (instruction instanceof FieldInsnNode) {
                FieldInsnNode access = (FieldInsnNode) instruction;
                if (access.owner.equals(minecraft.name)) {
                    assertTrue("YAMCore accesses an unavailable Minecraft field: " + access.name,
                            minecraft.fields.stream().anyMatch(field ->
                                    field.name.equals(access.name) && field.desc.equals(access.desc)));
                    checkedMembers++;
                }
            }
        }
        assertEquals("Check both the Minecraft singleton and its player field", 2, checkedMembers);
    }

    private ClassNode readClass(String name) throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull("Missing runtime class: " + name, input);
            ClassNode node = new ClassNode();
            new ClassReader(input).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return node;
        }
    }
}
