package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.ROLES })
public class TestFullContentShowsRoleEncoded extends FuncTestCase
{
    /* JRA-14541 & JRA-14827 */
    public void testFullContentViewShowsCommentRoleInEncodedForm()
    {
        administration.restoreData("TestFullContentShowsRoleEncoded.xml");
        navigation.issueNavigator().displayFullContentAllIssues();
        Locator page = new WebPageLocator(tester);
        text.assertTextPresent(page.getHTML(), "Users &lt;b&gt;Test&lt;/b&gt; &amp;copy;");
    }
}
