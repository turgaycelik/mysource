package com.atlassian.jira.functest.config.service;

import java.util.List;

/**
* Class for CRUD operations related to the ConfigService.
*
* @since v4.1
*/
public interface ConfigServiceManager
{
    List<ConfigService> loadServices();
    boolean saveServices(List<ConfigService> newList);
}
