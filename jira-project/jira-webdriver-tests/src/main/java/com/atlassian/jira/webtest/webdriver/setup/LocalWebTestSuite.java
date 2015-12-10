package com.atlassian.jira.webtest.webdriver.setup;

import com.atlassian.jira.functest.framework.suite.SystemPropertyBasedSuite;

import org.junit.runner.RunWith;

/**
 * Web test suite based on local environment data.
 *
 * @since v4.4
 */
@RunWith(LocalSuiteRunner.class)
public class LocalWebTestSuite extends SystemPropertyBasedSuite
{
}
