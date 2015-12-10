package com.atlassian.jira.functest.framework.util.junit;

import org.junit.runner.Description;

import java.lang.annotation.Annotation;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Wrapper around {@link org.junit.runner.Description} to resolve its annotations.
 *
 * @since v4.4
 */
public class AnnotatedDescription
{
    private final Description description;

    public AnnotatedDescription(Description description)
    {
        this.description = notNull(description);
    }

    /**
     * Checks whether the underlying description has given <tt>annotation</tt> - itself or on its parent suite
     * (only if this description is a test).
     *
     * @return <code>true</code> if this description is annotated with given annotation (test method or class)
     */
    public <A extends Annotation> boolean hasAnnotation(Class<A> annotation)
    {
        return getAnnotation(annotation) != null;
    }

    /**
     * Gets annotation of given type for this description, or <code>null</code> if this description is not annotated
     * with <tt>annotation</tt>. Looks up both test method and test class (if applicable).
     *
     * @param annotation annotation to find
     * @param <A> annotation type
     * @return annotation instance for this description, or <code>null</code> if not found
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotation)
    {
        A fromTest = getAnnotationFromTestMethod(annotation);
        if (fromTest == null)
        {
            fromTest = getAnnotationFromTestClass(annotation);
        }
        return fromTest;
    }

    public <A extends Annotation> A getAnnotationFromTestMethod(Class<A> annotation)
    {
        if (description.isTest())
        {
            return description.getAnnotation(annotation);
        }
        else
        {
            return null;
        }
    }

    public <A extends Annotation> A getAnnotationFromTestClass(Class<A> annotation)
    {
        if (description.isTest())
        {
            return description.getTestClass().getAnnotation(annotation);
        }
        else
        {
            return description.getAnnotation(annotation);
        }
    }

    public boolean isAnnotatedWith(Class<? extends Annotation> annotation)
    {
        return getAnnotation(annotation) != null;
    }

    public boolean isMethodAnnotated(Class<? extends Annotation> annotation)
    {
        return getAnnotationFromTestMethod(annotation) != null;
    }

    public boolean isClassAnnotated(Class<? extends Annotation> annotation)
    {
        return getAnnotationFromTestClass(annotation) != null;
    }

    @Override
    public String toString()
    {
        return description.toString();
    }
}
