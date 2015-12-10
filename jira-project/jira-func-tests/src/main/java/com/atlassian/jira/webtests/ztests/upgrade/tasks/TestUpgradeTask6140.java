package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import java.io.IOException;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Issue;

import org.xml.sax.SAXException;

/**
 * Responsible for testing project roles.
 *
 * From TestUpgradeTask6108
 */
@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS, Category.PROJECTS, Category.ROLES })
public class TestUpgradeTask6140 extends FuncTestCase
{

    public void testProjectRolesCorrectForMixedCaseUsersAndGroups() throws IOException, SAXException
    {
        administration.restoreData("TestUpgradeTask6140.xml");

        navigation.login("fred_renamed", "fred");

        Issue issue = backdoor.issues().getIssue("HSP-1");
        assertNotNull(issue);
    }
}


