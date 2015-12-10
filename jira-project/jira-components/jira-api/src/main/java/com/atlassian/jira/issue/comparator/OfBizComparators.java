package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.ofbiz.OfBizStringFieldComparator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

public class OfBizComparators
{
    public static final Comparator<GenericValue> NAME_COMPARATOR = new OfBizStringFieldComparator("name");
}
