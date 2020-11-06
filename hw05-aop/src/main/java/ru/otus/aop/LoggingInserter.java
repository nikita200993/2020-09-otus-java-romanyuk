package ru.otus.aop;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.stream.Collectors;

class LoggingInserter extends AdviceAdapter {

    private static final String CONCAT_WITH_CONSTANTS_DESCRIPTOR =
            "(Ljava/lang/invoke/MethodHandles$Lookup;"
                    + "Ljava/lang/String;Ljava/lang/invoke/MethodType;"
                    + "Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;";
    private boolean logAnnotationIsPresent;

    public LoggingInserter(
            final int api,
            final MethodVisitor methodVisitor,
            int access,
            final String name,
            final String descriptor) {
        super(api, methodVisitor, access, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        if (visible && Type.getType(Log.class).equals(Type.getType(descriptor))) {
            logAnnotationIsPresent = true;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    protected void onMethodEnter() {
        if (getArgumentTypes().length == 0 || !logAnnotationIsPresent) {
            return;
        }
        getStatic(
                Type.getType(System.class),
                "out",
                Type.getType(PrintStream.class));
        loadArgs();
        visitInvokeDynamicInsn(
                "makeConcatWithConstants",
                getInvokeDynamicDescriptor(),
                getBootstrapMethodHandleForStringConcatWithConstants(),
                getTemplateStringForStringConcatDynamicInstr(getArgumentTypes().length));
        invokeVirtual(
                Type.getType(PrintStream.class),
                Method.getMethod("void println(String)"));
    }

    private static String getTemplateStringForStringConcatDynamicInstr(final int numArgs) {
        final var strBuilder = new StringBuilder();
        for (int argIndex = 1; argIndex <= numArgs; argIndex++) {
            strBuilder.append("argument")
                    .append(argIndex)
                    .append(": ")
                    .appendCodePoint(1);
            if (argIndex != numArgs) {
                strBuilder.append(System.lineSeparator());
            }
        }
        return strBuilder.toString();
    }

    private String getInvokeDynamicDescriptor() {
        return "(" + Arrays.stream(getArgumentTypes())
                .map(Type::getDescriptor)
                .collect(Collectors.joining())
                + ")Ljava/lang/String;";
    }

    private static Handle getBootstrapMethodHandleForStringConcatWithConstants() {
        return new Handle(
                Opcodes.H_INVOKESTATIC,
                "java/lang/invoke/StringConcatFactory",
                "makeConcatWithConstants",
                CONCAT_WITH_CONSTANTS_DESCRIPTOR,
                false);
    }
}
