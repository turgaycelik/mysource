/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import org.apache.commons.lang.StringUtils;

public class StringUtilsBean
{
    public String leftPad(String string, int pad)
    {
        return StringUtils.leftPad(string, pad);
    }
}
