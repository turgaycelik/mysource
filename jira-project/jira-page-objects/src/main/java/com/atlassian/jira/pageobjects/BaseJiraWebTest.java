package com.atlassian.jira.pageobjects;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.pageobjects.setup.JiraWebTestClassRules;
import com.atlassian.jira.pageobjects.setup.JiraWebTestLogger;
import com.atlassian.jira.pageobjects.setup.JiraWebTestRules;
import com.atlassian.jira.pageobjects.setup.SingleJiraWebTestRunner;
import com.atlassian.pageobjects.PageBinder;

import com.google.inject.Inject;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

/**
 * <p/>
 * Lightweight base class mainly containing annotations common for all tests.
 *
 * <p/>
 * DO NOT put any utility methods here. Use page objects framework for that. In fact, do not put anything here
 * without permission :P
 *
 * <p/>
 *
 * @since v4.4
 */
@RunWith(SingleJiraWebTestRunner.class)
public abstract class BaseJiraWebTest
{
    protected static final Logger logger = JiraWebTestLogger.LOGGER;

    @Inject protected static JiraTestedProduct jira;
    @Inject protected static PageBinder pageBinder;
    @Inject protected static Backdoor backdoor;

    @ClassRule @Inject public static JiraWebTestClassRules jiraWebTestClassRules;

    @Rule public TestRule webTestRule = JiraWebTestRules.forJira(jira);

}
