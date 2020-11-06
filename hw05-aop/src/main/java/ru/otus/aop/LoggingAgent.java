package ru.otus.aop;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class LoggingAgent {

    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        instrumentation.addTransformer(
                new ClassFileTransformer() {
                    @Override
                    public byte[] transform(
                            final ClassLoader loader,
                            final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer) {
                        if ("ru/otus/aop/Main".equals(className)) {
                            return addArgumentLogging(classfileBuffer);
                        } else {
                            return null;
                        }
                    }
                }
        );
    }

    private static byte[] addArgumentLogging(final byte[] classData) {
        final ClassReader reader = new ClassReader(classData);
        final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        reader.accept(
                new ClassVisitor(Opcodes.ASM7, writer) {

                    @Override
                    public MethodVisitor visitMethod(
                            final int access,
                            final String name,
                            final String descriptor,
                            final String signature,
                            final String[] exceptions) {
                        var classWriterMethodVisitor = super.visitMethod(
                                access, name, descriptor, signature, exceptions);
                        return new LoggingInserter(
                                api, classWriterMethodVisitor, access, name, descriptor);
                    }
                },
                0);
        return writer.toByteArray();
    }
}