package com.atlassian.jira.functest.framework.admin;

import java.util.Collection;
import java.util.Map;

/**
 * A Page Object useful for retrieving and setting advanced application properties
 *
 * @since v4.4.5
 */
public interface AdvancedApplicationProperties
{
    Map<String, String> getApplicationProperties();

    void setApplicationProperty(String key, String value);

    String getApplicationProperty(String key);
}
