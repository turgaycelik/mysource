package com.atlassian.jira.webtest.webdriver.tests.security;

import javax.inject.Inject;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.framework.util.JiraLocators;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.PageElementJavascript;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@WebTest ({ com.atlassian.jira.functest.framework.suite.Category.WEBDRIVER_TEST })
public class TestAccessLogFilter extends BaseJiraWebTest
{
    public static final String USERNAME = "fre09-. _*~'()!%+\u263A\u200B\u4E80";

    public static final String X_AUSERNAME = "X-AUSERNAME";

    private static final String JS_SELF_XHR = "var req = new XMLHttpRequest();"
                                            + "req.open('GET', document.location, false);"
                                            + "req.send(null);"
                                            + "return decodeURIComponent(req.getResponseHeader(arguments[1]))";
    public static final String JS_META_USERNAME = "return AJS.Meta.get('remote-user')";

    @Inject
    PageElementFinder elementFinder;

    @Before
    public void setUp() throws Exception
    {
        backdoor.usersAndGroups().addUser(USERNAME, USERNAME, USERNAME, "fred09@example.com");
        jira.gotoLoginPage().loginAndGoToHome(USERNAME, USERNAME);
    }

    @After
    public void tearDown() throws Exception
    {
        backdoor.usersAndGroups().deleteUser(USERNAME);
    }

    /**
     * AccessLogFilter is supposed to add username header to every request, some plugins depend on that and also on fact
     * that header value is compatible with javascript decodeURIComponent
     */
    @Test
    public void testRecordUserInformation()
    {
        final PageElementJavascript javascript = elementFinder.find(JiraLocators.body()).javascript();
        final String headerUsername = javascript.execute(JS_SELF_XHR, X_AUSERNAME).toString();
        final String metaUsername = javascript.execute(JS_META_USERNAME).toString();

        assertEquals("Decoded value from header equals provided user name", headerUsername, USERNAME);
        assertEquals("Meta user name equals decoded header user name", metaUsername, headerUsername);
    }
}