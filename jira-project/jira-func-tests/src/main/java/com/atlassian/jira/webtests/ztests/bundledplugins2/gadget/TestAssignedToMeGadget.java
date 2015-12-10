package com.atlassian.jira.webtests.ztests.bundledplugins2.gadget;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the "Assigned to Me" gadget.
 *
 * @since v4.2
 */
@WebTest({ Category.FUNC_TEST, Category.GADGETS })
public class TestAssignedToMeGadget extends FuncTestCase
{
    // JRA-14238
    public void testXssInImageUrls() throws Exception
    {
        administration.restoreData("TestImageUrlXss.xml");

        tester.gotoPage("/rest/gadget/1.0/issueTable/jql?jql=assignee+%3D+currentUser()+AND+resolution+%3D+unresolved+ORDER+BY+priority+DESC,+created+ASC&num=10&addDefault=true&enableSorting=true&sortBy=null&paging=true&startIndex=0&showActions=true");

        // priority icon URL
        tester.assertTextNotPresent("\"'/><script>alert('prioritiezz');</script>");
        tester.assertTextPresent("&quot;'/&gt;&lt;script&gt;alert('prioritiezz');&lt;/script&gt;");

        // issue type icon URL
        tester.assertTextNotPresent("\"'/><script>alert('issue typezz');</script>");
        tester.assertTextPresent("&quot;'/&gt;&lt;script&gt;alert('issue typezz');&lt;/script&gt;");
    }
}
