package com.atlassian.jira.webtests.ztests.plugin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test verifying that the <tt>PrepareAction</tt> interface is supported by JIRA action plugin module.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.PLUGINS, Category.REFERENCE_PLUGIN })
public class TestWebWork1PrepareActionInterface extends FuncTestCase
{
    public void testPrepareActionCalled() throws Exception
    {
        administration.restoreData("TestWebWork1PrepareActionInterface.xml");
        tester.gotoPage("/PreparedReferenceAction.jspa");
        text.assertTextPresent(locator.id("prepared-message"), "I am ready for anything");
    }

}
