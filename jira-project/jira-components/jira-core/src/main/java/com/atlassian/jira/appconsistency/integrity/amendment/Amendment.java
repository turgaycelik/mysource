/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.amendment;

public interface Amendment
{
    public static final int CORRECTION = 0;
    public static final int UNFIXABLE_ERROR = 1;
    public static final int ERROR = 2;

    public String getMessage();

    public String getBugId();

    public boolean isCorrection();

    public boolean isWarning();

    public boolean isError();
}
