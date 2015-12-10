package com.atlassian.jira.issue.views.util;

import com.opensymphony.util.TextUtils;

import java.util.Locale;

public class RssViewUtils
{
    private RssViewUtils()
    {
    }

    /**
     * Get language code RSS is i18n'ed in - JRA-12547
     * @param locale the Locale to extract the language code information from
     * @return language code following <a href="http://www.w3.org/TR/REC-html40/struct/dirlang.html#langcodes">RFC1766</a>.
     * returns null if locale is null
     */
    public static String getRssLocale(Locale locale)
    {
        if (locale == null)
        {
            return null;
        }
        return locale.getLanguage() + (TextUtils.stringSet(locale.getCountry()) ? "-" + locale.getCountry().toLowerCase() : "");
    }
}
