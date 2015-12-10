package com.atlassian.jira.functest.framework;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * Abstract class for domain specific navigation.  E.g. {@link com.atlassian.jira.functest.framework.navigation.FilterNavigation}
 *
 */
public class AbstractNavigationUtil extends AbstractFuncTestUtil
{
    public AbstractNavigationUtil(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    protected Navigation getNavigation()
    {
        return getFuncTestHelperFactory().getNavigation();
    }
}