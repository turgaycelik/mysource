package com.atlassian.jira.functest.framework.navigation;

import com.atlassian.jira.functest.framework.AbstractNavigationUtil;
import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.issue.AttachmentsBlock;
import com.atlassian.jira.functest.framework.navigation.issue.DefaultAttachmentManagement;
import com.atlassian.jira.functest.framework.navigation.issue.DefaultAttachmentsBlock;
import com.atlassian.jira.functest.framework.navigation.issue.DefaultFileAttachmentsList;
import com.atlassian.jira.functest.framework.navigation.issue.DefaultImageAttachmentsGallery;
import com.atlassian.jira.functest.framework.page.ViewIssuePage;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Navigate Issue functionality
 *
 * @since v3.13
 */
public class IssueNavigationImpl extends AbstractNavigationUtil implements IssueNavigation
{
    private static final String WORKFLOW_ACTION_RESOLVE_ISSUE = "action_id_5";
    private static final String WORKFLOW_ACTION_REOPEN_ISSUE = "action_id_3";
    private static final String WORKFLOW_ACTION_CLOSE_ISSUE = "action_id_2";

    public IssueNavigationImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData);
    }

    public ViewIssuePage viewIssue(String issueKey)
    {
        tester.gotoPage("/browse/" + issueKey);
        return new ViewIssuePage(getFuncTestHelperFactory());
    }

    public void viewPrintable(String issueKey)
    {
        tester.gotoPage("/si/jira.issueviews:issue-html/" + issueKey + "/" + issueKey + ".html");
    }

    public void viewXml(String issueKey)
    {
        tester.gotoPage("/si/jira.issueviews:issue-xml/" + issueKey + "/" + issueKey + ".xml");
    }

    public void gotoEditIssue(final String issueKey)
    {
        tester.gotoPage("/browse/" + issueKey);
        tester.clickLink("edit-issue");
    }

    public void gotoEditIssue(long issueId)
    {
        tester.gotoPage("/secure/EditIssue!default.jspa?id=" + issueId);
    }

    public void deleteIssue(String issueKey)
    {
        viewIssue(issueKey);
        tester.clickLink("delete-issue");
        tester.submit("Delete");
    }

    public void gotoIssue(String issueKey)
    {
        if (issueKey == null)
        {
            throw new IllegalArgumentException("IssueKey should not be null.");
        }
        tester.beginAt("/browse/" + issueKey + "?showAll=true");
    }

    public void gotoIssueChangeHistory(String issueKey)
    {
        if (issueKey == null)
        {
            throw new IllegalArgumentException("IssueKey should not be null.");
        }

        tester.beginAt("/browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel");
    }

    public void gotoIssueWorkLog(final String issueKey)
    {
        tester.beginAt("/browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:worklog-tabpanel");
    }

    public String createIssue(final String projectName, final String issueType, final String summary)
    {
        goToCreateIssueForm(projectName, issueType);
        return createIssueDetails(summary, null);
    }

    public String createIssue(final String projectName, final String issueType, final String summary, final Map<String,String[]> params)
    {
        goToCreateIssueForm(projectName, issueType);
        return createIssueDetails(summary, null, params);
    }

    public String createSubTask(final String parentIssueKey, final String subTaskType, final String subTaskSummary, final String subTaskDescription)
    {
        return createSubTask(parentIssueKey, subTaskType, subTaskSummary, subTaskDescription, null);
    }

    public String createSubTask(final String parentIssueKey, final String subTaskType, final String subTaskSummary, final String subTaskDescription, final String originalEstimate)
    {
        createSubTaskStep1(parentIssueKey, subTaskType);
        if (originalEstimate != null)
        {
            tester.setFormElement("timetracking", "2h");
        }
        return createIssueDetails(subTaskSummary, subTaskDescription);
    }


    private String createIssueDetails(final String summary, final String description)
    {
        String issueKey;
        tester.setFormElement("summary", summary);
        if (description != null)
        {
            tester.setFormElement("description", description);
        }
        tester.submit("Create");
        Locator idLocator =  new IdLocator(tester, "key-val");
        issueKey = getIssueKey(idLocator);
        if (issueKey == null)
        {
            Locator locator = new CssLocator(tester, "#cant-browse-warning");
            issueKey = getNoPermissionError(locator);
        }
        return issueKey;
    }

    private String getIssueKey(Locator keyLocator)
    {
        String issueKey = null;
        // find the issue key on the page is in the first row and the second TD
        Node[] tds = keyLocator.getNodes();

        if (tds.length > 0)
        {
            issueKey = keyLocator.getRawText();
            Assert.assertNotNull(issueKey);
        }
        return issueKey;
    }

    private String getNoPermissionError (Locator locator)
    {
        // issue created but user does not have permission to see it?
        final Pattern compile = Pattern.compile("\\((.*-\\d+)\\)");
        Node[] tds = locator.getNodes();
        for (Node td : tds)
        {
            final String text = locator.getText(td);
            if (text != null)
            {
                final Matcher matcher = compile.matcher(text);
                if (matcher.find())
                {
                    return matcher.group(1);
                }
            }
        }
        // don't know what to do?
        Assert.fail("Could not find issue key for newly created issue");
        return null;
    }

    private String createIssueDetails(final String summary, final String description, final Map<String,String[]> params)
    {
        if (params == null)
        {
            return createIssueDetails(summary, description);
        }
        else
        {
            FormParameterUtil formParamHelper = new FormParameterUtil(tester,"issue-create","Create");
            formParamHelper.setFormElement("summary", summary);
            if (description != null)
            {
                formParamHelper.setFormElement("description", description);
            }
            formParamHelper.setParameters(params);
            Node createdIssueNode =  formParamHelper.submitForm();
            // find the issue key on the page is in the first row and the second TD
            Locator issueLocator = new XPathLocator(createdIssueNode, "//*[@id='key-val']");
            String  issueKey =  getIssueKey(issueLocator);
            if (issueKey == null)
            {
                Locator locator = new CssLocator(createdIssueNode, "#cant-browse-warning");
                issueKey = getNoPermissionError(locator);
            }
            return issueKey;
        }
    }

    public void goToCreateIssueForm(final String projectName, final String issueType)
    {
        //make sure we're no longer in the admin section (where the create issue link is no longer displayed).
        if (tester.getDialog().isLinkPresent("leave_admin"))
        {
            tester.clickLink("leave_admin");
        }
        tester.clickLink("create_link");
        if (tester.getDialog().getElement("issuetype") != null)
        {
            if (projectName != null)
            {
                tester.selectOption("pid", projectName);
            }
            if (issueType != null)
            {
                tester.selectOption("issuetype", issueType);
            }
            tester.setWorkingForm("issue-create");
            tester.submit();
        }
        tester.assertTextPresent("CreateIssueDetails.jspa");
        if (issueType != null)
        {
            tester.assertTextPresent(issueType);
        }
    }

    private void createSubTaskStep1(String issueKey, String task_type)
    {
        getAdministration().subtasks().enable();
        viewIssue(issueKey);
        tester.clickLink("create-subtask");
        tester.assertTextPresent("Create Sub-Task");
        if (tester.getDialog().getElement("issuetype") == null)
        {
            log("Bypassing step 1 of sub task creation");
        }
        else
        {
            tester.setWorkingForm("subtask-create-start");
            tester.selectOption("issuetype", task_type);
            tester.submit("Create");
        }
        tester.assertElementPresent("subtask-create-details"); // ID of the Subtask Details
    }

    public void setPriority(String issueKey, String priority)
    {
        viewIssue(issueKey);
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.selectOption("priority", priority);
        tester.submit("Update");
    }

    public void setEnvironment(final String issueKey, final String environment)
    {
        setSingleIssueField(issueKey, "environment", environment);
    }

    public void setDescription(final String issueKey, final String description)
    {
        setSingleIssueField(issueKey, "description", description);
    }

    public void setFreeTextCustomField(final String issueKey, final String customFieldId, final String text)
    {
        setSingleIssueField(issueKey, customFieldId, text);
    }

    private void setSingleIssueField(final String issueKey, final String formElementName, final String value)
    {
        viewIssue(issueKey);
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.setFormElement(formElementName, value);
        tester.submit("Update");
    }

    public void assignIssue(String issueKey, String comment, String userFullName)
    {
        viewIssue(issueKey);
        tester.clickLink("assign-issue");
        tester.selectOption("assignee", userFullName);
        if (comment != null)
        {
            tester.setFormElement("comment", comment);
        }
        tester.submit("Assign");
        tester.assertTextPresent(userFullName);
        if (comment != null)
        {
            tester.assertTextPresent(comment);
        }
    }

    public void assignIssue(final String issueKey, final String userFullName, final String comment, final String commentLevel)
    {
        viewIssue(issueKey);
        tester.clickLink("assign-issue");
        tester.setWorkingForm("assign-issue");

        tester.selectOption("assignee", userFullName);

        if (comment != null)
        {
            tester.setFormElement("comment", comment);
            if (commentLevel != null)
            {
                tester.selectOption("commentLevel", commentLevel);
            }
        }
        tester.submit("Assign");
    }

    public void unassignIssue(final String issueKey, final String comment)
    {
        assignIssue(issueKey, comment, "Unassigned");
    }

    public void unassignIssue(final String issueKey, final String comment, final String commentLevel)
    {
        assignIssue(issueKey, "Unassigned", comment, commentLevel);
    }

    public void resolveIssue(String issueKey, String resolution, String comment)
    {
        viewIssue(issueKey);
        tester.clickLink(WORKFLOW_ACTION_RESOLVE_ISSUE);
        tester.setWorkingForm("issue-workflow-transition");
        tester.selectOption("resolution", resolution);
        tester.setFormElement("comment", comment);
        tester.submit("Transition");
    }

    public void closeIssue(String issueKey, String resolution, String comment)
    {
        viewIssue(issueKey);

        tester.clickLink(WORKFLOW_ACTION_CLOSE_ISSUE);
        tester.setWorkingForm("issue-workflow-transition");
        tester.selectOption("resolution", resolution);
        tester.setFormElement("comment", comment);
        tester.submit("Transition");
    }

    public AttachmentsBlock attachments(final String issueKey)
    {
        viewIssue(issueKey);
        return new DefaultAttachmentsBlock(tester, logger, new DefaultFileAttachmentsList(tester, logger),
                new DefaultImageAttachmentsGallery(tester, logger), new DefaultAttachmentManagement(tester, logger));
    }

    public void resolveIssue(String issueKey, String resolution, String comment, String originalEstimate,
            String remainingEstimate)
    {
        final String originalEstimateFieldId = getOriginalEstimateFieldId();
        final String remainingEstimateFieldId = getRemainingEstimateFieldId();

        viewIssue(issueKey);
        tester.clickLink(WORKFLOW_ACTION_RESOLVE_ISSUE);
        tester.setWorkingForm("issue-workflow-transition");
        tester.selectOption("resolution", resolution);
        tester.setFormElement("comment", comment);

        if (originalEstimate != null)
        {
            tester.setFormElement(originalEstimateFieldId, originalEstimate);
        }
        if (remainingEstimate != null)
        {
            tester.setFormElement(remainingEstimateFieldId, remainingEstimate);
        }
        tester.submit("Transition");
    }

    public void logWork(final String issueKey, final String timeLogged)
    {
        viewIssue(issueKey);
        tester.clickLink("log-work");
        tester.setFormElement("timeLogged", timeLogged);
        tester.submit("Log");
    }

    /**
     * Logs work on the issue with the given key.
     *
     * @param issueKey the key of the issue to log work on.
     * @param timeLogged formatted time spent e.g. 1h 30m.
     * @param newEstimate formatted new estimate e.g. 1d 2h.
     */
    public void logWork(String issueKey, String timeLogged, String newEstimate)
    {
        gotoIssue(issueKey);
        tester.clickLink("log-work");
        tester.setFormElement("timeLogged", timeLogged);
        tester.checkCheckbox("adjustEstimate", "new");
        tester.setFormElement("newEstimate", newEstimate);
        tester.submit("Log");
    }

    public void logWorkWithComment(String issueKey, String timeLogged, String comment)
    {
        gotoIssue(issueKey);
        tester.clickLink("log-work");
        tester.setFormElement("timeLogged", timeLogged);
        tester.checkCheckbox("adjustEstimate", "new");
        if (comment != null)
        {
            tester.setFormElement("comment", comment);
        }
        tester.submit("Log");
    }

    public void reopenIssue(final String issueKey)
    {
        viewIssue(issueKey);
        tester.clickLink(WORKFLOW_ACTION_REOPEN_ISSUE);
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");
    }


    @Override
    public void performIssueActionWithoutDetailsDialog(final String issueKey, final String actionName)
    {
        viewIssue(issueKey);
        tester.clickLinkWithText(actionName);
    }

    public void unwatchIssue(final String issueKey)
    {
        viewIssue(issueKey);
        if (tester.getDialog().getResponseText().contains("watch-state-on"))
        {
            tester.clickLink("toggle-watch-issue");
        }
    }

    public void watchIssue(final String issueKey)
    {
        viewIssue(issueKey);
        if (tester.getDialog().getResponseText().contains("watch-state-off"))
        {
            tester.clickLink("toggle-watch-issue");
        }
    }

    @Override
    public IssueNavigation addWatchers(String issueKey, String... usernames)
    {
        viewIssue(issueKey);
        tester.clickLink("view-watcher-list");
        tester.setWorkingForm("startform");
        tester.setFormElement("userNames", StringUtils.join(usernames, ","));
        tester.submit("add");
        for (String username : usernames)
        {
            getAssertions().assertNodeExists(locators.id("watcher_link_" + username));
        }
        return this;
    }

    public void unvoteIssue(final String issueKey)
    {
        viewIssue(issueKey);
        if (tester.getDialog().getResponseText().contains("vote-state-on"))
        {
            tester.clickLink("toggle-vote-issue");
        }
        else
        {
            throw new RuntimeException("Could not unvote on issue");
        }
    }

    public void voteIssue(final String issueKey)
    {
        viewIssue(issueKey);
        if (tester.getDialog().getResponseText().contains("vote-state-off"))
        {
            tester.clickLink("toggle-vote-issue");
        }
        else
        {
            throw new RuntimeException("Could no vote on issue");
        }
    }

    public void addComment(String issueKey, String comment)
    {
        addComment(issueKey, comment, null);
    }

    public void addComment(final String issueKey, final String comment, final String roleLevel)
    {
        viewIssue(issueKey);
        tester.clickLink("footer-comment-button");
        tester.setFormElement("comment", comment);
        if (roleLevel != null)
        {
            tester.selectOption("commentLevel", roleLevel);
        }
        tester.submit();
    }

    public void setFixVersions(final String issueKey, final String... fixVersions)
    {
        setIssueMultiSelectField(issueKey, "fixVersions", fixVersions);
    }

    public void setAffectsVersions(final String issueKey, final String... affectsVersions)
    {
        setIssueMultiSelectField(issueKey, "versions", affectsVersions);
    }

    public void setComponents(final String issueKey, final String... components)
    {
        setIssueMultiSelectField(issueKey, "components", components);
    }

    public void setDueDate(String issueKey, String dateString)
    {
        setSingleIssueField(issueKey,"duedate",dateString);
    }

    public void setIssueMultiSelectField(final String issueKey, final String selectName, final String... options)
    {
        viewIssue(issueKey);
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        for (String option : options)
        {
            final String value = tester.getDialog().getValueForOption(selectName, option);
            tester.checkCheckbox(selectName, value);
        }
        tester.submit("Update");
    }

    public void setEstimates(final String issueKey, final String originalEstimate, final String remainingEstimate)
    {
        final String originalEstimateFieldId = getOriginalEstimateFieldId();
        final String remainingEstimateFieldId = getRemainingEstimateFieldId();

        viewIssue(issueKey);
        tester.clickLink("edit-issue");

        tester.assertFormElementPresent(originalEstimateFieldId);
        tester.setFormElement(originalEstimateFieldId, originalEstimate);

        tester.assertFormElementPresent(remainingEstimateFieldId);
        tester.setFormElement(remainingEstimateFieldId, remainingEstimate);

        tester.submit();
    }

    public void setOriginalEstimate(final String issueKey, final String newValue)
    {
        final String estimateFieldId = getOriginalEstimateFieldId();

        viewIssue(issueKey);
        tester.clickLink("edit-issue");

        tester.assertFormElementPresent(estimateFieldId);
        tester.setFormElement(estimateFieldId, newValue);

        tester.submit();
    }

    public void setRemainingEstimate(String issueKey, String newValue)
    {
        final String remainingEstimateFieldId = getRemainingEstimateFieldId();

        viewIssue(issueKey);
        tester.clickLink("edit-issue");

        tester.assertFormElementPresent(remainingEstimateFieldId);
        tester.setFormElement(remainingEstimateFieldId, newValue);

        tester.submit();
    }

    public IssueNavigation editLabels(final int issueId)
    {
        tester.clickLink("edit-labels-" + issueId + "-labels");
        return this;
    }

    public IssueNavigation editCustomLabels(final int issueId, final int customFieldId)
    {
        tester.clickLink("edit-labels-" + issueId + "-customfield_" + customFieldId);
        return this;
    }

    public IssueNavigatorNavigation returnToSearch()
    {
        tester.gotoPage("/issues/?jql=");
        return new IssueNavigatorNavigationImpl(tester, getEnvironmentData());
    }

    public String getId(final String issueKey)
    {
        gotoIssue(issueKey);

        String text;
        String issueId = "";

        try
        {
            text = tester.getDialog().getResponse().getText();
            String paramName = "ViewVoters!default.jspa?id=";
            int issueIdLocation = text.indexOf(paramName) + paramName.length();
            issueId = text.substring(issueIdLocation, issueIdLocation + 5);
        }
        catch (IOException e)
        {
            log("Unable to retrieve issue id" + e.getMessage());
            Assert.fail(String.format("No issue id could be found using issue key:'%s'. IOException caught: '%s'",
                    issueKey, e.getMessage()));
        }

        return issueId;
    }

    private String getOriginalEstimateFieldId()
    {
        if (getAdministration().timeTracking().isIn(TimeTracking.Mode.LEGACY))
        {
            return "timetracking";
        }
        else
        {
            return "timetracking_originalestimate";
        }
    }

    private String getRemainingEstimateFieldId()
    {
        if (getAdministration().timeTracking().isIn(TimeTracking.Mode.LEGACY))
        {
            return "timetracking";
        }
        else
        {
            return "timetracking_remainingestimate";
        }
    }

    protected Administration getAdministration()
    {
        return getFuncTestHelperFactory().getAdministration();
    }


}
