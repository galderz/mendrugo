package org.acme.byteman;

import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassAnalysisHelper extends Helper
{
    static final Map<String, List<String>> ALL_CLASS_STACKS = new HashMap<>();
    static final List<Map<String, List<String>>> ITERATION_CLASS_STACKS = new ArrayList<>();

    public ClassAnalysisHelper(Rule rule)
    {
        super(rule);
    }

    public void trackClass(int iteration, Class<?> clazz)
    {
        trackIterationStack(iteration, clazz);

//        System.out.println("[" + System.identityHashCode(this) + " ] Track " + clazz);
//        System.out.println(formatStack());

        ALL_CLASS_STACKS.computeIfAbsent(formatStack(), k -> new ArrayList<>()).add(clazz.toString());
    }

    private void trackIterationStack(int iteration, Class<?> clazz)
    {
        final int index = iteration - 1;

        Map<String, List<String>> iterationStack;
        if (index >= ITERATION_CLASS_STACKS.size()) {
            iterationStack = new HashMap<>();
            ITERATION_CLASS_STACKS.add(iterationStack);
        } else {
            iterationStack = ITERATION_CLASS_STACKS.get(index);
        }

        iterationStack.computeIfAbsent(formatStack(), k -> new ArrayList<>()).add(clazz.toString());
    }

    public void complete()
    {
        System.out.println("Complete");

        System.out.println("Number of stacks: " + ALL_CLASS_STACKS.size());
        for (Map.Entry<String, List<String>> stack : ALL_CLASS_STACKS.entrySet())
        {
            System.out.println("Number of classes in that stack: " + stack.getValue().size());
            System.out.println(stack.getKey());
        }

        

        System.out.println("Iteration 1 summary:");

        final Map<String, List<String>> iterationStacks = ITERATION_CLASS_STACKS.get(0);
        System.out.printf("[iteration-1] Contains %d different stacks:%n", iterationStacks.size());
        for (Map.Entry<String, List<String>> stack : iterationStacks.entrySet()) {
            System.out.println("[iteration-1] Number of classes in that stack: " + stack.getValue().size());
            System.out.println(stack.getKey());
        }
    }
}
