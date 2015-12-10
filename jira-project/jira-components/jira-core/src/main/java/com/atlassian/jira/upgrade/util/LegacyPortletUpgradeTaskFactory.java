package com.atlassian.jira.upgrade.util;

import java.util.Map;

/**
 * Provides LegacyPortlet to Gadget upgrade tasks.
 *
 * @since v4.0
 */
public interface LegacyPortletUpgradeTaskFactory
{

    /**
     * Returns an immutable map of portletKey to upgrade task mapping.
     *
     * @return an immutable map of portletKey to upgrade task mapping.
     */
    Map<String, LegacyPortletUpgradeTask> createPortletToUpgradeTaskMapping();
}
