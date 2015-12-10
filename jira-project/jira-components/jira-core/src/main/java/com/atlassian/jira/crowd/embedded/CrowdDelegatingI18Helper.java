package com.atlassian.jira.crowd.embedded;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;

import com.google.common.collect.Maps;

/**
 * Delegate to JIRA's I18Helper.
 *
 * @since v4.3
 */
public class CrowdDelegatingI18Helper implements I18nHelper
{

    @Override
    public String getText(String key)
    {
        com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean();
        return i18nBean.getText(key);
    }

    @Override
    public String getText(String key, String value1)
    {
        com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean();
        return i18nBean.getText(key, value1);
    }

    @Override
    public String getText(String key, String value1, String value2)
    {
        com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean();
        return i18nBean.getText(key, value1, value2);
    }

    @Override
    public String getText(String key, Object parameters)
    {
        com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean();
        return i18nBean.getText(key, parameters);
    }

    @Override
    public String getUnescapedText(String key)
    {
        com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean();
        return i18nBean.getUnescapedText(key);
    }

    @Override
    public Map<String, String> getAllTranslationsForPrefix(final String prefix)
    {
        final com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean();
        final Set<String> keysForPrefix = i18nBean.getKeysForPrefix(prefix);
        final Map<String,String> translations = Maps.newHashMapWithExpectedSize(keysForPrefix.size());

        for (String key : keysForPrefix)
        {
            translations.put(key, i18nBean.getText(key));
        }

        return translations;
    }

    @Override
    public String getText(final Locale locale, final String key, final Serializable... arguments)
    {
        final com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean(locale);
        return i18nBean.getText(key, arguments);
    }
}
