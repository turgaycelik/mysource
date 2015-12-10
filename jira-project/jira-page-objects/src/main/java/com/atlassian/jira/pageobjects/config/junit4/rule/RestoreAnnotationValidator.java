package com.atlassian.jira.pageobjects.config.junit4.rule;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.util.junit.AnnotatedDescription;
import com.atlassian.jira.pageobjects.config.ResetData;
import com.atlassian.jira.pageobjects.config.ResetDataOnce;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.runner.Description;

import java.lang.annotation.Annotation;
import java.util.BitSet;
import java.util.Map;

/**
 * Validates test descriptions annotated with:
 * <ul>
 *     <li>{@link com.atlassian.integrationtesting.runner.restore.Restore}</li>
 *     <li>{@link com.atlassian.integrationtesting.runner.restore.RestoreOnce}</li>
 *     <li>{@link com.atlassian.jira.pageobjects.config.ResetData}</li>
 *     <li>{@link com.atlassian.jira.pageobjects.config.ResetDataOnce}</li>
 * </ul>
 *
 * @since 5.1
 */
final class RestoreAnnotationValidator
{
    private static final Map<Integer,Class<? extends Annotation>> METHOD_LEVEL_ANNOTATIONS = ImmutableMap.of(
            2, Restore.class,
            3, ResetData.class
    );

    private static final Map<Integer,Class<? extends Annotation>> CLASS_LEVEL_ANNOTATIONS = ImmutableMap.of(
            0, RestoreOnce.class,
            1, ResetDataOnce.class
    );
    private static final Map<Integer,Class<? extends Annotation>> ALL_ANNOTATIONS = ImmutableMap.<Integer,Class<? extends Annotation>>builder()
            .putAll(METHOD_LEVEL_ANNOTATIONS)
            .putAll(CLASS_LEVEL_ANNOTATIONS)
            .build();

    private RestoreAnnotationValidator()
    {
        throw new AssertionError("Don't instantiate me");
    }

    static void validate(Description testDescription)
    {
        final AnnotatedDescription annotatedDescription =  new AnnotatedDescription(testDescription);
        validateClassLevel(annotatedDescription);
        validateExclusive(annotatedDescription);

    }

    private static void validateClassLevel(AnnotatedDescription description)
    {
        for (Class<? extends Annotation> classLevelAnnotation : CLASS_LEVEL_ANNOTATIONS.values())
        {
            if (description.isMethodAnnotated(classLevelAnnotation))
            {
                throw new IllegalStateException("Test method " + description + " is annotated with a class-level "
                        + "annotation " + classLevelAnnotation.getName());
            }
        }
    }

    private static void validateExclusive(AnnotatedDescription description)
    {
        final BitSet annotations = new BitSet(4);
        for (Map.Entry<Integer,Class<? extends Annotation>> classLevelAnnotation : CLASS_LEVEL_ANNOTATIONS.entrySet())
        {
            if (description.isClassAnnotated(classLevelAnnotation.getValue()))
            {
                annotations.set(classLevelAnnotation.getKey());
            }
        }
        for (Map.Entry<Integer,Class<? extends Annotation>> methodLevelAnnotation : METHOD_LEVEL_ANNOTATIONS.entrySet())
        {
            if (description.hasAnnotation(methodLevelAnnotation.getValue()))
            {
                annotations.set(methodLevelAnnotation.getKey());
            }
        }
        if (annotations.cardinality() > 1)
        {
            throw new IllegalStateException("Test " + description + " is marked with conflicting annotations: "
                    + getAnnotations(annotations));
        }
    }

    private static String getAnnotations(BitSet annotations)
    {
        final ImmutableList.Builder<String> names = ImmutableList.builder();
        for (int i=0; i<annotations.length(); i++)
        {
            if (annotations.get(i))
            {
                names.add(ALL_ANNOTATIONS.get(i).getName());
            }
        }
        return names.build().toString();
    }

}
