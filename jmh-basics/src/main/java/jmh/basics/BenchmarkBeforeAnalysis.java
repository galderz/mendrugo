package jmh.basics;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.options.TimeValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class BenchmarkBeforeAnalysis
{
    final Effects effects;

    public BenchmarkBeforeAnalysis(Effects effects)
    {
        this.effects = effects;
    }

    void beforeAnalysis()
    {
        registerValueOfConverter(Integer.class);
        registerValueOfConverter(Boolean.class);
        registerValueOfConverter(TimeValue.class);
        registerStringConstructorConverter(String.class);

        reflectionAndUnsafe("markerBegin", "IterationParamsL0");
        registerIterationParamsL1();
        registerIterationParamsL2();
        registerIterationParamsL3();
        reflectionAndUnsafe("markerEnd", "IterationParamsL4");

        reflectionAndUnsafe("markerBegin", "BenchmarkParamsL0");
        registerBenchmarkParamsL1();
        registerBenchmarkParamsL2();
        registerBenchmarkParamsL3();
        reflectionAndUnsafe("markerEnd", "BenchmarkParamsL4");

        registerFormatReflection();
    }

    private void registerFormatReflection()
    {
        effects.registerMethod.accept(method(OutputFormat.class, "iteration", BenchmarkParams.class, IterationParams.class, int.class));
        effects.registerMethod.accept(method(OutputFormat.class, "iterationResult", BenchmarkParams.class, IterationParams.class, int.class, IterationResult.class));
        effects.registerMethod.accept(method(OutputFormat.class, "startBenchmark", BenchmarkParams.class));
        effects.registerMethod.accept(method(OutputFormat.class, "endBenchmark", BenchmarkResult.class));
        effects.registerMethod.accept(method(OutputFormat.class, "startRun"));
        effects.registerMethod.accept(method(OutputFormat.class, "endRun", Collection.class));
        effects.registerMethod.accept(method(OutputFormat.class, "print", String.class));
        effects.registerMethod.accept(method(OutputFormat.class, "println", String.class));
        effects.registerMethod.accept(method(OutputFormat.class, "flush"));
        effects.registerMethod.accept(method(OutputFormat.class, "close"));
        effects.registerMethod.accept(method(OutputFormat.class, "verbosePrintln", String.class));
        effects.registerMethod.accept(method(OutputFormat.class, "write", int.class));
        effects.registerMethod.accept(method(OutputFormat.class, "write", byte[].class));
    }

    private void registerBenchmarkParamsL3()
    {
        final Consumer<String> l3 = reflection(jmhForName(
            "BenchmarkParamsL3"
        ));

        registerPaddingL3(l3);
    }

    void registerBenchmarkParamsL2()
    {
        final List<String> fieldNames = Arrays.asList(
            "benchmark", "generatedTarget", "synchIterations"
            , "threads", "threadGroups", "forks", "warmupForks"
            , "warmup", "measurement"
            , "mode", "params"
            , "timeUnit", "opsPerInvocation"
            , "jvm", "jvmArgs"
        );

        final Class<?> jmhClass = jmhForName("BenchmarkParamsL2");
        final Consumer<String> reflection = reflection(jmhClass);
        final Consumer<String> unsafe = unsafe(jmhClass);

        fieldNames.forEach(fieldName ->
        {
            reflection.accept(fieldName);
            unsafe.accept(fieldName);
        });
    }

    private void registerBenchmarkParamsL1()
    {
        final Consumer<String> l1 = reflection(jmhForName(
            "BenchmarkParamsL1"
        ));

        registerPaddingL1(l1);
    }

    private void registerIterationParamsL3()
    {
        final Consumer<String> l3 = reflection(jmhForName(
            "IterationParamsL3"
        ));

        registerPaddingL3(l3);
    }

    private void registerPaddingL3(Consumer<String> l3)
    {
        l3.accept("q001"); l3.accept("q002"); l3.accept("q003"); l3.accept("q004"); l3.accept("q005"); l3.accept("q006"); l3.accept("q007"); l3.accept("q008");
        l3.accept("q011"); l3.accept("q012"); l3.accept("q013"); l3.accept("q014"); l3.accept("q015"); l3.accept("q016"); l3.accept("q017"); l3.accept("q018");
        l3.accept("q021"); l3.accept("q022"); l3.accept("q023"); l3.accept("q024"); l3.accept("q025"); l3.accept("q026"); l3.accept("q027"); l3.accept("q028");
        l3.accept("q031"); l3.accept("q032"); l3.accept("q033"); l3.accept("q034"); l3.accept("q035"); l3.accept("q036"); l3.accept("q037"); l3.accept("q038");
        l3.accept("q041"); l3.accept("q042"); l3.accept("q043"); l3.accept("q044"); l3.accept("q045"); l3.accept("q046"); l3.accept("q047"); l3.accept("q048");
        l3.accept("q051"); l3.accept("q052"); l3.accept("q053"); l3.accept("q054"); l3.accept("q055"); l3.accept("q056"); l3.accept("q057"); l3.accept("q058");
        l3.accept("q061"); l3.accept("q062"); l3.accept("q063"); l3.accept("q064"); l3.accept("q065"); l3.accept("q066"); l3.accept("q067"); l3.accept("q068");
        l3.accept("q071"); l3.accept("q072"); l3.accept("q073"); l3.accept("q074"); l3.accept("q075"); l3.accept("q076"); l3.accept("q077"); l3.accept("q078");
        l3.accept("q101"); l3.accept("q102"); l3.accept("q103"); l3.accept("q104"); l3.accept("q105"); l3.accept("q106"); l3.accept("q107"); l3.accept("q108");
        l3.accept("q111"); l3.accept("q112"); l3.accept("q113"); l3.accept("q114"); l3.accept("q115"); l3.accept("q116"); l3.accept("q117"); l3.accept("q118");
        l3.accept("q121"); l3.accept("q122"); l3.accept("q123"); l3.accept("q124"); l3.accept("q125"); l3.accept("q126"); l3.accept("q127"); l3.accept("q128");
        l3.accept("q131"); l3.accept("q132"); l3.accept("q133"); l3.accept("q134"); l3.accept("q135"); l3.accept("q136"); l3.accept("q137"); l3.accept("q138");
        l3.accept("q141"); l3.accept("q142"); l3.accept("q143"); l3.accept("q144"); l3.accept("q145"); l3.accept("q146"); l3.accept("q147"); l3.accept("q148");
        l3.accept("q151"); l3.accept("q152"); l3.accept("q153"); l3.accept("q154"); l3.accept("q155"); l3.accept("q156"); l3.accept("q157"); l3.accept("q158");
        l3.accept("q161"); l3.accept("q162"); l3.accept("q163"); l3.accept("q164"); l3.accept("q165"); l3.accept("q166"); l3.accept("q167"); l3.accept("q168");
        l3.accept("q171"); l3.accept("q172"); l3.accept("q173"); l3.accept("q174"); l3.accept("q175"); l3.accept("q176"); l3.accept("q177"); l3.accept("q178");
    }

    void registerIterationParamsL2()
    {
        final List<String> fieldNames = Arrays.asList(
            "type"
            , "count"
            , "timeValue"
            , "batchSize"
        );

        final Class<?> jmhClass = jmhForName("IterationParamsL2");
        final Consumer<String> reflection = reflection(jmhClass);
        final Consumer<String> unsafe = unsafe(jmhClass);

        fieldNames.forEach(fieldName ->
        {
            reflection.accept(fieldName);
            unsafe.accept(fieldName);
        });
    }

    private void registerIterationParamsL1()
    {
        final Consumer<String> l1 = reflection(jmhForName(
            "IterationParamsL1"
        ));

        registerPaddingL1(l1);
    }

    private void registerPaddingL1(Consumer<String> l1)
    {
        l1.accept("p001"); l1.accept("p002"); l1.accept("p003"); l1.accept("p004"); l1.accept("p005"); l1.accept("p006"); l1.accept("p007"); l1.accept("p008");
        l1.accept("p011"); l1.accept("p012"); l1.accept("p013"); l1.accept("p014"); l1.accept("p015"); l1.accept("p016"); l1.accept("p017"); l1.accept("p018");
        l1.accept("p021"); l1.accept("p022"); l1.accept("p023"); l1.accept("p024"); l1.accept("p025"); l1.accept("p026"); l1.accept("p027"); l1.accept("p028");
        l1.accept("p031"); l1.accept("p032"); l1.accept("p033"); l1.accept("p034"); l1.accept("p035"); l1.accept("p036"); l1.accept("p037"); l1.accept("p038");
        l1.accept("p041"); l1.accept("p042"); l1.accept("p043"); l1.accept("p044"); l1.accept("p045"); l1.accept("p046"); l1.accept("p047"); l1.accept("p048");
        l1.accept("p051"); l1.accept("p052"); l1.accept("p053"); l1.accept("p054"); l1.accept("p055"); l1.accept("p056"); l1.accept("p057"); l1.accept("p058");
        l1.accept("p061"); l1.accept("p062"); l1.accept("p063"); l1.accept("p064"); l1.accept("p065"); l1.accept("p066"); l1.accept("p067"); l1.accept("p068");
        l1.accept("p071"); l1.accept("p072"); l1.accept("p073"); l1.accept("p074"); l1.accept("p075"); l1.accept("p076"); l1.accept("p077"); l1.accept("p078");
        l1.accept("p101"); l1.accept("p102"); l1.accept("p103"); l1.accept("p104"); l1.accept("p105"); l1.accept("p106"); l1.accept("p107"); l1.accept("p108");
        l1.accept("p111"); l1.accept("p112"); l1.accept("p113"); l1.accept("p114"); l1.accept("p115"); l1.accept("p116"); l1.accept("p117"); l1.accept("p118");
        l1.accept("p121"); l1.accept("p122"); l1.accept("p123"); l1.accept("p124"); l1.accept("p125"); l1.accept("p126"); l1.accept("p127"); l1.accept("p128");
        l1.accept("p131"); l1.accept("p132"); l1.accept("p133"); l1.accept("p134"); l1.accept("p135"); l1.accept("p136"); l1.accept("p137"); l1.accept("p138");
        l1.accept("p141"); l1.accept("p142"); l1.accept("p143"); l1.accept("p144"); l1.accept("p145"); l1.accept("p146"); l1.accept("p147"); l1.accept("p148");
        l1.accept("p151"); l1.accept("p152"); l1.accept("p153"); l1.accept("p154"); l1.accept("p155"); l1.accept("p156"); l1.accept("p157"); l1.accept("p158");
        l1.accept("p161"); l1.accept("p162"); l1.accept("p163"); l1.accept("p164"); l1.accept("p165"); l1.accept("p166"); l1.accept("p167"); l1.accept("p168");
        l1.accept("p171"); l1.accept("p172"); l1.accept("p173"); l1.accept("p174"); l1.accept("p175"); l1.accept("p176"); l1.accept("p177"); l1.accept("p178");
    }

    void reflectionAndUnsafe(String fieldName, String jmhName)
    {
        final Class<?> jmhClass = jmhForName(jmhName);
        reflection(jmhClass).accept(fieldName);
        unsafe(jmhClass).accept(fieldName);
    }

    Consumer<String> reflection(Class<?> argumentType)
    {
        return fieldName ->
            effects.registerField.accept(field(argumentType, fieldName));
    }

    Consumer<String> unsafe(Class<?> argumentType)
    {
        return fieldName ->
            effects.registerUnsafe.accept(field(argumentType, fieldName));
    }

    void registerValueOfConverter(Class<?> argumentType)
    {
        effects.registerMethod.accept(method(argumentType, "valueOf", String.class));
    }

    void registerStringConstructorConverter(Class<?> argumentType)
    {
        effects.registerMethod.accept(constructor(argumentType, String.class));
    }

    final static class Effects
    {

        final Consumer<Field> registerField;

        final Consumer<Executable> registerMethod;

        final Consumer<Field> registerUnsafe;
        Effects(Consumer<Field> registerField, Consumer<Executable> registerMethod, Consumer<Field> registerUnsafe) {
            this.registerField = registerField;
            this.registerMethod = registerMethod;
            this.registerUnsafe = registerUnsafe;
        }

    }
    static Field field(Class<?> argumentType, String fieldName)
    {
        try
        {
            return argumentType.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    static Method method(Class<?> argumentType, String methodName, Class<?>... parameterTypes)
    {
        try
        {
            return argumentType.getDeclaredMethod(methodName, parameterTypes);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    static Constructor<?> constructor(Class<?> argumentType, Class<?>... parameterTypes)
    {
        try
        {
            return argumentType.getDeclaredConstructor(parameterTypes);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    static Class<?> jmhForName(String className)
    {
        try
        {
            return Class.forName(String.format(
                "org.openjdk.jmh.infra.%s"
                , className
            ));
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}
