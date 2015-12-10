package com.atlassian.jira.util;

import com.atlassian.crowd.embedded.api.User;

public interface GroupPermissionChecker
{
    boolean hasViewGroupPermission(String group, User user);
}
