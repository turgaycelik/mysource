package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests for the administration of priorities.
 *
 * @since v4.0
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestPriorities extends FuncTestCase
{
    //JRA-18985: It is possible to make a priority a duplicate.
    public void testSameName() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoAdmin();

        tester.clickLink("priorities");

        //Check to see that we can't add a priority of the same name.
        addNewPrioirty("Blocker");
        assertDuplicatePriorityError();

        addNewPrioirty("BlocKER");
        assertDuplicatePriorityError();

        //Check to see that we can't change a priority name to an already existing name.
        tester.gotoPage("secure/admin/EditPriority!default.jspa?id=2");

        //Check to see that we can't edit a priority such that becomes a duplicate.
        tester.setFormElement("name", "Blocker");
        tester.submit();
        assertDuplicatePriorityError();

        tester.setFormElement("name", "blocker");
        tester.submit();
        assertDuplicatePriorityError();
    }

    private void assertDuplicatePriorityError()
    {
        assertions.getJiraFormAssertions().assertFieldErrMsg("A priority with that name already exists.");
    }

    private void addNewPrioirty(String name)
    {
        tester.setFormElement("name", name);
        tester.setFormElement("iconurl", "/images/icons/priorities/blocker.png");
        tester.setFormElement("statusColor", "#efefef");

        tester.submit();
    }
}
