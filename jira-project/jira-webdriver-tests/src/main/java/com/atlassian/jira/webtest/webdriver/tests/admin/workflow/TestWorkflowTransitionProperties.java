package com.atlassian.jira.webtest.webdriver.tests.admin.workflow;

import java.util.Map;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowTransitionPropertiesPage;
import com.atlassian.jira.pageobjects.util.PollerUtil;
import com.atlassian.jira.pageobjects.util.UserSessionHelper;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudo;

import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v6.2
 */
@Restore ("TestWorkflowTransitionProperties.xml")
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PROJECTS, Category.WORKFLOW })
public class TestWorkflowTransitionProperties extends BaseJiraWebTest
{
    private static final String INACTIVE_WORKFLOW = "InactiveWorkflow";
    private static final long CLOSE_ISSUE_TRANSITION = 2;
    private static final long OPEN_STEP = 1;

    private static final String PROPERTY_SEQUENCE = "opsbar-sequence";

    private final Map<String, String> expectedProperties = Maps.newHashMap();
    private UserSessionHelper sessionHelper;

    @Before
    public void before()
    {
        expectedProperties.put("jira.i18n.title", "closeissue.title");
        expectedProperties.put(PROPERTY_SEQUENCE, "60");
        expectedProperties.put("jira.i18n.submit", "closeissue.close");

        sessionHelper = pageBinder.bind(UserSessionHelper.class);
    }

