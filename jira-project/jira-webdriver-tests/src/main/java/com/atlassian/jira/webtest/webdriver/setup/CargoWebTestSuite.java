package com.atlassian.jira.webtest.webdriver.setup;

import com.atlassian.jira.functest.framework.suite.SystemPropertyBasedSuite;

import org.junit.runner.RunWith;

/**
 * Web test suite to run in cargo.
 *
 * @since v4.4
 */
@RunWith(CargoSuiteRunner.class)
public class CargoWebTestSuite extends SystemPropertyBasedSuite
{
}
