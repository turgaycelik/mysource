/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.enterprise;

import com.atlassian.jira.jelly.tag.admin.enterprise.AddIssueSecurity;
import com.atlassian.jira.jelly.tag.admin.enterprise.AddIssueSecurityLevel;
import com.atlassian.jira.jelly.tag.admin.enterprise.CreateIssueSecurityScheme;
import com.atlassian.jira.jelly.tag.admin.enterprise.SelectProjectScheme;
import org.apache.commons.jelly.impl.TagFactory;

public class JiraTagLib extends com.atlassian.jira.jelly.JiraTagLib implements TagFactory
{
    public JiraTagLib()
    {
        super();

        //Enterprise tags
        registerTag("AddIssueSecurity", AddIssueSecurity.class);
        registerTag("AddIssueSecurityLevel", AddIssueSecurityLevel.class);
        registerTagFactory("SelectComponentAssignees", jiraTagFactory);
        registerTag("CreateIssueSecurityScheme", CreateIssueSecurityScheme.class);
        registerTag("SelectProjectScheme", SelectProjectScheme.class);
    }
}
