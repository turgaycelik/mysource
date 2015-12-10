package com.atlassian.jira.webtest.webdriver.tests.visualregression;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Check some of the states a bulk edit operation can end up in.
 *
 * @since v6.0
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.VISUAL_REGRESSION })
@Restore ("xml/TestVisualRegressionSmoke.zip")
public class TestVisualRegressionBulkEdit extends JiraVisualRegressionTest
{
    @Inject private PageElementFinder elementFinder;

    final String targetProjectName = "QA";
    final String targetIssueType = "New Feature";

    @Test
    public void testListingIssuesToPerformBulkOperationOn()
    {
        PageElement nextStep;
        nextStep = startBulkEditProcess();

        assertUIMatches("bulkops-step1");
    }

    @Test
    public void testBulkChooseOperationToPerform()
    {
        PageElement nextStep;
        nextStep = startBulkEditProcess();
        nextStep.click();

        nextStep = chooseBulkOperation("Move Issues");

        assertUIMatches("bulkops-step2");
    }

    @Test
    public void testBulkMoveFromSeveralProjectsDetails()
    {
        PageElement nextStep;
        nextStep = startBulkEditProcess();
        nextStep.click();

        nextStep = chooseBulkOperation("Move Issues");
        nextStep.click();

        assertUIMatches("bulkops-moveissues-choices");
    }

    @Test
    public void testBulkMoveUpdateIssueFields()
    {
        PageElement nextStep;
        nextStep = startBulkEditProcess();
        nextStep.click();

        nextStep = chooseBulkOperation("Move Issues");
        nextStep.click();

        nextStep = setNewProjectsAndIssueTypesForBulkMove();
        nextStep.click();

        assertUIMatches("bulkops-moveissues-update-fields");
    }

    @Test
    @Ignore ("The options on this screens are non-deterministic. I do not know why.")
    public void testBulkTransitionIssueChoices()
    {
        PageElement nextStep;
        nextStep = startBulkEditProcess();
        nextStep.click();

        nextStep = chooseBulkOperation("Transition Issues");
        nextStep.click();

        assertUIMatches("bulkops-transitions-choices");
    }

    @Test
    @Ignore ("The options on the previous screen are non-deterministic, thus the fields on this screen are equally so.")
    public void testBulkTransitionIssueDetails()
    {
        PageElement nextStep;
        nextStep = startBulkEditProcess();
        nextStep.click();

        nextStep = chooseBulkOperation("Transition Issues");
        nextStep.click();

        nextStep = chooseWorkflowTransitionStep("id_jira_2_6");
        nextStep.click();

        assertUIMatches("bulkops-transitions-field-changes");
    }

    private PageElement chooseWorkflowTransitionStep(String workflowStepId)
    {
//        final String xpathFormat = "//h4[contains(text(),'%s')]/following-sibling::table//td[contains(text(),'%s')]/input[@type='radio']";
//        final String xpathSelector = String.format(xpathFormat, "Workflow: " + workflowName, transitionName);
//        final PageElement radioButtonForTransition = elementFinder.find(By.xpath(xpathSelector));
        final PageElement radioButtonForTransition = findElement(By.id(workflowStepId));
        radioButtonForTransition.click();

        return findElement(By.id("next"));
    }

    //
    // And now, the methods that run through the bulk edit process.
    //
    //

    private PageElement startBulkEditProcess()
    {
        // Search for the JQL: project = "BLUK" or (project = "XSS" AND cf[10010] = "test") or project = "ARA" or issuekey = BULK-82
        goTo("/issues/?jql=project%20%3D%20\"BLUK\"%20or%20(project%20%3D%20\"XSS\"%20AND%20cf%5B10010%5D%20%3D%20\"test\")%20or%20project%20%3D%20\"ARA\"%20or%20issuekey%20%3D%20BULK-82");
        /* This finds 10 issues across 4 projects */
        goTo("/secure/views/bulkedit/BulkEdit1!default.jspa?reset=true&tempMax=10");
        clickOnElement("input[type='checkbox'][name='all']"); // Select all checkboxes

        visualComparer.setRefreshAfterResize(false); // Because we don't want browsers throwing modal dialogs in our faces asking to resubmit POSTed information.

        return findElement(By.id("next"));
    }

    private PageElement chooseBulkOperation(String labelName)
    {
        // Find the label with the exact text provided, and click it.
        // If the 'for' attribute of the label is set properly, this will select the radio button we want.
        PageElement optionLabel = findElement(By.xpath("//label[contains(text(),\""+labelName+"\")]"));
        optionLabel.click();

        return findElement(By.id("next"));
    }

    private PageElement setNewProjectsAndIssueTypesForBulkMove()
    {
        // Move three of the four projects' issues in to the target project
        getSSFor("10031_2_project_container").select(targetProjectName);
        getSSFor("10031_1_project_container").select(targetProjectName);
        getSSFor("10001_2_project_container").select(targetProjectName);

        // Convert most of the issue types
        getSSFor("10000_1_issuetype_container").select(targetIssueType);
        getSSFor("10001_1_issuetype_container").select(targetIssueType);
        getSSFor("10020_1_issuetype_container").select(targetIssueType);

        return findElement(By.id("next"));
    }

    //
    // Internal convenience methods to retrieve various page elements
    //

    private PageElement findElement(final By by)
    {
        return elementFinder.find(by);
    }

    private SingleSelect getSSFor(final String id)
    {
        final PageElement el = elementFinder.find(By.id(id));
        return pageBinder.bind(SingleSelect.class, el);
    }
}
