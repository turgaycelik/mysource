package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.security.groups.GroupManager;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

public class MultiGroupConverterImpl extends GroupConverterImpl implements MultiGroupConverter
{
    private static final String COMMA_REPLACEMENT = "&#44;";

    public MultiGroupConverterImpl(GroupManager groupManager)
    {
        super(groupManager);
    }

    public Collection extractGroupStringsFromString(String value)
    {
        Collection valuesToAdd = null;
        String[] a = StringUtils.split(StringUtils.replace(value, "\\,", COMMA_REPLACEMENT), ",");

        if (a != null)
        {
            valuesToAdd = new ArrayList();
            for (String s : a)
            {
                valuesToAdd.add(StringUtils.replace(s, COMMA_REPLACEMENT, ",").trim());
            }
        }

        return valuesToAdd;
    }
}
