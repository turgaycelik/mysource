/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.jelly.JiraDynaBeanTagSupport;
import com.opensymphony.util.TextUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;

import java.util.List;

public class StringContains extends JiraDynaBeanTagSupport
{
    private boolean doesContain = true;

    public void doTag(XMLOutput xmlOutput) throws JellyTagException
    {
        String doesContainStr = (String) getProperties().get("doesContain");
        if (TextUtils.stringSet(doesContainStr))
        {
            doesContain = Boolean.valueOf(doesContainStr).booleanValue();
        }

        List possiblyContains = org.ofbiz.core.util.StringUtil.split((String) getProperties().get("possiblyContains"), ",");
        String value = (String) getProperties().get("value");

        if (doesContain == StringUtils.contains(value, possiblyContains))
        {
            Script body = getBody();
            if (body != null)
            {
                body.run(getContext(), xmlOutput);
            }
        }
    }
}
