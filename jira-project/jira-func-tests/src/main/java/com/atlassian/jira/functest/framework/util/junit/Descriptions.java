package com.atlassian.jira.functest.framework.util.junit;

import org.junit.runner.Description;

import java.lang.reflect.Method;

/**
 * Factory for JUnit test descriptions.
 *
 * @since 6.0
 */
public final class Descriptions
{
    private Descriptions()
    {
        throw new AssertionError("Don't instantiate me");
    }

    // TODO maybe useful maybe not
//    public static class DescriptionBuilder
//    {
//        private final Class<?> testClass;
//
//        private String methodName;
//        private final List<Annotation> annotations = Lists.newArrayList();
//
//
//        public DescriptionBuilder(Class<?> testClass)
//        {
//            this.testClass = testClass;
//        }
//
//        public DescriptionBuilder methodName(String methodName)
//        {
//            this.methodName = notNull(methodName, "methodName");
//            return this;
//        }
//
//        public DescriptionBuilder annotations(Annotation... annotations)
//        {
//            return annotations(Arrays.asList(annotations));
//        }
//
//        public DescriptionBuilder annotations(Iterable<Annotation> annotations)
//        {
//            this.annotations.addAll(ImmutableList.copyOf(annotations));
//            return this;
//        }
//
//
//        public Description build()
//        {
//            if (methodName != null)
//            {
//                return Description.createTestDescription(testClass, methodName, Iterables.toArray(annotations, Annotation.class));
//            }
//            else
//            {
//                return Description.createSuiteDescription(testClass, Iterables.toArray(annotations, Annotation.class));
//            }
//        }
//
//    }

    public static Description createTestDescription(Class<?> testClass, String testMethodName)
    {
        try
        {
            Method method = testClass.getMethod(testMethodName);
            return Description.createTestDescription(testClass, testMethodName, method.getAnnotations());
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException("Unable to find method with name " + testMethodName, e);
        }
    }
}
