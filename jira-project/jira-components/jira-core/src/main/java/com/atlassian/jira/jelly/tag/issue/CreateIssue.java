/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.jira.jelly.IssueContextAccessor;
import com.atlassian.jira.jelly.ProjectContextAccessor;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;

public class CreateIssue extends AbstractCreateIssue implements ProjectContextAccessor, IssueContextAccessor
{
    public CreateIssue(VersionManager versionManager, UserManager userManager)
    {
        super(versionManager, userManager);
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        this.getProperties().remove(KEY_ISSUE_SECURITY);
        super.prePropertyValidation(output);
    }
}
