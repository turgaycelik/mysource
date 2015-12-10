package com.atlassian.jira.pageobjects.setup;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.functest.framework.test.TestLoggers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default SLF4J logger for JIRA web tests.
 *
 * @since 5.1
 */
@Internal
public final class JiraWebTestLogger
{
    public static final Logger LOGGER = LoggerFactory.getLogger(TestLoggers.forTestSuite("WebDriver"));
}
