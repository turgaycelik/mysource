package com.atlassian.jira.datetime;

/**
 * SPI for DateTimeFormatterStrategy implementations.
 *
 * @since v4.4
 */
interface DateTimeFormatterServiceProvider
{
    /**
     * @see com.atlassian.jira.config.properties.ApplicationProperties#getDefaultBackedString(String)
     */
    String getDefaultBackedString(String key);

    /**
     * @see com.atlassian.jira.web.bean.I18nBean#getUnescapedText(String)
     */
    String getUnescapedText(String key);

    /**
     * @see com.atlassian.jira.web.bean.I18nBean#getText(String, Object)
     */
    String getText(String key, Object... parameters);
}
