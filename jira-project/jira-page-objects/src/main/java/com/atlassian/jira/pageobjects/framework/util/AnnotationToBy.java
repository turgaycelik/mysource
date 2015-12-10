package com.atlassian.jira.pageobjects.framework.util;

import com.google.common.collect.Sets;
import org.openqa.selenium.By;
import org.openqa.selenium.support.How;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import com.atlassian.jira.util.dbc.Assertions;

import static org.apache.commons.lang.StringUtils.isNotEmpty;


/**
 * <p/>
 * Builds {@link org.openqa.selenium.By} out of arbitrary annotation that follows the pattern of
 * {@link org.openqa.selenium.support.FindBy}.
 *
 * <p/>
 * Partially Copied from Selenium2 cause it can't be reused for custom annotations :P
 *
 * @since v4.4
 */
public final class AnnotationToBy
{
    private static final Logger logger = LoggerFactory.getLogger(AnnotationToBy.class);

    public static final String DEFAULT_LOCATOR_METHOD_NAME = "using";
    private static final String HOW_METHOD_NAME = "how";

    static
    {
        checkHowMappings();
    }

    private static void checkHowMappings()
    {
        for (How how : How.values())
        {
            if (HowMappings.safeForHow(how) == null)
            {
                logger.warn(HowMappings.class.getName() + " is incomplete: no mapping for " + how);
            }
        }
    }

    private AnnotationToBy()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static By build(Annotation findBy)
    {
        return build(findBy, DEFAULT_LOCATOR_METHOD_NAME);
    }

    public static By build(Annotation findBy, String locatorMethodName)
    {
        assertValidAnnotation(findBy, locatorMethodName);
        By ans = buildByFromShortFindBy(findBy);
        if (ans == null)
        {
            ans = buildByFromLongFindBy(findBy, locatorMethodName);
        }
        return ans;
    }

    private static void assertValidAnnotation(Annotation findBy, String locatorMethodName)
    {
        if (getHow(findBy) != null)
        {
            Assertions.is("Locator can't be empty when how is set", isNotEmpty(getLocator(findBy, locatorMethodName)));
        }
        Set<String> finders = Sets.newHashSet();
        if (hasLocator(findBy, locatorMethodName))
        {
            finders.add("USING: " + getLocator(findBy, locatorMethodName));
        }
        for (HowMappings mapping : HowMappings.values())
        {
            if (mapping.hasAnnotationValue(findBy))
            {
                finders.add(mapping + ": " + mapping.getAnnotationValue(findBy));
            }
        }
        Assertions.is(String.format("You must specify at most one location strategy. Number found: %d (%s)", finders.size(),
                finders.toString()), finders.size() == 1
        );
    }

    private static By buildByFromShortFindBy(Annotation findBy)
    {
        for (HowMappings mapping : HowMappings.values())
        {
            if (mapping.hasAnnotationValue(findBy))
            {
                final String locator = mapping.getAnnotationValue(findBy);
                return mapping.getBy(locator);
            }
        }
        return null;
    }

    private static By buildByFromLongFindBy(Annotation findBy, String locatorMethodName)
    {
        final How how = getHow(findBy);
        final String locator = getLocator(findBy, locatorMethodName);
        return HowMappings.forHow(how).getBy(locator);
    }

    private static How getHow(Annotation annotation)
    {
        return safeInvoke(annotation, HOW_METHOD_NAME, How.class);
    }

    private static String getLocator(Annotation annotation, String locatorMethodName)
    {
        return safeInvoke(annotation, locatorMethodName, String.class);
    }

    private static boolean hasLocator(Annotation annotation, String locatorMethodName)
    {
        return isNotEmpty(getLocator(annotation, locatorMethodName));
    }

    static <T> T safeInvoke(Annotation annotation, String methodName, Class<T> returnType)
    {
        try
        {
            final Method method = annotation.getClass().getMethod(methodName);
            return returnType.cast(method.invoke(annotation));
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

}
