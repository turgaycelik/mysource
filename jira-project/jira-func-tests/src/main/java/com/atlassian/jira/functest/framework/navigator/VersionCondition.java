package com.atlassian.jira.functest.framework.navigator;

import net.sourceforge.jwebunit.WebTester;

/**
 * A version condition for FixVersions and AffectedVersions form field in the issue navigator simple search.
 * @since v4.0
 */
public class VersionCondition extends MultiSelectCondition
{
    public VersionCondition(String fieldName)
    {
        super(fieldName);
    }

    public VersionCondition(final VersionCondition componentCondition)
    {
        super(componentCondition);
    }

    public NavigatorCondition copyCondition()
    {
        return new VersionCondition(this);
    }

    public NavigatorCondition copyConditionForParse()
    {
        return new VersionCondition(getElementName());
    }


}
