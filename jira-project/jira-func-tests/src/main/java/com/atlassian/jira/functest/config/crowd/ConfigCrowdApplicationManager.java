package com.atlassian.jira.functest.config.crowd;

import java.util.List;

/**
 * A class related to the CRUD operations for the {@link ConfigCrowdApplication}.
 *
 * @since v4.3
 */
public interface ConfigCrowdApplicationManager
{
    List<ConfigCrowdApplication> loadApplications();
    boolean saveApplications(List<ConfigCrowdApplication> applications);
}
