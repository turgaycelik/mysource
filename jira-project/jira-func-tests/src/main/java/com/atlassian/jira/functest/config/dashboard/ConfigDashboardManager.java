package com.atlassian.jira.functest.config.dashboard;

import java.util.Collection;
import java.util.List;

/**
 * The CRUD operations for the {@link ConfigDashboard}.
 *
 * @since v4.2
 */
public interface ConfigDashboardManager
{
    List<ConfigDashboard> loadDashboards();
    boolean saveDashboards(Collection<? extends ConfigDashboard> dashboards);
}
