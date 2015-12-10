package com.atlassian.jira.pageobjects.framework.util;

import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.support.How;

import java.lang.annotation.Annotation;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Mappings for each {@link org.openqa.selenium.support.How} to get {@link org.openqa.selenium.By}
 *
 * @since v4.4
 */
public enum HowMappings
{
    ID(How.ID, ByFactories.ID, "id"),
    NAME(How.NAME, ByFactories.NAME, "name"),
    ID_OR_NAME(How.ID_OR_NAME, ByFactories.ID_OR_NAME),
    CLASS_NAME(How.CLASS_NAME, ByFactories.CLASS_NAME, "className"),
    CSS(How.CSS, ByFactories.CSS, "css"),
    TAG_NAME(How.TAG_NAME, ByFactories.TAG_NAME, "tagName"),
    XPATH(How.XPATH, ByFactories.XPATH, "xpath"),
    LINK_TEXT(How.LINK_TEXT, ByFactories.LINK_TEXT, "linkText"),
    PARTIAL_LINK_TEXT(How.PARTIAL_LINK_TEXT, ByFactories.PARTIAL_LINK_TEXT, "partialLinkText");


    private final How how;
    private final Function<String,By> toBy;
    private final String[] annotationMethodNames;

    HowMappings(How how, Function<String, By> toBy, String... annotationMethodNames)
    {
        this.how = how;
        this.toBy = toBy;
        this.annotationMethodNames = annotationMethodNames;
    }

    public static HowMappings forHow(How how)
    {
        final HowMappings answer = safeForHow(how);
        if (answer == null)
        {
            throw new IllegalArgumentException("No mapping for " + how);
        }
        return answer;
    }

    public static HowMappings safeForHow(How how)
    {
        for (HowMappings mapping : values())
        {
            if (mapping.how == how)
            {
                return mapping;
            }
        }
        return null;
    }

    public How how()
    {
        return how;
    }

    public By getBy(String locator)
    {
        return toBy.apply(locator);
    }

    public String getAnnotationValue(Annotation findBy)
    {
        for (String methodName : annotationMethodNames)
        {
            final String value = AnnotationToBy.safeInvoke(findBy, methodName, String.class);
            if (isNotEmpty(value))
            {
                return value;
            }
        }
        return null;
    }

    public boolean hasAnnotationValue(Annotation findBy)
    {
        return isNotEmpty(getAnnotationValue(findBy));
    }

}
