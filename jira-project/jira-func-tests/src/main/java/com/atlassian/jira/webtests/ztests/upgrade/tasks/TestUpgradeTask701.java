package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;

import java.io.InputStream;

import static com.atlassian.jira.functest.matcher.ConditionClassMatcher.usesConditionClass;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS })
public class TestUpgradeTask701 extends FuncTestCase
{
    static final String XML_BACKUP = "TestUpgradeTask701.xml";

    public void testUpgradeTaskRemovesReferencesToOSUserGroupCondition() throws Exception
    {
        administration.restoreDataWithBuildNumber(XML_BACKUP, 700);

        WebResponse upgraded = tester.getDialog().getWebClient().getResponse(new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + "/secure/admin/workflows/ViewWorkflowXml.jspa?workflowMode=live&workflowName=Workflow+that+uses+OSUserGroupCondition"));

        // make sure the condition class has been upgraded by looking at the workflow XML
        InputStream workflow = upgraded.getInputStream();
        assertThat(workflow, not(usesConditionClass("com.opensymphony.workflow.util.OSUserGroupCondition")));
        workflow.reset();
        assertThat(workflow, usesConditionClass("com.atlassian.jira.workflow.condition.UserInGroupCondition"));

        // now actually exercise the workflow. admin should be able to close the issue, fred shouldn't.
        String key = "HSP-1";
        navigation.issue().viewIssue(key);
        text.assertTextPresent(locator.css(".issueaction-workflow-transition"), "Close Issue");

        navigation.login("fred");
        text.assertTextNotPresent(locator.css(".issueaction-workflow-transition"), "Close Issue");
    }
}