    @Test
    public void testPropertiesTableContent()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();
        assertEquals(expectedProperties, page.getProperties());
    }

    @Test
    public void testAddingPropertiesHappyPath()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();

        page.create().setKey("foo").setValue("bar").submit();
        expectedProperties.put("foo", "bar");

        assertCurrentProperties(page);

        page.create().setKey("lonely").submit();
        expectedProperties.put("lonely", "");

        assertCurrentProperties(page);
    }

    @Test
    public void testAddingPropertiesWithWebsudo()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        final WorkflowTransitionPropertiesPage page = gotoPropertiesPage();

        backdoor.websudo().enable();
        try
        {
            sessionHelper.clearWebSudo();

            //Try and submit a new property, but we will get websudo instead.
            JiraWebSudo webSudo = page.create().setKey("foo").setValue("bar").submitWithWebsudo();
            assertEquals(expectedProperties, getCurrentProperties());

            //Fail with websudo.
            webSudo = webSudo.authenticateFail("badPassword");
            assertEquals(expectedProperties, getCurrentProperties());

            //Now pass. The change should be made on the second request.
            webSudo.authenticate("admin");
            expectedProperties.put("foo", "bar");
            Poller.waitUntilTrue(Conditions.forSupplier(DefaultTimeouts.DEFAULT_AJAX_ACTION, new Supplier<Boolean>() {
                @Override
                public Boolean get() {
                    return expectedProperties.equals(page.getProperties());
                }
            }));
            assertCurrentProperties(page);
        }
        finally
        {
            backdoor.websudo().disable();
        }
    }

    @Test
    public void testDeleteProperties()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();

        page.create().setKey("foo").setValue("bar").submit();
        expectedProperties.put("foo", "bar");
        assertCurrentProperties(page);

        page.property("foo").delete();
        expectedProperties.remove("foo");
        assertCurrentProperties(page);
    }

    @Test
    public void testDeletePropertiesWithWebsudo()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();
        backdoor.websudo().enable();
        try
        {
            sessionHelper.clearWebSudo();

            //Try to remove property, but we will get a websudo instead.
            final JiraWebSudo websudo = page.property(PROPERTY_SEQUENCE).deleteWebsudo();
            assertEquals(expectedProperties, getCurrentProperties());

            //Fail websudo, nothing should happen.
            websudo.authenticateFail("badPassword");
            assertEquals(expectedProperties, getCurrentProperties());

            //The change should be made on the second request.
            websudo.authenticate("admin");
            expectedProperties.remove(PROPERTY_SEQUENCE);

            assertCurrentProperties(page);
        }
        finally
        {
            backdoor.websudo().disable();
        }
    }

    @Test
    public void testAddingDuplicateKey()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();

        page.create().setKey("foo").setValue("bar").submit();
        expectedProperties.put("foo", "bar");
        assertCurrentProperties(page);

        page.create().setKey("foo").setValue("duplicate").submit();
        assertEquals("Error message doesn't exist or is wrong", "Key 'foo' already exists.", page.create().getKeyError());

        assertCurrentProperties(page);
    }

    @Test
    public void testAddingInvalidValue()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();

        page.create().setKey("foo").setValue("<").submit();
        assertEquals("Error message doesn't exist or is wrong", "Invalid character: '<'", page.create().getValueError());

        assertCurrentProperties(page);
    }

    @Test
    public void testAddingWhitelistedJiraKey()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();

        page.create().setKey("jira.permission").setValue("something").submit();
        page.create().setKey("jira.issue.editable").setValue("something").submit();
        page.create().setKey("jira.field.resolution.exclude").setValue("something").submit();
        page.create().setKey("jira.field.resolution.include").setValue("something").submit();

        expectedProperties.put("jira.permission", "something");
        expectedProperties.put("jira.issue.editable", "something");
        expectedProperties.put("jira.field.resolution.exclude", "something");
        expectedProperties.put("jira.field.resolution.include", "something");

        assertCurrentProperties(page);
    }

    @Test
    public void testAddingReservedJiraKeysShouldThrowAnError()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();

        page.create().setKey("jira.i18n.description").setValue("something").submit();
        assertEquals("Error message doesn't exist or is wrong", "Key starts with 'jira.' but is not known to JIRA.", page.create().getKeyError());

        assertCurrentProperties(page);
    }

    @Test
    public void testShowMessageWhenTableEmpty()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();

        page.property("jira.i18n.title").delete();
        page.property(PROPERTY_SEQUENCE).delete();
        page.property("jira.i18n.submit").delete();

        page.isTransitionPropertyTableEmpty();
        assertTrue(getCurrentProperties().isEmpty());
    }

    @Test
    public void testEditPropertyValue()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();

        page.create().setKey("foo").setValue("bar").submit();
        expectedProperties.put("foo", "bar");

        assertCurrentProperties(page);

        page.property("foo").setValue("something").submitUpdate();
        expectedProperties.put("foo", "something");

        assertCurrentProperties(page);
    }

    @Test
    public void testEditPropertyValueWithWebsudo()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();
        backdoor.websudo().enable();
        try
        {
            sessionHelper.clearWebSudo();

            //Try to update property, but we will get a websudo instead.
            final JiraWebSudo websudo = page.property(PROPERTY_SEQUENCE).setValue("70").submitUpdateWebsudo();
            assertEquals(expectedProperties, getCurrentProperties());

            //Fail websudo. Nothing should change. The change should be made on the second request.
            websudo.authenticateFail("badPassword");
            assertEquals(expectedProperties, getCurrentProperties());

            //Assert that the change was made.
            websudo.authenticate("admin");
            expectedProperties.put(PROPERTY_SEQUENCE, "70");
            assertEquals(expectedProperties, page.getProperties());
        }
        finally
        {
            backdoor.websudo().disable();
        }
    }

    @Test
    public void testEditPropertyValueCancellation()
    {
        // Go to the properties for the Start Progress transition of InactiveWorkflow
        WorkflowTransitionPropertiesPage page = gotoPropertiesPage();

        page.create().setKey("foo").setValue("bar").submit();
        expectedProperties.put("foo", "bar");
        assertCurrentProperties(page);

        page.property("foo").setValue("something").cancelUpdate();

        page = gotoPropertiesPage();
        assertCurrentProperties(page);
    }

    private WorkflowTransitionPropertiesPage gotoPropertiesPage()
    {
        return jira.goTo(WorkflowTransitionPropertiesPage.class, INACTIVE_WORKFLOW, false, OPEN_STEP, CLOSE_ISSUE_TRANSITION);
    }

    private Map<String, String> getCurrentProperties()
    {
        return backdoor.workflow().getProperties(INACTIVE_WORKFLOW, false, CLOSE_ISSUE_TRANSITION);
    }

    private void assertCurrentProperties(final WorkflowTransitionPropertiesPage page)
    {
        assertEquals(expectedProperties, page.getProperties());
        assertEquals(expectedProperties, getCurrentProperties());
    }
}
