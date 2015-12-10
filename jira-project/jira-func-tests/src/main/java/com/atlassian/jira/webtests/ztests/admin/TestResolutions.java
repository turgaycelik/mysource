package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests for the administration of resolutions.
 *
 * @since v4.0
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestResolutions extends FuncTestCase
{
    //JRA-18985: It is possible to make a resolution a duplicate.
    public void testSameName() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoAdmin();

        tester.clickLink("resolutions");

        //Check to see that we can't add a resolution of the same name.
        addResolution("Fixed");
        assertDuplicateResolutionError();

        addResolution("fixED");
        assertDuplicateResolutionError();

        //Check to see that we can't change a resolution name to an already existing name.
        tester.gotoPage("secure/admin/EditResolution!default.jspa?id=2");

        //Check to see that we can't edit a resolution such that becomes a duplicate.
        tester.setFormElement("name", "Fixed");
        tester.submit();
        assertDuplicateResolutionError();

        tester.setFormElement("name", "FIXED");
        tester.submit();
        assertDuplicateResolutionError();
    }

    private void assertDuplicateResolutionError()
    {
        assertions.getJiraFormAssertions().assertFieldErrMsg("A resolution with that name already exists.");
    }

    private void addResolution(String name)
    {
        tester.setFormElement("name", name);
        tester.submit();
    }
}