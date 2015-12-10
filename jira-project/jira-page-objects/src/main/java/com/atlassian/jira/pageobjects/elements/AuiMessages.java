package com.atlassian.jira.pageobjects.elements;

import com.atlassian.jira.pageobjects.framework.util.JiraLocators;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;

/**
 * Utilities for finding and manipulating AUI messages on JIRA forms.
 *
 * @since 5.1
 */
public final class AuiMessages
{
    private AuiMessages()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static final String AUI_MESSAGE_CLASS = JiraLocators.CLASS_AUI_MESSAGE;

    public static final String AUI_MESSAGE_ERROR_SELECTOR = auiMessageSelector(AuiMessage.Type.ERROR);
    public static final String AUI_MESSAGE_WARNING_SELECTOR = auiMessageSelector(AuiMessage.Type.WARNING);
    public static final String AUI_MESSAGE_SUCCESS_SELECTOR = auiMessageSelector(AuiMessage.Type.SUCCESS);

    public static String auiMessageSelector(AuiMessage.Type type)
    {
        checkType(type);
        return "." + AUI_MESSAGE_CLASS + "." + type.className();
    }





    /**
     * Predicate checking for AUI message of any type.
     *
     * @return prediacte for finding AUI message page elements
     */
    public static Predicate<PageElement> isAuiMessage()
    {
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(@Nullable PageElement input)
            {
                return input.hasClass(AUI_MESSAGE_CLASS);
            }
        };
    }

    public static Predicate<PageElement> isAuiMessageOfType(final AuiMessage.Type type)
    {
        checkType(type);
        return Predicates.and(isAuiMessage(), new Predicate<PageElement>()
        {
            @Override
            public boolean apply(@Nullable PageElement input)
            {
                return input.hasClass(type.className());
            }
        });
    }

    public static Predicate<AuiMessage> isOfType(final AuiMessage.Type type)
    {
        checkType(type);
        return new Predicate<AuiMessage>()
        {
            @Override
            public boolean apply(AuiMessage message)
            {
                return message.isOfType(type);
            }
        };
    }

    private static void checkType(AuiMessage.Type type)
    {
        if (!type.isClassifiable())
        {
            throw new IllegalArgumentException("Cannot look for unclassifiable type: <" + type + ">");
        }
    }
}
