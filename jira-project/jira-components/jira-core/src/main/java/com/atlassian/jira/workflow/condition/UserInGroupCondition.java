package com.atlassian.jira.workflow.condition;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.Map;

/**
 * Workflow condition that checks if the caller is in the required argument "group".
 *
 * @since v5.0
 */
public class UserInGroupCondition extends AbstractJiraCondition
{
    @Override
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        ApplicationUser caller = getCallerUser(transientVars, args);
        if (caller == null)
        {
            return false;
        }

        String username = caller.getUsername();
        String groupname = (String) args.get("group");
        return ComponentAccessor.getGroupManager().isUserInGroup(username, groupname);
    }
}
