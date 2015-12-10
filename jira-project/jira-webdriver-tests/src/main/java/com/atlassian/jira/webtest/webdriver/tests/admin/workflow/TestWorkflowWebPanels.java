package com.atlassian.jira.webtest.webdriver.tests.admin.workflow;

import java.util.List;

import com.atlassian.jira.functest.framework.admin.WorkflowTransition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowSteps;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowTransitionPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowHeader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PROJECTS, Category.WORKFLOW })
public class TestWorkflowWebPanels extends BaseJiraWebTest
{
    private static final String WEB_PANEL_KEY = "com.atlassian.jira.dev.reference-plugin:reference-transitions-tab";
    private static final String WEB_PANEL_ID = "view_reference_transitions_tab";

    private static final String FIRST_WEB_PANEL_KEY = "com.atlassian.jira.dev.reference-plugin:reference-transitions-tab-first";
    private static final String FIRST_WEB_PANEL_ID = "view_reference_transitions_tab_first";

    private static final String TAB_CONTENT = "<h3>A tab appears!</h3>";
    private static final String WF_NAME = "classic default workflow";

    @Before
    public void setUp() throws Exception
    {
        backdoor.restoreBlankInstance();
    }

    @After
    public void tearDown() throws Exception
    {
        backdoor.plugins().disablePluginModule(WEB_PANEL_KEY);
        backdoor.plugins().disablePluginModule(FIRST_WEB_PANEL_KEY);
    }

    @Test
    public void testTabWebPanelIsRendered() throws Exception
    {
        // having
        backdoor.plugins().enablePluginModule(WEB_PANEL_KEY);
        final ViewWorkflowTransitionPage transitionPage = getViewWorkflowTransitionPage();

        // when
        final List<String> tabIds = transitionPage.getVisibleTabsIds();
        final ViewWorkflowTransitionPage transitionPageWithTabOpen = transitionPage.openTab(WEB_PANEL_ID);
        final String activeTabId = transitionPageWithTabOpen.getActiveTabId();
        final String activeTabHtml = transitionPageWithTabOpen.getActiveTabHtml();

        // then
        assertThat(tabIds, contains(WorkflowTransition.Tabs.TRIGGERS.linkId(), WorkflowTransition.Tabs.CONDITIONS.linkId(),
                WEB_PANEL_ID, WorkflowTransition.Tabs.VALIDATORS.linkId(), WorkflowTransition.Tabs.POST_FUNCTIONS.linkId()));
        assertThat(activeTabId, equalTo(WEB_PANEL_ID));
        assertThat(activeTabHtml, containsString(TAB_CONTENT));
    }

    @Test
    public void testTabWebPanelIsRenderedFirst() throws Exception
    {
        // having
        backdoor.plugins().enablePluginModule(FIRST_WEB_PANEL_KEY);
        backdoor.plugins().enablePluginModule(WEB_PANEL_KEY);

        final ViewWorkflowTransitionPage transitionPage = getViewWorkflowTransitionPage();

        // when
        final List<String> tabIds = transitionPage.getVisibleTabsIds();
        final String activeTabId = transitionPage.getActiveTabId();
        final String activeTabHtml = transitionPage.getActiveTabHtml();

        // then
        assertThat(tabIds, contains(FIRST_WEB_PANEL_ID, WorkflowTransition.Tabs.TRIGGERS.linkId(), WorkflowTransition.Tabs.CONDITIONS.linkId(),
                WEB_PANEL_ID, WorkflowTransition.Tabs.VALIDATORS.linkId(), WorkflowTransition.Tabs.POST_FUNCTIONS.linkId()));
        assertThat(activeTabId, equalTo(FIRST_WEB_PANEL_ID));
        assertThat(activeTabHtml, containsString(TAB_CONTENT));
    }

    private ViewWorkflowTransitionPage getViewWorkflowTransitionPage()
    {
        ViewWorkflowSteps steps = jira.goTo(ViewWorkflowSteps.class, WF_NAME);
        steps.setCurrentEditMode(WorkflowHeader.WorkflowMode.TEXT);

        return steps.goToEditTransition(steps.getWorkflowStepItems().get(0).getTransitions().get(0).getTransition(),
                "", WF_NAME, "", "");
    }
}
