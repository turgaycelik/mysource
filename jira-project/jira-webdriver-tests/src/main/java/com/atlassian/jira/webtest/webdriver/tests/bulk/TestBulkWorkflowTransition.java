package com.atlassian.jira.webtest.webdriver.tests.bulk;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.form.FormUtils;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.navigator.BulkEdit;
import com.atlassian.jira.pageobjects.navigator.BulkOperationProgressPage;
import com.atlassian.jira.pageobjects.navigator.ChooseOperation;
import com.atlassian.jira.pageobjects.navigator.IssueNavigatorResults;
import com.atlassian.jira.pageobjects.navigator.TransitionOperationConfirmation;
import com.atlassian.jira.pageobjects.navigator.TransitionOperationDetails;
import com.atlassian.jira.pageobjects.navigator.TransitionsDetails;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.List;
import javax.inject.Inject;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @see {@link com.atlassian.jira.webtests.ztests.bulk.TestBulkWorkflowTransition}
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.BULK_OPERATIONS, Category.WORKFLOW })
public class TestBulkWorkflowTransition extends BaseJiraWebTest
{
    @Inject PageElementFinder elementFinder;

    protected static final String NOT_AVAILABLE_BULK_EDIT = "NOTE: This field is not available for bulk update operations.";
    protected static final String WORKFLOW_TRANSITION_CHOOSE_ERROR_TEXT = "Please select a transition to execute";
    protected static final String WORKFLOW_TRANSITION_EDIT_TEXT = "Select and edit the fields available on this transition.";
    protected static final String WORKFLOW_TRANSITION_MULTI_PROJECT_ERROR = "NOTE: This operation can be performed only on issues from ONE project.";
    protected static final String WORKFLOW_TRANSITION_SELECTION_TEXT = "Select the workflow transition to execute on the associated issues";
    protected static final String WORKFLOW_TRANSITION_CONFIRMATION_TEXT = "Please confirm the details of this operation.";
    protected static final String WORKFLOW_TRANSITION_PROGRESS_TEXT = "Bulk Operation Progress";

    private static final String COMMENT_1 = "This issue is resolved now.";
    private static final String COMMENT_2 = "Viewable by developers group.";
    private static final String COMMENT_3 = "Viewable by Developers role.";
    public static final String TABLE_EDITFIELDS_ID = "screen-tab-1-editfields";

    public static final int BULK_CHANGE = 33;

