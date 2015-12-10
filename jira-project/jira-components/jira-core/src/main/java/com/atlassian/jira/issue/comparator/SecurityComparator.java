/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class SecurityComparator implements java.util.Comparator
{
    public int compare(Object o1, Object o2)
    {
        GenericValue i1 = (GenericValue) o1;
        GenericValue i2 = (GenericValue) o2;

        if (i1 == null && i2 == null)
            return 0;
        else if (i2 == null) // any value is less than null

            return -1;
        else if (i1 == null) // null is greater than any value

            return 1;

        Long securityId1 = i1.getLong("security");
        Long securityId2 = i2.getLong("security");

        if (securityId1 == null && securityId2 == null)
            return 0;
        else if (securityId1 == null)
            return 1;
        else if (securityId2 == null)
            return -1;

        // Retrieve the security levels and compare there names
        IssueSecurityLevelManager issueSecurityLevelManager = ComponentAccessor.getIssueSecurityLevelManager();
        try
        {
            GenericValue issueSecurityLevel1 = issueSecurityLevelManager.getIssueSecurityLevel(securityId1);
            GenericValue issueSecurityLevel2 = issueSecurityLevelManager.getIssueSecurityLevel(securityId2);

            return issueSecurityLevel1.getString("name").compareTo(issueSecurityLevel2.getString("name"));
        }
        catch (GenericEntityException e)
        {
            return 0;
        }
    }
}
