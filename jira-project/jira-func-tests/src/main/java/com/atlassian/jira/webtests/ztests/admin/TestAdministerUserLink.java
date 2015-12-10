package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebLink;

import org.xml.sax.SAXException;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;

@WebTest ({Category.FUNC_TEST, Category.ADMINISTRATION, Category.USERS_AND_GROUPS })
public class TestAdministerUserLink extends FuncTestCase
{
    public void testLinkContainsUserName() throws SAXException
    {
        administration.restoreBlankInstance();

        navigation.userProfile().gotoCurrentUserProfile();
        assertAdministerUserLinkIsPresentAndContainsTheUserName();
    }

    private void assertAdministerUserLinkIsPresentAndContainsTheUserName() throws SAXException
    {
        tester.assertLinkPresent("admin_user");
        final WebLink webLink = tester.getDialog().getResponse().getLinkWith("Administer User");
        assertThat(webLink.getURLString(), endsWith("?name=admin"));
    }
}
