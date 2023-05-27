package org.altmc;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Transformer implements ClassFileTransformer {

    public static Instrumentation instrumentation;

    public static void start(Instrumentation instrumentation) {
        Transformer.instrumentation = instrumentation;
        Main.LOGGER.info("Adding Transformer");
        instrumentation.addTransformer(new Transformer());
        Main.LOGGER.info("Added Transformer");
    }

    public static AbstractInsnNode getPrevious(AbstractInsnNode node, int amount) {
        AbstractInsnNode result = node;
        for (int i = 0; i < amount; i++) {
            result = result.getPrevious();
        }
        return result;
    }

    public static boolean patchedMinecraftMain = false;
    public static boolean patchedAuthlibYggdrasil = false;
    public static boolean patchedClassLoader = false;

    public static byte[] patchClassLoader(String className, byte[] bytes) throws Exception {
        patchedClassLoader = true;
        ClassReader r = new ClassReader(bytes);
        ClassWriter w = new ClassWriter(r, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassNode cn = new ClassNode();
        r.accept(cn, 0);
        Main.LOGGER.info("Patching " + className);
        for (MethodNode method : cn.methods) {
            String name = method.name;
            String desc = method.desc;
            if (name.equals("loadClass") && desc != null && desc.equals("(Ljava/lang/String;Z)Ljava/lang/Class;")) {
                InsnList instructions = method.instructions;
                LabelNode label = null;
                for (AbstractInsnNode instruction : instructions) {
                    if (instruction instanceof LabelNode) {
                        Main.LOGGER.info("Found a label to jump to");
                        label = (LabelNode) instruction;
                        break;
                    }
                }
                if (label == null) {
                    Main.LOGGER.info("No label found, making one");
                    label = new LabelNode();
                    instructions.insertBefore(instructions.get(0), label);
                }
                instructions.insertBefore(getPrevious(label, 0), new InsnNode(176));
                instructions.insertBefore(getPrevious(label, 1), new MethodInsnNode(182, "java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", false));
                instructions.insertBefore(getPrevious(label, 2), new VarInsnNode(25, 1));
                instructions.insertBefore(getPrevious(label, 3), new VarInsnNode(25, 5));
                instructions.insertBefore(getPrevious(label, 4), new MethodInsnNode(184, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false));
                instructions.insertBefore(getPrevious(label, 5), new VarInsnNode(58, 5));
                instructions.insertBefore(getPrevious(label, 6), new MethodInsnNode(183, "java/net/URLClassLoader", "<init>", "([Ljava/net/URL;Ljava/lang/ClassLoader;)V", false));
                instructions.insertBefore(getPrevious(label, 7), new VarInsnNode(25, 4));
                instructions.insertBefore(getPrevious(label, 8), new InsnNode(83));
                instructions.insertBefore(getPrevious(label, 9), new VarInsnNode(25, 3));
                instructions.insertBefore(getPrevious(label, 10), new InsnNode(3));
                instructions.insertBefore(getPrevious(label, 11), new InsnNode(89));
                instructions.insertBefore(getPrevious(label, 12), new TypeInsnNode(189, "java/net/URL"));
                instructions.insertBefore(getPrevious(label, 13), new LdcInsnNode(1));
                instructions.insertBefore(getPrevious(label, 14), new InsnNode(89));
                instructions.insertBefore(getPrevious(label, 15), new TypeInsnNode(187, "java/net/URLClassLoader"));
                instructions.insertBefore(getPrevious(label, 16), new VarInsnNode(58, 4));
                instructions.insertBefore(getPrevious(label, 17), new MethodInsnNode(184, "java/lang/ClassLoader", "getSystemClassLoader", "()Ljava/lang/ClassLoader;", false));
                instructions.insertBefore(getPrevious(label, 18), new VarInsnNode(58, 3));
                instructions.insertBefore(getPrevious(label, 19), new MethodInsnNode(183, "java/net/URL", "<init>", "(Ljava/lang/String;)V", false));
                instructions.insertBefore(getPrevious(label, 20), new LdcInsnNode("file:" + Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
                instructions.insertBefore(getPrevious(label, 21), new InsnNode(89));
                instructions.insertBefore(getPrevious(label, 22), new TypeInsnNode(187, "java/net/URL"));
                instructions.insertBefore(getPrevious(label, 23), new JumpInsnNode(153, label));
                instructions.insertBefore(getPrevious(label, 24), new MethodInsnNode(182, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false));
                instructions.insertBefore(getPrevious(label, 25), new LdcInsnNode("org.altmc.Inject"));
                instructions.insertBefore(getPrevious(label, 26), new VarInsnNode(25, 1));
            }
        }
        Main.LOGGER.info("Patched " + className);
        cn.accept(w);
        return w.toByteArray();
    }

    public byte[] transform(ClassLoader classLoader, String className, Class<?> classGettingRedefined, ProtectionDomain protectionDomain, byte[] bytes) {
        try {
            if (className == null) return bytes;
            if (!patchedClassLoader && (className.equals("cpw/mods/cl/ModuleClassLoader") || className.equals("net/fabricmc/loader/impl/launch/knot/KnotClassDelegate"))) {
                return patchClassLoader(className, bytes);
            }
            if (!patchedMinecraftMain && className.equals("net/minecraft/client/main/Main")) {
                patchedMinecraftMain = true;
                ClassReader r = new ClassReader(bytes);
                ClassWriter w = new ClassWriter(r, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                ClassNode cn = new ClassNode();
                r.accept(cn, 0);
                Main.LOGGER.info("Patching " + className);
                for (MethodNode method : cn.methods) {
                    String name = method.name;
                    String desc = method.desc;
                    if (name.equals("main") && desc != null && desc.equals("([Ljava/lang/String;)V")) {
                        InsnList instructions = method.instructions;
                        instructions.insertBefore(instructions.get(0), new MethodInsnNode(184, "org/altmc/Inject", "testForMicrosoftAuth", "()V", false));
                        instructions.insertBefore(instructions.get(0), new MethodInsnNode(184, "org/altmc/Inject", "saveMinecraftArgs", "([Ljava/lang/String;)V", false));
                        instructions.insertBefore(instructions.get(0), new VarInsnNode(25, 0));
                    }
                }
                Main.LOGGER.info("Patched " + className);
                cn.accept(w);
                return w.toByteArray();
            } else if (!patchedAuthlibYggdrasil && className.equals("com/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService")) {
                patchedAuthlibYggdrasil = true;
                ClassReader r = new ClassReader(bytes);
                ClassWriter w = new ClassWriter(r, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                ClassNode cn = new ClassNode();
                r.accept(cn, 0);
                Main.LOGGER.info("Patching " + className);
                for (MethodNode method : cn.methods) {
                    InsnList instructions = method.instructions;
                    for (AbstractInsnNode instruction : instructions) {
                        boolean isFieldNode;
                        if ((isFieldNode = instruction instanceof FieldInsnNode && (instruction.getOpcode() == Opcodes.GETSTATIC && (((FieldInsnNode) instruction).name.equals("JOIN_URL") || ((FieldInsnNode) instruction).name.equals("CHECK_URL") || ((FieldInsnNode) instruction).name.equals("BASE_URL")))) || (instruction instanceof LdcInsnNode && instruction.getOpcode() == Opcodes.LDC && ((LdcInsnNode) instruction).cst instanceof String && ((String) ((LdcInsnNode) instruction).cst).startsWith(Inject.originalAuthServer))) {
                            instructions.insert(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC, "org/altmc/Inject", "getAuthServer", "(Ljava/lang/Object;)Ljava/lang/Object;", false));
                            String type;
                            if (isFieldNode) {
                                type = ((FieldInsnNode) instruction).desc;
                                type = type.substring(1, type.length() - 1);
                            } else {
                                type = ((LdcInsnNode) instruction).cst.getClass().getTypeName().replaceAll("\\.", "/");
                            }
                            instructions.insert(instruction.getNext(), new TypeInsnNode(Opcodes.CHECKCAST, type));
                        }
                    }
                }
                Main.LOGGER.info("Patched " + className);
                cn.accept(w);
                return w.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
