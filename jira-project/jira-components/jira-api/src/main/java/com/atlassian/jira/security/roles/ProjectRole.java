package com.atlassian.jira.security.roles;

import com.atlassian.annotations.PublicApi;

/**
 * A way to group users ({@link RoleActor RoleActors}) with projects.
 *
 * <p>An example would be a global role called "testers". If you
 * have a project X and a project Y, you would then be able to configure different RoleActors in the "testers" role
 * for project X than for project Y.</p>
 *
 * <p>You can use ProjectRole objects as the target of Notification and Permission schemes.</p>
 */
@PublicApi
public interface ProjectRole
{
    /**
     * Will return the unique identifier for this project role.
     *
     * @return Long the unique id for this proejct role.
     */
    Long getId();

    /**
     * Will get the name of this role, null if not set.
     *
     * @return name or null if not set
     */
    String getName();

    /**
     * Will get the description of this role, null if not set.
     *
     * @return description or null if not set
     */
    String getDescription();
}
