package com.atlassian.jira.util;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;

public class EasyFvh
{

    public static FieldValuesHolder build(String s, Object o)
    {
        return new FieldValuesHolderImpl(EasyMap.build(s, o));
    }

    public static FieldValuesHolder build(String s, Object o, String s1, Object o1)
    {
        return new FieldValuesHolderImpl(EasyMap.build(s, o, s1, o1));
    }

    public static FieldValuesHolder build(String s, Object o, String s1, Object o1, String s2, Object o2)
    {
        return new FieldValuesHolderImpl(EasyMap.build(s, o, s1, o1, s2, o2));
    }

    public static FieldValuesHolder buildNull()
    {
        return new FieldValuesHolderImpl();
    }
}
