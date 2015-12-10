/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.check;

public class CheckAmendment extends AbstractAmendment
{
    public CheckAmendment(int type, String message, String bugId)
    {
        super(type, bugId, message);
    }
}
