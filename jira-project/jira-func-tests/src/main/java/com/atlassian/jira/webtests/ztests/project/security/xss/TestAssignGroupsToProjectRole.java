package com.atlassian.jira.webtests.ztests.project.security.xss;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for verifying that a malicious user can not perform an XSS attack on the
 * <em>Assign Groups to Project Role </em> page. 
 * </p>
 *
 * <p>This page is used to manage which JIRA groups are associated to a project role.</p>
 * <p>See <a href="http://jdog.atlassian.com/browse/JRADEV-1273">JRADEV-1273</a></p>
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY, Category.PROJECTS })
public class TestAssignGroupsToProjectRole extends FuncTestCase
{

    private final static String XSS_ID = "__xss_injected_id__";
    private final static String XSS = "\"/><script id='" + XSS_ID + "'>alert(3);</script>";
    private final static String XSS_ENCODED = "&quot;/&gt;&lt;script id=&#39;__xss_injected_id__&#39;&gt;alert(3);&lt;/script&gt;";

    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    /**
     * Responsible for verifying that a malicious user can not perform an XSS attack through the projectId parameter.
     */
    private void assertXssNotInPage(String url)
    {
        tester.gotoPage(url);
        tester.assertElementNotPresent(XSS_ID);
        tester.assertTextNotPresent(XSS);
        tester.assertTextPresent(XSS_ENCODED);
    }

    public void testXssOnProjectIdParameter()
    {
        assertXssNotInPage("jira/secure/project/GroupRoleActorAction.jspa?projectRoleId=10002&projectId=10000" + XSS);
    }

    /**
     * Responsible for verifying that a malicious user can not perform an XSS attack through the projectRoleId
     * parameter.
     */
    public void testXssOnProjectRoleIdParameter()
    {
        assertXssNotInPage("jira/secure/project/GroupRoleActorAction.jspa?projectRoleId=10002" + XSS +
                "&projectId=10000");
    }

    /**
     * Responsible for verifying that a malicious user can not perform an XSS attack through the groupNames
     * parameter.
     */
    public void testXssOnProjectGroupNamesParameter()
    {
        /* To trigger this xss the surrounding textarea element must first be closed */
        assertXssNotInPage("jira/secure/project/GroupRoleActorAction.jspa?projectRoleId=10002&projectID=10000&groupNames=</textarea>" + XSS);
    }
}