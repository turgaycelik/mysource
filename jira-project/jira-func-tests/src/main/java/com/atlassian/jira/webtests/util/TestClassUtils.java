package com.atlassian.jira.webtests.util;

import com.atlassian.jira.util.Predicate;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.atlassian.jira.util.collect.CollectionUtil.filter;

/**
 * Utility methods to find tests classes.
 * 
 * @since v4.0
 */
public final class TestClassUtils
{
    private static final String FUNC_TEST_PACKAGE = "com.atlassian.jira.webtests.ztests";

    private TestClassUtils()
    {
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("I'm not meant to be created.");
    }

    /**
     * Get all test classes, including JUnit4 POJOs.
     *
     * @param packageName package name
     * @param recursive recursive search
     * @return list of all test classes in given package
     */
    public static List<Class<?>> getTestClasses(String packageName, boolean recursive)
    {
        final ClassLocator<Object> classLocator = ClassLocator.forAnyClass()
                .setPackage(packageName)
                .setAllowInner(true)
                .setPredicate(TestPredicate.INSTANCE);

        if (!recursive)
        {
            classLocator.setLevel(0);
        }

        return classLocator.findClasses();
    }

    public static List<Class<? extends TestCase>> getJUni3TestClasses(String packageName, boolean recursive)
    {
        final ClassLocator<TestCase> classLocator = new ClassLocator<TestCase>(TestCase.class)
                .setPackage(packageName)
                .setAllowInner(true)
                .setPredicate(JUnit3TestPredicate.getInstance());

        if (!recursive)
        {
            classLocator.setLevel(0);
        }

        return classLocator.findClasses();
    }

    public static List<Class<? extends TestCase>> getJUnit3TestClasses(String packageName)
    {
        return getJUni3TestClasses(packageName, true);
    }

    public static List<Class<? extends TestCase>> getAllFuncTests()
    {
        return getJUnit3TestClasses(FUNC_TEST_PACKAGE);
    }

    public static List<Method> getTestMethods(Class<? extends TestCase> testClass)
    {
        return new ArrayList<Method>(filter(Arrays.asList(testClass.getMethods()), IS_TEST_METHOD));
    }

    private static final Predicate<Method> IS_TEST_METHOD = new Predicate<Method>()
    {
        @Override
        public boolean evaluate(Method input)
        {
            return Modifier.isPublic(input.getModifiers())
                    && input.getReturnType().equals(Void.TYPE)
                    && (input.getParameterTypes().length == 0)
                    && input.getName().startsWith("test");
        }
    };

    private final static class JUnit3TestPredicate implements Predicate<Class<? extends TestCase>>
    {
        private final static JUnit3TestPredicate INSTANCE = new JUnit3TestPredicate();

        public static JUnit3TestPredicate getInstance()
        {
            return INSTANCE;
        }

        private JUnit3TestPredicate()
        {
        }

        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            throw new CloneNotSupportedException("I'm a singleton.");
        }

        public boolean evaluate(final Class<? extends TestCase> input)
        {
            final int modifiers = input.getModifiers();
            return !Modifier.isAbstract(modifiers) && Modifier.isPublic(modifiers);
        }
    }

    private final static class TestPredicate implements Predicate<Class<?>>
    {
        final static TestPredicate INSTANCE = new TestPredicate();

        public boolean evaluate(final Class<?> input)
        {
            if (!hasTestName(input.getSimpleName()))
            {
                return false;
            }
            final int modifiers = input.getModifiers();
            return !Modifier.isAbstract(modifiers) && Modifier.isPublic(modifiers);
        }

        private boolean hasTestName(String simpleName)
        {
            return simpleName.startsWith("Test") || simpleName.endsWith("Test");
        }
    }
}
