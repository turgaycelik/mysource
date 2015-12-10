package com.atlassian.jira.i18n;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.message.MessageCollection;

import java.io.Serializable;
import java.util.Locale;

/**
 * Copied from sal-core.
 * <p>
 * IMPORTANT! JRA-25571 Do not add dependency on sal-core to jira-core: this causes Bugs due to ClassLoader issues.
 *
 * Note: to be removed once SAL-214 is fixed.
 *
 * @since v5.0
 */
public abstract class AbstractI18nResolver implements I18nResolver
{
    private static final Serializable[] EMPTY_SERIALIZABLE = new Serializable[0];

    public String getText(String key, Serializable... arguments)
    {
        Serializable[] resolvedArguments = new Serializable[arguments.length];
        for (int i = 0; i < arguments.length; i++)
        {
            Serializable argument = arguments[i];
            if (argument instanceof Message)
            {
                resolvedArguments[i] = getText((Message) argument);
            }
            else
            {
                resolvedArguments[i] = arguments[i];
            }
        }
        return resolveText(key, resolvedArguments);
    }

    public String getText(Locale locale, String key, Serializable... arguments)
    {
        Assertions.notNull(locale);
        Serializable[] resolvedArguments = new Serializable[arguments.length];
        for (int i = 0; i < arguments.length; i++)
        {
            Serializable argument = arguments[i];
            if (argument instanceof Message)
            {
                resolvedArguments[i] = getText(locale, (Message) argument);
            }
            else
            {
                resolvedArguments[i] = arguments[i];
            }
        }
        return resolveText(locale, key, resolvedArguments);
    }

    public String getText(String key)
    {
        return resolveText(key, EMPTY_SERIALIZABLE);
    }

    public String getText(Locale locale, String key)
    {
        Assertions.notNull(locale);
        return resolveText(locale, key, EMPTY_SERIALIZABLE);
    }

    public String getText(Message message)
    {
        return getText(message.getKey(), message.getArguments());
    }

    public String getText(Locale locale, Message message)
    {
        return getText(locale, message.getKey(), message.getArguments());
    }

    /**
     * Subclasses should implement this method to dispatch to a matching language in (in order of preference):
     * <ul>
     *     <li>the user's locale</li>
     *     <li>the application's configured locale, or</li>
     *     <li>the system default locale</li>
     * </ul>
     * @param key the key to translate.
     * @param arguments the arguments to be inserted into the translated string.
     * @return the translated string.
     */
    public abstract String resolveText(String key, Serializable[] arguments);

    /**
     * Subclasses should implement this method to dispatch to a matching language in the given locale.
     * @param locale the locale to translate into.
     * @param key the key to translate.
     * @param arguments the arguments to be inserted into the translated string.
     * @return the translated string.
     */
    public abstract String resolveText(Locale locale, String key, Serializable[] arguments);

    public Message createMessage(String key, Serializable... arguments)
    {
        return new DefaultMessage(key, arguments);
    }

    public MessageCollection createMessageCollection()
    {
        return new DefaultMessageCollection();
    }
}