    // The preconditions and assumptions about permissions for this test.
    protected TransitionsDetails setup()
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, "admin");
        backdoor.permissions().addGlobalPermission(BULK_CHANGE, Groups.USERS);
        return _testToOperationDetailsWorkflowTranisition();
    }

    @After
    public void tearDown()
    {
        backdoor.permissions().removeGlobalPermission(BULK_CHANGE, Groups.USERS);
    }

    /**
     * Issue Level Security
     */
    @Test
    @Restore("TestBulkWorkflowTransitionEnterprise2.xml")
    public void testForIssueSchemeConflictsBetweenProjects()
    {
        TransitionsDetails page = setup();
        TransitionOperationDetails step2 = page.chooseWorkflowAction("jira_5_5");

        assertEquals(elementFinder.find(By.cssSelector(".aui-page-header + p")).getText(), WORKFLOW_TRANSITION_EDIT_TEXT);
        // assert that multiple tabs exist.
        assertEquals("There should be two AUI tabs", 2, step2.screenTabs().size());
        assertEquals("Second Tab", step2.screenTabs().get(1).getText());

        PageElement tab = step2.switchToScreenTab("Second Tab");
        assertEquals(tab.getAttribute("id"), "screen-tab-2");

        // Check for Security scheme warning
        List<PageElement> cells = tab.findAll(By.cssSelector("tr:nth-child(18) > td"));
        assertEquals("N/A", StringUtils.trim(cells.get(0).getText()));
        assertEquals("Change Security Level", StringUtils.trim(cells.get(1).getText()));
        assertEquals("NOTE: The projects of the selected issues are associated with different issue level security schemes.", StringUtils.trim(cells.get(2).getText()));

        // Set a resolution
        step2.switchToScreenTab("Field Tab");
        selectOption("resolution", "Fixed");

        TransitionOperationConfirmation step3 = step2.submit();
        assertEquals(elementFinder.find(By.cssSelector(".aui-page-header + p")).getText(), WORKFLOW_TRANSITION_CONFIRMATION_TEXT);

        BulkOperationProgressPage step4 = step3.confirm();
        assertEquals(elementFinder.find(By.cssSelector("h3.formtitle")).getText(), WORKFLOW_TRANSITION_PROGRESS_TEXT);

        IssueNavigatorResults results = step4.submit().getResults();
        assertEquals("RESOLVED", results.selectIssue("TST-8").getStatus());
        assertEquals("Fixed", results.selectIssue("TST-8").getResolution());
    }

    /**
     * Issue Level Security
     */
    @Test
    @Restore("TestBulkWorkflowTransitionEnterprise3.xml")
    public void testForIssueSchemeAvailableForIssuesOfProjects()
    {
        TransitionsDetails page = setup();
        TransitionOperationDetails step2 = page.chooseWorkflowAction("jira_5_5");

        checkCheckbox("actions", "resolution");
        selectOption("resolution", "Fixed");

        step2.switchToScreenTab("Second Tab");
        checkCheckbox("actions", "security");

        selectOption("security", "High");

        TransitionOperationConfirmation step3 = step2.submit();
        assertThat(step3.getUpdatedFields().values(), contains("High", "Fixed"));

        BulkOperationProgressPage step4 = step3.confirm();
        assertEquals(elementFinder.find(By.cssSelector("h3.formtitle")).getText(), WORKFLOW_TRANSITION_PROGRESS_TEXT);
        step4.submit();

        ViewIssuePage issue = jira.goToViewIssue("TST-1");
        assertEquals("Fixed", issue.getResolution());
        assertEquals("RESOLVED", issue.getStatus());
    }

    /**
     * Workflows - ensure post function executed
     */
    @Test
    @Restore("TestBulkWorkflowTransitionEnterprise.xml")
    public void testEnsurePostFunctionExecutedInWorkflow()
    {
        TransitionsDetails page = setup();
        TransitionOperationDetails step2 = page.chooseWorkflowAction("Second Workflow_2_6");

        checkCheckbox("actions", "resolution");
        selectOption("resolution", "Fixed");

        step2.switchToScreenTab("Second Tab");
        checkCheckbox("actions", "description");
        setFormElement("description", "Functional Test description");

        TransitionOperationConfirmation step3 = step2.submit();
        assertThat(step3.getUpdatedFields().values(), contains("Functional Test description", "Fixed"));

        BulkOperationProgressPage step4 = step3.confirm();
        assertEquals(elementFinder.find(By.cssSelector("h3.formtitle")).getText(), WORKFLOW_TRANSITION_PROGRESS_TEXT);
        step4.submit();

        ViewIssuePage issue = jira.goToViewIssue("TSTWO-1");

        assertThat(issue.getDescriptionValue().getText(), containsString("Post Function has been fired"));
    }

    protected TransitionsDetails _testToOperationDetailsWorkflowTranisition()
    {
        AdvancedSearch searchPage = jira.visit(AdvancedSearch.class);
        searchPage = searchPage.enterQuery("").submit();

        BulkEdit bulkEdit = searchPage.toolsMenu().open().bulkChange();
        ChooseOperation bulkEdit2 = bulkEdit.selectAllIssues().chooseOperation();
        TransitionsDetails bulkEdit3 = bulkEdit2.transitionIssues();
        return bulkEdit3;
    }

    protected void selectOption(final String fieldName, final String value)
    {
        SelectElement selectList = elementFinder.find(By.name(fieldName), SelectElement.class);
        selectList.select(Options.text(value));
    }

    protected void checkCheckbox(final String fieldName, final String value)
    {
        final By valueSelector = By.cssSelector("input[name='"+fieldName+"'][value='"+value+"']");
        CheckboxElement checkbox = elementFinder.find(valueSelector, CheckboxElement.class);
        checkbox.check();
    }

    protected void setFormElement(final String fieldName, final String value)
    {
        FormUtils.setElement(elementFinder.find(By.name(fieldName)), value);
    }
}
