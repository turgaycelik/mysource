/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserUtils;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;

import java.util.Map;

/**
 * Variant of {@link PermissionValidator} which lets the user specify the name of the field specifying the username.
 *
 * @deprecated since 3.5.2 use {@link PermissionValidator} instead. Note this cannot be removed without an upgrade task for existing users.
 */
public class UserPermissionValidator extends AbstractPermissionValidator implements Validator
{
    public static final String NULL_ALLOWED_KEY = "nullallowed";

    /**
     * Looks up a transient var based on a argument passed in.
     * If the transient Var exists then lookup the user with that username and see if they
     * have the permission, also passed in the args.
     */
    public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException
    {
        String varKey = (String) args.get("vars.key");
        if (transientVars.containsKey(varKey))
        {
            String username = (String) transientVars.get(varKey);

            // Check if the username is null and if the NULL_ALLOWED_KEY has been set to 'true' - JRA-2970
            boolean ignoreNull = Boolean.valueOf((String) args.get(NULL_ALLOWED_KEY)).booleanValue();
            if (username == null && ignoreNull)
            {
                // If so, no need to check the permission
                return;
            }

            User user = null;

            if (username != null)
            {
                user = UserUtils.getUser(username);
                if (user == null)
                {
                    throw new InvalidInputException("You don't have the correct permissions - user (" + username + ") not found");
                }
            }

            hasUserPermission(args, transientVars, user);
        }
    }
}
