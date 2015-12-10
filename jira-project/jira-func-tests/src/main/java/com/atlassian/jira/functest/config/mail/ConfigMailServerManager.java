package com.atlassian.jira.functest.config.mail;

import java.util.List;

/**
* A class related to the CRUD operations for the {@link ConfigMailServer}.
*
* @since v4.1
*/
public interface ConfigMailServerManager
{
    List<ConfigMailServer> loadServers();
    boolean saveServers(List<ConfigMailServer> newList);
}
