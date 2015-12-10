package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.axis.utils.StringUtils;
import org.xml.sax.SAXException;

import java.util.Arrays;
import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestManageLinkClosedIssues extends FuncTestCase
{
    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestManageLinksClosedIssues.xml");
    }

    public void testManageLinkClosedIssue() throws SAXException
    {
        navigation.issue().gotoIssue("HSP-4");
        List<String> classNames = Arrays.asList(StringUtils.split(tester.getDialog().getResponse().getLinkWith("HSP-5").getClassName(), ' '));
        assertTrue("Expected one of the classes to be 'resolution'", classNames.contains("resolution"));
    }

    public void testManageLinkNonClosedIssue() throws SAXException
    {
        navigation.issue().gotoIssue("HSP-5");
        List<String> classNames = Arrays.asList(StringUtils.split(tester.getDialog().getResponse().getLinkWith("HSP-4").getClassName(), ' '));
        assertFalse("Did not expect the 'resolution' class", classNames.contains("resolution"));
        text.assertTextPresent(locator.page(), "HSP-4");
    }
}
