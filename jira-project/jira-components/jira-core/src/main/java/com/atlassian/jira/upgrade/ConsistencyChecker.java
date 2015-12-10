/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade;

import javax.servlet.ServletContext;

public interface ConsistencyChecker
{
    void destroy(ServletContext context);

    void initialise(ServletContext context);

    void checkDataConsistency(final ServletContext context) throws Exception;
}
