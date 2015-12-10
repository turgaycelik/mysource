package com.atlassian.jira.web.action.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.util.Locale;

/**
 * This class exists to get around problems with calling out to other methods in velocity.
 * <p/>
 * For the date pickers, we need to include the javascript for the calendar.  However, many of the
 * date pickers are generated in velocity, where it is not easy to call out to WebResourceManager (see
 * <code>macros.vm</code>).
 * <p/>
 * This class gives you a nice way of including resources.
 *
 * @see com.atlassian.jira.issue.customfields.CustomFieldUtils#buildParams(com.atlassian.jira.issue.fields.CustomField,com.atlassian.jira.issue.fields.config.FieldConfig,com.atlassian.jira.issue.Issue,com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem,Object,java.util.Map,webwork.action.Action,java.util.Map)
 * @see com.atlassian.jira.issue.fields.AbstractOrderableField#getVelocityParams(com.atlassian.jira.issue.Issue)
 */
public class CalendarResourceIncluder
{
    private static final String CALENDAR_JS_RESOURCE_KEY = "jira.webresources:calendar";
    private static final String CALENDAR_LOCALISATION_RESOURCE_PREFIX = "jira.webresources:calendar-";
    private static final String OBSOLETE_HEBREW_LANGUAGE_CODE = "iw";
    private static final String CURRENT_HEBREW_LANGUAGE_CODE = "he";

    private CalendarLanguageUtil getCalendarLanguageUtil()
    {
        return ComponentAccessor.getComponent(CalendarLanguageUtil.class);
    }

    WebResourceManager getWebResourceManager()
    {
        return ComponentAccessor.getComponent(WebResourceManager.class);
    }

    /**
     * Checks if there is a translation for the pop-up calendar in the language of the given locale.
     *
     * @param locale user's locale.
     * @return true if calendar is translated, false otherwise.
     * @since v3.12
     */
    public boolean hasTranslation(Locale locale)
    {
        // The translation information is held in two places:
        //   - calendar-language-util.properties
        //   - system-webresources-plugin.xml
        //
        // : calendar-language-util.properties
        // This mapping uwas used in the past to find the file name of the localized calendar JavaScript.
        // However, now it only serves the purpose of a check, an entry in this file means that
        // a localized version exists, the actual value is irrelevant as the real mapping is
        // in system-webresources-plugin.xml.
        //
        // : system-webresources-plugin.xml
        // This is where the actual mapping for the localized calendar JavaScript is done.
        //
        // The danger is that "the same" information is held in two places and could possible go out of sync,
        // this could result in not including calendar for languages we have localized (degradation of UI) or
        // trying to include the calendar but not having the localized file (which is worse, error in the user's face).

        return getCalendarLanguageUtil().hasTranslationForLanguage(getLanguageCode(locale));
    }

    /**
     * Call this method when you want to include the "jira.webresources:calendar" resource (probably because you
     * are about to use the DHTML calendar)
     *
     * @param locale user's locale
     * @since v3.12
     * @deprecated since 4.3. use {@link #includeForLocale(java.util.Locale)} instead.
     */
    @Deprecated
    public void includeIfTranslated(Locale locale)
    {
        if (hasTranslation(locale))
        {
            includeCalendarResources(locale);
        }
    }

    private void includeCalendarResources(Locale locale)
    {
        getWebResourceManager().requireResource(CALENDAR_JS_RESOURCE_KEY);
        getWebResourceManager().requireResource(CALENDAR_LOCALISATION_RESOURCE_PREFIX + getLanguageCode(locale));
    }

    private String getLanguageCode(Locale locale)
    {
        if (locale == null)
        {
            return "unsupported";
        }
        String languageCode = locale.getLanguage();
        // JRA-23371: hack for hebrew support. Java is silly and returns an obsolete ISO 639-1 code for hebrew.
        if (OBSOLETE_HEBREW_LANGUAGE_CODE.equals(languageCode))
        {
            languageCode = CURRENT_HEBREW_LANGUAGE_CODE;
        }
        return languageCode;
    }

    /**
     * Include the javascript resources for the DHTML calendar.
     *
     * If a translation is not found for the locale
     * then an English translation is used. See JRA-23434.
     *
     * @param locale The user's locale.
     * @since v4.2
     */
    public void includeForLocale(Locale locale)
    {
        if (hasTranslation(locale))
        {
            includeCalendarResources(locale);
        }
        else
        {
            includeCalendarResources(null);
        }
    }
}
