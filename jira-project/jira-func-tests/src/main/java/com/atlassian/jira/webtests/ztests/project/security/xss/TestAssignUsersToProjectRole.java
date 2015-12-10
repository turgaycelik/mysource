package com.atlassian.jira.webtests.ztests.project.security.xss;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for verifying that a malicious user can not perform an XSS attack on the
 * <em>Assign Users to Project Role </em> page.
 * </p>
 *
 * <p>This page is used to manage which JIRA users are associated to a project role.</p>
 * <p>See <a href="http://jdog.atlassian.com/browse/JRADEV-1273">JRADEV-1273</a></p>
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY, Category.PROJECTS })
public class TestAssignUsersToProjectRole extends FuncTestCase
{
    /**
     * Responsible for verifying that a malicious user can not perform an XSS attack through the projectId parameter.
     */
    public void testXssOnProjectIdParameter()
    {
        tester.gotoPage("jira/secure/project/UserRoleActorAction.jspa?projectRoleId=10002&projectId=10000<script>alert('xss exploit');</script>");
        tester.assertTextPresent("&lt;script&gt;alert(&#39;xss exploit&#39;);&lt;/script&gt;");
        tester.assertTextNotPresent("<script>alert('xss exploit');</script>");
    }

    /**
     * Responsible for verifying that a malicious user can not perform an XSS attack through the projectRoleId
     * parameter.
     */
    public void testXssOnProjectRoleIdParameter()
    {
        tester.gotoPage("jira/secure/project/UserRoleActorAction.jspa?projectRoleId=10002<script>alert('xss exploit');</script>&projectId=10000");
        tester.assertTextPresent("&lt;script&gt;alert(&#39;xss exploit&#39;);&lt;/script&gt;");
        tester.assertTextNotPresent("<script>alert('xss exploit');</script>");
    }
}