package it.com.atlassian.jira.webtest;

import com.atlassian.jira.webtest.webdriver.setup.CargoWebTestSuite;

/**
 * AMPS requires tests to be local, so this is a hack to have CargoWebTestSuite in this module.
 *
 * @since v5.0
 */
public class IntegrationCargoWebTestSuite extends CargoWebTestSuite
{
}
