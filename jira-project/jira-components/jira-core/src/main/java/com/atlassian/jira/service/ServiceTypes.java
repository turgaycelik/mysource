package com.atlassian.jira.service;

import com.atlassian.crowd.embedded.api.User;

/**
 * Represents the service class which implement the {@link JiraService} interface.
 *
 * @since v4.3
 */
public interface ServiceTypes
{
    /**
     * Determines whether the service class has not shipped with JIRA.
     * @return true if the service class has not shipped with JIRA; otherwise, false.
     * @param serviceClassName The name of the service class.
     */
    boolean isCustom(String serviceClassName);

    /**
     * Determines whether the service class has shipped with JIRA.
     * @param serviceClassName The name of the service class.
     * @return true if the service class has not shipped with JIRA; otherwise, false.
     */
    boolean isInBuilt(String serviceClassName);

    /**
     * Determines whether an user is able to perform administrative tasks on services of an specified class.
     *
     * @param user The user in play.
     * @param serviceClassName The class of the service to check.
     * @return true if the user is able to perform administrative tasks on services of this class; otherwise false.
     */
    boolean isManageableBy(User user, String serviceClassName);

}
