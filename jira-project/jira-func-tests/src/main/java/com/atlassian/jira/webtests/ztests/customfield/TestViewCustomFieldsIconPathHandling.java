package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * This test introduced to verify a fix for JRA-15536. Previously a template that rendered the issue type icon on the
 * view custom fields page. When the issue icon was an absolute url the url was bogus because the jira application context
 * path was prefixed to the absolute url resulting in garbage.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.CUSTOM_FIELDS })
public class TestViewCustomFieldsIconPathHandling extends FuncTestCase
{

    protected void setUpTest()
    {
        administration.restoreData("TestViewCustomFieldsIconPathHandling.xml");
    }

    /**
     * This test is quite simple, it logs in as admin and then jumps to view custom fields.
     *
     * A number of asserts are made to verify absolute http, absolute https, and relative urls are emitted correctly.
     */
    public void testViewCustomFields() throws Exception
    {

        // login as admin.
        this.navigation.gotoAdmin();

        // jump to view custom fields...
        this.navigation.gotoCustomFields();

        // start asserting the html thats produced...
        final String contextPath = this.getEnvironmentData().getContext();
        final String absoluteHttpUrl = "http://localhost:8080/jira/images/icons/bug.gif";
        final String absoluteHttpsUrl = "https://localhost:8443/jira/images/icons/improvement.gif";
        final String relativeUrl = "/images/icons/issuetypes/newfeature.png";

        this.assertBodyContains( absoluteHttpUrl );
        this.assertBodyDoesntContain( contextPath + absoluteHttpUrl );

        this.assertBodyContains( absoluteHttpsUrl );
        this.assertBodyDoesntContain( contextPath + absoluteHttpsUrl );

        this.assertBodyContains( contextPath + relativeUrl );
        // dunno why better double check doesnt double prefix...
        this.assertBodyDoesntContain( contextPath + contextPath + relativeUrl );

        // logout...
        navigation.logout();
    }

    void assertBodyContains(final String url)
    {
        final String body = this.tester.getDialog().getResponseText();
        final String srcEqualsUrl = this.makeIntoSrcAttribute(url);

        final int index = body.indexOf(srcEqualsUrl);
        if (-1 == index)
        {
            fail("The body of the view custom fields page should contain \"" + srcEqualsUrl + "\" but doesnt...\nbody:" + body);
        }

    }

    void assertBodyDoesntContain(final String url)
    {
        // a special case tests because the windows installer build uses an empty contextpath and wrongly fails.
        final String contextPath = this.getEnvironmentData().getContext();
        if( 0 == contextPath.length() ){
            return;
        }

        final String body = this.tester.getDialog().getResponseText();
        final String srcEqualsUrl = this.makeIntoSrcAttribute(url);

        final int index = body.indexOf(srcEqualsUrl);
        if (-1 != index)
        {
            fail("The body of the view custom fields page contains \"" + srcEqualsUrl + "\" when it shouldnt...\nbody:" + body);
        }
    }

    /**
     * Helper which takes a url and surrounds it with a src="" with double quotes...
     */
    String makeIntoSrcAttribute(final String url)
    {
        return "src=\"" + url + "\"";
    }

}

