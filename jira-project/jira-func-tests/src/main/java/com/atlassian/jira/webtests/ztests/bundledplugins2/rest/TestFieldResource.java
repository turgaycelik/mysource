package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Field;
import com.atlassian.jira.testkit.client.restclient.FieldClient;

import java.util.Arrays;
import java.util.List;

/**
 * Func tests for FieldResource.
 *
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestFieldResource extends RestFuncTest
{
    private static final String UNSEARCHABLE[] = {"Key", "Votes", "Watchers", "Images"};
    private static final String UNNAVIGABLE[] = {"Comment", "Attachment"};
    private static final String UNORDERABLE[] = {"Key", "Votes", "Resolved", "Updated", "Created", "Status", "Work Ratio", "Project", "Images", "Watchers"};

    private FieldClient fieldClient;

    /**
     * Get all fields
     *
     * @throws Exception if anything goes wrong
     */
    public void testAllFields() throws Exception
    {
        List<Field> fields = fieldClient.get();
        // Test we get at least the number we expect.  Don't want to be fragile if more fields com along
        assertTrue(fields.size() > 40);
        assertFieldsContain(fields, "Affects Version/s", false);
        assertFieldsContain(fields, "Assignee", false);
        assertFieldsContain(fields, "Attachment", false);
        assertFieldsContain(fields, "Comment", false);
        assertFieldsContain(fields, "Component/s", false);
        assertFieldsContain(fields, "Created", false);
        assertFieldsContain(fields, "CSF", true);
        assertFieldsContain(fields, "Description", false);
        assertFieldsContain(fields, "DP", true);
        assertFieldsContain(fields, "DT", true);
        assertFieldsContain(fields, "Due Date", false);
        assertFieldsContain(fields, "Environment", false);
        assertFieldsContain(fields, "Fix Version/s", false);
        assertFieldsContain(fields, "FTF", true);
        assertFieldsContain(fields, "GP", true);
        assertFieldsContain(fields, "II", true);
        assertFieldsContain(fields, "Images", false);
        assertFieldsContain(fields, "Issue Type", false);
        assertFieldsContain(fields, "Key", false);
        assertFieldsContain(fields, "Labels", false);
        assertFieldsContain(fields, "Linked Issues", false);
        assertFieldsContain(fields, "MC", true);
        assertFieldsContain(fields, "MGP", true);
        assertFieldsContain(fields, "MS", true);
        assertFieldsContain(fields, "MUP", true);
        assertFieldsContain(fields, "NF", true);
        assertFieldsContain(fields, "PP", true);
        assertFieldsContain(fields, "Priority", false);
        assertFieldsContain(fields, "Project", false);
        assertFieldsContain(fields, "RB", true);
        assertFieldsContain(fields, "Reporter", false);
        assertFieldsContain(fields, "Resolution", false);
        assertFieldsContain(fields, "Resolved", false);
        assertFieldsContain(fields, "ROTF", true);
        assertFieldsContain(fields, "Security Level", false);
        assertFieldsContain(fields, "SL", true);
        assertFieldsContain(fields, "Status", false);
        assertFieldsContain(fields, "Summary", false);
        assertFieldsContain(fields, "SVP", true);
        assertFieldsContain(fields, "TF", true);
        assertFieldsContain(fields, "Updated", false);
        assertFieldsContain(fields, "UP", true);
        assertFieldsContain(fields, "URL", true);
        assertFieldsContain(fields, "Votes", false);
        assertFieldsContain(fields, "VP", true);
        assertFieldsContain(fields, "Watchers", false);
        assertFieldsContain(fields, "Work Ratio", false);

        // Assert a few fields fully.

    }

    /**
     * Test if fields that are not available to the current logged in user are hidden.
     *
     * @throws Exception if anything goes wrong
     */
    public void testAvailableFields() throws Exception
    {
        List<Field> fields = fieldClient.loginAs("admin", "admin").get();
        assertFieldsContain(fields, "Salary", true);

        fields = fieldClient.loginAs("fred", "fred").get();
        for (Field field : fields)
        {
            if (field.name().equals("Salary"))
            {
                fail("Field 'Salary' should be hidden from user Fred");
            }
        }
    }


    private void assertFieldsContain(List<Field> fields, String name, boolean custom)
    {
        for (Field field : fields)
        {
            if (field.name().equals(name))
            {
                assertEquals(custom, field.custom());
                assertEquals(field.name() + " orderability", !Arrays.asList(UNORDERABLE).contains(field.name()), field.orderable());
                assertEquals(field.name() + " navigability", !Arrays.asList(UNNAVIGABLE).contains(field.name()), field.navigable());
                assertEquals(field.name() + " searchability", !Arrays.asList(UNSEARCHABLE).contains(field.name()), field.searchable());
                return;
            }
        }
        fail("Field " + name + " not in list");
    }



    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        fieldClient = new FieldClient(getEnvironmentData());
        administration.restoreData("TestFieldResource.xml");
    }
}
