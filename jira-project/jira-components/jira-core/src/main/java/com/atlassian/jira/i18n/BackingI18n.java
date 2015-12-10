package com.atlassian.jira.i18n;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.IteratorEnumeration;
import com.atlassian.jira.util.i18n.I18nTranslationMode;
import com.atlassian.jira.util.resourcebundle.DefaultResourceBundle;
import com.atlassian.jira.web.bean.i18n.TranslationStore;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.Immutable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * DEVSPEED-34: We wanted to properly cache the I18nBean. To make that happen we turned the I18nBean into a flyweight
 * during JIRA 4.3. All of the original logic was moved into this class.
 * <p/>
 * Looking up translations in plugins happens lazily. This (shouldn't) have any real world impact but it makes certain
 * unit tests easier. (You don't need to worry about mocking out PluginAccessor.)
 * <p/>
 * Note that this class is cached by the {@link CachingI18nFactory} per Locale and will
 * be re-created during plugin reload events.
 *
 * @see http://en.wikipedia.org/wiki/Flyweight_pattern
 * @since 4.3
 */
@Immutable
@Internal
@VisibleForTesting
public class BackingI18n implements I18nHelper
{
    private static final Logger log = Logger.getLogger(I18nHelper.class);

    /** Slight optimization to avoid spurious Object[0] creations for messages
     * with no args.
     */
    private static final Object[] EMPTY_ARRAY = { };
    private static final char START_HIGHLIGHT_CHAR = '\uFEFF';  // BOM
    private static final char MIDDLE_HIGHLIGHT_CHAR = '\u26A1'; // lightning
    private static final char END_HIGHLIGHT_CHAR = '\u2060';    // zero width word joiner

    private final Locale locale;
    private final TranslationStore translationMap;
    private final I18nTranslationMode i18nTranslationMode;
    private final List<TranslationTransform> translationTransforms;

    @ClusterSafe
    private final Cache<String, Set<String>> prefixKeysCache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(CacheLoader.from(new PrefixFunction()));

    /**
     * Construct an I18nBean in the given Locale.
     *
     * @param locale the Locale
     * @param i18nTranslationMode whether the magic translate mode is on or not
     * @param translationTransforms a list of active translation transforms to apply
     */
    BackingI18n(
            final Locale locale,
            final I18nTranslationMode i18nTranslationMode,
            final Iterable<? extends TranslationTransform> translationTransforms,
            final TranslationStore translationStore)
    {
        this.i18nTranslationMode = notNull("i18nTranslationMode", i18nTranslationMode);
        this.locale = notNull("locale", locale);
        this.translationTransforms = ImmutableList.copyOf(notNull("translationTransforms", translationTransforms));
        this.translationMap = notNull("translationStore", translationStore);
    }

    public Locale getLocale()
    {
        return locale;
    }

    public ResourceBundle getDefaultResourceBundle()
    {
        return DefaultResourceBundle.getDefaultResourceBundle(locale);
    }

    public Set<String> getKeysForPrefix(final String prefix)
    {
        return prefixKeysCache.getUnchecked(prefix);
    }

    private class PrefixFunction implements Function<String, Set<String>>
    {
        public Set<String> apply(final String prefix)
        {
            final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            for (String key : translationMap.keys())
            {
                if (key.startsWith(prefix))
                {
                    builder.add(key.intern());
                }
            }
            return builder.build();
        }
    }

    /**
     * Get the raw property value, complete with {0}'s.
     *
     * @param key Non-null key to look up
     * @return Unescaped property value for the key, or the key itself if no property with the specified key is found
     */
    @HtmlSafe
    public String getUnescapedText(String key)
    {
        // if the key was not found, escape it in order to thwart XSS attacks. see JRA-21360
        // This has been backed out as it causes double escaping problems. See JRADEV-2996
        key = cleanKey(key);
        final String value = getTranslation(key);
        return (value != null) ? value : key;
    }

    @HtmlSafe
    public String getUntransformedRawText(String key)
    {
        key = cleanKey(key);
        String rawMessage = translationMap.get(key);
        if (rawMessage != null)
        {
            return rawMessage;
        }
        return key;
    }

    @Override
    public boolean isKeyDefined(String key)
    {
        return translationMap.containsKey(cleanKey(key));
    }

    @HtmlSafe
    public String getText(final String key)
    {
        return formatI18nMsg(key);
    }

    @HtmlSafe
    public String getText(final String key, final String value1)
    {
        return formatI18nMsg(key, value1);
    }

    @HtmlSafe
    public String getText(final String key, final String value1, final String value2)
    {
        return formatI18nMsg(key, value1, value2);
    }

    @HtmlSafe
    public String getText(final String key, final String value1, final String value2, final String value3)
    {
        return formatI18nMsg(key, value1, value2, value3);
    }

    @HtmlSafe
    public String getText(final String key, final Object value1, final Object value2, final Object value3)
    {
        return formatI18nMsg(key, value1, value2, value3);
    }

    @HtmlSafe
    public String getText(final String key, final String value1, final String value2, final String value3, final String value4)
    {
        return formatI18nMsg(key, value1, value2, value3, value4);
    }

    @HtmlSafe
    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4)
    {
        return formatI18nMsg(key, value1, value2, value3, value4);
    }

    @HtmlSafe
    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5);
    }

    @HtmlSafe
    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5, value6);
    }

    @HtmlSafe
    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7);
    }

    @HtmlSafe
    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6, final Object value7)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7);
    }

    @HtmlSafe
    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6, final Object value7, final Object value8)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7, value8);
    }

    @HtmlSafe
    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7, final String value8, final String value9)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7, value8, value9);
    }

    @HtmlSafe
    public String getText(final String key, final Object parameters)
    {
        final Object[] substitutionParameters;
        if (parameters instanceof Object[])
        {
            substitutionParameters = (Object[]) parameters;
        }
        else if (parameters instanceof Iterable)
        {
            substitutionParameters = Iterables.toArray((Iterable<?>) parameters, Object.class);
        }
        else if (parameters == null)
        {
            substitutionParameters = EMPTY_ARRAY;
        }
        else
        {
            substitutionParameters = new Object[] { parameters };
        }
        return formatI18nMsg(key, substitutionParameters);
    }

    // Ugly kludge to allow caller to ask for 'key' instead of just the key.
    private static String cleanKey(String key)
    {
        return (key != null && key.length() >= 2 && key.charAt(0) == '\'' && key.charAt(key.length() - 1) == '\'')
                ? key.substring(1, key.length() - 1)
                : key;
    }

    private String formatI18nMsg(String key, final Object... substitutionParameters)
    {
        key = cleanKey(key);
        String rawMessage = getTranslation(key);
        if (rawMessage == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Could not find i18n key: " + key);
            }
            return hilightMsg(key, key, key);
        }

        final MessageFormat mf;
        try
        {
            mf = new MessageFormat(rawMessage, locale);
        }
        catch (final IllegalArgumentException e)
        {
            log.error("Error rendering '" + rawMessage + "': " + e.getMessage(), e);
            throw e;
        }
        final String formattedMsg = mf.format((substitutionParameters == null) ? EMPTY_ARRAY : substitutionParameters);
        return hilightMsg(key, rawMessage, formattedMsg);
    }

    private String getTranslation(String key)
    {
        String rawMessage = translationMap.get(key);
        if (rawMessage != null)
        {
            rawMessage = processTranslationTransforms(key, rawMessage);
        }
        return rawMessage;
    }

    private String processTranslationTransforms(String key, String rawMessage)
    {
        String result = rawMessage;
        for (TranslationTransform translationTransform : translationTransforms)
        {
            result = translationTransform.apply(this.locale, key, result);
        }
        return result;
    }

    private String hilightMsg(String key, String rawMessage, String formattedMsg)
    {
        if (i18nTranslationMode.isTranslationMode())
        {
            if (formattedMsg.equals(rawMessage))
            {
                // slight network traffic optimisation
                return String.format("%c%s%c%s%c%c", START_HIGHLIGHT_CHAR, formattedMsg, MIDDLE_HIGHLIGHT_CHAR, key, MIDDLE_HIGHLIGHT_CHAR, END_HIGHLIGHT_CHAR);
            }
            else
            {
                return String.format("%c%s%c%s%c%s%c", START_HIGHLIGHT_CHAR, formattedMsg, MIDDLE_HIGHLIGHT_CHAR, key, MIDDLE_HIGHLIGHT_CHAR, rawMessage, END_HIGHLIGHT_CHAR);
            }
        }
        else
        {
            return formattedMsg;
        }
    }

    /**
     * Returns the ResourceBundle for this I18nHelper.
     *
     * @return a non-null
     */
    public ResourceBundle getResourceBundle()
    {
        return new MapWrappingResourceBundle(translationMap);
    }

    /**
     * A {@link ResourceBundle} that delegates to a map of message codes to translations.
     */
    private static class MapWrappingResourceBundle extends ResourceBundle
    {
        private final TranslationStore translationMap;

        public MapWrappingResourceBundle(final TranslationStore translationMap)
        {
            Validate.notNull(translationMap);
            this.translationMap = translationMap;
        }

        @Override
        public Enumeration<String> getKeys()
        {
            return IteratorEnumeration.fromIterable(translationMap.keys());
        }

        @Override
        protected Object handleGetObject(final String key)
        {
            return translationMap.get(key);
        }
    }
}


