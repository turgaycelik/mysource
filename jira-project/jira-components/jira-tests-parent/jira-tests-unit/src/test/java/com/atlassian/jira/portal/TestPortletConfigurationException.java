/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import com.opensymphony.util.TextUtils;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestPortletConfigurationException
{
    @Test
    public void testConstuct()
    {
        PortletConfigurationException pce = new PortletConfigurationException(new Exception("This exception"));
        assertTrue(TextUtils.stringSet(pce.getMessage()));
    }
}
