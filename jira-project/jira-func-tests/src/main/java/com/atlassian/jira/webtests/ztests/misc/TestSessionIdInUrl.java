package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.WebTesterFactory;
import com.meterware.httpunit.WebLink;
import org.xml.sax.SAXException;

import static com.atlassian.jira.permission.ProjectPermissions.BROWSE_PROJECTS;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestSessionIdInUrl extends FuncTestCase
{
    public void testBrowseIssue() throws SAXException
    {
        administration.restoreBlankInstance();

        navigation.logout();

        // Ensure the "Remember Me" option is checked
        tester.beginAt("/login.jsp");
        tester.setFormElement("os_username", ADMIN_USERNAME);
        tester.setFormElement("os_password", ADMIN_PASSWORD);
        tester.checkCheckbox("os_cookie", "true");
        tester.setWorkingForm("login-form");
        tester.submit();

        navigation.issue().goToCreateIssueForm(null,null);
        tester.setFormElement("summary", "Bug 1");
        tester.selectOption("versions", "New Version 4");
        tester.selectOption("fixVersions", "New Version 5");
        tester.selectOption("components", "New Component 1");
        tester.selectOption("components", "New Component 2");
        tester.submit("Create");

        text.assertTextPresent(locator.page(), "New Component 2");

        text.assertTextPresent(locator.page(),"New Version 4");
        text.assertTextPresent(locator.page(),"New Version 5");
        tester.assertLinkPresentWithText("New Component 2");
        tester.assertLinkNotPresentWithText("New Version 4");
        tester.assertLinkPresentWithText("New Version 5");

        Long projectId = backdoor.project().getProjectId("HSP");
        Long schemeId = backdoor.project().getSchemes(projectId).permissionScheme.id;
        tester.gotoPage("/secure/admin/EditPermissions!default.jspa?schemeId=" + schemeId);

        tester.clickLink("add_perm_" + BROWSE_PROJECTS.permissionKey());
        tester.checkCheckbox("type", "group");
        tester.submit(" Add ");

        // Delete all cookies - including the session cookie - to replicate the case where the users session has finished
        // and they direct their browser to view an issue directly.

        tester.getDialog().getWebClient().clearContents();
        // Need to ensure that the tester is still pointed at the right tenant though, which clearContents() clears.
        WebTesterFactory.setupWebTester(tester, environmentData);

        tester.gotoPage("/browse/HSP-1");

        text.assertTextPresent(locator.page(), "New Component 2");
        text.assertTextPresent(locator.page(), "New Version 4");
        text.assertTextPresent(locator.page(), "New Version 5");
        tester.assertLinkPresentWithText("New Component 2");
        tester.assertLinkNotPresentWithText("New Version 4");
        tester.assertLinkPresentWithText("New Version 5");

        final WebLink componentLink = tester.getDialog().getResponse().getLinkWith("New Component 2");
        verifyUrl(componentLink.getURLString());

        final WebLink fixVersionLink = tester.getDialog().getResponse().getLinkWith("New Version 5");
        verifyUrl(fixVersionLink.getURLString());
    }

    private void verifyUrl(String url)
    {
        final String JSESSIONID = "jsessionid";
        int index = url.indexOf(JSESSIONID);
        int index2 = url.lastIndexOf(JSESSIONID);
        assertEquals(index, index2); // 0 (both set to -1) or 1 (both have same index) sessionid
    }
}
