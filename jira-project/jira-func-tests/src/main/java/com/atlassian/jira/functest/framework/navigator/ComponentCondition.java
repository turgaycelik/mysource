package com.atlassian.jira.functest.framework.navigator;

import net.sourceforge.jwebunit.WebTester;

/**
 * Navigator condition that can be used to specify components to search for.
 * 
 * @since v4.0
 */
public class ComponentCondition extends MultiSelectCondition
{
    public ComponentCondition()
    {
        super("component");
    }

    public ComponentCondition(final ComponentCondition componentCondition)
    {
        super(componentCondition);
    }

    public NavigatorCondition copyCondition()
    {
        return new ComponentCondition(this);
    }

    public NavigatorCondition copyConditionForParse()
    {
        return new ComponentCondition();
    }


}
