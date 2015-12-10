/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.importer.issue;

public class UploadXmlFile extends AbstractImportIssue
{
    public UploadXmlFile()
    {
    }

    public int getActionOrder()
    {
        return 1;
    }
}
